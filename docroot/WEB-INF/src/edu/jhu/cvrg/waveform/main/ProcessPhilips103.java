package edu.jhu.cvrg.waveform.main;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Random;

import org.sierraecg.*;
import org.sierraecg.schema.*;

import edu.jhu.cvrg.waveform.model.AnnotationData;

/**
 * This class will take the annotation data that has been gathered and put it into a form which complies
 * with our XML schema
 * 
 * @author bbenite1
 *
 */
public class ProcessPhilips103 {
	Restingecgdata restingECG;
	private ArrayList<AnnotationData> globalAnnotationsList;
	private ArrayList<AnnotationData[]> groupAnnotationsList;
	private ArrayList<AnnotationData[]> leadAnnotationsList;
	private Philips103Annotations annotationRetriever;
	private String studyID;
	private String userID;
	private String recordName;
	private String subjectID;
	private final String createdBy = "Philips Upload";
	
	public ProcessPhilips103(Restingecgdata newECG, String newStudyID, String newUserID, String newRecordName, String newSubjectID) {
		restingECG = newECG;
		annotationRetriever = new Philips103Annotations();
		globalAnnotationsList = new ArrayList<AnnotationData>();
		groupAnnotationsList = new ArrayList<AnnotationData[]>();
		leadAnnotationsList = new ArrayList<AnnotationData[]>();
		groupAnnotationsList = new ArrayList<AnnotationData[]>();
		leadAnnotationsList = new ArrayList<AnnotationData[]>();
		studyID = newStudyID;
		userID = newUserID;
		recordName = newRecordName;
		subjectID = newSubjectID;
	}
	
	public void setRestingECG(Restingecgdata newECG) {
		restingECG = newECG;
	}
	
	public Restingecgdata getRestingECG() {
		return restingECG;
	}
	
	public ArrayList<AnnotationData> getGlobalAnnotations() {
		return globalAnnotationsList;
	}
	
	public ArrayList<AnnotationData[]> getGroupAnnotations() {
		return groupAnnotationsList;
	}
	
	public ArrayList<AnnotationData[]> getLeadAnnotations() {
		return leadAnnotationsList;
	}
	
	public void populateAnnotations() {
		
		this.processGlobalAnnotations();
		this.processGroupAnnotations();
		this.processLeadAnnotations();
	}
	
	
	
	private void processGlobalAnnotations() {
		Globalmeasurements globalAnnotations = restingECG.getMeasurements().getGlobalmeasurements();
		
		LinkedHashMap<String, String> annotationMappings = annotationRetriever.extractGlobalElements(globalAnnotations);
		
		for(String key : annotationMappings.keySet()) {
			if(annotationMappings.get(key) != null) {
				//System.out.println("Annotation Name = " + key + " and value = " + annotationMappings.get(key));
				AnnotationData annData = new AnnotationData();
				annData.setIsComment(true); // TODO:  Rename this to isNonLeadAnnotation instead
				annData.setIsSinglePoint(true);
				annData.setStudyID(studyID);
				annData.setSubjectID(subjectID);
				annData.setUserID(userID);
				annData.setDatasetName(recordName);
				annData.setAnnotation(annotationMappings.get(key).toString());
				annData.setConceptLabel(key);
				annData.setCreator(createdBy);
				
				Random randomNum = new Random();
				
				long randomID = java.lang.System.currentTimeMillis() * (long)randomNum.nextInt(10000);
				String ms = String.valueOf(randomID);  // used for GUID
				annData.setUniqueID(ms);
				
				
				globalAnnotationsList.add(annData);
			}
		}
		
	}
	
	private void processGroupAnnotations() {
		Groupmeasurements groupAnnotations = restingECG.getMeasurements().getGroupmeasurements();
		
		List<Groupmeasurement> groupAnnotation = groupAnnotations.getGroupmeasurement();
		
		for(Groupmeasurement annotation : groupAnnotation) {
			LinkedHashMap<String, Object> groupMappings = annotationRetriever.extractGroupMeasurements(annotation);
			AnnotationData[] annotationsToAdd = new AnnotationData[groupMappings.size()];
			int index = 0;
			
			for(String key : groupMappings.keySet()) {
				System.out.println("Annotation Name = " + key + " and value = " + groupMappings.get(key).toString());
				AnnotationData annData = new AnnotationData();
				annData.setIsComment(true); // TODO:  Rename this to isNonLeadAnnotation instead
				annData.setIsSinglePoint(true);
				annData.setStudyID(studyID);
				annData.setSubjectID(subjectID);
				annData.setUserID(userID);
				annData.setDatasetName(recordName);
				annData.setAnnotation(groupMappings.get(key).toString());
				annData.setConceptLabel(key);
				annData.setCreator(createdBy);
				
				Random randomNum = new Random();
				
				long randomID = java.lang.System.currentTimeMillis() * (long)randomNum.nextInt(10000);
				String ms = String.valueOf(randomID);  // used for GUID
				annData.setUniqueID(ms);
				
				
				annotationsToAdd[index] = annData;
				
				index++;
			}
			
			groupAnnotationsList.add(annotationsToAdd);
		}
	}
	
	private void processLeadAnnotations() {
		Leadmeasurements allLeadAnnotations = restingECG.getMeasurements().getLeadmeasurements();
		
		List<Leadmeasurement> leadAnnotationGroup = allLeadAnnotations.getLeadmeasurement();
		int leadIndex = 0;
		
		for(Leadmeasurement annotation: leadAnnotationGroup) {
			System.out.println("Lead name BEFORE insertion into list = " + annotation.getLeadname());
			LinkedHashMap<String, Object> leadMappings = annotationRetriever.extractLeadMeasurements(annotation);
			AnnotationData[] annotationsToAdd = new AnnotationData[leadMappings.size()];
			int arrayIndex = 0;
			
			for(String key : leadMappings.keySet()) {
				System.out.println("Annotation Name = " + key + " and value = " + leadMappings.get(key).toString());
				AnnotationData annData = new AnnotationData();
				annData.setIsComment(true); // TODO:  Rename this to isNonLeadAnnotation instead
				annData.setIsSinglePoint(true);
				annData.setStudyID(studyID);
				annData.setSubjectID(subjectID);
				annData.setUserID(userID);
				annData.setDatasetName(recordName);
				annData.setAnnotation(leadMappings.get(key).toString());
				annData.setConceptLabel(key);
				annData.setCreator(createdBy);
				annData.setLeadIndex(leadIndex);
				
				Random randomNum = new Random();
				
				long randomID = java.lang.System.currentTimeMillis() * (long)randomNum.nextInt(10000);
				String ms = String.valueOf(randomID);  // used for GUID
				annData.setUniqueID(ms);
				
				
				annotationsToAdd[arrayIndex] = annData;
				
				arrayIndex++;
			}
			
			leadAnnotationsList.add(annotationsToAdd);
			leadIndex++;
		}
	}
}
