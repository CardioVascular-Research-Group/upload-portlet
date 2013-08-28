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
				System.out.println("Annotation Name = " + key + " and value = " + annotationMappings.get(key).toString());
				this.generateWaveformAnnotations(key, annotationMappings.get(key).toString());
			}
		}
		
	}
	
	private void populateGroupAnnotations() {
		
	}
	
	private void populateLeadAnnotations() {
		
	}
	
	private LinkedHashMap<String, Object> extractCrossleadElements(Crossleadmeasurements globalAnnotations) {
		LinkedHashMap<String, Object> annotationMappings = new LinkedHashMap<String, Object>();
		
		// The mapping will need to be filled manually since currently we cannot get a list of all of the elements
		// Do not do complex annotation measurements yet.  Stick with the simple ones.
		
		// TODO:  Skipping pacedetectleads and pacepulses for now, return to them later.
		
		// Main set of cross lead measurements
		annotationMappings.put("pacemode", globalAnnotations.getPacemode());
		annotationMappings.put("pacemalf", globalAnnotations.getPacemalf());
		annotationMappings.put("pacemisc", globalAnnotations.getPacemisc());
		annotationMappings.put("ectopicrhythm", globalAnnotations.getEctopicrhythm());
		annotationMappings.put("qintdispersion", globalAnnotations.getQtintdispersion());
		annotationMappings.put("numberofcomplexes", globalAnnotations.getNumberofcomplexes());
		annotationMappings.put("numberofgroups", globalAnnotations.getNumberofgroups());
		
		String beatClassificationValues = "";
		
		// concatenate all the beat classifications, as that is how they appear in the XML
		for (Integer beatValue : globalAnnotations.getBeatclassification()) {
			beatClassificationValues = beatClassificationValues + " " + beatValue;
		}
		annotationMappings.put("beatclassification", beatClassificationValues);
		int subscript = 1;
		
		// enter the qamessagecodes one by one, as that is how they are in the XML
		for(TYPEmessagecode mCode : globalAnnotations.getQamessagecodes().getQamessagecode()) {
			if(mCode != null) {
				String messageName = "qamessagecode" + subscript;
				annotationMappings.put(messageName, mCode.value());
				subscript++;
			}
		}
		
		// additional checks for null are needed on some of these retrievals.  Some of them require methods other
		// than toString()
		if(globalAnnotations.getQaactioncode() != null) {
			annotationMappings.put("qaactioncode", globalAnnotations.getQaactioncode().value());
		}
		annotationMappings.put("pfrontaxis", globalAnnotations.getPfrontaxis());
		annotationMappings.put("phorizaxis", globalAnnotations.getPhorizaxis());
		annotationMappings.put("i40frontaxis", globalAnnotations.getI40Frontaxis());
		annotationMappings.put("i40horizaxis", globalAnnotations.getI40Horizaxis());
		annotationMappings.put("qrsfrontaxis", globalAnnotations.getQrsfrontaxis());
		annotationMappings.put("qrshorizaxis", globalAnnotations.getQrshorizaxis());
		annotationMappings.put("t40frontaxis", globalAnnotations.getT40Frontaxis());
		annotationMappings.put("t40horizaxis", globalAnnotations.getT40Horizaxis());
		annotationMappings.put("stfrontaxis", globalAnnotations.getStfrontaxis());
		annotationMappings.put("sthorizaxis", globalAnnotations.getSthorizaxis());
		annotationMappings.put("tfrontaxis", globalAnnotations.getTfrontaxis());
		annotationMappings.put("thorizaxis", globalAnnotations.getThorizaxis());
		annotationMappings.put("atrialrate", globalAnnotations.getAtrialrate());
		annotationMappings.put("lowventrate", globalAnnotations.getLowventrate());
		annotationMappings.put("meanventrate", globalAnnotations.getMeanventrate());
		annotationMappings.put("highventrate", globalAnnotations.getHighventrate());
		annotationMappings.put("meanprint", globalAnnotations.getMeanprint());
		annotationMappings.put("meanprseg", globalAnnotations.getMeanprseg());
		annotationMappings.put("meanqrsdur", globalAnnotations.getMeanqrsdur());
		annotationMappings.put("meanqtint", globalAnnotations.getMeanqtint());
		annotationMappings.put("meanqtc", globalAnnotations.getMeanqtc());
		annotationMappings.put("deltawavecount", globalAnnotations.getDeltawavecount());
		annotationMappings.put("deltawavepercent", globalAnnotations.getDeltawavepercent());
		annotationMappings.put("bigeminycount", globalAnnotations.getBigeminycount());
		annotationMappings.put("bigeminystring", globalAnnotations.getBigeminystring());
		annotationMappings.put("trigeminycount", globalAnnotations.getTrigeminycount());
		annotationMappings.put("trigeminystring", globalAnnotations.getTrigeminystring());
		annotationMappings.put("wenckcount", globalAnnotations.getWenckcount());
		annotationMappings.put("wenckstring", globalAnnotations.getWenckstring());
		annotationMappings.put("flutterfibcount", globalAnnotations.getFlutterfibcount());
		
		annotationMappings.put("leadreversalcode", globalAnnotations.getLeadreversalcode());
		
		annotationMappings.put("atrialrhythm", globalAnnotations.getAtrialrhythm());
		annotationMappings.put("atrialratepowerspect", globalAnnotations.getAtrialratepowerspect());
		annotationMappings.put("ventrhythm", globalAnnotations.getVentrhythm());
		annotationMappings.put("randomrrpercent", globalAnnotations.getRandomrrpercent());
		annotationMappings.put("regularrrpercent", globalAnnotations.getRegularrrpercent());
		annotationMappings.put("biggestrrgrouppercent", globalAnnotations.getBiggestrrgrouppercent());
		annotationMappings.put("biggestrrgroupvar", globalAnnotations.getBiggestrrgroupvar());
		annotationMappings.put("nrrgroups", globalAnnotations.getNrrgroups());
		annotationMappings.put("bigemrrintvlacf", globalAnnotations.getBigemrrintvlacf());
		annotationMappings.put("trigemrrintvlacf", globalAnnotations.getTrigemrrintvlacf());
		annotationMappings.put("fibfreqmhz", globalAnnotations.getFibfreqmhz());
		annotationMappings.put("fibampnv", globalAnnotations.getFibampnv());
		annotationMappings.put("fibwidthmhz", globalAnnotations.getFibwidthmhz());
		annotationMappings.put("fibmedianfreqmhz", globalAnnotations.getFibmedianfreqmhz());
		annotationMappings.put("afltcyclelen", globalAnnotations.getAfltcyclelen());
		annotationMappings.put("afltacfpeak", globalAnnotations.getAfltacfpeak());
		annotationMappings.put("afltacfpeakwidth", globalAnnotations.getAfltacfpeakwidth());
		annotationMappings.put("constantpshapepct", globalAnnotations.getConstantpshapepct());
		annotationMappings.put("atrialrateirregpct", globalAnnotations.getAtrialrateirregpct());
		
		// the vector loop mxs elements
		annotationMappings.put("transpcwrot", globalAnnotations.getTranspcwrot());
		annotationMappings.put("transpinitangle", globalAnnotations.getTranspinitangle());
		annotationMappings.put("transpinitmag", globalAnnotations.getTranspinitmag());
		annotationMappings.put("transpmaxangle", globalAnnotations.getTranspmaxangle());
		annotationMappings.put("transpmaxmag", globalAnnotations.getTranspmaxmag());
		annotationMappings.put("transptermangle", globalAnnotations.getTransptermangle());
		annotationMappings.put("transptermmag", globalAnnotations.getTransptermmag());
		annotationMappings.put("transqrscwrot", globalAnnotations.getTransqrscwrot());
		annotationMappings.put("transqrsinitangle", globalAnnotations.getTransqrsinitangle());
		annotationMappings.put("transqrsinitmag", globalAnnotations.getTransqrsinitmag());
		annotationMappings.put("transqrsmaxangle", globalAnnotations.getTransqrsmaxangle());
		annotationMappings.put("transqrsmaxmag", globalAnnotations.getTransqrsmaxmag());
		annotationMappings.put("transqrstermangle", globalAnnotations.getTransqrstermangle());
		annotationMappings.put("transqrstermmag", globalAnnotations.getTransqrstermmag());
		annotationMappings.put("transtcwrot", globalAnnotations.getTranstcwrot());
		annotationMappings.put("transtinitangle", globalAnnotations.getTranstinitangle());
		annotationMappings.put("transtinitmag", globalAnnotations.getTranstinitmag());
		annotationMappings.put("transtinitmag", globalAnnotations.getTranstinitmag());
		annotationMappings.put("transtmaxangle", globalAnnotations.getTranstmaxangle());
		annotationMappings.put("transtmaxmag", globalAnnotations.getTranstmaxmag());
		annotationMappings.put("transttermangle", globalAnnotations.getTransttermangle());
		annotationMappings.put("transttermmag", globalAnnotations.getTransttermmag());
		annotationMappings.put("frontpcwrot", globalAnnotations.getFrontpcwrot());
		annotationMappings.put("frontpinitangle", globalAnnotations.getFrontpinitangle());
		annotationMappings.put("frontpinitmag", globalAnnotations.getFrontpinitmag());
		annotationMappings.put("frontpmaxangle", globalAnnotations.getFrontpmaxangle());
		annotationMappings.put("frontpmaxmag", globalAnnotations.getFrontpmaxmag());
		annotationMappings.put("frontptermangle", globalAnnotations.getFrontptermangle());
		annotationMappings.put("frontptermmag", globalAnnotations.getFrontptermmag());
		annotationMappings.put("frontqrscwrot", globalAnnotations.getFrontqrscwrot());
		annotationMappings.put("frontqrsinitangle", globalAnnotations.getFrontqrsinitangle());
		annotationMappings.put("frontqrsinitmag", globalAnnotations.getFrontqrsinitmag());
		annotationMappings.put("frontqrsmaxangle", globalAnnotations.getFrontqrsmaxangle());
		annotationMappings.put("frontqrsmaxmag", globalAnnotations.getFrontqrsmaxmag());
		annotationMappings.put("frontqrstermangle", globalAnnotations.getFrontqrstermangle());
		annotationMappings.put("frontqrstermmag", globalAnnotations.getFrontqrstermmag());
		annotationMappings.put("fronttcwrot", globalAnnotations.getFronttcwrot());
		annotationMappings.put("fronttinitangle", globalAnnotations.getFronttinitangle());
		annotationMappings.put("fronttinitmag", globalAnnotations.getFronttinitmag());
		annotationMappings.put("fronttmaxangle", globalAnnotations.getFronttmaxangle());
		annotationMappings.put("fronttmaxmag", globalAnnotations.getFronttmaxmag());
		annotationMappings.put("frontttermangle", globalAnnotations.getFrontttermangle());
		annotationMappings.put("frontttermmag", globalAnnotations.getFrontttermmag());
		annotationMappings.put("sagpcwrot", globalAnnotations.getSagpcwrot());
		annotationMappings.put("sagpinitangle", globalAnnotations.getSagpinitangle());
		annotationMappings.put("sagpinitmag", globalAnnotations.getSagpinitmag());
		annotationMappings.put("sagpmaxangle", globalAnnotations.getSagpmaxangle());
		annotationMappings.put("sagpmaxmag", globalAnnotations.getSagpmaxmag());
		annotationMappings.put("sagptermangle", globalAnnotations.getSagptermangle());
		annotationMappings.put("sagptermmag", globalAnnotations.getSagptermmag());
		annotationMappings.put("sagqrscwrot", globalAnnotations.getSagqrscwrot());
		annotationMappings.put("sagqrsinitangle", globalAnnotations.getSagqrsinitangle());
		annotationMappings.put("sagqrsinitmag", globalAnnotations.getSagqrsinitmag());
		annotationMappings.put("sagqrsmaxangle", globalAnnotations.getSagqrsmaxangle());
		annotationMappings.put("sagqrsmaxmag", globalAnnotations.getSagqrsmaxmag());
		annotationMappings.put("sagqrstermangle", globalAnnotations.getSagqrstermangle());
		annotationMappings.put("sagqrstermmag", globalAnnotations.getSagqrstermmag());
		annotationMappings.put("sagtcwrot", globalAnnotations.getSagtcwrot());
		annotationMappings.put("sagtinitangle", globalAnnotations.getSagtinitangle());
		annotationMappings.put("sagtinitmag", globalAnnotations.getSagtinitmag());
		annotationMappings.put("sagtmaxangle", globalAnnotations.getSagtmaxangle());
		annotationMappings.put("sagtmaxmag", globalAnnotations.getSagtmaxmag());
		annotationMappings.put("sagttermangle", globalAnnotations.getSagttermangle());
		annotationMappings.put("sagttermmag", globalAnnotations.getSagttermmag());
		
		annotationMappings.put("preexcitation", globalAnnotations.getPreexcitation());
		
		// TODO:  Get beat annotations once the schema has been changed to accomdate for them
		
		annotationMappings.put("analysiserror", globalAnnotations.getAnalysiserror());
		annotationMappings.put("analysiserrormessage", globalAnnotations.getAnalysiserrormessage());
		
		// TODO:  Get namedmeasurements, if any.
		
		return annotationMappings;
	}
	
	private void generateWaveformAnnotations(String annotationName, String annotationValue) {
		
	}

}
