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
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Random;

import com.liferay.portal.model.User;

import edu.jhu.cvrg.waveform.exception.UploadFailureException;
import edu.jhu.cvrg.waveform.model.StudyEntry;
import edu.jhu.cvrg.waveform.utility.EnumFileType;
import edu.jhu.cvrg.waveform.utility.FTPUtility;
import edu.jhu.cvrg.waveform.utility.MetaContainer;
import edu.jhu.cvrg.waveform.utility.ResourceUtility;
import edu.jhu.cvrg.waveform.utility.StudyEntryUtility;
import edu.jhu.cvrg.waveform.utility.UploadUtility;
import edu.jhu.cvrg.waveform.utility.WebServiceUtility;

public class UploadManager {

	private char name_separator = (char) 95;

	private MetaContainer metaData = new MetaContainer();
	private String tempHeaderFile = "";
	private User user;
 
	public void processUploadedFile(InputStream fileToSave, String fileName, long fileSize, String studyID, String datatype, String virtualPath) {

		metaData.setFileName(fileName);
		metaData.setStudyID(studyID);
		metaData.setDatatype(datatype);
		metaData.setTreePath(virtualPath);

		user = ResourceUtility.getCurrentUser();

		File file =	saveFileToTemp(fileToSave, fileSize);
			
			try {
				String userId = user.getScreenName();
				if(userId == null || userId.equals("")){
					userId = user.getEmailAddress();
				}
				metaData.setUserID(userId);
				
				String outputDirectory = uploadFileFtp(userId, file);
	
//				stageUploadedFile(outputDirectory, userId, false);
	
				convertUploadedFile(outputDirectory, userId, false);
			} catch (Exception e) {
				e.printStackTrace();
			}	

	}

