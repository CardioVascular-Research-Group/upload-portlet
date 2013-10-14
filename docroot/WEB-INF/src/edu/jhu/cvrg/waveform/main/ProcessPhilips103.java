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
	private Restingecgdata restingECG;
	private ArrayList<AnnotationData> orderAnnotationsList;
	private ArrayList<AnnotationData> dataAcquisitionList;
	private ArrayList<AnnotationData> globalAnnotationsList;
	private ArrayList<AnnotationData[]> groupAnnotationsList;
	private ArrayList<AnnotationData[]> leadAnnotationsList;
	private Philips103Annotations annotationRetriever;
	private String studyID;
	private String userID;
	private String recordName;
	private String subjectID;
	private final String createdBy = "Philips Upload";
	private long orderinfoRuntime;
	private long dataacquisitionRuntime;
	private long crossleadAnnotationsRuntime;
	private long groupAnnotationsRuntime;
	private long leadmeasurementsRuntime;
	
	public ProcessPhilips103(Restingecgdata newECG, String newStudyID, String newUserID, String newRecordName, String newSubjectID) {
		restingECG = newECG;
		annotationRetriever = new Philips103Annotations();
		orderAnnotationsList = new ArrayList<AnnotationData>();
		dataAcquisitionList = new ArrayList<AnnotationData>();
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
	
	public ArrayList<AnnotationData> getOrderInfo() {
		return orderAnnotationsList;
	}
	
	public ArrayList<AnnotationData> getDataAcquisitions() {
		return dataAcquisitionList;
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
		
		this.extractOrderInformation();
		this.processDataAcquisition();
		// Note:  Checks for null in each individual method are not needed.  These are required
		// in the schema for this version of Philips 
		if(restingECG.getMeasurements() != null) {
			this.processGlobalAnnotations();
			this.processGroupAnnotations();		// This is not being used temporarily until we decide how to fit this into the schema.  Do NOT remove this method for any reason
			this.processLeadAnnotations();
		}
		
		System.out.println("The total runtime for parsing order information is " + orderinfoRuntime);
		System.out.println("The total runtime for parsing data acquisitions is " + dataacquisitionRuntime);
		System.out.println("The total runtime for parsing cross lead measurements is " + crossleadAnnotationsRuntime);
		System.out.println("The total runtime for parsing group measurements is " + groupAnnotationsRuntime);
		System.out.println("The total runtime for parsing lead measurements is " + leadmeasurementsRuntime);
	}
	
	private void extractOrderInformation() {
		long orderinfoStarttime = java.lang.System.currentTimeMillis();
		Orderinfo orderinfoAnn = restingECG.getOrderinfo();
		if(orderinfoAnn != null) {
			LinkedHashMap<String, Object> orderMappings = annotationRetriever.extractOrderInfo(orderinfoAnn);
			
			for(String key : orderMappings.keySet()) {
				if((orderMappings.get(key) != null)) {
					AnnotationData annData = new AnnotationData();
					annData.setIsComment(true); // TODO:  Rename this to isNonLeadAnnotation instead
					annData.setIsSinglePoint(true);
					annData.setStudyID(studyID);
					annData.setSubjectID(subjectID);
					annData.setUserID(userID);
					annData.setDatasetName(recordName);
					annData.setAnnotation(orderMappings.get(key).toString());
					annData.setConceptLabel(key);
					annData.setCreator(createdBy);
					
					Random randomNum = new Random();
					
					long randomID = java.lang.System.currentTimeMillis() * (long)randomNum.nextInt(10000);
					String ms = String.valueOf(randomID);  // used for GUID
					annData.setUniqueID(ms);
					
					
					orderAnnotationsList.add(annData);
				}
			}
		}
		long orderinfoEndtime = java.lang.System.currentTimeMillis();
		
		orderinfoRuntime = orderinfoEndtime - orderinfoStarttime;
	}
	
	private void processDataAcquisition() {
		long dataacquisitionStarttime = java.lang.System.currentTimeMillis();

		
		Dataacquisition dataAcquisAnn = restingECG.getDataacquisition();

		//  This one is does not have a check for null since Data Acquisition is a required tag in the Schema
		LinkedHashMap<String, Object> dataMappings = annotationRetriever.extractDataAcquisition(dataAcquisAnn);
		
		System.out.println("Size of hashmap = " + dataMappings.size());
		
		for(String key : dataMappings.keySet()) {
			if((dataMappings.get(key) != null)) {
				AnnotationData annData = new AnnotationData();
				annData.setIsComment(true); // TODO:  Rename this to isNonLeadAnnotation instead
				annData.setIsSinglePoint(true);
				annData.setStudyID(studyID);
				annData.setSubjectID(subjectID);
				annData.setUserID(userID);
				annData.setDatasetName(recordName);
				annData.setAnnotation(dataMappings.get(key).toString());
				annData.setConceptLabel(key);
				annData.setCreator(createdBy);
				
				Random randomNum = new Random();
				
				long randomID = java.lang.System.currentTimeMillis() * (long)randomNum.nextInt(10000);
				String ms = String.valueOf(randomID);  // used for GUID
				annData.setUniqueID(ms);
				
				
				dataAcquisitionList.add(annData);
			}
		}
		
		long dataacquisitionEndtime = java.lang.System.currentTimeMillis();
		
		dataacquisitionRuntime = dataacquisitionEndtime - dataacquisitionStarttime;
	}
	
	private void processGlobalAnnotations() {
		long crossleadStarttime = java.lang.System.currentTimeMillis();
		
		Globalmeasurements globalAnnotations = restingECG.getMeasurements().getGlobalmeasurements();
		
		LinkedHashMap<String, String> annotationMappings = annotationRetriever.extractGlobalElements(globalAnnotations);
		
		for(String key : annotationMappings.keySet()) {
			if(annotationMappings.get(key) != null) {
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
		
		long crossleadEndtime = java.lang.System.currentTimeMillis();
		
		crossleadAnnotationsRuntime = crossleadEndtime - crossleadStarttime;
		
	}
	
	private void processGroupAnnotations() {
		long groupStarttime = java.lang.System.currentTimeMillis();
		
		Groupmeasurements groupAnnotations = restingECG.getMeasurements().getGroupmeasurements();
		
		List<Groupmeasurement> groupAnnotation = groupAnnotations.getGroupmeasurement();
		
		for(Groupmeasurement annotation : groupAnnotation) {
			LinkedHashMap<String, Object> groupMappings = annotationRetriever.extractGroupMeasurements(annotation);
			AnnotationData[] annotationsToAdd = new AnnotationData[groupMappings.size()];
			int index = 0;
			
			for(String key : groupMappings.keySet()) {
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
			
			long groupEndtime = java.lang.System.currentTimeMillis();
			
			groupAnnotationsRuntime = groupEndtime - groupStarttime;
		}
	}
	
	private void processLeadAnnotations() {
		long leadStarttime = java.lang.System.currentTimeMillis();
		
		Leadmeasurements allLeadAnnotations = restingECG.getMeasurements().getLeadmeasurements();
		
		List<Leadmeasurement> leadAnnotationGroup = allLeadAnnotations.getLeadmeasurement();
		int leadIndex = 0;
		
		for(Leadmeasurement annotation: leadAnnotationGroup) {
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
		
		long leadEndtime = java.lang.System.currentTimeMillis();
		
		leadmeasurementsRuntime = leadEndtime - leadStarttime;
	}
}
