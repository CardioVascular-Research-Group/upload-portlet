package edu.jhu.cvrg.waveform.utility;

import java.io.Serializable;

import edu.jhu.cvrg.dbapi.EnumXMLInsertLocation;
import edu.jhu.cvrg.dbapi.XQueryBuilder;

public class StudyQueryBuilder extends XQueryBuilder implements Serializable{

	private static final long serialVersionUID = 1L;

	/**
	 * The correct constructor to use, which has the URI and collection.
	 * 
	 * @param URI - The URI of the eXist database
	 * @param collection - the Database collection that this query builder will be using as the basis for queries.
	 */
	public StudyQueryBuilder(String URI, String collection, String dbDriver) {
		super(URI, collection);
		// TODO Auto-generated constructor stub
	}
	
	// The default constructor has been made private so it cannot be invoked.  A developer MUST provide a URI and collection
	private StudyQueryBuilder() {
		super();

	}

	/**
	 * A default for clause.  This one searches by the record term
	 * 
	 */
	@Override
	public String defaultFor() {
		// Create a basic study entry query
		
		 this.forClause = "for $x in collection('" + this.dbCollection + "')//record";
		
		
		return this.forClause;
	}
	
	/**
	 * An alternative for clause.  In some cases, a direct search on the studyEntry term is used instead
	 * 
	 * @return
	 */
	public String forStudyEntry() {
		// Create a study entry query to search through the study entry field
		
		 this.forClause = "for $x in collection('" + this.dbCollection + "')//record/studyEntry";
		
		
		return this.forClause;
	}

	/**
	 * Default method for a let statement.
	 * 
	 * WARNING:  Currently not in use.  Returns null if invoked.  The method is derived from the parent class but has not been implemented in this case.
	 */
	@Override
	public String defaultLet() {
		// TODO fill this in later
		return null;
	}

	/**
	 * Default method a where statement
	 * 
	 * WARNING:  Currently not in use.  Returns null if invoked.  The method is derived from the parent class but has not been implemented in this case.
	 */
	@Override
	public String defaultWhere() {
		
		//This currently does not work, will make an overloaded version instead.
		
/*		String userID = userBean.getUsername();
		
		this.whereClause = " where $x/studyEntry/submitterID=\"" + userID + "\" ";
		return this.whereClause;*/
		
		System.out.println("The defaultWhere(void) method in StudyQueryBuilder has been invoked.  This version should not be used and will return null!");
		return null;		
	}
	
	/** 
	 * This is the version of the defaultWhere to use.  This searches through the userID when the for clause is only using the record term in the schema
	 * 
	 * @param userID
	 * @return
	 */
	public String defaultWhere(String userID) {
		// Use this version until the original is properly debugged
		
		this.whereClause = " where $x/studyEntry/submitterID=\"" + userID + "\" ";
		return this.whereClause;		
	}
	
	// Overload
	/**
	 * Deprecated:  Use defaultWhere(), whereUser2, or andWhereUser methods instead
	 * 
	 */
	// FIXME:  Check to see if this is even in use.  If not, remove it
	public String userDefinedWhere(String userID) {
		
		this.whereClause = " where collection('" + this.dbCollection + "')//record/studyEntry/submitterID=\"" + userID + "\" ";
		return this.whereClause;
	}
	
	/**
	 * This where clause is used when the query's for clause uses the studyEntry term
	 * 
	 * @param userID
	 * @return
	 */
	//FIXME:  Make a better name for this
	public String whereUser2(String userID) {
		
		this.whereClause = " where $x/submitterID=\"" + userID + "\" ";
		return this.whereClause;
	}
	
	/**
	 * 
	 * 
	 * @param userID
	 * @return
	 */
	public String andWhereUser(String userID) {
		
		this.whereClause = "  and $x/submitterID=\"" + userID + "\" ";
		return this.whereClause;
	}
	
	public String andWhereStudy(String studyID) {
		
		this.whereClause = " and $x/studyID=\"" + studyID + "\" ";
		return this.whereClause;
	}

	/**
	 * Defautlt order by clause.  Sorts by subject ID.  Assumes that the for clause is using the record term 
	 */
	@Override
	public String defaultOrderBy() {
		// TODO Auto-generated method stub
		this.orderByClause = " order by $x/studyEntry/subjectID ";
		return this.orderByClause;
	}
	
	/**
	 * Customized order by clause.  This one assumes that the for clause is using the studyEntry term
	 * 
	 */
	public String customOrderBySubjectID() {
		// TODO Auto-generated method stub
		this.orderByClause = " order by $x/subjectID ";
		return this.orderByClause;
	}

