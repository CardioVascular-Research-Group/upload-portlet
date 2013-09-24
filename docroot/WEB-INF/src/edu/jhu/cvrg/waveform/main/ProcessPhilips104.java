package edu.jhu.cvrg.waveform.main;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import org.cvrgrid.philips.jaxb.beans.*;
import org.cvrgrid.philips.jaxb.schema.*;

/**
 * This class will take the annotation data that has been gathered and put it into a form which complies
 * with our XML schema
 * 
 * @author bbenite1
 *
 */
public class ProcessPhilips104 {
	Restingecgdata restingECG;
	ArrayList<String> globalAnnotations;
	ArrayList<String[]> groupAnnotations;
	ArrayList<String[]> leadAnnotations;
	Philips104Annotations annotationRetriever;
	
	public ProcessPhilips104(Restingecgdata newECG) {
		restingECG = newECG;
		annotationRetriever = new Philips104Annotations();
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
		
		
		this.processCrossleadAnnotations();
		this.processGroupAnnotations();
		this.processLeadAnnotations();
	}
	
	
	
	private void getOrderInformation() {
		
	}
	
	private void getReportInformation() {
		
	}
	
	private void processCrossleadAnnotations() {
		Crossleadmeasurements crossAnnotations = restingECG.getInternalmeasurements().getCrossleadmeasurements();
		
		LinkedHashMap<String, Object> annotationMappings = annotationRetriever.extractCrossleadElements(crossAnnotations);
		
		for(String key : annotationMappings.keySet()) {
			if((annotationMappings.get(key) != null)) {
				//System.out.println("Annotation Name = " + key + " and value = " + annotationMappings.get(key).toString());
				this.generateWaveformAnnotations(key, annotationMappings.get(key).toString());
			}
		}
		
	}
	
	private void processGroupAnnotations() {
		Groupmeasurements groupAnnotations = restingECG.getInternalmeasurements().getGroupmeasurements();
		
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
		Leadmeasurements allLeadAnnotations = restingECG.getInternalmeasurements().getLeadmeasurements();
		
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
