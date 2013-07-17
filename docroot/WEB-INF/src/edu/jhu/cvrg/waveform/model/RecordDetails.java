package edu.jhu.cvrg.waveform.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

//import existprototype.FileDetails;


public class RecordDetails implements Serializable{

	private static final long serialVersionUID = -3604690086463699683L;

	private List<FileDetails> fileDetails = new ArrayList<FileDetails>();
	
	public RecordDetails () {
		
	}
	
	public List getAllFiles() {
		return fileDetails;
	}
	
	public FileDetails getFileByURI(String fileSearchURI) {
		FileDetails returnedFile = null;
		
		for (int i=0;i<fileDetails.size();i++) {
			if (fileSearchURI.equals(((FileDetails)fileDetails.get(i)).getFileLocation())) {
				returnedFile = (FileDetails)fileDetails.get(i);
				break;
			}
		}			
		
		return returnedFile;
	}
	
	public FileDetails getFileByExtension(String extension) {
		FileDetails returnedFile = null;
		
		for (int i=0;i<fileDetails.size();i++) {
			if (((FileDetails)fileDetails.get(i)).getFileLocation().endsWith(extension)) {
				returnedFile = (FileDetails)fileDetails.get(i);
				break;
			}
		}			
		
		return returnedFile;
	}
	
	public FileDetails getFileByRecordName(String theName) {
		FileDetails returnedFile = null;
		
		for (int i=0;i<fileDetails.size();i++) {
			if ((((FileDetails)fileDetails.get(i)).getFileLocation()).contains(theName)) {
				returnedFile = (FileDetails)fileDetails.get(i);
				break;
			}
		}			
		
		return returnedFile;
	}
	
	public void addFile(FileDetails newFile) {
		fileDetails.add(newFile);
	}
	
	public void addFile(String newFileURI, int newFileSize) {
		FileDetails temp = new FileDetails(newFileURI, newFileSize);
		fileDetails.add(temp);
		
		FileDetails[] tempArray = (FileDetails[])fileDetails.toArray();
		
		Arrays.sort(tempArray);
		
		temp = null;
	}
}
