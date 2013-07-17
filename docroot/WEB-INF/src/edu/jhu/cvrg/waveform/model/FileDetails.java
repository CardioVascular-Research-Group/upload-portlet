package edu.jhu.cvrg.waveform.model;

import java.io.Serializable;

/**
 * This is a helper class that corresponds to the fileDetails block in the XML.  In the XML version,
 * there could be one or more of these items and they contain a tag for the actual URI of a file, and the size of
 * the file.
 * 
 * It remains public because we will need fileDetails classes to be returned in accessor functions.
 * 
 * @author bbenite1
 */

public class FileDetails implements Comparable<FileDetails>, Serializable {

	private static final long serialVersionUID = 7616701541127593259L;
	
	private String fileLocation = "";
	private int fileSizeBytes = 0;
	
	public FileDetails(String fileURI, int fileSize) {
		this.fileLocation = fileURI;
		this.fileSizeBytes = fileSize;
	}
	
	public FileDetails() {
		
	}
	
	/**
	 * Every file details block is sorted by the file name until otherwise stated.
	 * 
	 * @param fileDetails rhs - the fileDetails object to be compared
	 * @return the value returned by comparing the two file location strings
	 * 
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 * @see java.lang.String#compareTo(String)
	 */
	// TODO:  Modify this to include file size as well
	public int compareTo(FileDetails rhs) {
		return (this.fileLocation.compareTo(rhs.getFileLocation()));
	}
	
	public String getFileLocation() {
		return fileLocation;
	}
	
	public int getfileSize() {
		return fileSizeBytes;
	}
	
	public void setFileLocation(String newLocation) {
		this.fileLocation = newLocation;
	}
	
	public void setFileSize(int newSize) {
		this.fileSizeBytes = newSize;
	}
}
