package edu.jhu.cvrg.waveform.utility;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

import org.xmldb.api.base.Resource;
import org.xmldb.api.base.ResourceIterator;
import org.xmldb.api.base.ResourceSet;
import org.xmldb.api.base.XMLDBException;

import com.thoughtworks.xstream.XStream;

import edu.jhu.cvrg.dbapi.XMLUtility;
import edu.jhu.cvrg.waveform.model.FileDetails;
import edu.jhu.cvrg.waveform.model.RecordDetails;
import edu.jhu.cvrg.waveform.model.StudyEntry;

public class StudyEntryUtility extends XMLUtility implements Serializable{
	
	private static final long serialVersionUID = 1L;
	StudyQueryBuilder studyBuilder;


	/**
	 * Default Constructor
	 * 
	 * tells the query builder where to find the database URI and collection
	 */
	public StudyEntryUtility(String userName, String userPassword, String uRI, 
			String driver, String mainDatabase) {
		
		super(userName, userPassword, uRI, driver, mainDatabase);
		studyBuilder = new StudyQueryBuilder(this.dbURI, this.dbMainCollection, this.dbDriver);		
	}
	
	/**
	 * This function makes the call to the XML database and retrieves all the file metadata for use in ECGridToolkit.  The data is first retrieved
	 * in a series of XML blocks, and then they are converted into StudyEntry Objects and returned to the user
	 * 
	 * @param userID - The ID of the user.  This is used to check which files the user has submitted and thus has access to
	 * @return The list of StudyEntry Objects taken from the file metadata in the XML database
	 */
	public ArrayList<StudyEntry> getEntries(String userID) {
		ArrayList<StudyEntry> tempList = new ArrayList<StudyEntry>();
		
		try {			
			
			// create first query to get the entire studyEntry block, the collection() XQuery function does this across
			// all documents in the XML Collection
			
			//  The goal of this query is to find all the data we need for the StudyEntry object based on which user submitted them.  For all the documents searched, the query looks to see
			//  if any files for that subject's ECG repository were submitted by that user.  If any files were submitted, retrieve their metadata contained in the studyEntry block.
			String sQuery = studyBuilder.defaultFor() + 
							studyBuilder.defaultWhere(userID) + 
							studyBuilder.defaultOrderBy() + 
							studyBuilder.defaultReturn();
			
			System.out.println("Query to be executed = " + sQuery);									
			
			ResourceSet resultSet = executeQuery(sQuery);
			ResourceIterator iter = resultSet.getIterator();
			Resource selection = null;
			Resource fileSelection = null;
			int listIndex=0;
			int subjectCount = 1;
			
			while(iter.hasMoreResources()) {
				selection = iter.nextResource();
				String studyEntryResult = (selection.getContent()).toString();
				
				
				// Now we will create an XStream object and then put that into our StudyEntry objects
				// the StudyEntry objects are de-serialized versions of the studyEntry blocks.
				XStream xmlStream = new XStream();
				
				xmlStream.alias("studyEntry", StudyEntry.class);
				xmlStream.alias("recordDetails", RecordDetails.class);
				xmlStream.alias("fileDetails", FileDetails.class);
				xmlStream.addImplicitCollection(RecordDetails.class, "fileDetails");
				
				StudyEntry newStudy = (StudyEntry)xmlStream.fromXML(studyEntryResult);
				
				//System.out.println(newStudy);
				
				// Add it to the return array
				tempList.add(newStudy);
			}
		}
		catch (Exception ex) {
			System.out.println("StudyEntryUtility.getEntries():  AN EXCEPTION HAS BEEN CAUGHT!  IF A LIST IS RETURNED, IT WILL BE EMPTY!!!");
			ex.printStackTrace();
		}
		
		return tempList;
	}
	
