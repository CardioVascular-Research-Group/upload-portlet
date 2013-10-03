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
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.util.Iterator;

import javax.xml.bind.JAXBException;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.jdom.filter.ElementFilter;
import org.jdom.input.SAXBuilder;
import org.jdom.output.XMLOutputter;

// This is for Philips 1.03 format
import org.sierraecg.DecodedLead;
import org.sierraecg.SierraEcgFiles;
import org.sierraecg.schema.Generalpatientdata;
import org.sierraecg.schema.Restingecgdata;
import org.sierraecg.schema.Signalcharacteristics;

// This is for Philips 1.04 format
import org.cvrgrid.philips.*;
import org.cvrgrid.philips.jaxb.schema.*;

import com.liferay.portal.model.User;

import edu.jhu.cvrg.waveform.exception.UploadFailureException;
import edu.jhu.cvrg.waveform.model.AnnotationData;
import edu.jhu.cvrg.waveform.model.StudyEntry;
import edu.jhu.cvrg.waveform.utility.AnnotationUtility;
import edu.jhu.cvrg.waveform.utility.EnumFileType;
import edu.jhu.cvrg.waveform.utility.FTPUtility;
import edu.jhu.cvrg.waveform.utility.MetaContainer;
import edu.jhu.cvrg.waveform.utility.ResourceUtility;
import edu.jhu.cvrg.waveform.utility.UploadUtility;
import edu.jhu.cvrg.waveform.utility.WebServiceUtility;

public class UploadManager {

	private char name_separator = (char) 95;

	private MetaContainer metaData = new MetaContainer();
	private String tempHeaderFile = "";
	private User user;
	EnumFileType fileType;
	Document xmlJdom;
	
	// For Philips 1.03 format
	org.sierraecg.schema.Restingecgdata philipsECG103;
	org.sierraecg.DecodedLead[] leadData103;
	
	// For Philips 1.04 format
	org.cvrgrid.philips.jaxb.beans.Restingecgdata philipsECG104;
	org.cvrgrid.philips.DecodedLead[] leadData104;
 
