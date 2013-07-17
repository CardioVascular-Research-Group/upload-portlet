package edu.jhu.cvrg.waveform.model;

import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;

public class FileNode extends DefaultTreeNode {

	private static final long serialVersionUID = 3282562534862428877L;
	private boolean folder;
	private String folderName;
	private StudyEntry studyEntry;

	public FileNode(Object data, TreeNode parent) {
		super(data, parent);
		// TODO Auto-generated constructor stub
	}
	
	public FileNode(Object data, TreeNode parent, boolean folder, StudyEntry studyEntry) {
		super(data, parent);
		this.folder = folder;
		if(data.getClass().equals("java.lang.String")){
			folderName = data.toString();
		}
		this.studyEntry = studyEntry;
	}

	public FileNode(String type, Object data, TreeNode parent) {
		super(type, data, parent);
		// TODO Auto-generated constructor stub
	}

	public boolean isFolder() {
		return folder;
	}

	public void setFolder(boolean folder) {
		this.folder = folder;
	}

	public String getFolderName() {
		return folderName;
	}

	public void setFolderName(String folderName) {
		this.folderName = folderName;
	}

	public StudyEntry getStudyEntry() {
		return studyEntry;
	}

	public void setStudyEntry(StudyEntry studyEntry) {
		this.studyEntry = studyEntry;
	}

}
