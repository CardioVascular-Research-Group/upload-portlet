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
	ArrayList<String>[] groupAnnotations;
	ArrayList<String>[] leadAnnotations;
	
	public Philips104Annotations(Restingecgdata newECG) {
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
		
		LinkedHashMap<String, Object> annotationMappings = this.extractCrossleadElements(crossAnnotations);
		
		for(String key : annotationMappings.keySet()) {
			if((annotationMappings.get(key) != null)) {
				//System.out.println("Annotation Name = " + key + " and value = " + annotationMappings.get(key).toString());
				this.generateWaveformAnnotations(key, annotationMappings.get(key).toString());
			}
		}
		
	}
	
	private void populateGroupAnnotations() {
		Groupmeasurements groupAnnotations = restingECG.getInternalmeasurements().getGroupmeasurements();
		
		List<Groupmeasurement> groupAnnotation = groupAnnotations.getGroupmeasurement();
		
		for(Groupmeasurement annotation : groupAnnotation) {
			LinkedHashMap<String, Object> groupMappings = this.extractGroupMeasurements(annotation);
			
			for(String key : groupMappings.keySet()) {
				System.out.println("Annotation Name = " + key + " and value = " + groupMappings.get(key).toString());
				this.generateWaveformAnnotations(key, groupMappings.get(key).toString());
			}
		}
	}
	
	private void populateLeadAnnotations() {
		
	}
	
	private LinkedHashMap<String, Object> extractCrossleadElements(Crossleadmeasurements crossLeadAnnotations) {
		LinkedHashMap<String, Object> annotationMappings = new LinkedHashMap<String, Object>();
		
		// The mapping will need to be filled manually since currently we cannot get a list of all of the elements
		// Do not do complex annotation measurements yet.  Stick with the simple ones.
		
		// TODO:  Skipping pacedetectleads and pacepulses for now, return to them later.
		
		// Main set of cross lead measurements
		annotationMappings.put("pacemode", crossLeadAnnotations.getPacemode());
		annotationMappings.put("pacemalf", crossLeadAnnotations.getPacemalf());
		annotationMappings.put("pacemisc", crossLeadAnnotations.getPacemisc());
		annotationMappings.put("ectopicrhythm", crossLeadAnnotations.getEctopicrhythm());
		annotationMappings.put("QT Interval Dispersion", crossLeadAnnotations.getQtintdispersion());
		annotationMappings.put("numberofcomplexes", crossLeadAnnotations.getNumberofcomplexes());
		annotationMappings.put("numberofgroups", crossLeadAnnotations.getNumberofgroups());
		
		String beatClassificationValues = "";
		
		// concatenate all the beat classifications, as that is how they appear in the XML
		for (Integer beatValue : crossLeadAnnotations.getBeatclassification()) {
			beatClassificationValues = beatClassificationValues + " " + beatValue;
		}
		annotationMappings.put("beatclassification", beatClassificationValues);
		int subscript = 1;
		
		// enter the qamessagecodes one by one, as that is how they are in the XML
		for(TYPEmessagecode mCode : crossLeadAnnotations.getQamessagecodes().getQamessagecode()) {
			if(mCode != null) {
				String messageName = "qamessagecode" + subscript;
				annotationMappings.put(messageName, mCode.value());
				subscript++;
			}
		}
		
		// additional checks for null are needed on some of these retrievals.  Some of them require methods other
		// than toString()
		if(crossLeadAnnotations.getQaactioncode() != null) {
			annotationMappings.put("qaactioncode", crossLeadAnnotations.getQaactioncode().value());
		}
		annotationMappings.put("P Frontal Axis", crossLeadAnnotations.getPfrontaxis());
		annotationMappings.put("P Horizontal Axis", crossLeadAnnotations.getPhorizaxis());
		annotationMappings.put("i40 Frontal Axis", crossLeadAnnotations.getI40Frontaxis());
		annotationMappings.put("i40 Horizontal Axis", crossLeadAnnotations.getI40Horizaxis());
		annotationMappings.put("QRS Frontal Axis", crossLeadAnnotations.getQrsfrontaxis());
		annotationMappings.put("QRS Horizontal Axis", crossLeadAnnotations.getQrshorizaxis());
		annotationMappings.put("t40 Frontal Axis", crossLeadAnnotations.getT40Frontaxis());
		annotationMappings.put("t40 Horizontal Axis", crossLeadAnnotations.getT40Horizaxis());
		annotationMappings.put("ST Frontal Axis", crossLeadAnnotations.getStfrontaxis());
		annotationMappings.put("ST Horizontal Axis", crossLeadAnnotations.getSthorizaxis());
		annotationMappings.put("T Frontal Axis", crossLeadAnnotations.getTfrontaxis());
		annotationMappings.put("T Horizontal Axis", crossLeadAnnotations.getThorizaxis());
		annotationMappings.put("Atrial Rate", crossLeadAnnotations.getAtrialrate());
		annotationMappings.put("Minimum Ventreicular Rate", crossLeadAnnotations.getLowventrate());
		annotationMappings.put("Mean Ventricular Rate", crossLeadAnnotations.getMeanventrate());
		annotationMappings.put("Maximum Ventricular Rate", crossLeadAnnotations.getHighventrate());
		annotationMappings.put("Mean PR Interval", crossLeadAnnotations.getMeanprint());
		annotationMappings.put("Mean PR Segment", crossLeadAnnotations.getMeanprseg());
		annotationMappings.put("Mean QRS Duration", crossLeadAnnotations.getMeanqrsdur());
		annotationMappings.put("Mean QT Interval", crossLeadAnnotations.getMeanqtint());
		annotationMappings.put("Mean QT Corrected", crossLeadAnnotations.getMeanqtc());
		annotationMappings.put("Delta Wave Count", crossLeadAnnotations.getDeltawavecount());
		annotationMappings.put("Delta Wave Percent", crossLeadAnnotations.getDeltawavepercent());
		annotationMappings.put("bigeminycount", crossLeadAnnotations.getBigeminycount());
		annotationMappings.put("bigeminystring", crossLeadAnnotations.getBigeminystring());
		annotationMappings.put("trigeminycount", crossLeadAnnotations.getTrigeminycount());
		annotationMappings.put("trigeminystring", crossLeadAnnotations.getTrigeminystring());
		annotationMappings.put("wenckcount", crossLeadAnnotations.getWenckcount());
		annotationMappings.put("wenckstring", crossLeadAnnotations.getWenckstring());
		annotationMappings.put("flutterfibcount", crossLeadAnnotations.getFlutterfibcount());
		
		annotationMappings.put("leadreversalcode", crossLeadAnnotations.getLeadreversalcode());
		
		annotationMappings.put("Atrial Rhythm", crossLeadAnnotations.getAtrialrhythm());
		annotationMappings.put("atrialratepowerspect", crossLeadAnnotations.getAtrialratepowerspect());
		annotationMappings.put("Ventricular Rhythm", crossLeadAnnotations.getVentrhythm());
		annotationMappings.put("randomrrpercent", crossLeadAnnotations.getRandomrrpercent());
		annotationMappings.put("regularrrpercent", crossLeadAnnotations.getRegularrrpercent());
		annotationMappings.put("biggestrrgrouppercent", crossLeadAnnotations.getBiggestrrgrouppercent());
		annotationMappings.put("biggestrrgroupvar", crossLeadAnnotations.getBiggestrrgroupvar());
		annotationMappings.put("nrrgroups", crossLeadAnnotations.getNrrgroups());
		annotationMappings.put("bigemrrintvlacf", crossLeadAnnotations.getBigemrrintvlacf());
		annotationMappings.put("trigemrrintvlacf", crossLeadAnnotations.getTrigemrrintvlacf());
		annotationMappings.put("fibfreqmhz", crossLeadAnnotations.getFibfreqmhz());
		annotationMappings.put("fibampnv", crossLeadAnnotations.getFibampnv());
		annotationMappings.put("fibwidthmhz", crossLeadAnnotations.getFibwidthmhz());
		annotationMappings.put("fibmedianfreqmhz", crossLeadAnnotations.getFibmedianfreqmhz());
		annotationMappings.put("afltcyclelen", crossLeadAnnotations.getAfltcyclelen());
		annotationMappings.put("afltacfpeak", crossLeadAnnotations.getAfltacfpeak());
		annotationMappings.put("afltacfpeakwidth", crossLeadAnnotations.getAfltacfpeakwidth());
		annotationMappings.put("constantpshapepct", crossLeadAnnotations.getConstantpshapepct());
		annotationMappings.put("atrialrateirregpct", crossLeadAnnotations.getAtrialrateirregpct());
		
		// the vector loop mxs elements
		annotationMappings.put("Transverse P Clockwise Rotation", crossLeadAnnotations.getTranspcwrot());
		annotationMappings.put("Transverse P Initial Angle", crossLeadAnnotations.getTranspinitangle());
		annotationMappings.put("Transverse P Initial Magnitude", crossLeadAnnotations.getTranspinitmag());
		annotationMappings.put("Transverse P Maximum Angle", crossLeadAnnotations.getTranspmaxangle());
		annotationMappings.put("Transverse P Maximum Magnitude", crossLeadAnnotations.getTranspmaxmag());
		annotationMappings.put("Transverse P Terminal Angle", crossLeadAnnotations.getTransptermangle());
		annotationMappings.put("Transverse P Terminal Magnitude", crossLeadAnnotations.getTransptermmag());
		annotationMappings.put("Transverse qrscwrot", crossLeadAnnotations.getTransqrscwrot());
		annotationMappings.put("transqrsinitangle", crossLeadAnnotations.getTransqrsinitangle());
		annotationMappings.put("transqrsinitmag", crossLeadAnnotations.getTransqrsinitmag());
		annotationMappings.put("transqrsmaxangle", crossLeadAnnotations.getTransqrsmaxangle());
		annotationMappings.put("transqrsmaxmag", crossLeadAnnotations.getTransqrsmaxmag());
		annotationMappings.put("transqrstermangle", crossLeadAnnotations.getTransqrstermangle());
		annotationMappings.put("transqrstermmag", crossLeadAnnotations.getTransqrstermmag());
		annotationMappings.put("transtcwrot", crossLeadAnnotations.getTranstcwrot());
		annotationMappings.put("transtinitangle", crossLeadAnnotations.getTranstinitangle());
		annotationMappings.put("transtinitmag", crossLeadAnnotations.getTranstinitmag());
		annotationMappings.put("transtinitmag", crossLeadAnnotations.getTranstinitmag());
		annotationMappings.put("transtmaxangle", crossLeadAnnotations.getTranstmaxangle());
		annotationMappings.put("transtmaxmag", crossLeadAnnotations.getTranstmaxmag());
		annotationMappings.put("transttermangle", crossLeadAnnotations.getTransttermangle());
		annotationMappings.put("transttermmag", crossLeadAnnotations.getTransttermmag());
		annotationMappings.put("frontpcwrot", crossLeadAnnotations.getFrontpcwrot());
		annotationMappings.put("frontpinitangle", crossLeadAnnotations.getFrontpinitangle());
		annotationMappings.put("frontpinitmag", crossLeadAnnotations.getFrontpinitmag());
		annotationMappings.put("frontpmaxangle", crossLeadAnnotations.getFrontpmaxangle());
		annotationMappings.put("frontpmaxmag", crossLeadAnnotations.getFrontpmaxmag());
		annotationMappings.put("frontptermangle", crossLeadAnnotations.getFrontptermangle());
		annotationMappings.put("frontptermmag", crossLeadAnnotations.getFrontptermmag());
		annotationMappings.put("frontqrscwrot", crossLeadAnnotations.getFrontqrscwrot());
		annotationMappings.put("frontqrsinitangle", crossLeadAnnotations.getFrontqrsinitangle());
		annotationMappings.put("frontqrsinitmag", crossLeadAnnotations.getFrontqrsinitmag());
		annotationMappings.put("frontqrsmaxangle", crossLeadAnnotations.getFrontqrsmaxangle());
		annotationMappings.put("frontqrsmaxmag", crossLeadAnnotations.getFrontqrsmaxmag());
		annotationMappings.put("frontqrstermangle", crossLeadAnnotations.getFrontqrstermangle());
		annotationMappings.put("frontqrstermmag", crossLeadAnnotations.getFrontqrstermmag());
		annotationMappings.put("fronttcwrot", crossLeadAnnotations.getFronttcwrot());
		annotationMappings.put("fronttinitangle", crossLeadAnnotations.getFronttinitangle());
		annotationMappings.put("fronttinitmag", crossLeadAnnotations.getFronttinitmag());
		annotationMappings.put("fronttmaxangle", crossLeadAnnotations.getFronttmaxangle());
		annotationMappings.put("fronttmaxmag", crossLeadAnnotations.getFronttmaxmag());
		annotationMappings.put("frontttermangle", crossLeadAnnotations.getFrontttermangle());
		annotationMappings.put("frontttermmag", crossLeadAnnotations.getFrontttermmag());
		annotationMappings.put("sagpcwrot", crossLeadAnnotations.getSagpcwrot());
		annotationMappings.put("sagpinitangle", crossLeadAnnotations.getSagpinitangle());
		annotationMappings.put("sagpinitmag", crossLeadAnnotations.getSagpinitmag());
		annotationMappings.put("sagpmaxangle", crossLeadAnnotations.getSagpmaxangle());
		annotationMappings.put("sagpmaxmag", crossLeadAnnotations.getSagpmaxmag());
		annotationMappings.put("sagptermangle", crossLeadAnnotations.getSagptermangle());
		annotationMappings.put("sagptermmag", crossLeadAnnotations.getSagptermmag());
		annotationMappings.put("sagqrscwrot", crossLeadAnnotations.getSagqrscwrot());
		annotationMappings.put("sagqrsinitangle", crossLeadAnnotations.getSagqrsinitangle());
		annotationMappings.put("sagqrsinitmag", crossLeadAnnotations.getSagqrsinitmag());
		annotationMappings.put("sagqrsmaxangle", crossLeadAnnotations.getSagqrsmaxangle());
		annotationMappings.put("sagqrsmaxmag", crossLeadAnnotations.getSagqrsmaxmag());
		annotationMappings.put("sagqrstermangle", crossLeadAnnotations.getSagqrstermangle());
		annotationMappings.put("sagqrstermmag", crossLeadAnnotations.getSagqrstermmag());
		annotationMappings.put("sagtcwrot", crossLeadAnnotations.getSagtcwrot());
		annotationMappings.put("sagtinitangle", crossLeadAnnotations.getSagtinitangle());
		annotationMappings.put("sagtinitmag", crossLeadAnnotations.getSagtinitmag());
		annotationMappings.put("sagtmaxangle", crossLeadAnnotations.getSagtmaxangle());
		annotationMappings.put("sagtmaxmag", crossLeadAnnotations.getSagtmaxmag());
		annotationMappings.put("sagttermangle", crossLeadAnnotations.getSagttermangle());
		annotationMappings.put("sagttermmag", crossLeadAnnotations.getSagttermmag());
		
		annotationMappings.put("preexcitation", crossLeadAnnotations.getPreexcitation());
		
		// TODO:  Get beat annotations once the schema has been changed to accomdate for them
		
		annotationMappings.put("analysiserror", crossLeadAnnotations.getAnalysiserror());
		annotationMappings.put("analysiserrormessage", crossLeadAnnotations.getAnalysiserrormessage());
		
		// TODO:  Get namedmeasurements, if any.
		
		return annotationMappings;
	}
	
	private LinkedHashMap<String, Object> extractGroupMeasurements(Groupmeasurement groupMeasurements) {
		LinkedHashMap<String, Object> annotationMappings = new LinkedHashMap<String, Object>();
		
		annotationMappings.put("membercount", groupMeasurements.getMembercount());
		annotationMappings.put("memberpercent", groupMeasurements.getMemberpercent());
		annotationMappings.put("longestrun", groupMeasurements.getLongestrun());
		annotationMappings.put("meanqrsdur", groupMeasurements.getMeanqrsdur());
		annotationMappings.put("lowventrate", groupMeasurements.getLowventrate());
		annotationMappings.put("meanventrate", groupMeasurements.getMeanventrate());
		annotationMappings.put("highventrate", groupMeasurements.getHighventrate());
		annotationMappings.put("ventratestddev", groupMeasurements.getVentratestddev());
		annotationMappings.put("meanrrint", groupMeasurements.getMeanrrint());
		annotationMappings.put("atrialrate", groupMeasurements.getAtrialrate());
		annotationMappings.put("atrialratestdev", groupMeasurements.getAtrialratestddev());
		annotationMappings.put("avgpcount", groupMeasurements.getAvgpcount());
		annotationMappings.put("notavgbeats", groupMeasurements.getNotavgpbeats());
		annotationMappings.put("lowprint", groupMeasurements.getLowprint());
		annotationMappings.put("meanprint", groupMeasurements.getMeanprint());
		annotationMappings.put("highprint", groupMeasurements.getHighprint());
		annotationMappings.put("printstddev", groupMeasurements.getPrintstddev());
		annotationMappings.put("meanprseg", groupMeasurements.getMeanprseg());
		annotationMappings.put("meanqtint", groupMeasurements.getMeanqtint());
		annotationMappings.put("meanqtseg", groupMeasurements.getMeanqtseg());
		annotationMappings.put("comppausecount", groupMeasurements.getComppausecount());
		
		return annotationMappings;
		
	}
	
	private void generateWaveformAnnotations(String annotationName, String annotationValue) {
		
	}

}
