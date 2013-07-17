package edu.jhu.cvrg.waveform.model;

import java.io.Serializable;
import java.util.List;

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
* @author Brandon Benitez
* 
 * The StudyEntry class is a bean that will fetch the data from the XML database that is needed for the Analyze
 * list display.  This is a prototype class that will be expanded on in the future. 
 *
 */

public class StudyEntry implements Serializable{
	
	private static final long serialVersionUID = 186114464199110323L;

	private String studyID = "";
	private String datatype = "";
	private String subjectID = "";
	private String submitterID = "";
	private String recordName = "";
	private int fileFormat = -1;
	private RecordDetails recordDetails;
	private double samplingRate = 0;
	private int leadCount = 0;
	private int numberOfPoints = 0;
	private String subjectGender = "";
	private int subjectAgeAtECGRecording;
	private String dateOfRecording = "";
	private String virtualPath;
	
	//  These are static enumerations that help us identify what file format it is.  These
	//  are also used in comparisons to determine which one it is from an XML query.
	
	/**
	 *  Indicates an ECG file used by GE Magellan 
	 */
	public static final int GE_MAGELLAN = 0;
	
	/**
	 *  Indicates an WFDB file format.  Requires both a header and a .dat file 
	 */
	public static final int WFDB_HEADER = 1;
	
	public static final int WFDB_DATA = 2;
	
	/**
	 *  Indicates an RDT file 
	 */
	public static final int RDT = 3;
	
	/**
	 *  Indicates a Holter2 formatted file 
	 */
	public static final int HOLTER12 = 4;
	
	/**
	 *  Indicates a Holter13 formatted file 
	 */
	public static final int HOLTER3 = 5;
	
	/**
	 * Indicates a GE Muse file
	 */
	public static final int GE_MUSE = 6;
	
	/**
	 * Indicates an HL7 format
	 */
	public static final int HL7 = 7;
	
	/**
	 * Indicates an xyFile (which is has a .csv extension)
	 */
	public static final int XY_FILE = 8;
	
	/**
	 * 	
	 * public StudyEntry(String newStudy, String newSubject, String newDataType, String newDate)
	 * 
	 * Takes in four strings and explicitly sets the members of the object
	 * 
	 * @param newStudy - The name of the study that uses the signal
	 * @param newSubject - The ID of the subject being studied
	 * @param newDataType - The type of data contained in the signal
	 * @param newDate - The date when the signal was recorded
	 */
	public StudyEntry(String newStudy, String newDataType,  String newSubject, String newUser, String newRecord, int newFormat, RecordDetails newFiles, double newRate, int newLeads, int newPoints, String newGender, int newAge, String newDate) {
		studyID = newStudy;
		datatype = newDataType;
		subjectID = newSubject;
		submitterID = newUser;
		recordName = newRecord;
		fileFormat = newFormat;
		recordDetails = newFiles;
		samplingRate = newRate;
		leadCount = newLeads;
		numberOfPoints = newPoints;
		subjectGender = newGender;
		subjectAgeAtECGRecording = newAge;
		dateOfRecording = newDate;
	}
	
	/**
	 * public StudyEntry()
	 * 
	 * Default Constructor
	 * 
	 * Note:  This is the one that will be used in the future for XML data retrieval
	 * 
	 */
	public StudyEntry() {
		// just a stub for now
		recordDetails = new RecordDetails();
	}
	
	/**
	 * 
	 * public String getStudy()
	 * 
	 * Retrieves the name of the study being used.
	 * 
	 * @return The name of the study that uses the signal
	 */
	public String getStudy() {
		return studyID;
	}
	
	
	/**
	 * 
	 * public String getSubjectID()
	 * 
	 * Retrieves the ID of the subject that the recorded signal belongs to.
	 * 
	 * @return The name of the subject ID
	 */
	public String getSubjectID() {
		return subjectID;
	}
	
	
	/**
	 * 
	 * public String getDataType()
	 * 
	 * Retrieves the type of data captured in the recorded.
	 * 
	 * @return The data type that is used
	 */
	public String getDatatype() {
		return datatype;
	}
	
	public String getSubmitterID() {
		return submitterID;
	}
	
	public String getRecordName() {
		return recordName;
	}
	
	public int getFileFormatEnum() {
		return fileFormat;
	}
	
	public String getFileFormat() {
		
		String returnValue = "Unknown";
		
		switch(fileFormat){
			case StudyEntry.GE_MAGELLAN:
				returnValue = "GE Magellan";
				break;
			case StudyEntry.GE_MUSE:
				returnValue = "GE Muse";
				break;
			case StudyEntry.HL7:
				returnValue="HL7";
				break;
			case StudyEntry.HOLTER12:
				returnValue="Holter 12";
				break;
			case StudyEntry.HOLTER3:
				returnValue="Holter 3";
				break;
			case StudyEntry.RDT:
				returnValue="RDT";
				break;
			case StudyEntry.WFDB_HEADER:
				returnValue="WFDB_HEADER";
				break;
			case StudyEntry.WFDB_DATA:
				returnValue="WFDB_DATA";
				break;
			case StudyEntry.XY_FILE:
				returnValue="XY formatted CSV file";
				break;
			default:
				returnValue="Unknown";
				break;
		}
		
		return returnValue;
	}
	
	public RecordDetails getRecordDetails () {
		return recordDetails;
	}
	
	public String[] getAllFilenames() {
		List<FileDetails> allFiles = recordDetails.getAllFiles();
		
		FileDetails[] files = (FileDetails[]) allFiles.toArray(new FileDetails[allFiles.size()]);
		
		String[] fileNames = new String[files.length];
		
		for(int i=0;i<fileNames.length;i++) {
			fileNames[i] = files[i].getFileLocation();
		}
		
		return fileNames;
	}
	
