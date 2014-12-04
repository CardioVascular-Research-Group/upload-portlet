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

* @author Chris Jurado, Mike Shipway, Brandon Benitez, Andre Vilardo

* 

*/



import java.io.BufferedReader;

import java.io.ByteArrayInputStream;

import java.io.DataInputStream;

import java.io.IOException;

import java.io.InputStream;

import java.io.InputStreamReader;

import java.io.UnsupportedEncodingException;

import java.util.ArrayList;

import java.util.List;

import java.util.UUID;



import javax.faces.context.FacesContext;

import javax.portlet.PortletSession;



import org.apache.log4j.Logger;



import com.liferay.portal.kernel.exception.PortalException;

import com.liferay.portal.kernel.exception.SystemException;

import com.liferay.portal.model.User;

import com.liferay.portal.security.auth.PrincipalThreadLocal;

import com.liferay.portal.security.permission.PermissionChecker;

import com.liferay.portal.security.permission.PermissionCheckerFactoryUtil;

import com.liferay.portal.security.permission.PermissionThreadLocal;

import com.liferay.portal.service.UserLocalServiceUtil;



import edu.jhu.cvrg.data.dto.UploadStatusDTO;

import edu.jhu.cvrg.data.factory.Connection;

import edu.jhu.cvrg.data.util.DataStorageException;

import edu.jhu.cvrg.filestore.enums.EnumFileExtension;

import edu.jhu.cvrg.filestore.enums.EnumFileType;

import edu.jhu.cvrg.filestore.enums.EnumUploadState;

import edu.jhu.cvrg.filestore.main.FileStoreFactory;

import edu.jhu.cvrg.filestore.main.FileStorer;

import edu.jhu.cvrg.filestore.model.ECGFile;

import edu.jhu.cvrg.waveform.exception.UploadFailureException;

import edu.jhu.cvrg.waveform.model.LocalFileTree;

import edu.jhu.cvrg.waveform.utility.ResourceUtility;



public class UploadManager extends Thread{

	

	private LocalFileTree fileTree;

	private Long validationTime; 

	private EnumFileExtension fileExtension;

	private UploadStatusDTO uploadStatusDTO;

	private Logger log = Logger.getLogger(UploadManager.class);

	private Connection db = null;

	

	public UploadManager(LocalFileTree fileTree){

		this.fileTree = fileTree;

	}

	

	@Override

	public void run() {



		try {

			convertUploadedFile();

		} catch (Exception e) {

			if(db!=null){

				uploadStatusDTO.setStatus(Boolean.FALSE);

				uploadStatusDTO.setMessage(e.getMessage());

				try {

					db.storeUploadStatus(uploadStatusDTO);

				} catch (DataStorageException e1) {

					log.error("Exception is:", e1);

				}

			}else{

				log.error("Exception is:", e);	

			}

		}

	}



	public UploadStatusDTO storeUploadedFile(ECGFile ecgFile, UUID folderUuid) throws UploadFailureException {

		

		validationTime = java.lang.System.currentTimeMillis();

		fileExtension = EnumFileExtension.valueOf(extension(ecgFile.getFileName()).toUpperCase());



//		companyId = ResourceUtility.getCurrentCompanyId();

		boolean WFDBComplete = true;

		String message = "";

		boolean performConvesion = true;



		if (fileExtension == EnumFileExtension.XML) {

			log.info("Processing XML file.");

			processXMLFileExtension(ecgFile, folderUuid);

		}



		if (fileExtension == EnumFileExtension.HEA) {

			log.info("Checking HEA file.");

			checkHeaFile(ecgFile);

		}



		if (fileExtension == EnumFileExtension.DAT) {

			log.info("Checking DAT file.");

			checkDatFile(ecgFile);

		}



		if (fileExtension == EnumFileExtension.HEA || fileExtension == EnumFileExtension.DAT) {

			

			ecgFile.setFileType(EnumFileType.WFDB);

			saveFile(ecgFile, folderUuid);



			try {

				performConvesion = checkWFDBFiles(ecgFile, fileExtension, folderUuid);

			} catch (PortalException e) {

				log.error("Exception is:", e);

			} catch (SystemException e) {

				log.error("Exception is:", e);

			}



			WFDBComplete = findWFDBMatch(ecgFile, fileTree.findNameByUuid(folderUuid));

		}



		if (WFDBComplete) {

			validationTime = java.lang.System.currentTimeMillis() - validationTime;

		} else {

			validationTime = null;

			message = "Incomplete ECG, waiting files.";

		}



		uploadStatusDTO = new UploadStatusDTO(null, null, null, validationTime,	null, null, message);

		uploadStatusDTO.setRecordName(ecgFile.getSubjectID());



		return uploadStatusDTO;

	}

	

