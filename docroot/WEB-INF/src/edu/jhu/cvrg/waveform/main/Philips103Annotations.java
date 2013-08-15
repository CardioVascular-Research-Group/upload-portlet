package edu.jhu.cvrg.waveform.main;

import org.sierraecg.*;
import org.sierraecg.schema.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

public class Philips103Annotations {

		Restingecgdata restingECG;
		ArrayList<String> globalAnnotations;
		ArrayList<String> groupAnnotations;
		ArrayList<String>[] leadAnnotations;
		
		Philips103Annotations(Restingecgdata newECG) {
			restingECG = newECG;
		}
		
		public void setRestingECG(Restingecgdata newECG) {
			restingECG = newECG;
		}
		
		public Restingecgdata getRestingECG() {
			return restingECG;
		}
		
		public void populateAnnotations() {
			
			this.populateGlobalAnnotations();
			this.populateGroupAnnotations();
			this.populateLeadAnnotations();
		}
		
		
		
		private void populateGlobalAnnotations() {
			Globalmeasurements globalAnnotations = restingECG.getMeasurements().getGlobalmeasurements();
			
			LinkedHashMap<String, String> annotationMappings = this.extractGlobalElements(globalAnnotations);
			
		}
		
		private void populateGroupAnnotations() {
			
		}
		
		private void populateLeadAnnotations() {
			
		}
		
		private LinkedHashMap<String, String> extractGlobalElements(Globalmeasurements globalAnnotations) {
			LinkedHashMap<String, String> annotationMappings = new LinkedHashMap<String, String>();
			
			return annotationMappings;
		}
	
}
