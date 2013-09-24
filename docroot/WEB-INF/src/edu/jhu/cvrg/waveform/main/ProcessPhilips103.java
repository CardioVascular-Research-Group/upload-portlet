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
	ArrayList<String> globalAnnotations;
	ArrayList<String[]> groupAnnotations;
	ArrayList<String[]> leadAnnotations;
	Philips103Annotations annotationRetriever;
	
	public ProcessPhilips103(Restingecgdata newECG) {
		restingECG = newECG;
		annotationRetriever = new Philips103Annotations();
		globalAnnotations = new ArrayList<String>();
		groupAnnotations = new ArrayList<String[]>();
		leadAnnotations = new ArrayList<String[]>();
	}
	
	public void setRestingECG(Restingecgdata newECG) {
		restingECG = newECG;
	}
	
	public Restingecgdata getRestingECG() {
		return restingECG;
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
				this.generateWaveformAnnotations(key, annotationMappings.get(key));
			}
		}
		
	}
	
	private void processGroupAnnotations() {
		Groupmeasurements groupAnnotations = restingECG.getMeasurements().getGroupmeasurements();
		
		List<Groupmeasurement> groupAnnotation = groupAnnotations.getGroupmeasurement();
		
		for(Groupmeasurement annotation : groupAnnotation) {
			LinkedHashMap<String, Object> groupMappings = annotationRetriever.extractGroupMeasurements(annotation);
			
			for(String key : groupMappings.keySet()) {
				//System.out.println("Annotation Name = " + key + " and value = " + groupMappings.get(key).toString());
				this.generateWaveformAnnotations(key, groupMappings.get(key).toString());
			}
		}
	}
	
	private void processLeadAnnotations() {
		Leadmeasurements allLeadAnnotations = restingECG.getMeasurements().getLeadmeasurements();
		
		List<Leadmeasurement> leadAnnotationGroup = allLeadAnnotations.getLeadmeasurement();
		
		for(Leadmeasurement annotation: leadAnnotationGroup) {
			System.out.println("Lead name BEFORE insertion into list = " + annotation.getLeadname());
			LinkedHashMap<String, Object> leadMappings = annotationRetriever.extractLeadMeasurements(annotation);
			
			for(String key : leadMappings.keySet()) {
				System.out.println("Annotation Name = " + key + " and value = " + leadMappings.get(key).toString());
				this.generateWaveformAnnotations(key, leadMappings.get(key).toString());
			}			
		}
	}
	
	private void generateWaveformAnnotations(String annotationName, String annotationValue) {
		
	}
}
