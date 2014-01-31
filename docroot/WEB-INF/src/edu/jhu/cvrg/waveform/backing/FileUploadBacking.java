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
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

import org.apache.log4j.Logger;
import org.primefaces.context.RequestContext;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.event.NodeSelectEvent;

import com.liferay.portal.model.User;

import edu.jhu.cvrg.waveform.exception.UploadFailureException;
import edu.jhu.cvrg.waveform.main.UploadManager;
import edu.jhu.cvrg.waveform.model.LocalFileTree;
import edu.jhu.cvrg.waveform.utility.ResourceUtility;

@ManagedBean(name="fileUploadBacking")
@ViewScoped

public class FileUploadBacking implements Serializable{
	
	private static final long serialVersionUID = -4715402539808469047L;
	
	private LocalFileTree fileTree;
	private String text;  
	private User userModel;

	private int totalUpload;
	private int done;
	private int failed;
	
	private Logger log = Logger.getLogger(FileUploadBacking.class);
	private List<FacesMessage> messages;
	
	@PostConstruct
	public void init() {
		userModel = ResourceUtility.getCurrentUser();
		if(fileTree == null && userModel != null){
			fileTree = new LocalFileTree(userModel.getUserId());
		}
		messages = new ArrayList<FacesMessage>();
	}
	
    public void handleFileUpload(FileUploadEvent event) {
    	//TODO: Implement a means for the user to input the studyID and the datatype.
    	fileUpload(event, "Mesa",  "Rhythm Strips");
    }
    
    private void fileUpload(FileUploadEvent event, String studyID, String datatype) {

    	totalUpload++;
    	
    	log.info("Handling upload... Folder name is " + fileTree.getSelectFolder().getName());

    	UploadManager uploadManager = new UploadManager();
    	
		InputStream fileToSave;
		FacesMessage msg = null;
	
		try {
			fileToSave = event.getFile().getInputstream();

			String fileName = event.getFile().getFileName();
			long fileSize = event.getFile().getSize();

			// The new 5th parameter has been added for character encoding, specifically for XML files.  If null is passed in,
			// the function will use UTF-8 by default
			uploadManager.processUploadedFile(fileToSave, fileName, fileSize, studyID, datatype, fileTree.getSelectFolder());
			msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "" , event.getFile().getFileName() + " is uploaded.");
		} catch (IOException e) {
			//ServerUtility.logStackTrace(e, log);
			e.printStackTrace();
			msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "" , event.getFile().getFileName() + " failed to upload.  Could not read file.");
			failed++;
		} catch (UploadFailureException ufe) {
			//ServerUtility.logStackTrace(ufe, log);
			ufe.printStackTrace();
			msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "" , "Uploading " + event.getFile().getFileName() + " failed because:  " + ufe.getMessage());
			failed++;
		} catch (Exception ex) {
			//ServerUtility.logStackTrace(ex, log);
			ex.printStackTrace();
			msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "" , "The file " + event.getFile().getFileName() + " failed to upload for unknown reasons");
			failed++;
		}
		
		messages.add(msg);
		done++;
		
    }
    
    public void onNodeSelect(NodeSelectEvent event) { 
    	log.info("Node selected... ID is " + fileTree.getSelectedNodeId());
    }
    
    public String getText() {return text;}  
    public void setText(String text) {this.text = text;}

	public LocalFileTree getFileTree() {
		return fileTree;
	}

	public void setFileTree(LocalFileTree fileTree) {
		this.fileTree = fileTree;
	}
	
    public void updateProgressBar() {  
    	int progress = 0;
        if(totalUpload > 0){
        	progress = (100 * done)/totalUpload;
        }  
        
        if(progress > 100){
        	progress = 100;
        }
        RequestContext context = RequestContext.getCurrentInstance();  
        context.execute("PF(\'pbClient\').setValue("+progress+");");
    }  
  
    public void onComplete() {
    	ResourceUtility.showMessages("Upload Completed ["+done+" File(s) - "+failed+" fail(s)]", messages);
    	
    	done = 0;
    	totalUpload = 0;
    	failed = 0;
    	fileTree.initialize(userModel.getUserId());
    	messages.clear();
    }
	
	public User getUser(){
		return userModel;
	}

}