	/**
	 * Default return clause.  Returns the entire study entry XML block (for purposes of creating a java object).
	 * Assumes that the for clause was using the record term
	 */
	@Override
	public String defaultReturn() {
		// TODO Auto-generated method stub
		 this.returnClause = " return $x/studyEntry";
		return this.returnClause;
	}
	
	/**
	 * This return clause is meant to return a study ID or data type for use in the selection tree.  This version assumes that the 
	 * for clause uses the record term
	 * 
	 * @param nodeType - The type of tree nodes to be searched 
	 * 
	 */
	public String returnDistinct(EnumStudyTreeNode nodeType) {
		this.returnClause = "";
		
		switch(nodeType) {
		case STUDY:			this.returnClause = " return fn:distinct-values($x/studyEntry/studyID)";	break;
		case DATATYPE:		this.returnClause = " return fn:distinct-values($x/studyEntry/datatype)";	break;
		}
		
		return this.returnClause;
	}
	
	/**
	 * This return clause is meant to return a study ID or data type for use in the selection tree.  This version assumes that the 
	 * for clause uses the studyEntry term
	 * 
	 * @param nodeType  - The type of tree nodes to be searched
	 * 
	 */
	//FIXME:  Come up with a better name for this
	public String returnTreeNodeDistinct(EnumStudyTreeNode nodeType) {
		this.returnClause = "";
		
		switch(nodeType) {
		case STUDY:			this.returnClause = " return fn:distinct-values($x/studyID)";	break;
		case DATATYPE:		this.returnClause = " return fn:distinct-values($x/datatype)";	break;
		}
		
		return this.returnClause;
	}
	
	/**
	 * Returns the entire study entry XML block (for purposes of creating a java object).
	 * Assumes that the for clause was using the studyEntry term.
	 * 
	 * 
	 */
	public String returnX() {
		this.returnClause = " return $x";
		return this.returnClause;
	}

	/**
	 * A default statement for conditional statements.  These ones in sqare brackets are XQuery specific and represent attempts to further
	 * refine
	 */
	@Override
	public String defaultBracket() {
		String studyName = "Mesa";
		String bracketClause = "[studyID=\"" + studyName + "\"]";
		return bracketClause;
	}

	public String customNameBracket(String name, EnumStudyTreeNode nodeType) {
		String bracketClause = "";
		
		switch(nodeType) {
		case STUDY:			bracketClause = "[studyID=\"" + name + "\"]";	break;
		case DATATYPE:		bracketClause = "[datatype=\"" + name + "\"]";	break;
		}
		
		return bracketClause;
	}
	
	public String customReturnBracket(String name, EnumStudyTreeNode nodeType) {
		String bracketClause = "";
		
		switch(nodeType) {
		case STUDY:			bracketClause = "[studyID=\"" + name + "\"]";	break;
		case DATATYPE:		bracketClause = "[datatype=\"" + name + "\"]";	break;
		}
		
		return bracketClause;
	}
	
	/**
	 * Default method for replacing one XML node with another.  Includes the XML node to change, the XML node to replace it with, and the
	 * condition to find the specific node in the right record (so it updates only one node).
	 * 
	 * WARNING:  Currently not in use.  Returns null if invoked.  The method is derived from the parent class but has not been implemented in this case.
	 */
	@Override
	public String update(String oldNode, String newNode, String condition) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * Default method for changing a value of an XML node.  Includes the XML node to change, the value to use, and the search condition to find
	 * the correct node
	 * 
	 * WARNING:  Currently not in use.  Returns null if invoked.  The method is derived from the parent class but has not been implemented in this case.
	 */
	@Override
	public String modify(String nodeToModify, String newValue, String condition) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 *  Default method for writing a delete statement in XQuery.  Includes XML node to delete and the search condition for to ensure
	 *   the right one gets deleted
	 *   
	 *   WARNING:  Currently not in use.  Returns null if invoked.  The method is derived from the parent class but has not been implemented in this case.
	 */
	@Override
	public String delete(String nodeToDelete, String condition) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * Creates an insert statement which will insert a new XML node into an existing lead node
	 * 
	 * WARNING:  Currently not in use.  Returns null if invoked.  The method is derived from the parent class but has not been implemented in this case.
	 */
	@Override
	public String insert(String arg0, EnumXMLInsertLocation arg1, String arg2) {
		// TODO Auto-generated method stub
		return null;
	}

}