	/**
	 * This function makes the call to the XML database and retrieves all the file metadata for use in ECGridToolkit.  The data is first retrieved
	 * in a series of XML blocks, and then they are converted into StudyEntry Objects and returned to the user
	 * 
	 * @param userID - The ID of the user.  This is used to check which files the user has submitted and thus has access to
	 * @param studyID - The ID of the study selected to search
	 * @param datatype - The type of data that will be observed
	 * @return The list of StudyEntry Objects taken from the file metadata in the XML database
	 */
	public ArrayList<StudyEntry> getEntries(String userID, String studyID, String datatype) {
		System.out.println("In function getEntries");
		
		ArrayList<StudyEntry> tempList = new ArrayList<StudyEntry>();
		
		try {
			
			// create first query to get the entire studyEntry block, the collection() XQuery function does this across
			// all documents in the XML Collection
			
			//  The goal of this query is to find all the data we need for the StudyEntry object based on which user submitted them.  For all the documents searched, the query looks to see
			//  if any files for that subject's ECG repository were submitted by that user.  If any files were submitted, retrieve their metadata contained in the studyEntry block.
			//CompiledExpression query = subjectQuery.compile("for $x in collection('" + allConstants.getDBCollection() + "')//record where collection('" + allConstants.getDBCollection() + "')//record/studyEntry/submitterID=\"" + userID + "\" order by $x/studyEntry/subjectID return $x/studyEntry");
			
			//***
			// 02/25/2013 - Brandon Benitez:  Commented this out temporarily in case the query builder version of this failed.
			//***
			//String sQuery = "for $x in collection('" + dbHandle.getMainCollection() + "')//record/studyEntry[studyID=\"" + studyID + "\"] where $x/submitterID=\"" + userID + "\" return $x[datatype=\"" + datatype + "\"]";
			
			String sQuery = studyBuilder.forStudyEntry() +
			//studyBuilder.customNameBracket(studyID, EnumStudyTreeNode.STUDY) + 
			studyBuilder.whereUser2(userID) + 
			studyBuilder.andWhereStudy(studyID) +
			studyBuilder.customOrderBySubjectID() +
			studyBuilder.returnX() +
			studyBuilder.customNameBracket(datatype, EnumStudyTreeNode.DATATYPE);
			
			System.out.println("Query to be executed = " + sQuery);
			
			ResourceSet resultSet = executeQuery(sQuery);
			ResourceIterator iter = resultSet.getIterator();
			Resource selection = null;
			Resource fileSelection = null;
			int listIndex=0;
			int subjectCount = 1;
			
			while(iter.hasMoreResources()) {
				selection = iter.nextResource();
				String studyEntryResult = (selection.getContent()).toString();
				
				
				// Now we will create an XStream object and then put that into our StudyEntry objects
				// the StudyEntry objects are de-serialized versions of the studyEntry blocks.
				XStream xmlStream = new XStream();
				
				xmlStream.alias("studyEntry", StudyEntry.class);
				xmlStream.alias("recordDetails", RecordDetails.class);
				xmlStream.alias("fileDetails", FileDetails.class);
				xmlStream.addImplicitCollection(RecordDetails.class, "fileDetails");
				
				StudyEntry newStudy = (StudyEntry)xmlStream.fromXML(studyEntryResult);
				
				//System.out.println(newStudy);
				
				// Add it to the return array
				tempList.add(newStudy);
			}
		}
		catch (Exception ex) {
			System.out.println("StudyEntryUtility.getEntries():  AN EXCEPTION HAS BEEN CAUGHT!  IF A LIST IS RETURNED, IT WILL BE EMPTY!!!");
			ex.printStackTrace();
		}
		
		return tempList;
	}

	public ArrayList<String> queryDatatypeNodes(String userID) {
		System.out.println("In function queryDatatypeTypes");
		
	ArrayList<String> tempList = new ArrayList<String>();
	
	try {
		
		// create first query to get the entire studyEntry block, the collection() XQuery function does this across
		// all documents in the XML Collection
		
		//  The goal of this query is to find all the data we need for the StudyEntry object based on which user submitted them.  For all the documents searched, the query looks to see
		//  if any files for that subject's ECG repository were submitted by that user.  If any files were submitted, retrieve their metadata contained in the studyEntry block.
		String sQuery = studyBuilder.defaultFor() +
						studyBuilder.userDefinedWhere(userID) +
						studyBuilder.defaultOrderBy() +
						studyBuilder.returnDistinct(EnumStudyTreeNode.DATATYPE);
		
		System.out.println("Query to be executed = " + sQuery);
		
		ResourceSet resultSet = executeQuery(sQuery);
		ResourceIterator iter = resultSet.getIterator();
		Resource selection = null;
		
		tempList = this.checkForDuplicates(iter, selection);
		
		for(String listing : tempList) {
			int i = 1;
			System.out.println("Entry " + i + " = " + listing);
			i++;
		}
		
	}
	catch (Exception ex) {
		System.out.println("StudyEntryUtility.queryStudies():  AN EXCEPTION HAS BEEN CAUGHT!  IF A LIST IS RETURNED, IT WILL BE EMPTY!!!");
		ex.printStackTrace();
	}
	
	return tempList;
}


	public ArrayList<String> queryDatatypes(String userID, String studyName) {
		System.out.println("In function queryDatatypes");
		
	ArrayList<String> tempList = new ArrayList<String>();
	
	try {
		
		// create first query to get the entire studyEntry block, the collection() XQuery function does this across
		// all documents in the XML Collection
		
		//  The goal of this query is to find all the data we need for the StudyEntry object based on which user submitted them.  For all the documents searched, the query looks to see
		//  if any files for that subject's ECG repository were submitted by that user.  If any files were submitted, retrieve their metadata contained in the studyEntry block.
		
		String sQuery = studyBuilder.forStudyEntry() +
						studyBuilder.customNameBracket(studyName, EnumStudyTreeNode.STUDY) + 
						studyBuilder.userDefinedWhere(userID) + 
						studyBuilder.customOrderBySubjectID() +
						studyBuilder.returnTreeNodeDistinct(EnumStudyTreeNode.DATATYPE);
		
		System.out.println("Query to be executed = " + sQuery);
		
		ResourceSet resultSet = executeQuery(sQuery);
		ResourceIterator iter = resultSet.getIterator();
		Resource selection = null;
		
		tempList = this.checkForDuplicates(iter, selection);
		
		for(String listing : tempList) {
			int i = 1;
			System.out.println("Entry " + i + " = " + listing);
			i++;
		}
	}
	catch (Exception ex) {
		System.out.println("StudyEntryUtility.queryStudies():  AN EXCEPTION HAS BEEN CAUGHT!  IF A LIST IS RETURNED, IT WILL BE EMPTY!!!");
		ex.printStackTrace();
	}
	
	return tempList;
}

