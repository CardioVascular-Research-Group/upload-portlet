package edu.jhu.cvrg.waveform.main;
/*
Copyright 2013 Johns Hopkins University Institute for Computational Medicine

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/
/**
* @author Chris Jurado, Mike Shipway, Brandon Benitez
* 
*/


import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.axiom.om.OMElement;
import org.apache.log4j.Logger;

import com.liferay.faces.portal.context.LiferayFacesContext;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.repository.model.FileEntry;
import com.liferay.portal.kernel.repository.model.Folder;
import com.liferay.portal.model.User;
import com.liferay.portal.security.auth.PrincipalThreadLocal;
import com.liferay.portal.security.permission.PermissionChecker;
import com.liferay.portal.security.permission.PermissionCheckerFactoryUtil;
import com.liferay.portal.security.permission.PermissionThreadLocal;
import com.liferay.portal.service.ServiceContext;
import com.liferay.portal.service.UserLocalServiceUtil;
import com.liferay.portlet.documentlibrary.DuplicateFileException;
import com.liferay.portlet.documentlibrary.service.DLAppLocalServiceUtil;

import edu.jhu.cvrg.dbapi.dto.UploadStatusDTO;
import edu.jhu.cvrg.dbapi.enums.EnumFileExtension;
import edu.jhu.cvrg.dbapi.enums.EnumFileType;
import edu.jhu.cvrg.dbapi.factory.Connection;
import edu.jhu.cvrg.dbapi.factory.ConnectionFactory;
import edu.jhu.cvrg.dbapi.factory.exists.model.MetaContainer;
import edu.jhu.cvrg.waveform.exception.UploadFailureException;
import edu.jhu.cvrg.waveform.utility.ResourceUtility;
import edu.jhu.cvrg.waveform.utility.Semaphore;
import edu.jhu.cvrg.waveform.utility.WebServiceUtility;

public class UploadManager extends Thread{

	private MetaContainer metaData = new MetaContainer();
	
	private User user;
	private EnumFileType fileType;
	
	private long validationTime;
	
	private FileEntry wfdbPairFile;
	
	private FileEntry liferayFile; 
	private EnumFileExtension fileExtension;
	private UploadStatusDTO dto;
	private long companyId;
	private Logger log = Logger.getLogger(UploadManager.class);
	
	Connection db = null;
	
	@Override
	public void run() {
		try {
			this.convertUploadedFile();
		} catch (Exception e) {
			if(db!=null){
				dto.setStatus(Boolean.FALSE);
				dto.setMessage(e.getMessage());
				db.storeUploadStatus(dto);
			}else{
				e.printStackTrace();	
			}
		}
	}
	