	private void processXMLFileExtension(ECGFile ecgFile, UUID folderUuid){

			

		try {

			SniffedXmlInputStream xmlSniffer = new SniffedXmlInputStream(ecgFile.getEcgDataFileAsInputStream());

			String encoding = xmlSniffer.getXmlEncoding();

			StringBuilder xmlString = new StringBuilder(new String(ecgFile.getEcgDataFileAsBytes(), encoding));



		if(xmlString.indexOf("restingecgdata") != -1) {						

			if(xmlString.indexOf("<documentversion>1.03</documentversion>") != -1) {

				

				ecgFile.setFileType(EnumFileType.PHILIPS_103);

			}

			else if(xmlString.indexOf("<documentversion>1.04</documentversion>") != -1) {

				ecgFile.setFileType(EnumFileType.PHILIPS_104);

			}

			else {

				if(xmlSniffer != null) {

					xmlSniffer.close();

				}



				throw new UploadFailureException("Unrecognized version number for Philips file");

			}

		

		// indicates GE Muse 7

		}else if(xmlString.indexOf("RestingECG") != -1) {

			ecgFile.setFileType(EnumFileType.MUSE_XML);

		}else{

			ecgFile.setFileType(EnumFileType.HL7);

		}

		

		saveFile(ecgFile, folderUuid);



			if(xmlSniffer != null) {

				xmlSniffer.close();

			}



		} catch (IOException e) {

			log.error("Exception is:", e);

		} catch (UploadFailureException e) {

			log.error("Exception is:", e);

		}

	}

	

	private boolean checkWFDBFiles(ECGFile ecgFile, EnumFileExtension fileExtension, UUID folderUuid) throws PortalException, SystemException {

		

		boolean ret = false;

		

		if(EnumFileExtension.HEA == fileExtension){

			String fileNameToFind = extractRecordName(ecgFile.getFileName()) + ".dat";

			ret = fileTree.fileExistsInFolder(fileNameToFind, folderUuid);

		}

		else{

			String fileNameToFind = extractRecordName(ecgFile.getFileName()) + ".hea";

			ret = fileTree.fileExistsInFolder(fileNameToFind, folderUuid);

		}



		return ret;

	}



	private void checkDatFile(ECGFile ecgFile) throws UploadFailureException {

		byte[] fileBytes = ecgFile.getEcgDataFileAsBytes();

		

		boolean isBinary = false;

		

		int bytesToAnalyze = Double.valueOf(fileBytes.length * 0.3).intValue();

		

		int encodeErrorLimit = Double.valueOf(bytesToAnalyze * 0.2).intValue();

		int encodeErrorCount = 0;

		

		for (int j = 0; j < bytesToAnalyze; j++) {

			

			int c = (int) fileBytes[j]; 

			if(c == 9 || c == 10 || c == 13 || (c >= 32 && c <= 126) ){

				isBinary = false;

			} else {

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

				String s = new String(fileBytes, "UTF-16");

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

				log.error("Exception is:", e);

			}

		}

		

		if(!isBinary){

			throw new UploadFailureException("Unexpected file.");

		}

	}