	public ArrayList<String> queryStudies(String userID) {
		System.out.println("In function queryStudies");
		
	ArrayList<String> tempList = new ArrayList<String>();
	
	try {
		
		// create first query to get the entire studyEntry block, the collection() XQuery function does this across
		// all documents in the XML Collection
		
		//  The goal of this query is to find all the data we need for the StudyEntry object based on which user submitted them.  For all the documents searched, the query looks to see
		//  if any files for that subject's ECG repository were submitted by that user.  If any files were submitted, retrieve their metadata contained in the studyEntry block.
		String sQuery = studyBuilder.defaultFor() +
						studyBuilder.userDefinedWhere(userID) + 
						studyBuilder.defaultOrderBy() +
						studyBuilder.returnDistinct(EnumStudyTreeNode.STUDY);
		
		System.out.println("Query to be executed = " + sQuery);
		
		ResourceSet resultSet = executeQuery(sQuery);
		ResourceIterator iter = resultSet.getIterator();
		Resource selection = null;
		
		tempList = this.checkForDuplicates(iter, selection);
		
		for(String listing : tempList) {
			int i = 1;
			System.out.println("Entry " + i + " = " + listing);
			i++;
		}
	}
	catch (Exception ex) {
		System.out.println("StudyEntryUtility.queryStudies():  AN EXCEPTION HAS BEEN CAUGHT!  IF A LIST IS RETURNED, IT WILL BE EMPTY!!!");
		ex.printStackTrace();
	}
	
	return tempList;
}

	public ArrayList<String> queryStudyTypes(String userID, String dataName) {
		
		System.out.println("In function queryStudyTypes");
		
	ArrayList<String> tempList = new ArrayList<String>();
	
	try {
		
		// create first query to get the entire studyEntry block, the collection() XQuery function does this across
		// all documents in the XML Collection
		
		//  The goal of this query is to find all the data we need for the StudyEntry object based on which user submitted them.  For all the documents searched, the query looks to see
		//  if any files for that subject's ECG repository were submitted by that user.  If any files were submitted, retrieve their metadata contained in the studyEntry block.
		
		System.out.println("queryDatatypes:");
		
		String sQuery = studyBuilder.forStudyEntry() +
						studyBuilder.customNameBracket(dataName, EnumStudyTreeNode.DATATYPE) + 
						studyBuilder.userDefinedWhere(userID) + 
						studyBuilder.customOrderBySubjectID() +
						studyBuilder.returnTreeNodeDistinct(EnumStudyTreeNode.STUDY);
		
		System.out.println("Query to be executed = " + sQuery);
		
		ResourceSet resultSet = executeQuery(sQuery);
		ResourceIterator iter = resultSet.getIterator();
		Resource selection = null;
		
		tempList = this.checkForDuplicates(iter, selection);
		
		for(String listing : tempList) {
			int i = 1;
			System.out.println("Entry " + i + " = " + listing);
			i++;
		}
	}
	catch (Exception ex) {
		System.out.println("ExistMainDatabase.queryStudies():  AN EXCEPTION HAS BEEN CAUGHT!  IF A LIST IS RETURNED, IT WILL BE EMPTY!!!");
		ex.printStackTrace();
	}
	
	return tempList;
}
	
	private ArrayList<String> checkForDuplicates(ResourceIterator iter, Resource selection) {
	
	ArrayList<String> tempList = new ArrayList<String>();
	
	HashMap<String, Integer> duplicateFilter = new HashMap<String, Integer>();
	
	try {
		while(iter.hasMoreResources()) {
			selection = iter.nextResource();
			String studyNameResult = (selection.getContent()).toString();
			
			if(duplicateFilter.isEmpty()) {
				// Add it to the return array
				tempList.add(studyNameResult);
				
				duplicateFilter.put(studyNameResult, 1);
				
			}
			else {
				
				boolean duplicateCheck = duplicateFilter.containsKey(studyNameResult);
				
				if(!duplicateCheck) {
					// Add it to the return array
					tempList.add(studyNameResult);
					
					duplicateFilter.put(studyNameResult, 1);
				}
				else {
					// Otherwise, increment the count
					
					Integer increaseCount = duplicateFilter.get(studyNameResult);
					duplicateFilter.put(studyNameResult, increaseCount++);
				}
			}
		
			
			System.out.println("The size of the duplicate filter is " + duplicateFilter.size());
							
		}
	} catch (XMLDBException e) {
		System.out.println("An XML Exception has been caught in the checkForDuplicates() method");
		e.printStackTrace();
	}
	
		return tempList;
	}
}