	public UploadStatusDTO processUploadedFile(InputStream fileToSave, String fileName, String recordName, long fileSize, String studyID, String datatype, Folder destFolder) throws UploadFailureException {

		validationTime = java.lang.System.currentTimeMillis();
		
		companyId = ResourceUtility.getCurrentCompanyId();
		
		metaData.setFileName(fileName);
		metaData.setStudyID(studyID);
		metaData.setDatatype(datatype);
		metaData.setSubjectID(recordName);
		metaData.setRecordName(recordName);
		
		user = ResourceUtility.getCurrentUser();
		
		String userId = String.valueOf(user.getUserId());
		if(userId == null || userId.equals("")){
			userId = user.getEmailAddress();
		}
		metaData.setUserID(userId);
		
		try {
			
			boolean performConvesion = true;
			
			fileExtension = EnumFileExtension.valueOf(extension(metaData.getFileName()).toUpperCase());
			
			byte[] bytes = new byte[(int)fileSize];
			fileToSave.read(bytes);
			
			switch (fileExtension) {
			case XML:
				
				StringBuilder xmlString = new StringBuilder();
				
				xmlString.append(new String(bytes));
 				
 				// check for the first xml tag
 				// if it does not exist, remake the file using UTF-16 
 				// UTF-16 is known not to work with the input stream passed in, and saves the raw bytes
 				if(xmlString.indexOf("xml") == -1) {
 					// TODO:  In the future, use a library to check for the encoding that the stream uses
 					xmlString = new StringBuilder(new String(bytes, "UTF-16"));
 					
 					if(xmlString.indexOf("xml") == -1) {
 						throw new UploadFailureException("Unexpected file.");
 					}
 					
				}
				
				// indicates one of the Philips formats
				
				// JDOM seems to be having problems building the XML structure for Philips 1.03,
				// Checking for the elements directly in the string is required to get the right version
				// and also takes less memory
				
				if(xmlString.indexOf("restingecgdata") != -1) {						
					if(xmlString.indexOf("<documentversion>1.03</documentversion>") != -1) {
						fileType = EnumFileType.PHILIPS_103;
					}
					else if(xmlString.indexOf("<documentversion>1.04</documentversion>") != -1) {
						fileType = EnumFileType.PHILIPS_104;
					}
					else {
						throw new UploadFailureException("Unrecognized version number for Philips file");
					}
				
				// indicates GE Muse 7
				}else if(xmlString.indexOf("RestingECG") != -1) {
					fileType = EnumFileType.MUSE_XML;
				
				// indicates Schiller 
				}else if(xmlString.indexOf("examdescript") != -1) {
					fileType = EnumFileType.SCHILLER;
				
				}else{
					fileType = EnumFileType.HL7;
				}
				
				liferayFile = saveFile(fileSize, destFolder, bytes);
				
				break;
			case HEA:
				
				checkHeaFile(bytes, metaData.getRecordName(), fileSize);
				
				liferayFile = saveFile(fileSize, destFolder, bytes);
				
				performConvesion = this.checkWFDBFiles(liferayFile, fileExtension);
				
				fileType = EnumFileType.WFDB;
				
				break;
				
			case DAT:
				
				checkDatFile(bytes);
				
				liferayFile = saveFile(fileSize, destFolder, bytes);
				
				performConvesion = this.checkWFDBFiles(liferayFile, fileExtension);
				
				fileType = EnumFileType.WFDB;
				
				break;
			default:
				break;
			}

			if(performConvesion){
				validationTime = java.lang.System.currentTimeMillis() - validationTime;
				dto = new UploadStatusDTO(null, null, null, validationTime, null, null, null);
				dto.setRecordName(metaData.getSubjectID());
			}else{
				dto = new UploadStatusDTO(null, null, null, null, null, null, "Incomplete ECG, waiting files.");
				dto.setRecordName(metaData.getSubjectID());
			}
			return dto;
			
		} catch (UploadFailureException e){
			throw e;
		} catch (Exception e) {
			log.error(e.getMessage());
			throw new UploadFailureException("This upload failed because a " + e.getClass() + " was thrown with the following message:  " + e.getMessage(), e);
		} finally {
			try {
				fileToSave.close();
			} catch (IOException e) {
				log.error(e.getMessage());
				e.printStackTrace();
			}
		}
	}

	private void checkDatFile(byte[] bytes) throws UploadFailureException {
		boolean isBinary = false;
		
		int bytesToAnalyze = Double.valueOf(bytes.length*0.3).intValue();
		
		int encodeErrorLimit = Double.valueOf(bytesToAnalyze*0.2).intValue();
		int encodeErrorCount = 0;
		
		for (int j = 0; j < bytesToAnalyze; j++) {
			
			int c = (int) bytes[j]; 
			if(c == 9 || c == 10 || c == 13 || (c >= 32 && c <= 126) ){
				isBinary = false;
			} else {
				//System.out.print(c + " " + (char) c);
				isBinary = true;
				if(encodeErrorCount == encodeErrorLimit ){
					break;	
				}else{
					encodeErrorCount++;
				}
			}
		}
		
		if (!isBinary) {
			encodeErrorCount = 0;
			
			try {
				String s = new String(bytes, "UTF-16");
				for (int j = 0; j < s.length(); j++) {
					
					int c = (int) (s.charAt(j)); 
					if(c == 9 || c == 10 || c == 13 || (c >= 32 && c <= 126) ){
						isBinary =  false;
					} else {
						isBinary = true;
						if(encodeErrorCount == encodeErrorLimit ){
							break;	
						}else{
							encodeErrorCount++;
						}
					}
				}
			} catch (UnsupportedEncodingException e) {
				isBinary = true;
			}
		}
		
		if(!isBinary){
			throw new UploadFailureException("Unexpected file.");
		}
	}