	public void processUploadedFile(InputStream fileToSave, String fileName, long fileSize, String studyID, String datatype, String virtualPath) throws UploadFailureException {

		metaData.setFileName(fileName);
		metaData.setStudyID(studyID);
		metaData.setDatatype(datatype);
		if(virtualPath != null) {
			metaData.setTreePath(virtualPath);
		}
		else {
			throw new UploadFailureException("Please select a folder");
		}

		user = ResourceUtility.getCurrentUser();

		try {
		
				fileType = EnumFileType.valueOf(extension(metaData.getFileName()).toUpperCase());
				
				File tempFile =	saveFileToTemp(fileToSave, fileSize);
				
				if(fileType == EnumFileType.XML) {
					String xmlString = "";
					
					BufferedReader xmlBuf = new BufferedReader(new FileReader(tempFile));
					String line = xmlBuf.readLine();
					
					while(line != null) {			
						if(!(line.contains("!DOCTYPE"))) {
							xmlString = xmlString + line;
						}
						line = xmlBuf.readLine();
					}
					
					xmlBuf.close();
					
					// check for the first xml tag
					// if it does not exist, remake the file using UTF-16 
					// UTF-16 is known not to work with the input stream passed in, and saves the raw bytes
					if(!(xmlString.contains("xml"))) {
						// TODO:  In the future, use a library to check for the encoding that the stream uses
						FileInputStream tempFis = new FileInputStream(tempFile);
						xmlString = convertStreamToString(tempFis, "UTF-16");
						tempFis.close();
						tempFile.delete();
						
						fileToSave = new ByteArrayInputStream(xmlString.getBytes("UTF-16"));
						tempFile = saveFileToTemp(fileToSave, fileSize);
						
					}
					
					
					// indicates one of the Philips formats
					
					// JDOM seems to be having problems building the XML structure for Philips 1.03,
					// Checking for the elements directly in the string is required to get the right version
					// and also takes less memory
					if(xmlString.contains("restingecgdata")) {						
						if(xmlString.contains("<documentversion>1.03</documentversion>")) {
							fileType = EnumFileType.PHIL103;
							extractPhilips103Data(tempFile);
						}
						else if(xmlString.contains("<documentversion>1.04</documentversion>")) {
							fileType = EnumFileType.PHIL104;
							extractPhilips104Data(tempFile);
						}
						else {
							throw new UploadFailureException("Unrecognized version number for Philips file");
						}
							
							//TODO:  Insert the call to the method which strips any identifiable information if it is a Philips XML
							// Make sure to convert the resulting String back to an InputStream so it can be fed to the saveFileToTemp method
							
					}
					// indicates GE Muse 7
					else if(xmlString.contains("RestingECG")) {
						//TODO:  Insert the call to the method which strips any identifiable information if it is a Philips XML
						// Make sure to convert the resulting String back to an InputStream so it can be fed to the saveFileToTemp method
						xmlJdom = build(xmlString);
						fileType = EnumFileType.MUSEXML;
						extractMuseXMLData();
					}						
					
				}
				
				String userId = user.getScreenName();
				if(userId == null || userId.equals("")){
					userId = user.getEmailAddress();
				}
				metaData.setUserID(userId);
				
				String outputDirectory = uploadFileFtp(userId, tempFile);
	
				convertUploadedFile(outputDirectory, userId, false);

			} catch (Exception e) {
				e.printStackTrace();
				throw new UploadFailureException("This upload failed because a " + e.getClass() + " was thrown with the following message:  " + e.getMessage());
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

	private String uploadFileFtp(String userId, File file) throws UploadFailureException {

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

	private void convertUploadedFile(String outputDirectory, String uId, boolean isPublic) throws UploadFailureException {

			String method = "na";
			String outfilename = ""; 
			if(File.separator.equals("\\")) {
				outfilename = outputDirectory + "//" + metaData.getFileName();
			}
			else {
				outfilename = outputDirectory + File.separator + File.separator + metaData.getFileName();
			}
			boolean correctFormat = true;

			switch (fileType) {
			case RDT:	method = "rdtToWFDB16";					break;
			case XYZ:	method = "wfdbToRDT"; 		metaData.setFileFormat(StudyEntry.WFDB_DATA);		break;
			case DAT:	method = "wfdbToRDT"; 		metaData.setFileFormat(StudyEntry.WFDB_DATA);		break;
			case HEA:	method = "wfdbToRDT"; 		metaData.setFileFormat(StudyEntry.WFDB_HEADER);		break;
			case ZIP:	method = "processUnZipDir";	/* leave the fileFormat tag alone*/ 				break;
			case TXT:	method = evaluateTextFile(outfilename);	/* will eventually process GE MUSE Text files*/	break;
			case CSV:	method = "xyFile";						break;
			case NAT:	method = "na";							break;
			case GTM:	method = "na";							break;
			case XML:	method = "hL7";							break;
			case PHIL103:	method = "philips103ToWFDB";	metaData.setFileFormat(StudyEntry.PHILIPSXML103);		break;
			case PHIL104:	method = "philips104ToWFDB";	metaData.setFileFormat(StudyEntry.PHILIPSXML104);		break;
			case MUSEXML:	method = "museXML";	metaData.setFileFormat(StudyEntry.MUSEXML);		break;
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
			
			// Now do annotations from Muse or Philips files
			if(fileType == EnumFileType.PHIL103) {
				ProcessPhilips103 phil103Ann = new ProcessPhilips103(philipsECG103, metaData.getStudyID(), metaData.getUserID(), metaData.getRecordName(), metaData.getSubjectID());
				phil103Ann.populateAnnotations();
				
				ArrayList<AnnotationData> orderList = phil103Ann.getOrderInfo();
				ArrayList<AnnotationData> dataList = phil103Ann.getDataAcquisitions();
				ArrayList<AnnotationData> globalList = phil103Ann.getGlobalAnnotations();
				ArrayList<AnnotationData[]> leadList = phil103Ann.getLeadAnnotations();
				
				convertNonLeadAnnotations(globalList, "");
				convertLeadAnnotations(leadList);
				convertNonLeadAnnotations(orderList, "");
				convertNonLeadAnnotations(dataList, "");
			}
			else if(fileType == EnumFileType.PHIL104) {
				ProcessPhilips104 phil104Ann = new ProcessPhilips104(philipsECG104, metaData.getStudyID(), metaData.getUserID(), metaData.getRecordName(), metaData.getSubjectID());
				phil104Ann.populateAnnotations();
				
				ArrayList<AnnotationData> orderList = phil104Ann.getOrderInfo();
				ArrayList<AnnotationData> dataList = phil104Ann.getDataAcquisitions();
				ArrayList<AnnotationData> globalList = phil104Ann.getCrossleadAnnotations();
				ArrayList<AnnotationData[]> leadList = phil104Ann.getLeadAnnotations();
				
				convertNonLeadAnnotations(globalList, "");
				convertLeadAnnotations(leadList);
				convertNonLeadAnnotations(orderList, "");
				convertNonLeadAnnotations(dataList, "");
			}
			
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
		    else {  //This is an improperly formed header file.  There should be at least two fields, separated by spaces
		    	returnValue = false;
		    }
		    
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
	
	// Unfortunately, the two different data structures for Philips are not interchangeable.  Since the underlying 
	// schema is different, different beans are used to house the structure.  So while similar on the surface, under
	// the hood they are organized differently.
	private void extractPhilips103Data(File file) throws IOException, JAXBException {
		philipsECG103 = SierraEcgFiles.preprocess(file);
		leadData103 = SierraEcgFiles.extractLeads(file);
		Signalcharacteristics signalMetaData = philipsECG103.getDataacquisition().getSignalcharacteristics();
		
		metaData.setSampFrequency(Float.valueOf(signalMetaData.getSamplingrate()));
		metaData.setChannels(Integer.valueOf(signalMetaData.getNumberchannelsallocated()));
		metaData.setNumberOfPoints(leadData103[0].size() * metaData.getChannels());
		
	}
	
	private void extractPhilips104Data(File file) throws IOException, JAXBException {
		philipsECG104 = org.cvrgrid.philips.SierraEcgFiles.preprocess(file);
		leadData104 = org.cvrgrid.philips.SierraEcgFiles.extractLeads(file);
		org.cvrgrid.philips.jaxb.beans.Signalcharacteristics signalMetaData = philipsECG104.getDataacquisition().getSignalcharacteristics();
		
		metaData.setSampFrequency(Float.valueOf(signalMetaData.getSamplingrate()));
		metaData.setChannels(signalMetaData.getNumberchannelsallocated().intValue());  // Method returns a BigInteger, so a conversion to int is required.
		metaData.setNumberOfPoints(leadData104[0].size() * metaData.getChannels());

	}
	
	private void extractMuseXMLData() {
		Element rootElement = xmlJdom.getRootElement();
		List waveformElements = rootElement.getChildren("Waveform");
		
		// Since the DTD was unable to be found, the XML had to be traversed one level at a time
		if(!(waveformElements.isEmpty())) {
			Iterator waveformIter = waveformElements.iterator();
			while(waveformIter.hasNext()) {
				Element nextWaveform = (Element)waveformIter.next();
				Element waveformType = nextWaveform.getChild("WaveformType");
				
				// Check to make sure there are valid waveforms, then get each WaveFormData tag, which is a child of a LeadData tag
				if((waveformType != null) && (waveformType.getText().equals("Rhythm"))) {
					
					// get the Sampling Rate of the waveform in the process
					metaData.setSampFrequency(Float.valueOf(nextWaveform.getChild("SampleBase").getText()));
					
					List leadDataList = nextWaveform.getChildren("LeadData");
					
					if(!(leadDataList.isEmpty())) {
						metaData.setChannels(leadDataList.size());
						
						Iterator leadIter = leadDataList.iterator();
						
						Element leadData = (Element)leadIter.next();
						Element sampleCount = leadData.getChild("LeadSampleCountTotal");
							
						metaData.setNumberOfPoints(Integer.valueOf(sampleCount.getText()) * metaData.getChannels());
					}
				}
			}
		}
	}
	
	public static void convertAnnotations(AnnotationData[] annotationArray, boolean isLeadAnnotation, String groupName) throws UploadFailureException {
		//AnnotationUtility dbAnnUtility = new AnnotationUtility("waveformUser", "d@rks0uls!", "xmldb:exist://128.220.76.161:8080/exist-1.4.2-rev16251/xmlrpc", "org.exist.xmldb.DatabaseImpl", "/db/waveformrecords");
		AnnotationUtility dbAnnUtility = new AnnotationUtility(com.liferay.util.portlet.PortletProps.get("dbUser"),
				com.liferay.util.portlet.PortletProps.get("dbPassword"), 
				com.liferay.util.portlet.PortletProps.get("dbURI"),	
				com.liferay.util.portlet.PortletProps.get("dbDriver"), 
				com.liferay.util.portlet.PortletProps.get("dbMainDatabase"));
		
		for(AnnotationData annData : annotationArray) {			
			boolean success = true;
			
			if(isLeadAnnotation) {
				System.out.println("Storing lead index " + annData.getLeadIndex());
				success = dbAnnUtility.storeLeadAnnotationNode(annData);
				
			}
			else {
				success = dbAnnUtility.storeComment(annData);
			}
			
			if(!success) {
				throw new UploadFailureException("Annotations for Philips file failed to enter the database properly");
			}
		}
	}
	
	private void convertLeadAnnotations(ArrayList<AnnotationData[]> allLeadAnnotations) throws UploadFailureException {
		for(int i=0; i<allLeadAnnotations.size(); i++) {
			if(allLeadAnnotations.get(i).length != 0) {
				System.out.println("There are annotations in this lead.  The size is " + allLeadAnnotations.get(i).length);
				convertAnnotations(allLeadAnnotations.get(i), true, "");
			}
		}
	}
	
	private void convertNonLeadAnnotations(ArrayList<AnnotationData> allAnnotations, String groupName) throws UploadFailureException {
		AnnotationData[] annotationArray = new AnnotationData[allAnnotations.size()];
		annotationArray = allAnnotations.toArray(annotationArray);
		
		
		convertAnnotations(annotationArray, false, groupName);
	}
	
	
	/**
	 * Helper method to build a <code>jdom.org.Document</code> from an 
	 * XML document represented as a String
	 * @param  xmlDocAsString  <code>String</code> representation of an XML
	 *         document with a document declaration.
	 *         e.g., <?xml version="1.0" encoding="UTF-8"?>
	 *                  <root><stuff>Some stuff</stuff></root>
	 * @return Document from an XML document represented as a String
	 */
	private static Document build(String xmlDocAsString) 
	        throws JDOMException {
		Document doc = null;
	    SAXBuilder builder = new SAXBuilder();
	    Reader stringreader = new StringReader(xmlDocAsString);
	    try {
	    	doc = builder.build(stringreader);
	    } catch(IOException ioex) {
	    	ioex.printStackTrace();
	    }
	    return doc;
	}
	
	private static String convertStreamToString(InputStream is, String encoding) {
	    Scanner inputScan = new Scanner(is, encoding).useDelimiter("\\A");
	    if(inputScan.hasNext()) {
	    	return inputScan.next();
	    }

	    return "";
	}
	
}
