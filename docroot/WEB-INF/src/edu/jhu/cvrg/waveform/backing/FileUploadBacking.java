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

import java.util.HashSet;

import java.util.List;

import java.util.Map;

import java.util.Set;

import java.util.UUID;



import javax.annotation.PostConstruct;

import javax.faces.application.FacesMessage;

import javax.faces.bean.ManagedBean;

import javax.faces.bean.ViewScoped;

import javax.faces.context.FacesContext;



import org.primefaces.context.RequestContext;

import org.primefaces.event.FileUploadEvent;

import org.primefaces.event.NodeSelectEvent;

import org.primefaces.model.UploadedFile;



import com.liferay.portal.model.User;



import edu.jhu.cvrg.data.dto.UploadStatusDTO;

import edu.jhu.cvrg.data.factory.ConnectionFactory;

import edu.jhu.cvrg.data.util.DataStorageException;

import edu.jhu.cvrg.filestore.enums.EnumUploadState;

import edu.jhu.cvrg.waveform.main.UploadManager;

import edu.jhu.cvrg.waveform.model.FileTreeNode;

import edu.jhu.cvrg.waveform.model.LocalFileTree;

import edu.jhu.cvrg.waveform.utility.ResourceUtility;



@ManagedBean(name="fileUploadBacking")

@ViewScoped

public class FileUploadBacking extends BackingBean implements Serializable{

	

	private static final long serialVersionUID = -4715402539808469047L;

	private LocalFileTree fileTree;  

	private User userModel;

	private List<FacesMessage> messages;

	private UploadManager uploadManager;

	

	@PostConstruct

	public void init() {

		userModel = ResourceUtility.getCurrentUser();

		if(fileTree == null && userModel != null){

			fileTree = new LocalFileTree(userModel.getUserId());

		}

    	uploadManager = new UploadManager(fileTree);

		loadBackgroundQueue();

		messages = new ArrayList<FacesMessage>();

	}

	

    public void handleFileUpload(FileUploadEvent event) {

    	//TODO: Implement a means for the user to input the studyID and the datatype.



    	if(fileTree.getSelectedNode() == null){

    		fileTree.setDefaultSelected();

    	}

    	

    	UUID uuid = fileTree.getSelectedFolderUuid();



		UploadedFile file = event.getFile();

		String fileName = file.getFileName();

		long fileSize = file.getSize();

		InputStream fileStream = null;

		try {

			fileStream = file.getInputstream();

		} catch (IOException e) {

			getLog().error("Exception is:", e);

		}

    	uploadManager.fileUpload(uuid, fileName, fileSize, fileStream, "Mesa", "Rhythm Strips");

    }

    

    public List<UploadStatusDTO> getBackgroundQueue(){

    	return uploadManager.getBackgroundQueue();

    }

    

	public LocalFileTree getFileTree() {

		return fileTree;

	}

	

	public User getUser(){

		return userModel;

	}



    public String getSummary(){

    	int done = 0;

    	int error = 0;

    	List<UploadStatusDTO> backgroundQueue = uploadManager.getBackgroundQueue();

    	if(backgroundQueue!=null && !backgroundQueue.isEmpty()){

    		StringBuilder sb = new StringBuilder();

    		for (UploadStatusDTO u : backgroundQueue) {

    			if(u != null){

	    			switch (u.getState()) {

						case DONE: done++; break;

						case ERROR: error++; break;

						default:break;

					}

    			}

			}

    		sb.append("Summary ").append(backgroundQueue.size());

    		

    		if(backgroundQueue.size() > 1){

    			sb.append(" items [");

    		}else{

    			sb.append(" item [");

    		}

    		

    		sb.append(done).append(" done - ").append(error);

    		

    		if(error > 1){

    			sb.append(" fail(s)]");

    		}else{

    			sb.append(" fail]");

    		}

    		return sb.toString();

    	}

    	return null;

    }

    

    public boolean isShowBackgroundPanel(){

    	return uploadManager.getBackgroundQueue() != null && !uploadManager.getBackgroundQueue().isEmpty(); 

    }

	

	public void loadBackgroundQueue(){

		Set<Long> listenIds = null;	

		List<UploadStatusDTO> backgroundQueue = uploadManager.getBackgroundQueue();

        if(backgroundQueue != null && !backgroundQueue.isEmpty()){

        	listenIds = new HashSet<Long>();

        	for (UploadStatusDTO s : backgroundQueue) {

				if(s != null && s.getDocumentRecordId() != null){

					listenIds.add(s.getDocumentRecordId());

				}

			}

        }

        if(listenIds != null && !listenIds.isEmpty()){

        	try {

				List<UploadStatusDTO> tmpBackgroundQueue = ConnectionFactory.createConnection().getUploadStatusByUserAndDocId(userModel.getUserId(), listenIds);

	        	if(tmpBackgroundQueue != null){

			        for (UploadStatusDTO s : tmpBackgroundQueue) {

						if(backgroundQueue.contains(s)){

							backgroundQueue.get(backgroundQueue.indexOf(s)).update(s);

						}

					}

		        }else{

		        	backgroundQueue.clear();

		        }

        	} catch (DataStorageException e) {

        		getLog().error("Exception is:", e);

			}

        }    

        if(backgroundQueue != null){

        	boolean stopListening = false;

        	for (UploadStatusDTO u : backgroundQueue) {

        		stopListening = (EnumUploadState.DONE.equals(u.getState()) || EnumUploadState.ERROR.equals(u.getState()) );

        		if(!stopListening){

        			break;

        		}

			}	

        	if(stopListening){

        		RequestContext context = RequestContext.getCurrentInstance();

    			context.execute("stopListening();");

    			context.execute("totalFiles = " + backgroundQueue.size());

        	}

        }

	}

	

    public void onComplete() {

    	fileTree.initialize(userModel.getUserId());

    	messages.clear();

    }

    

    public void onNodeSelect(NodeSelectEvent event) { 

    	getLog().info("Node with GUID " + ((FileTreeNode)event.getTreeNode()).getUuid() + " selected.");

    }



    public void removeAllDoneItem(){

    	List<UploadStatusDTO> backgroundQueue = uploadManager.getBackgroundQueue();

		if(backgroundQueue != null && !backgroundQueue.isEmpty()){

			

			List<UploadStatusDTO> toRemove = new ArrayList<UploadStatusDTO>();

			for (UploadStatusDTO dto : backgroundQueue) {

				if(EnumUploadState.DONE.equals(dto.getState())){

					toRemove.add(dto);

				}

			}

			backgroundQueue.removeAll(toRemove);

			RequestContext context = RequestContext.getCurrentInstance();

			context.execute("totalFiles = " + backgroundQueue.size());

		}

	}

    

    public void removeTableItem(){

    	Map<String,String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();

        String index = params.get("index");

    	if(index != null ){

    		int indexTableToRemove = Integer.parseInt(index);

    		List<UploadStatusDTO> backgroundQueue = uploadManager.getBackgroundQueue();

    		if(indexTableToRemove >= 0 && (backgroundQueue != null && backgroundQueue.size() > indexTableToRemove)){

    			backgroundQueue.remove(indexTableToRemove);

    		}

    		RequestContext context = RequestContext.getCurrentInstance();

			context.execute("removeFile();");

    	}

    }	

}