	private void checkHeaFile(byte[] bytes, String recordName, long fileSize) throws UploadFailureException {
		boolean valid = false;
		
		StringBuilder line = new StringBuilder();
		int lineNumber = 0;
		int leadTotal = 0;
		int leadOnFile = 0;
		
		for (int i = 0; i < fileSize; i++) {
			
			char s = (char)bytes[i];
			
			line.append(s);
			
			if(s == '\n'){
				
				if(lineNumber > 0 && leadTotal == leadOnFile){
					break;
				}
				
				String[] headerInfo = line.toString().split(" ");
				
				valid = (headerInfo != null && headerInfo.length >= 2);
				if(!valid) break;
				
				if(lineNumber == 0){
					String fileRecordName = headerInfo[0];
					int index = fileRecordName.lastIndexOf('/');
					if(index != -1){
						fileRecordName = fileRecordName.substring(0, index);
					}
					
					valid = (recordName.equals(fileRecordName));
					if(!valid) break;

					leadTotal = Integer.valueOf(headerInfo[1]);
					
				}else if(lineNumber > 0 && valid){
					leadOnFile++;
				}
				
				line.delete(0, line.length()-1);
				lineNumber++;
			}
		}
		if(valid){
			valid = leadTotal == leadOnFile;
		}
		
		if(!valid){
			throw new UploadFailureException("Unexpected file.");
		}
		
	}

	private FileEntry saveFile(long fileSize, Folder destFolder, byte[] bytes) throws UploadFailureException {
		
		Folder recordNameFolder = createRecordNameFolder(destFolder, metaData.getRecordName(), user.getUserId());
		return  storeFile(bytes, fileSize, recordNameFolder, true);
		
	}

	private synchronized Folder createRecordNameFolder(Folder folder, String recordName, Long userId) throws UploadFailureException{
		
		Folder recordNameFolder = null;
		Semaphore s = Semaphore.getCreateFolderSemaphore();
		
		recordName = ResourceUtility.convertToLiferayDocName(recordName);
		
		try {
			s.take();
			long folderId;
			if(folder == null){
				folder = DLAppLocalServiceUtil.getFolder(0L);
			}
			folderId = folder.getFolderId();
			
			if(!folder.getName().equals(recordName)){
				
				List<Folder> subFolders = DLAppLocalServiceUtil.getFolders(folder.getRepositoryId(), folderId);
				
				if(subFolders!=null){
					for (Folder sub : subFolders) {
						if(sub.getName().equals(recordName)){
							return sub;
						}
					}
				}
				
				ServiceContext service = LiferayFacesContext.getInstance().getServiceContext();
				try{
					recordNameFolder = DLAppLocalServiceUtil.addFolder(userId, ResourceUtility.getCurrentGroupId(), folderId, recordName, "", service);
				}catch (Exception e){
					Thread.sleep(2000);
					int tries = 5;
					
					for (int i = 0; i < tries && recordNameFolder == null; i++) {
						try{
							recordNameFolder = DLAppLocalServiceUtil.getFolder(folder.getRepositoryId(), folder.getFolderId(), recordName);
						}catch (Exception e2){
							Thread.sleep(2000);
							log.warn("Sleep and Try Again. #"+(i));
						}
					}
					
				}
			}else{
				recordNameFolder = folder;
			}
			
			if(recordNameFolder != null) {
				StringBuilder treePath = new StringBuilder();
				extractFolderHierachic(recordNameFolder, treePath);
				metaData.setTreePath(treePath.toString());
			}else {
				throw new UploadFailureException("Please select a folder");
			}
			
		} catch (Exception e) {
			log.error(e.getMessage());
			throw new UploadFailureException("Error on record name folder's creation.", e);
		}finally{
			try {
				s.release();
			} catch (InterruptedException e) {
				log.error(e.getMessage());
			}
		}
		
		return recordNameFolder;
	}