	private void checkHeaFile(ECGFile ecgFile) throws UploadFailureException {

		boolean valid = false;

		StringBuilder line = new StringBuilder();

		int lineNumber = 0;

		int leadTotal = 0;

		int leadOnFile = 0;

		

		for (int i = 0; i < ecgFile.getFileSize(); i++) {

			

			char s = (char)ecgFile.getEcgDataFileAsBytes()[i];

			

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

					

					valid = (ecgFile.getRecordName().equals(fileRecordName));

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



	private void saveFile(ECGFile ecgFile, UUID folderUuid) throws UploadFailureException {



//		ecgFile.setTreePath(createRecordNameFolder(folderUuid, ecgFile.getRecordName(), ResourceUtility.getCurrentUserId()));

//		storeFile(ecgFile, ecgFile.getFileSize(), fileTree.getFolderPath(folderUuid), true, ecgFile.getFileName());



		String[] args = {String.valueOf(ResourceUtility.getCurrentGroupId()), String.valueOf(ResourceUtility.getCurrentUserId())};

		FileStorer fileStorer = FileStoreFactory.returnFileStore(fileTree.getFileStoreType(), args);



		fileStorer.storeFile(ecgFile, fileTree.getFolderPath(folderUuid), String.valueOf(ResourceUtility.getCurrentUserId()));

	}

	

//	private synchronized void storeFile(ECGFile ecgFile, long fileSize, String folderPath, boolean twice, String fileName) throws UploadFailureException {

//

//		System.out.println("Storing file...");

//		String[] args = {String.valueOf(ResourceUtility.getCurrentGroupId()), String.valueOf(ResourceUtility.getCurrentUserId())};

//		FileStorer fileStorer = FileStoreFactory.returnFileStore(fileTree.getFileStoreType(), args);

//		fileStorer.storeFile(ecgFile, folderPath, String.valueOf(ResourceUtility.getCurrentUserId()));

//	}

	

	private synchronized String createRecordNameFolder(UUID folderUuid, String recordName, Long userId) throws UploadFailureException{

//		return Liferay61FileStorer.createFolder(recordName, String.valueOf(userId));

		return "";

	}



	private boolean findWFDBMatch(ECGFile ecgFile, String folderName){

			

		String name = ecgFile.getFileName().split("\\.")[0];

		String extension = ecgFile.getFileName().split("\\.")[1];

		

		if(extension.toUpperCase().equals("HEA")){

//			return Liferay61FileStorer.fileExists(name + ".dat", folderName, userId, groupId);

		}

		else if (extension.toUpperCase().equals("DAT")){

//			return Liferay61FileStorer.fileExists(name + ".hea", folderName, userId, groupId);

		}

		else{

			return false;

		}

		return false;

	}



	public UploadStatusDTO convertUploadedFile() throws UploadFailureException {

	

//		initializeLiferayPermissionChecker(user.getUserId());

		//TODO: FileFormat Converter thingy

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

	private boolean checkWFDBHeader(ECGFile ecgFile) {

		

		// To find out more about the WFDB header format, go to:

		// http://www.physionet.org/physiotools/wag/header-5.htm

		//

		// The goal is to extract the metadata information needed without the need for using the physionet libraries

		

		boolean returnValue = false;

		DataInputStream wfdbInputStream = null;

		BufferedReader br = null;

		

		try{

			InputStream inputStream = new ByteArrayInputStream(ecgFile.getEcgDataFileAsBytes());

			wfdbInputStream = new DataInputStream(inputStream);

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

		    		if(!(firstField[0].equals(ecgFile.getRecordName()))) {

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

		    	ecgFile.setChannels(numLeads);

		    	

		    	// Step 3:  If there is a third section, parse it out.  The sampling frequency will be the first value that 

		    	//          is extracted from it.  If it is not present, the default is 250 (already set in metadata class)

		    	if(words.length >= 3) {

			    	String[] thirdField = words[2].split("/");

			    	if(thirdField != null && thirdField.length>0) {

			    		float sampFreq = Float.parseFloat(thirdField[0]);

			    		ecgFile.setSampFrequency(sampFreq);

			    	}

		    		

		    		// Step 4:  If there is a fourth field, then that is the number of samples per signal

		    		//          After that, we do not need anything else.

		    		if(words.length >= 4) {

		    			int numPoints = Integer.parseInt(words[3]);

		    			

		    			// if zero, then ignore

		    			if(numPoints>0) {

		    				ecgFile.setNumberOfPoints(numPoints);

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



		}catch (Exception e){

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

			throw new UploadFailureException("Fail on premission checker initialization. [userId="+userId+"]", e);

		}		

	}



	public void fileUpload(UUID folderUuid, String fileName, long fileSize, InputStream fileStream, String studyID, String dataType){

		

		UploadStatusDTO status = null;

		String recordName = extractRecordName(fileName);

		

		try {

			if (!fileTree.fileExistsInFolder(fileName, folderUuid)) {

				// The new 5th parameter has been added for character encoding,

				// specifically for XML files. If null is passed in,

				// the function will use UTF-8 by default

				ECGFile ecgFile = createECGFileWrapper(fileStream, fileName,

					studyID, dataType, recordName, recordName, fileSize);



				status = storeUploadedFile(ecgFile,	fileTree.getSelectedFolderUuid());

				if (status != null) {

					if (status.getMessage() == null) {

						start();

						// msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "" ,

						// event.getFile().getFileName() + " is uploaded.");

					}

				}

			} else {

				throw new UploadFailureException("This file already exists in selected folder.");

			}



		} catch (UploadFailureException ufe) {

			status = new UploadStatusDTO(null, null, null, null, null,

					Boolean.FALSE, fileName	+ " failed because:  " + ufe.getMessage());

			status.setRecordName(recordName);



		} catch (Exception ex) {

			ex.printStackTrace();

			status = new UploadStatusDTO(null, null, null, null, null,

					Boolean.FALSE, fileName + " failed to upload for unknown reasons");

			status.setRecordName(recordName);

		}

		this.addToBackgroundQueue(status);

	}

	

	public void addToBackgroundQueue(UploadStatusDTO dto) {

		if(dto!=null){

			if(this.getBackgroundQueue() == null){

				setBackgroundQueue(new ArrayList<UploadStatusDTO>());

			}

			int index = this.getBackgroundQueue().indexOf(dto);

			if(index != -1){

				UploadStatusDTO older = this.getBackgroundQueue().get(index);

				if(EnumUploadState.WAIT.equals(older.getState()) || 

				   EnumUploadState.ERROR.equals(older.getState())){

					this.getBackgroundQueue().remove(index);	

				}

			}

			this.getBackgroundQueue().add(dto);	

		}

	}

	

	@SuppressWarnings("unchecked")

	public List<UploadStatusDTO> getBackgroundQueue() {

		PortletSession session = (PortletSession) FacesContext.getCurrentInstance().getExternalContext().getSession(false);

		return (List<UploadStatusDTO>) session.getAttribute("upload.backgroundQueue");

	}

	

	public void setBackgroundQueue(List<UploadStatusDTO> backgroundQueue) {

		PortletSession session = (PortletSession) FacesContext.getCurrentInstance().getExternalContext().getSession(false);

		session.setAttribute("upload.backgroundQueue", backgroundQueue);

	}

	

    private ECGFile createECGFileWrapper(InputStream file, String fileName, String studyID, String dataType, 

    		String subjectId, String recordName, long fileSize) throws UploadFailureException{

    	

    	byte[] fileBytes = new byte[(int)fileSize];

    	ECGFile ecgFile = new ECGFile(fileBytes, fileName, fileSize, subjectId, recordName, dataType, studyID);

    	

		try {

			file.read(fileBytes);

		} catch (IOException e) {

			log.error(e.getMessage());

			throw new UploadFailureException("This upload failed because a " + e.getClass() + " was thrown with the following message:  " + e.getMessage(), e);

		}

		

		return ecgFile;

    }



	private String extractRecordName(String fileName) {



		String recordName = "";

		int location = fileName.indexOf(".");

		if (location != -1) {

			recordName = fileName.substring(0, location);

		} else {

			recordName = fileName;

		}

		return recordName;

	}

}