	public int getFileSize() {
		FileDetails thatFile = recordDetails.getFileByRecordName(this.recordName);
		
		int returnValue = thatFile.getfileSize();
		
		return returnValue;
	}
	
	public String getFileLocation() {
		FileDetails thatFile = recordDetails.getFileByRecordName(this.recordName);
		
		String returnValue = thatFile.getFileLocation();
		
		return returnValue;
	}

	public String getDataFile() {
		String fileName = "ERROR:  Could not find header file";
		
		FileDetails fileToUse = this.recordDetails.getFileByExtension(".dat");
		
		if(fileToUse != null) {
			fileName = fileToUse.getFileLocation();
		}
		
		return fileName;
	}
	
	public String getHeaderFile() {
		String fileName = "ERROR:  Could not find header file";
		
		FileDetails fileToUse = this.recordDetails.getFileByExtension(".hea");
		
		if(fileToUse != null) {
			fileName = fileToUse.getFileLocation();
		}
		
		return fileName;
	}
	
	public double getSamplingRate () {
		return samplingRate;
	}
	
	public int getLeadCount() {
		return leadCount;
	}
	
	public int getNumberOfPoints() {
		return numberOfPoints;
	}
	
	public String getSubjectGender() {
		return subjectGender;
	}
	
	public int getSubjectAgeAtECGRecording() {
		return subjectAgeAtECGRecording;
	}
	
	/**
	 * 
	 * public String getDateOfRecording()
	 * 
	 * Retrieves the date which the recording took place
	 * 
	 * @return A string representing the date in MM/DD/YYYY format
	 */
	public String getDateOfRecording() {
		return dateOfRecording;
	}
	
	/**
	 * 
	 * public void setStudy(String newStudy)
	 * 
	 * Sets a new value to the name of the study being used.
	 */
	public void setStudy(String newStudy) {
		studyID = newStudy;
	}

	/**
	 * public void setSubjectID(String newID)
	 * 
	 * Sets a new value to the ID of the subject in the study.
	 */
	public void setSubjectID(String newID) {
		subjectID = newID;
	}
	
	/**
	 * 
	 * public void setStudy(String newData)
	 * 
	 * Sets a new value to the type of data used in the recorded signal.
	 */
	public void setDataType(String newData) {
		datatype = newData;
	}
	
	public void setSubmitterID(String userNew) {
		this.submitterID = userNew;
	}
	
	public void setRecordName(String nameNew) {
		this.recordName = nameNew;
	}
	
	public void setFileFormat(int formatNew) {
		this.fileFormat = formatNew;
	}
	
	public void setRecordDetails (RecordDetails detailsNew) {
		this.recordDetails = detailsNew;
	}
	
	public void setSamplingRate (double rateNew) {
		this.samplingRate = rateNew;
	}
	
	public void setLeadCount(int countNew) {
		this.leadCount = countNew;
	}
	
	public void setNumberOfPoints(int pointsNew) {
		this.numberOfPoints = pointsNew;
	}
	
	public void setSubjectGender(String genderNew) {
		this.subjectGender = genderNew;
	}
	
	public void setSubjectAgeAtECGRecording(int ageNew) {
		this.subjectAgeAtECGRecording = ageNew;
	}
	
	/**
	 * 
	 * public void setStudy(String newDate)
	 * 
	 * Sets a new value to the date of the recording.  Currently, this method assumes that the date is in MMM/DD/YYYY format.
	 * Once the initial prototype is complete, use of that format will be required.
	 */
	public void setDateOfRecording(String newDate) {
		dateOfRecording = newDate;
	}
	
	public void addFile(FileDetails newFile) {
		recordDetails.addFile(newFile);
	}
	
	public String toString() {
		
		String tempString = "\n";
		
		tempString += "Study ID:  " + studyID + "\n";
		tempString += "Type of Data:  " + datatype + "\n";
		tempString += "Subject ID:  " + subjectID + "\n";
		tempString += "Submitter ID:  " + submitterID + "\n";
		tempString += "Name of the Record:  " + recordName + "\n";
		
		List fileDetails = recordDetails.getAllFiles();
		
		tempString += "File Format:  " + fileFormat + "\n";
		tempString += "Files Inside:\n";
		for (int i=0;i<fileDetails.size();i++) {
			tempString += "\tFile Name:  " + ((FileDetails)fileDetails.get(i)).getFileLocation() + "\n";
			tempString += "\tFile Size:  " + ((FileDetails)fileDetails.get(i)).getfileSize() + " bytes\n\n";
		}			
		
		tempString += "Sampling Rate:  " + samplingRate + "\n";
		tempString += "Number of Leads:  " + leadCount + "\n";
		tempString += "Number of Points:  " + numberOfPoints + "\n";
		tempString += "Suject Gender:  " + subjectGender + "\n";
		tempString += "Subject Age At ECG Recording:  " + subjectAgeAtECGRecording + "\n";
		tempString += "Date of Recording:  " + dateOfRecording + "\n";
		
		return tempString;
	}
	
	/** Calculated number of milliseconds in full ECG file, based on getNumberOfPoints and getSamplingRate.
	 * 
	 * @return
	 */
	public int getMsecDuration(){
		int duration = (int)((getNumberOfPoints()/getSamplingRate())*1000.0); // number of milliseconds in full ECG file.
		return duration;
	}

	public String getVirtualPath() {
		return virtualPath;
	}

	public void setVirtualPath(String virtualPath) {
		this.virtualPath = virtualPath;
	}
}
