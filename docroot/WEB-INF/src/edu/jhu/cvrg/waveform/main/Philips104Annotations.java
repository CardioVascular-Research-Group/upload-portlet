package edu.jhu.cvrg.waveform.main;

import org.cvrgrid.philips.jaxb.beans.*;
import org.cvrgrid.philips.jaxb.schema.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

public class Philips104Annotations {
	Restingecgdata restingECG;
	ArrayList<String> globalAnnotations;
	ArrayList<String> groupAnnotations;
	ArrayList<String>[] leadAnnotations;
	
	Philips104Annotations(Restingecgdata newECG) {
		restingECG = newECG;
	}
	
	public void setRestingECG(Restingecgdata newECG) {
		restingECG = newECG;
	}
	
	public Restingecgdata getRestingECG() {
		return restingECG;
	}
	
	public void populateAnnotations() {
		
		this.populateCrossleadAnnotations();
		this.populateGroupAnnotations();
		this.populateLeadAnnotations();
	}
	
	
	
	private void populateCrossleadAnnotations() {
		Crossleadmeasurements crossAnnotations = restingECG.getInternalmeasurements().getCrossleadmeasurements();
		
		LinkedHashMap<String, String> annotationMappings = this.extractCrossleadElements(crossAnnotations);
		
	}
	
	private void populateGroupAnnotations() {
		
	}
	
	private void populateLeadAnnotations() {
		
	}
	
	private LinkedHashMap<String, String> extractCrossleadElements(Crossleadmeasurements crossAnnotations) {
		LinkedHashMap<String, String> annotationMappings = new LinkedHashMap<String, String>();
		
		// The mapping will need to be filled manually since currently we cannot get a list of all of the elements
		// Do not do complex annotation measurements yet.  Stick with the simple ones.
		
		// TODO:  Skipping pacedetectleads and pacepulses for now
		
		annotationMappings.put("pacemode", crossAnnotations.getPacemode());
		annotationMappings.put("pacemalf", crossAnnotations.getPacemalf());
		annotationMappings.put("pacemisc", crossAnnotations.getPacemisc());
		annotationMappings.put("ectopicrhythm", crossAnnotations.getEctopicrhythm());
		annotationMappings.put("qintdispersion", crossAnnotations.getQtintdispersion());
		annotationMappings.put("numberofcomplexes", crossAnnotations.getNumberofcomplexes());
		annotationMappings.put("numberofgroups", crossAnnotations.getNumberofgroups());
		
		String beatClassificationValues = "";
		
		// concatenate all the beat classifications, as that is how they appear in the XML
		for (Integer beatValue : crossAnnotations.getBeatclassification()) {
			beatClassificationValues = beatClassificationValues + " " + beatValue.toString();
		}
		annotationMappings.put("beatclassification", beatClassificationValues);
		int subscript = 1;
		
		// enter the qamessagecodes one by one, as that is how they are in the XML
		for(TYPEmessagecode mCode : crossAnnotations.getQamessagecodes().getQamessagecode()) {
			String messageName = "qamessagecode" + subscript;
			annotationMappings.put(messageName, mCode.value());
			subscript++;
		}
		
		annotationMappings.put("qaactioncode", crossAnnotations.getQaactioncode().value());
		annotationMappings.put("pfrontaxis", crossAnnotations.getPfrontaxis());
		annotationMappings.put("phorizaxis", crossAnnotations.getPhorizaxis());
		annotationMappings.put("i40frontaxis", crossAnnotations.getI40Frontaxis());
		annotationMappings.put("i40horizaxis", crossAnnotations.getI40Horizaxis());
		annotationMappings.put("qrsfrontaxis", crossAnnotations.getQrsfrontaxis());
		annotationMappings.put("qrshorizaxis", crossAnnotations.getQrshorizaxis());
		annotationMappings.put("t40frontaxis", crossAnnotations.getT40Frontaxis());
		annotationMappings.put("t40horizaxis", crossAnnotations.getT40Horizaxis());
		annotationMappings.put("stfrontaxis", crossAnnotations.getStfrontaxis());
		annotationMappings.put("tfrontaxis", crossAnnotations.getTfrontaxis());
		annotationMappings.put("thorizaxis", crossAnnotations.getThorizaxis());
		annotationMappings.put("atrialrate", crossAnnotations.getNumberofcomplexes());
		annotationMappings.put("lowventrate", crossAnnotations.getLowventrate());
		annotationMappings.put("meanventrate", crossAnnotations.getMeanventrate());
		annotationMappings.put("highventrate", crossAnnotations.getHighventrate());
		annotationMappings.put("meanprint", crossAnnotations.getMeanprint());
		annotationMappings.put("meanprseg", crossAnnotations.getMeanprseg());
		annotationMappings.put("meanqrsdur", crossAnnotations.getMeanqrsdur());
		annotationMappings.put("meanqtint", crossAnnotations.getMeanqtint());
		annotationMappings.put("meanqtc", crossAnnotations.getMeanqtc());
		annotationMappings.put("deltawavecount", crossAnnotations.getDeltawavecount());
		annotationMappings.put("deltawavepercent", crossAnnotations.getDeltawavepercent());
		annotationMappings.put("bigeminycount", crossAnnotations.getBigeminycount());
		annotationMappings.put("bigeminystring", crossAnnotations.getBigeminystring());
		annotationMappings.put("trigeminycount", crossAnnotations.getTrigeminycount());
		annotationMappings.put("trigeminystring", crossAnnotations.getTrigeminystring());
		annotationMappings.put("wenckcount", crossAnnotations.getWenckcount());
		annotationMappings.put("wenckstring", crossAnnotations.getWenckstring());
		annotationMappings.put("flutterfibcount", crossAnnotations.getFlutterfibcount());
		annotationMappings.put("leadreversalcode", crossAnnotations.getLeadreversalcode().toString());
		annotationMappings.put("atrialrhythm", crossAnnotations.getAtrialrhythm().toString());
		annotationMappings.put("atrialratepowerspect", crossAnnotations.getAtrialratepowerspect());
		annotationMappings.put("ventrhythm", crossAnnotations.getVentrhythm().toString());
		
		return annotationMappings;
	}

}
