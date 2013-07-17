package edu.jhu.cvrg.waveform.utility;

import org.xmldb.api.base.Resource;
import org.xmldb.api.base.ResourceIterator;
import org.xmldb.api.base.ResourceSet;
import org.xmldb.api.base.XMLDBException;

import com.thoughtworks.xstream.XStream;

import edu.jhu.cvrg.dbapi.EnumXMLInsertLocation;
import edu.jhu.cvrg.dbapi.XMLUtility;
import edu.jhu.cvrg.waveform.utility.UploadQueryBuilder;
import edu.jhu.cvrg.waveform.model.FileDetails;
import edu.jhu.cvrg.waveform.model.RecordDetails;
import edu.jhu.cvrg.waveform.model.StudyEntry;
import edu.jhu.cvrg.waveform.utility.MetaContainer;

public class UploadUtility extends XMLUtility {
	
	//private String dbMainCollection = "";
	private UploadQueryBuilder uploadBuilder;


	/**
	 * Default Constructor
	 * 
	 * tells the query builder where to find the database URI and collection
	 */
	public UploadUtility() {
		super();
		System.out.println("Creating an upload utility - UploadUtility version 2.0");
		uploadBuilder = new UploadQueryBuilder(this.dbURI, this.dbMainCollection);
	}
	
	public UploadUtility(String userName, String userPassword, String uRI, 
			String driver, String mainDatabase) {
		super(userName, userPassword, uRI, driver, mainDatabase);
		System.out.println("Creating an upload utility - UploadUtility version 2.0");
		uploadBuilder = new UploadQueryBuilder(this.dbURI, this.dbMainCollection);
	}
		