	private File saveFileToTemp(InputStream fileToSave, long fileSize) {

		if (fileSize < Integer.MIN_VALUE || fileSize > Integer.MAX_VALUE) {
			throw new IllegalArgumentException(fileSize	+ " cannot be cast to int without changing its value.");
		}
		
		int fileSizeInt = (int) fileSize;
		metaData.setFileSize(fileSizeInt);

		try {
			File targetFile = new File(ResourceUtility.getStagingFolder() + metaData.getFileName());
			OutputStream fOutStream = new FileOutputStream(targetFile);

			int read = 0;
			byte[] bytes = new byte[1024];

			while ((read = fileToSave.read(bytes)) != -1) {
				fOutStream.write(bytes, 0, read);
			}

			fileToSave.close();
			fOutStream.flush();
			fOutStream.close();

			int location = metaData.getFileName().indexOf(".");

			if (location != -1) {
				metaData.setSubjectID(metaData.getFileName().substring(0, location));
			} else {
				metaData.setSubjectID(metaData.getFileName());
			}
			
			return targetFile;

		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return null;
	}

	private String uploadFileFtp(String userId, File file) {

		String outputDir = "";
		String targetSubjectId = "";

		if (extension(metaData.getFileName()).equalsIgnoreCase("zip")) {
			outputDir = userId + generateTime();
		} else {
			outputDir = userId + name_separator + metaData.getSubjectID();
		}
		
		if (metaData.getSubjectID().equals("")) {
			targetSubjectId = "null";
		} else {
			targetSubjectId = metaData.getSubjectID();
			targetSubjectId = targetSubjectId.replace("_", "");
		}

		metaData.setRecordName(metaData.getSubjectID());

		FTPUtility.uploadToRemote(outputDir, file);
		
		metaData.setFullFilePath("/" + outputDir + "/");
		
		EnumFileType fileType = EnumFileType.valueOf(extension(metaData.getFileName()).toUpperCase());
		
		if(fileType == EnumFileType.HEA) {
			// do not delete a header file just yet, we need it for parsing metadata (the app looks for the local
			// version of the file, NOT the remote one).  Without this the parsing will fail
			tempHeaderFile = file.getPath();
		}
		else {
			file.delete();
		}

		return outputDir;
	}

	private void stageUploadedFile(String outputDirectory, String uId, boolean isPublic) {
		
		if(ResourceUtility.getFtpHost().equals("0") ||
				ResourceUtility.getFtpUser().equals("0") ||
				ResourceUtility.getFtpPassword().equals("0")){
			
			System.out.println("Missing FTP Configuration.  Cannot run Node Staging Web Service.");
			return;		
		}
		
		if(ResourceUtility.getStagingServiceMethod().equals("0") || ResourceUtility.getStagingService().equals("0")){
			System.out.println("Missing Web Service Configuration.  Cannot run Node Staging Web Service.");
			return;	
		}

		LinkedHashMap<String, String> parameterMap = new LinkedHashMap<String, String>();

		parameterMap.put("userid", uId);
		parameterMap.put("subjectid", metaData.getSubjectID());
		parameterMap.put("filename", outputDirectory + "/" + metaData.getFileName());
		parameterMap.put("ftphost", ResourceUtility.getFtpHost());
		parameterMap.put("ftpuser", ResourceUtility.getFtpUser());
		parameterMap.put("ftppassword", ResourceUtility.getFtpPassword());
		parameterMap.put("service", "DataStaging");
		parameterMap.put("logindatetime",	new Long(System.currentTimeMillis()).toString());
		parameterMap.put("publicprivatefolder", String.valueOf(isPublic));

		String pubPriv = "private";

		if (isPublic) {
			pubPriv = "public";
		}

		metaData.setFullFilePath(ResourceUtility.getFtpRoot() + "/" + uId + "/"	+ pubPriv + "/" + metaData.getSubjectID() + "/input/");
		metaData.setUserID(uId);

		WebServiceUtility.callWebService(parameterMap, isPublic, ResourceUtility.getStagingServiceMethod(), ResourceUtility.getStagingService(), null);
	}
 
	private void convertUploadedFile(String outputDirectory, String uId, boolean isPublic) throws UploadFailureException {

			String method = "na";
			String outfilename = outputDirectory + File.separator + metaData.getFileName();
			boolean correctFormat = true;

			EnumFileType fileType = EnumFileType.valueOf(extension(metaData.getFileName()).toUpperCase());

			switch (fileType) {
			case RDT:	method = "rdtToWFDB16";					break;
			case XYZ:	method = "wfdbToRDT"; 		metaData.setFileFormat(StudyEntry.WFDB_DATA);		break;
			case DAT:	method = "wfdbToRDT"; 		metaData.setFileFormat(StudyEntry.WFDB_DATA);		break;
			case HEA:	method = "wfdbToRDT"; 		metaData.setFileFormat(StudyEntry.WFDB_HEADER);		break;
			case ZIP:	method = "processUnZipDir";	/* leave the fileFormat tag alone*/ 				break;
			case TXT:	method = evaluateTextFile(outfilename);	/* will eventually process GE MUSE*/	break;
			case CSV:	method = "xyFile";						break;
			case NAT:	method = "na";							break;
			case GTM:	method = "na";							break;
			case XML:	method = "hL7";							break;
			default:	method = "geMuse";						break;
			}
			
			if(fileType == EnumFileType.HEA) {
				
				// Parse the locally held header file
				correctFormat = checkWFDBHeader(tempHeaderFile);
				
				// now we can delete the file
				File file = new File(tempHeaderFile);
				file.delete();
			}
			
			if(!correctFormat) {
				throw new UploadFailureException("The header file has not been parsed properly");
			}
			
			System.out.println("method = " + method);
			
			if(ResourceUtility.getFtpHost().equals("0") ||
					ResourceUtility.getFtpUser().equals("0") ||
					ResourceUtility.getFtpPassword().equals("0")){
				
				System.out.println("Missing FTP Configuration.  Cannot run File Conversion Web Service.");
				return;		
			}
			
			if(ResourceUtility.getNodeConversionService().equals("0")){
				System.out.println("Missing Web Service Configuration.  Cannot run File Conversion Web Service.");
				return;	
			}

			if(!method.equals("na")){
			
				LinkedHashMap<String, String> parameterMap = new LinkedHashMap<String, String>();
			
				parameterMap.put("userid", uId);
				parameterMap.put("subjectid", metaData.getSubjectID());
				parameterMap.put("filename", outfilename);

				parameterMap.put("ftphost", ResourceUtility.getFtpHost());
				parameterMap.put("ftpuser", ResourceUtility.getFtpUser());
				parameterMap.put("ftppassword", ResourceUtility.getFtpPassword());

				parameterMap.put("publicprivatefolder", String.valueOf(isPublic));
				parameterMap.put("verbose", String.valueOf(false));
				parameterMap.put("service", "DataConversion");

				System.out.println("Calling Web Service.");
				
				WebServiceUtility.callWebService(parameterMap, isPublic, method, ResourceUtility.getNodeConversionService(), null);
			}
			
			UploadUtility utility = new UploadUtility(com.liferay.util.portlet.PortletProps.get("dbUser"),
					com.liferay.util.portlet.PortletProps.get("dbPassword"), 
					com.liferay.util.portlet.PortletProps.get("dbURI"),	
					com.liferay.util.portlet.PortletProps.get("dbDriver"), 
					com.liferay.util.portlet.PortletProps.get("dbMainDatabase"));
			utility.storeFileMetaData(metaData);
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
	
	private boolean checkWFDBHeader(String fileLocation) {
		
		// To find out more about the WFDB header format, go to:
		// http://www.physionet.org/physiotools/wag/header-5.htm
		//
		// The goal is to extract the metadata information needed without the need for using the physionet libraries
		
		boolean returnValue = false;

		FileInputStream file = null;
		DataInputStream wfdbInputStream = null;
		
		BufferedReader br = null;
		
		try{

			file = new FileInputStream(fileLocation);
			wfdbInputStream = new DataInputStream(file);
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
		    else {//This is an improperly formed header file.  There should be at least two fields, separated by spaces
		    	returnValue = false;
		    }
		    
		    //Close the input stream
			if(wfdbInputStream != null) {
				wfdbInputStream.close();
			}
			if(br != null) {
				br.close();
			}
			if(file != null) {
				file.close();
			}

		}catch (Exception e){ //Catch exception if any
			System.err.println("Error: " + e.getMessage());
			try {
			    //Close the input stream
				if(wfdbInputStream != null) {
					wfdbInputStream.close();
				}
				if(br != null) {
					br.close();
				}
				if(file != null) {
					file.close();
				}
			} catch (IOException e2) {
				System.err.println("Error: " + e2.getMessage());
			}
			returnValue = false;
			return returnValue;
		}	
		return returnValue;
	}

	private String generateTime() {

		// create a unique folder name based on current date/time plus a random
		// number.
		// Easier for humans read than a GUID and sorts in a meaningful way.
		
		Date newDate = new Date();
		String dateString = newDate.toString();	
		SimpleDateFormat newDateFormat = new SimpleDateFormat("MMddHHmmss");
		try {
			Date parsedDate = newDateFormat.parse(dateString);
			System.out.println("File date string: " + parsedDate.toString());
		} catch (ParseException e) {
			System.out.println("Date thingy failed.");
			e.printStackTrace();
		}
				
		Calendar now = Calendar.getInstance();
		
		int year = now.get(Calendar.YEAR);
		int month = now.get(Calendar.MONTH) + 1;
		int day = now.get(Calendar.DATE);
		int hour = now.get(Calendar.HOUR_OF_DAY); // 24 hour format
		int minute = now.get(Calendar.MINUTE);
		int second = now.get(Calendar.SECOND);
		String date = new Integer(year).toString();

		if (month < 10)
			date = date + "0"; // zero padding single digit months to aid
								// sorting.
		date = date + new Integer(month).toString();

		if (day < 10)
			date = date + "0"; // zero padding to aid sorting.
		date = date + new Integer(day).toString();

		String time = "";

		if (hour < 10)
			time = time + "0"; // zero padding to aid sorting.
		time = time + new Integer(hour).toString();

		if (minute < 10)
			time = time + "0"; // zero padding to aid sorting.
		time = time + new Integer(minute).toString();

		if (second < 10)
			time = time + "0"; // zero padding to aid sorting.
		time = time + new Integer(second).toString() + "|";

		// add the random number just to be sure we avoid name collisions
		Random rand = new Random();
		time = time + rand.nextInt(1000);

		metaData.setDate(month + "/" + day + "/" + year);

		return date + time;
	}
	
}
