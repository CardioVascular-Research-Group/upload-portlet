package edu.jhu.cvrg.waveform.backing;
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
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;

import org.primefaces.event.FileUploadEvent;
import org.primefaces.event.NodeSelectEvent;

import com.liferay.portal.model.User;

import edu.jhu.cvrg.waveform.exception.UploadFailureException;
import edu.jhu.cvrg.waveform.main.UploadManager;
import edu.jhu.cvrg.waveform.model.FileTree;
import edu.jhu.cvrg.waveform.utility.ResourceUtility;

@ManagedBean(name="fileUploadBacking")
@ViewScoped

public class FileUploadBacking implements Serializable{
	
	private static final long serialVersionUID = 1L;
	private FileTree fileTree;
	private String text;  
	private User user;
	
    public void handleFileUpload(FileUploadEvent event) {

    	fileUpload(event, "Mesa",  "Rhythm Strips");
    	//TODO: Implement a means for the user to input the studyID and the datatype.
	}
    
	@PostConstruct
	public void init() {

		user = ResourceUtility.getCurrentUser();
		if (fileTree == null) {
			fileTree = new FileTree();
			fileTree.initialize(user.getScreenName());
		}
	}
    
    private void fileUpload(FileUploadEvent event, String studyID, String datatype) {
    	
    	System.out.println("Handling upload... Path is " + fileTree.getSelectedNodePath());

    	UploadManager uploadManager = new UploadManager();
    	
		InputStream fileToSave;
		FacesMessage msg = null;
	
		try {
			fileToSave = event.getFile().getInputstream();

			String fileName = event.getFile().getFileName();
			long fileSize = event.getFile().getSize();

			// The new 5th parameter has been added for character encoding, specifically for XML files.  If null is passed in,
			// the function will use UTF-8 by default
			uploadManager.processUploadedFile(fileToSave, fileName, fileSize, studyID, datatype, fileTree.getSelectedNodePath());
			msg = new FacesMessage("Succesful", event.getFile().getFileName() + " is uploaded.");
		} catch (IOException e) {
			e.printStackTrace();
			msg = new FacesMessage("Failure", event.getFile().getFileName() + " failed to upload.  Could not read file.");
		} catch (UploadFailureException ufe) {
			ufe.printStackTrace();
			msg = new FacesMessage("Failure", "Uploading " + event.getFile().getFileName() + " failed because:  " + ufe.getMessage());
		} catch (Exception ex) {
			ex.printStackTrace();
			msg = new FacesMessage("Failure", "The file " + event.getFile().getFileName() + " failed to upload for unknown reasons");
		}
		
		fileTree.initialize(user.getScreenName());
		
		FacesContext.getCurrentInstance().addMessage(null, msg);
    }
    
    public void onNodeSelect(NodeSelectEvent event) { 
    	System.out.println("Node selected... Path is " + fileTree.getSelectedNodePath());
    }
    
    public String getText() {return text;}  
    public void setText(String text) {this.text = text;}

	public FileTree getFileTree() {
		return fileTree;
	}

	public void setFileTree(FileTree fileTree) {
		this.fileTree = fileTree;
	}
}