	private boolean checkWFDBFiles(FileEntry liferayFile, EnumFileExtension fileExtension) throws PortalException, SystemException {
		
		boolean ret = false;
		
		Long folderId = liferayFile.getFolderId();
		List<FileEntry> subFiles = DLAppLocalServiceUtil.getFileEntries(ResourceUtility.getCurrentGroupId(), folderId);
		if(subFiles != null){
			for (FileEntry file : subFiles) {
				if(EnumFileExtension.HEA.equals(fileExtension)){	
					ret = file.getTitle().substring(0, file.getTitle().indexOf('.')).equals(liferayFile.getTitle().substring(0, file.getTitle().indexOf('.'))) && 
							EnumFileExtension.DAT.toString().equalsIgnoreCase(file.getExtension());
				}else if(EnumFileExtension.DAT.equals(fileExtension)){
					ret = file.getTitle().substring(0, file.getTitle().indexOf('.')).equals(liferayFile.getTitle().substring(0, file.getTitle().indexOf('.'))) && 
							EnumFileExtension.HEA.toString().equalsIgnoreCase(file.getExtension());
				}
				
				if(ret){
					wfdbPairFile = file;
					break;
				}
			}				
		}
		return ret;
	}

	private void extractFolderHierachic(Folder folder, StringBuilder treePath) throws Exception {
		try {
			if(folder != null && !"waveform".equals(folder.getName())){
				if(folder.getParentFolder() != null){
					extractFolderHierachic(folder.getParentFolder(), treePath);
				}
				treePath.append('/').append(folder.getName());
			}
		} catch (Exception e) {
			log.error("Problems with the liferay folder structure");
			throw e;
		}
	}

	private synchronized FileEntry storeFile(byte[] bytes, long fileSize, Folder folder, boolean twice) throws UploadFailureException {

		if (fileSize < Integer.MIN_VALUE || fileSize > Integer.MAX_VALUE) {
			throw new IllegalArgumentException(fileSize	+ " cannot be cast to int without changing its value.");
		}

		int fileSizeInt = (int) fileSize;
		metaData.setFileSize(fileSizeInt);
		FileEntry file = null;
		
		try {
			
			//TODO [VILARDO] DEFINE THE FILE TYPE
			ServiceContext service = LiferayFacesContext.getInstance().getServiceContext();
			file = DLAppLocalServiceUtil.addFileEntry(user.getUserId(), ResourceUtility.getCurrentGroupId(), folder.getFolderId(), metaData.getFileName(), "", metaData.getFileName(), "", "1.0", bytes, service);
			
		} catch(DuplicateFileException e){
			log.error(e.getMessage());
			throw new UploadFailureException("This file already exist.", e);
		
		} catch (Exception e) {
			log.error(e.getMessage());
			throw new UploadFailureException("Error on file creation", e);
		}
		
		return file;
	}

