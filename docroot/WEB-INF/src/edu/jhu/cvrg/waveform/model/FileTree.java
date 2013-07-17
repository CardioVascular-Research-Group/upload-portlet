package edu.jhu.cvrg.waveform.model;

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
 * @author Chris Jurado
 * 
 */
import java.io.Serializable;
import java.util.ArrayList;

import javax.faces.event.ActionEvent;

import org.primefaces.event.NodeSelectEvent;
import org.primefaces.model.TreeNode;

import edu.jhu.cvrg.waveform.utility.ResourceUtility;
import edu.jhu.cvrg.waveform.utility.StudyEntryUtility;

public class FileTree implements Serializable{

	private static final long serialVersionUID = 1L;
	private TreeNode treeRoot;
	private FileNode selectedNode;
	private TreeNode[] selectedNodes;
	private String newFolderName = "";
	private ArrayList<StudyEntry> studyEntryList;
	private String username;
	StudyEntryUtility theDB;

	public void initialize(String username) {
		
		System.out.println("Initializing tree...");
		
		this.username = username;
		
		theDB = new StudyEntryUtility(com.liferay.util.portlet.PortletProps.get("dbUser"),
				com.liferay.util.portlet.PortletProps.get("dbPassword"), 
				com.liferay.util.portlet.PortletProps.get("dbURI"),	
				com.liferay.util.portlet.PortletProps.get("dbDriver"), 
				com.liferay.util.portlet.PortletProps.get("dbMainDatabase"));
		if (treeRoot == null) {
			buildTree();
		}
	}

	private void buildTree() {

		System.out.println("Getting files for user " + username);
		studyEntryList = theDB.getEntries(this.username);

		treeRoot = new FileNode("Root", null, true, null);

		if (studyEntryList.isEmpty()) {
			@SuppressWarnings("unused")
			TreeNode newNode = new FileNode("Default", treeRoot, true, null);
			return;
		}

		for (StudyEntry studyEntry : studyEntryList) {

			String[] path = studyEntry.getVirtualPath().split("\\|");
			TreeNode workNode = treeRoot;

			for (String step : path) {

				TreeNode newNode = getNodeByName(workNode, step);

				if (newNode == null) {
					newNode = new FileNode(step, workNode, true, null);
				}
				newNode.setExpanded(true);
				workNode = newNode;

			}

			@SuppressWarnings("unused")
			TreeNode recordNode = new FileNode(studyEntry.getRecordName(), workNode, false, studyEntry);
		}
	}

	public String getSelectedNodePath() {

		TreeNode node = this.selectedNode;
		String path = (String) node.getData();

		while (!node.getParent().getData().toString().equals("Root")) {
			node = node.getParent();
			path = node.getData().toString() + "|" + path;
		}

		return path;

	}

	private TreeNode getNodeByName(TreeNode searchNode, String name) {

		for (TreeNode node : searchNode.getChildren()) {
			if (node.getData().toString().equals(name)) {
				return node;
			}
		}
		return null;
	}

	public void addFolder(ActionEvent event) {
		
		System.out.println("Selected node " + selectedNode.getData().toString());
		
		if (selectedNode == null) {
			selectedNode = (FileNode) treeRoot;
		}

		if (!newFolderName.equals("")) {
			TreeNode newNode = new FileNode(newFolderName, selectedNode, true, null);
			selectedNode.setExpanded(true);
			selectedNode = (FileNode) newNode;
		}
	}

	public void removeFolder(ActionEvent event) {

		selectedNode.getChildren().clear();
		selectedNode.getParent().getChildren().remove(selectedNode);
		selectedNode.setParent(null);
		selectedNode = null;
	}

	public void renameFolder(ActionEvent event) {
		if (!newFolderName.equals("")) {
			selectedNode.setData(newFolderName);
		}
	}

	public ArrayList<StudyEntry> getSelectedFileNodes() {

		ArrayList<StudyEntry> fileEntries = new ArrayList<StudyEntry>();

		for (TreeNode selectedNode : selectedNodes) {
			if (selectedNode.isLeaf()) {
				fileEntries.add(((FileNode) selectedNode).getStudyEntry());
			}
		}
		return fileEntries;
	}

	public TreeNode getTreeRoot() {
		return treeRoot;
	}

	public TreeNode getSelectedNode() {
		return selectedNode;
	}

	public void setSelectedNode(TreeNode selectedNode) {
		this.selectedNode = (FileNode) selectedNode;
	}

	public String getNewFolderName() {
		return newFolderName;
	}

	public void setNewFolderName(String newFolderName) {
		this.newFolderName = newFolderName;
	}

	public TreeNode[] getSelectedNodes() {
		return selectedNodes;
	}

	public void setSelectedNodes(TreeNode[] selectedNodes) {
		this.selectedNodes = selectedNodes;
	}
}