	// FIXME:  The remaining queries must go in the upload query builder
	/**
	 * The primary function that determines which update queries get built and how the uploaded data gets stored.  It also translates
	 * the meta data gathered in Java into XML format for insertion.
	 * 
	 * @param metaData - a MetaContainer object which contains the meta data to be stored for an uploaded file
	 */
	public void storeFileMetaData(MetaContainer metaData) {
		
		System.out.println("Entering storeFileMetaData");
		
		//ModelConstants allConstants = new ModelConstants();
		StudyEntry insertion = createStudyEntry(metaData);
		String fileBlock = "";
		
		// Set up the XStream objects for later use
		XStream xmlStream = new XStream();
		
		xmlStream.alias("studyEntry", StudyEntry.class);
		xmlStream.alias("fileDetails", FileDetails.class);
		xmlStream.alias("recordDetails", RecordDetails.class);
		xmlStream.addImplicitCollection(RecordDetails.class, "fileDetails");
		
		fileBlock = xmlStream.toXML(insertion);
		
		//System.out.println("\n" + fileBlock + "\n");

			// First, search to see if there is already a file by the same name.
			// If there is, simply add that file
			// into the existing info block. Otherwise, create a new WFDB file
			// Create the entire new block of an ECG File for use in the
			// database.
		
		System.out.println("insertion record name = " + insertion.getRecordName());
		System.out.println("metadata subject name = " + metaData.getSubjectID());
		System.out.println("user name = " + metaData.getUserID());

		if (recordExists(insertion.getRecordName(), metaData.getSubjectID(), metaData.getUserID())) {
			System.out.println("A record already exists");
			
			// Only insert the file details portion and update the metadata
			// insert new file block into existing XML
			boolean commitToDB = true;
			
			// first, check to see if it is a header file
			if(metaData.getFileFormat() == StudyEntry.WFDB_HEADER) {
				// if it is a header file, check for duplicate .hea file 
				
				String fileQuery = uploadBuilder.findFile(metaData.getStudyID(), metaData.getSubjectID(), metaData.getRecordName(), metaData.getUserID());
				ResourceSet filesFound = executeQuery(fileQuery);
				
				ResourceIterator iter;
				try {
					iter = filesFound.getIterator();
				
					Resource selection = null;
					
					while(iter.hasMoreResources()) {
						selection = iter.nextResource();
						String fileResult = (selection.getContent()).toString();
						
						if(fileResult.endsWith(".hea")) {
							commitToDB = false;
						}
						
					}
				} catch (XMLDBException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					commitToDB = false;
				}
				
				// if duplicate is found, or an exception is caught, do not continue.  If not, proceed to update each of the three metadata values
				if(commitToDB) {
					
					// dynamically create leads as needed
					String leadBlocks = "";
					int leadsToInsert = 0;
					int previousLeadCount = 0;
					int totalLeads = metaData.getChannels();
					String getLeadCountQuery = uploadBuilder.getLead(metaData.getStudyID(), metaData.getSubjectID(), metaData.getRecordName(), metaData.getUserID());
					ResourceSet leadResults = executeQuery(getLeadCountQuery);
					ResourceIterator iterator;
					try {
						iterator = leadResults.getIterator();
					
						Resource selection = null;
						
						while(iterator.hasMoreResources()) {
							selection = iterator.nextResource();
							String leadString = (selection.getContent()).toString();
							previousLeadCount = Integer.parseInt(leadString);
							leadsToInsert = totalLeads - previousLeadCount;
							break;
						}
						
						// create the leads
						for(int i=previousLeadCount; i < leadsToInsert+1; i++) {
							int leadNum = i+1;
							leadBlocks = "<lead>   <bioportalReference>    <term>"
											+ leadNum
											+ "</term>   </bioportalReference>  </lead> ";
							
							// insert the results into the record
							String leadInsertionQuery = uploadBuilder.insertLeads(leadBlocks, EnumXMLInsertLocation.FOLLOWING, metaData.getStudyID(), metaData.getSubjectID(), metaData.getRecordName(), metaData.getUserID());
							executeQuery(leadInsertionQuery);
						}
						
						// now update the remaining metadata
						String replaceLeadQuery = uploadBuilder.headerUpdate("leadCount", Integer.toString(metaData.getChannels()), metaData.getStudyID(), metaData.getSubjectID(), metaData.getRecordName(), metaData.getUserID());
						executeQuery(replaceLeadQuery);
						
						if(metaData.getSampFrequency() != 250) {
							String replaceSampQuery = uploadBuilder.headerUpdate("samplingRate", Float.toString(metaData.getSampFrequency()), metaData.getStudyID(), metaData.getSubjectID(), metaData.getRecordName(), metaData.getUserID());
							executeQuery(replaceSampQuery);
						}	
						
						if(metaData.getNumberOfPoints() != 0) {
							String replacePointsQuery = uploadBuilder.headerUpdate("numberOfPoints", Integer.toString(metaData.getNumberOfPoints()), metaData.getStudyID(), metaData.getSubjectID(), metaData.getRecordName(), metaData.getUserID());
							executeQuery(replacePointsQuery);
						}
						
					} catch (XMLDBException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						commitToDB = false;
					}						
					
				}				
				
			}

			String newFileDetailBlock = "<fileDetails><fileLocation>"
					+ metaData.getFullFilePath() + metaData.getFileName() + "</fileLocation><fileSizeBytes>"
					+ metaData.getFileSize() + "</fileSizeBytes></fileDetails>";

			// Important Note: If there is more than one result in this, the
			// XML will be inserted multiple times. It will possibly find
			// two or more entries in the fileInfo tag and insert a new item
			// for each one.
			if(commitToDB) {
				String updateQuery = "for $x in collection('"
							+ this.dbMainCollection
							+ "')//record/studyEntry where $x/recordName=\""
							+ insertion.getRecordName()
							+ "\" and $x/subjectID=\"" + metaData.getSubjectID()
							+ "\" and $x/submitterID=\"" + metaData.getUserID() +  
							"\" return update insert "
							+ newFileDetailBlock + " into $x/recordDetails";
				
				//System.out.println("Update query = " + updateQuery);
	
				executeQuery(updateQuery);
			} else {
				System.out.println("ERROR:  A problem has been encountered and the file will not be added to the database");
			  }
		} else {

			if (subjectExists(insertion.getRecordName(), metaData.getSubjectID(), metaData.getUserID())) {
				
				System.out.println("The record does not exist, but the subject does");
				

								
				// Also, if somehow it should not be committed to the database, do not commit file
				// For now though, only in cases of a duplicate header existing will we do that.
					String newQuery = "for $x in collection('"
									+ this.dbMainCollection
									+ "')//record[studyEntry/recordName=\""
									+ metaData.getRecordName()
									+ "\"] where collection('"
									+ this.dbMainCollection
									+ "')//record/studyEntry/subjectID=\""
									+ metaData.getSubjectID()
									+ "\" return update insert " + fileBlock
									+ " preceding $x/lead";
				
					//System.out.println("New query = " + newQuery);

					executeQuery(newQuery);
			} else {
				// create new XML for the subject
				System.out.println("There is no existing record or subject");
				
				String newEGCFilePartOne = "<record>";
				String newECGFilePartTwo = " ";
				for (int i = 0; i < metaData.getChannels(); i++) {
					int leadNum = i + 1;
					newECGFilePartTwo = newECGFilePartTwo
							+ "<lead>   <bioportalReference>    <term>"
							+ leadNum
							+ "</term>   </bioportalReference>  </lead> ";
				}

				newECGFilePartTwo = newECGFilePartTwo + "</record>";

				String commitQuery = "xmldb:store('"
								+ this.dbMainCollection + "', '"
								+ metaData.getUserID() + "_" + metaData.getSubjectID() + ".xml', '"
								+ newEGCFilePartOne + fileBlock
								+ newECGFilePartTwo + "')";
				//System.out.println("Commit query = " + commitQuery);
				
				executeQuery(commitQuery);
			}
		}
	}
	