	public UploadStatusDTO convertUploadedFile() throws UploadFailureException {
	
		String method = "na";
		boolean correctFormat = true;
		
		initializeLiferayPermissionChecker(user.getUserId());
		db = ConnectionFactory.createConnection();

		switch (fileType) {
			case WFDB:			method = "wfdbToRDT"; 			break;
			case HL7:			method = "hL7";					break;
			case PHILIPS_103:	method = "philips103ToWFDB";	break;
			case PHILIPS_104:	method = "philips104ToWFDB";	break;
			case SCHILLER:	    method = "SCHILLERToWFDB";   	break;
			case MUSE_XML:		method = "museXML";				break;
			default:	
				switch (fileExtension) {
					case RDT:	method = "rdtToWFDB16";					break;
					case XYZ:	method = "wfdbToRDT"; 		fileType = EnumFileType.WFDB;		break;
					case ZIP:	method = "processUnZipDir";	/* leave the fileFormat tag alone*/ break;
					case TXT:	method = evaluateTextFile(liferayFile.getTitle());	/*currently a stub method -  will eventually process GE MUSE Text files*/	break;
					case CSV:	method = "xyFile";						break;
					case NAT:	method = "na";							break;
					case GTM:	method = "na";							break;
					default:	method = "geMuse";						break;
				}
			break;
		}
		
		if(fileType != null){
			metaData.setFileFormat(fileType.ordinal());
		
			if(EnumFileExtension.HEA.equals(fileExtension) || EnumFileExtension.DAT.equals(fileExtension)) {
				
				FileEntry headerFile = liferayFile;
				if(EnumFileExtension.DAT.equals(fileExtension)){
					headerFile = wfdbPairFile;
				}
				// Parse the locally held header file
				correctFormat = checkWFDBHeader(headerFile);
			}
			
			if(!correctFormat) {
				throw new UploadFailureException("The header file has not been parsed properly");
			}
			
			log.info("method = " + method);
			
			if(ResourceUtility.getNodeConversionService().equals("0")){
				log.error("Missing Web Service Configuration.  Cannot run File Conversion Web Service.");
				throw new UploadFailureException("Cannot run File Conversion Web Service. Missing Web Service Configuration.");
			}
	
			if(!method.equals("na")){
			
				LinkedHashMap<String, String> parameterMap = new LinkedHashMap<String, String>();
			
				parameterMap.put("userid", String.valueOf(user.getUserId()));
				parameterMap.put("subjectid", 	metaData.getSubjectID());
				parameterMap.put("filename", 	liferayFile.getTitle());
				parameterMap.put("studyID", 	metaData.getStudyID());
				parameterMap.put("datatype", 	metaData.getDatatype());
				parameterMap.put("treePath", 	metaData.getTreePath());
				parameterMap.put("recordName", 	metaData.getRecordName());
				parameterMap.put("fileSize", 	String.valueOf(metaData.getFileSize()));
				parameterMap.put("fileFormat", 	String.valueOf(metaData.getFileFormat()));
				
				parameterMap.put("verbose", 	String.valueOf(false));
				parameterMap.put("service", 	"DataConversion");
				
				parameterMap.put("companyId", 	String.valueOf(companyId));
				parameterMap.put("groupId", 	String.valueOf(liferayFile.getGroupId()));
				parameterMap.put("folderId", 	String.valueOf(liferayFile.getFolderId()));
				
				LinkedHashMap<String, FileEntry> filesMap = new LinkedHashMap<String, FileEntry>();
				
				switch (fileExtension) {
				case HEA:
					filesMap.put("contentFile", wfdbPairFile);
					filesMap.put("headerFile", liferayFile);
					break;
				case DAT:
					filesMap.put("contentFile", liferayFile);
					filesMap.put("headerFile", wfdbPairFile);
					break;
				default:
					filesMap.put("contentFile", liferayFile);
					break;
				}
				
	
				log.info("Calling Web Service with " + liferayFile.getTitle() + ".");
				
				long conversionTime = java.lang.System.currentTimeMillis();
				
				OMElement result = WebServiceUtility.callWebService(parameterMap, false, method, ResourceUtility.getNodeConversionService(), null, filesMap);
				
				conversionTime = java.lang.System.currentTimeMillis() - conversionTime;
				
				if(result == null){
					throw new UploadFailureException("Webservice return is null.");
				}
				
				Map<String, OMElement> params = WebServiceUtility.extractParams(result);
				
				if(params == null){
					throw new UploadFailureException("Webservice return params are null.");
				}else{
					if(params.get("documentId").getText() != null){
						long docId = Long.parseLong(params.get("documentId").getText());
						
						log.info("["+docId+"]The runtime file validation is = " + validationTime + " milliseconds");
						log.info("["+docId+"]The runtime for WS tranfer, read and store the document on database is = " + conversionTime + " milliseconds");
						
						dto.setDocumentRecordId(docId);
						dto.setTransferReadTime(conversionTime);
						
						db.storeUploadStatus(dto);
						
						return dto;
						
					}else if(params.get("errorMessage").getText() != null && !params.get("errorMessage").getText().isEmpty()){
						throw new UploadFailureException(params.get("errorMessage").getText());
					}	
					
				}
			}
		}else{
			throw new UploadFailureException("Unidentified file format/type.");
		}
			
		return null;
	}

