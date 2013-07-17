/**
 * 
 */
package edu.jhu.cvrg.waveform.utility;

import edu.jhu.cvrg.dbapi.EnumXMLInsertLocation;
import edu.jhu.cvrg.dbapi.XQueryBuilder;

/**
 * @author bbenite1
 *
 */
public class UploadQueryBuilder extends XQueryBuilder {

	// The default constructor is made private so no one can create a query builder without having a URI and collection
	// This is the desired behavior and it can only be done in the derived classes, not the base class.
	private UploadQueryBuilder() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * The correct constructor to use, which has the URI and collection.
	 * 
	 * @param URI - The URI of the eXist database
	 * @param collection - the Database collection that this query builder will be using as the basis for queries.
	 */
	public UploadQueryBuilder(String URI, String collection) {
		super(URI, collection);
		System.out.println("The Collection passed in is " + collection);
		System.out.println("The resulting collection = " + this.dbCollection);
		// TODO Auto-generated constructor stub
	}

	/* (non-Javadoc)
	 * @see edu.jhu.cvrg.dbapi.XQueryBuilder#bracketStatement()
	 */
	@Override
	public String defaultBracket() {
		// TODO Auto-generated method stub
		String bracket = "";	
		return bracket;
	}
	
	public String bracketStatement(String subjectId) {
		String bracket = "[subjectID=\"" + subjectId + "\"]";
		
		return bracket;
	}
	
	public String bpRefBracket(String term) {
		String bracket = "[bioportalReference/term='" + term + "']";
		
		return bracket;
	}

	public String customBracket(String node, String term) {
		String bracket = "[" + node + "='" + term + "']";
		
		return bracket;
	}
	
	public String existanceQuery(String field, String value, String returnValue, String userID){	

		String query = "for $x in collection('"
						+ this.dbCollection
						+ "')//record/studyEntry where $x/" + field + "=\""
						+ value + "\" and submitterID=\"" + userID + "\"" +
						returnValue;
		return query;
	}


	@Override
	public String defaultFor() {
		this.forClause = "for $x in collection('" + this.dbCollection + "')//record";		
		
		return this.forClause;
	}
	
	public String studyEntryFor() {
		this.forClause = "for $x in collection('" + this.dbCollection + "')//record/studyEntry";		
		
		return this.forClause;
	}

	@Override
	public String defaultReturn() {
		this.returnClause = " return $x";
		return this.returnClause;
	}
	
	public String fileLocationReturn() {
		this.returnClause = " return fn:data($x/studyEntry/recordDetails/fileDetails/fileLocation)";
		return this.returnClause;
	}
	
	public String returnSubjectIDData() {
		this.returnClause = " return fn:data($x/subjectID)";
		return this.returnClause;
	}
	
	public String leadCount() {
		this.returnClause = " return fn:data($x/studyEntry/leadCount)";
		return this.returnClause;
	}


	@Override
	public String delete(String arg0, String arg1) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * Default method for a let statement.
	 * 
	 * WARNING:  Currently not in use.  Returns null if invoked.  The method is derived from the parent class but has not been implemented in this case.
	 */
	@Override
	public String defaultLet() {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public String modify(String nodeToModify, String newValue, String condition) {
		// TODO Auto-generated method stub
		String query = " return update value " + nodeToModify + condition + " with " + newValue;
		return query;
	}
	
	


	@Override
	public String defaultOrderBy() {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public String update(String arg0, String arg1, String arg2) {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public String defaultWhere() {
		// TODO Auto-generated method stub
		return null;
	}
	
	public String andWhere(String node, String value) {
		String query = " studyEntry/" + node + "='" + value + "'";
		
		return query;
	}

	@Override
	public String insert(String newNode, EnumXMLInsertLocation location, String anchorNode) {
		
		String locale = "";
		switch(location) {
		case INTO:			locale = "into";						break;
		case PRECEEDING:	locale = "preceeding";					break;
		case FOLLOWING:		locale = "following";					break;
		default:			locale = "into";						break;
		}
		
		String query = "return update insert " + newNode + " " + locale + " " + anchorNode;
		
		return query;
	}
	
	public String headerUpdate(String metadataNode, String newMetaValue, String study, String subjectID, String recordName, String userName) {
		String finalQuery = recordSearchParameters(study, subjectID, recordName, userName) +
							this.modify("$x/studyEntry/" + metadataNode, newMetaValue, "");
		return finalQuery;
	}
	
	public String findFile(String study, String subjectID, String recordName, String userName) {
		String finalQuery = recordSearchParameters(study, subjectID, recordName, userName) +
							this.fileLocationReturn();
		
		return finalQuery;
	}
	
	public String getLead(String study, String subjectID, String recordName, String userName) {
		String finalQuery = recordSearchParameters(study, subjectID, recordName, userName) +
							this.leadCount();
		
		return finalQuery;
	}
	
	public String insertLeads(String leadBlock, EnumXMLInsertLocation location, String study, String subjectID, String recordName, String userName) {
		
		// This is the node that is the reference node.  It determines where the new block gets inserted
		String nodeToReference = "$x/lead" + customBracket("bioportalReference/term", "1");
		
		String finalQuery = recordSearchParameters(study, subjectID, recordName, userName) +
							insert(leadBlock, location, nodeToReference);
		
		return finalQuery;		
	}
	
	//public String getLead(String study, String subjectID)
	
	private String recordSearchParameters(String study, String subjectID, String recordName, String userName) {
		String partialQuery = this.defaultFor() +
		" where" + this.andWhere("studyID", study) +
		" and" +
		this.andWhere("subjectID", subjectID) +
		" and" +
		this.andWhere("recordName", recordName) +
		" and" +
		this.andWhere("submitterID", userName);
		
		return partialQuery;
	}

}
