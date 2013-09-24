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
	ArrayList<String> crossleadAnnotationsList;
	ArrayList<String[]> groupAnnotationsList;
	ArrayList<String[]> leadAnnotationsList;
	Philips104Annotations annotationRetriever;
	
	public ProcessPhilips104(Restingecgdata newECG) {
		restingECG = newECG;
		annotationRetriever = new Philips104Annotations();
		crossleadAnnotationsList = new ArrayList<String>();
		groupAnnotationsList = new ArrayList<String[]>();
		leadAnnotationsList = new ArrayList<String[]>();
		
	}
	
	public void setRestingECG(Restingecgdata newECG) {
		restingECG = newECG;
	}
	
	public Restingecgdata getRestingECG() {
		return restingECG;
	}
	
	public ArrayList<String> getCrossleadAnnotations() {
		return crossleadAnnotationsList;
	}
	
	public ArrayList<String[]> getGroupAnnotations() {
		return groupAnnotationsList;
	}
	
	public ArrayList<String[]> getLeadAnnotations() {
		return leadAnnotationsList;
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
				crossleadAnnotationsList.add(annotationMappings.get(key).toString());
			}
		}
		
	}
	
	private void processGroupAnnotations() {
		Groupmeasurements allGroupAnnotations = restingECG.getInternalmeasurements().getGroupmeasurements();
		
		List<Groupmeasurement> groupAnnotation = allGroupAnnotations.getGroupmeasurement();
		
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
		Leadmeasurements allLeadAnnotations = restingECG.getInternalmeasurements().getLeadmeasurements();
		
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