	// TODO: make this into a function which determines which kind of text file
	// this is, and returns the correct method to use.
	private String evaluateTextFile(String fName) {
		String method = "geMuse";

		return method;
	}
	
	private String extension(String filename){
		return filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
	}
	
	
	//TODO [VILARDO] TRY TO MOVE TO WEB SERVICE
	private boolean checkWFDBHeader(FileEntry file) {
		
		// To find out more about the WFDB header format, go to:
		// http://www.physionet.org/physiotools/wag/header-5.htm
		//
		// The goal is to extract the metadata information needed without the need for using the physionet libraries
		
		boolean returnValue = false;
		DataInputStream wfdbInputStream = null;
		BufferedReader br = null;
		
		try{
			wfdbInputStream = new DataInputStream(file.getContentStream());
		    br = new BufferedReader(new InputStreamReader(wfdbInputStream));
		    String strLine;
		    String[] words;
		    //Read File Line By Line
		    
		    while ((strLine = br.readLine()) != null)   {
		      // Print the content on the console
		    	
		    	if(strLine.length()>0) {
		    		
		    		// check to see if this line is a comment or not
		    		if(!strLine.startsWith("#")) {

		    			returnValue = true;
		    			break;
		    		}
		    	}
		    }
		    
		    // Begin parsing out different sections of the WFDB header file.  The array size should be two or more.
		    words = strLine.split("\\s");
		    
		    if(words != null && words.length>=2) {
		    	
		    	// Step 1:  Extract record name.  There is more after the "/", but it will be parsed out and ignored
		    	String[] firstField = words[0].split("/");
		    	if(firstField != null && firstField.length>0) {
		    		// validate the record name here against the record name in the metadata
		    		if(!(firstField[0].equals(metaData.getRecordName()))) {
		    			br.close();
		    			return false;
		    		}
		    	}
		    	else {
		    		br.close();
		    		return false;
		    	}
		    	
		    	// Step 2:  Get the number of leads
		    	int numLeads = Integer.parseInt(words[1]);
		    	metaData.setChannels(numLeads);
		    	
		    	// Step 3:  If there is a third section, parse it out.  The sampling frequency will be the first value that 
		    	//          is extracted from it.  If it is not present, the default is 250 (already set in metadata class)
		    	if(words.length >= 3) {
			    	String[] thirdField = words[2].split("/");
			    	if(thirdField != null && thirdField.length>0) {
			    		float sampFreq = Float.parseFloat(thirdField[0]);
			    		metaData.setSampFrequency(sampFreq);
			    	}
		    		
		    		// Step 4:  If there is a fourth field, then that is the number of samples per signal
		    		//          After that, we do not need anything else.
		    		if(words.length >= 4) {
		    			int numPoints = Integer.parseInt(words[3]);
		    			
		    			// if zero, then ignore
		    			if(numPoints>0) {
		    				metaData.setNumberOfPoints(numPoints);
		    			}
		    		}		
		    	}
		    }
		    else {  //This is an improperly formed header file.  There should be at least two fields, separated by spaces
		    	returnValue = false;
		    }
		    
			if(wfdbInputStream != null) {
				wfdbInputStream.close();
			}
			if(br != null) {
				br.close();
			}

		}catch (Exception e){ //Catch exception if any
			log.error("Error: " + e.getMessage());
			try {
				if(wfdbInputStream != null) {
					wfdbInputStream.close();
				}
				if(br != null) {
					br.close();
				}
			} catch (IOException e2) {
				log.error("Error: " + e2.getMessage());
			}
			returnValue = false;
			return returnValue;
		}	
		return returnValue;
	}
	
	private void initializeLiferayPermissionChecker(long userId) throws UploadFailureException {
		try{
			PrincipalThreadLocal.setName(userId);
			User user = UserLocalServiceUtil.getUserById(userId);
	        PermissionChecker permissionChecker = PermissionCheckerFactoryUtil.create(user);
	        PermissionThreadLocal.setPermissionChecker(permissionChecker);
		}catch (Exception e){
			throw new UploadFailureException("Fail on permission checker initialization. [userId="+userId+"]", e);
		}
		
	}

}