	/**
	 * Creates a StudyEntry model bean that will then be translated to XML.
	 * 
	 * @param metaData - The meta data gathered from previous steps in the upload process
	 * @return - The StudyEntry bean to be translated
	 */
	private StudyEntry createStudyEntry(MetaContainer metaData){
		System.out.println("Entering createStudyEntryXML");
		
		StudyEntry newEntry = new StudyEntry();

		FileDetails newFile = new FileDetails();

		newFile.setFileLocation(metaData.getFullFilePath() + metaData.getFileName());
		newFile.setFileSize(metaData.getFileSize());

		newEntry.setRecordName(metaData.getRecordName());
		newEntry.setLeadCount(metaData.getChannels());
		newEntry.setFileFormat(metaData.getFileFormat());
		newEntry.setNumberOfPoints(metaData.getNumberOfPoints());
		newEntry.setSubmitterID(metaData.getUserID());
		newEntry.setSubjectID(metaData.getSubjectID());
		newEntry.setSubjectAgeAtECGRecording(metaData.getSubjectAge());
		newEntry.setSubjectGender(metaData.getSubjectGender());
		newEntry.setStudy(metaData.getStudyID());
		newEntry.setDataType(metaData.getDatatype());
		newEntry.setDateOfRecording(metaData.getDate());
		newEntry.setSamplingRate(metaData.getSampFrequency());
		newEntry.setVirtualPath(metaData.getTreePath());

		newEntry.addFile(newFile);
		
		//System.out.println("\n" + newEntry.toString() + "\n");
		
		System.out.println("Finished createStudyEntryXML");

		return newEntry;
	}	
	
	/**
	 * Builds a query to determine if a specific record exists.  
	 * 
	 * @param recordName - record name to search for
	 * @param subjectId - the subject ID to search for
	 * @param userID - the username of the account calling this.  The query will be limited to records that user owns
	 * @return - true if the query returns results, false otherwise
	 */
	private boolean recordExists(String recordName, String subjectId, String userID){
		System.out.println("Entering recordExists function");
		String returnValue = uploadBuilder.returnSubjectIDData()
							 /* + uploadBuilder.bracketStatement(subjectId) */;
		
		//System.out.println("returnValue = " + returnValue);
		
		return exists("recordName", recordName, returnValue, userID);
	}
	
	/**
	 * Builds a query to determine if a specific subject ID exists.  
	 * 
	 * @param recordName - deprecated, use an empty string for this.  If a search for a record is required, use the recordExists() method
	 * @param subjectId - the subject ID to search for
	 * @param userID - the username of the account calling this.  The query will be limited to records that user owns
	 * @return - true if the query returns results, false otherwise
	 */
	//FIXME:  Take out the recordName parameter.  The only reason this is not done now is so that I do not risk breaking anything before the demo
	//			bbenite1 - 04/15/2013
	private boolean subjectExists(String recordName, String subjectId, String userID){
		System.out.println("Entering recordExists function");
		String returnValue = uploadBuilder.returnSubjectIDData();
		System.out.println("returnValue = " + returnValue);
		return exists("subjectId", subjectId, returnValue, userID);
	}
	
	/**
	 * Builds and executes a query that checks whether or not a given field exists within the database.
	 * 
	 * @param field - the XML term to search for in addition to the userID
	 * @param value - the value to query in the field term
	 * @param returnValue - A string passed in representing the return value of the resulting query
	 * @param userID - the username of the account calling this.  The query will be limited to records that user owns
	 * @return - true if the query returns results, false otherwise
	 */
	private boolean exists(String field, String value, String returnValue, String userID){	
		System.out.println("Entering \"exists\" function");

		String query = uploadBuilder.existanceQuery(field, value, returnValue, userID);
		
		//System.out.println("query being used = " + query);
		
		ResourceSet resourceSet = executeQuery(query);
		try {
			System.out.println("number of results = " + resourceSet.getSize());
			return (resourceSet.getSize() > 0);
		} catch (XMLDBException e) {
			e.printStackTrace();
		}
		return false;
	}

}
