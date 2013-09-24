package edu.jhu.cvrg.waveform.main;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import org.sierraecg.*;
import org.sierraecg.schema.*;

/**
 * This class will take the annotation data that has been gathered and put it into a form which complies
 * with our XML schema
 * 
 * @author bbenite1
 *
 */
public class ProcessPhilips103 {
	Restingecgdata restingECG;
	ArrayList<String> globalAnnotationsList;
	ArrayList<String[]> groupAnnotationsList;
	ArrayList<String[]> leadAnnotationsList;
	Philips103Annotations annotationRetriever;
	
	public ProcessPhilips103(Restingecgdata newECG) {
		restingECG = newECG;
		annotationRetriever = new Philips103Annotations();
		globalAnnotationsList = new ArrayList<String>();
		groupAnnotationsList = new ArrayList<String[]>();
		leadAnnotationsList = new ArrayList<String[]>();
	}
	
	public void setRestingECG(Restingecgdata newECG) {
		restingECG = newECG;
	}
	
	public Restingecgdata getRestingECG() {
		return restingECG;
	}
	
	public ArrayList<String> getGlobalAnnotations() {
		return globalAnnotationsList;
	}
	
	public ArrayList<String[]> getGroupAnnotations() {
		return groupAnnotationsList;
	}
	
	public ArrayList<String[]> getLeadAnnotations() {
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
				globalAnnotationsList.add(annotationMappings.get(key).toString());
			}
		}
		
	}
	
	private void processGroupAnnotations() {
		Groupmeasurements groupAnnotations = restingECG.getMeasurements().getGroupmeasurements();
		
		List<Groupmeasurement> groupAnnotation = groupAnnotations.getGroupmeasurement();
		
		for(Groupmeasurement annotation : groupAnnotation) {
			LinkedHashMap<String, Object> groupMappings = annotationRetriever.extractGroupMeasurements(annotation);
			String[] annotationsToAdd = new String[groupMappings.size()];
			int index = 0;
			
			for(String key : groupMappings.keySet()) {
				//System.out.println("Annotation Name = " + key + " and value = " + groupMappings.get(key).toString());
				System.out.println("Annotation Name = " + key + " and value = " + groupMappings.get(key).toString());
				annotationsToAdd[index] = groupMappings.get(key).toString();
				index++;
			}
			
			groupAnnotationsList.add(annotationsToAdd);
		}
	}
	
	private void processLeadAnnotations() {
		Leadmeasurements allLeadAnnotations = restingECG.getMeasurements().getLeadmeasurements();
		
		List<Leadmeasurement> leadAnnotationGroup = allLeadAnnotations.getLeadmeasurement();
		
		for(Leadmeasurement annotation: leadAnnotationGroup) {
			System.out.println("Lead name BEFORE insertion into list = " + annotation.getLeadname());
			LinkedHashMap<String, Object> leadMappings = annotationRetriever.extractLeadMeasurements(annotation);
			String[] annotationsToAdd = new String[leadMappings.size()];
			int index = 0;
			
			for(String key : leadMappings.keySet()) {
				System.out.println("Annotation Name = " + key + " and value = " + leadMappings.get(key).toString());
				annotationsToAdd[index] = leadMappings.get(key).toString();
				index++;
			}
			
			leadAnnotationsList.add(annotationsToAdd);			
		}
	}
}
