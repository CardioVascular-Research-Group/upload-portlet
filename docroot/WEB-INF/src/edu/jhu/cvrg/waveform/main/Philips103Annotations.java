package edu.jhu.cvrg.waveform.main;

import org.cvrgrid.philips.jaxb.beans.TYPEmessagecode;
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
		
		public Philips103Annotations(Restingecgdata newECG) {
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
			
			for(String key : annotationMappings.keySet()) {
				System.out.println("Annotation Name = " + key + " and value = " + annotationMappings.get(key));
				this.generateWaveformAnnotations(key, annotationMappings.get(key));
			}
			
		}
		
		private void populateGroupAnnotations() {
			
		}
		
		private void populateLeadAnnotations() {
			
		}
		
		private LinkedHashMap<String, String> extractGlobalElements(Globalmeasurements globalAnnotations) {
			LinkedHashMap<String, String> annotationMappings = new LinkedHashMap<String, String>();
			
			// The mapping will need to be filled manually since currently we cannot get a list of all of the elements
			// Do not do complex annotation measurements yet.  Stick with the simple ones.
			
			// TODO:  Skipping pacedetectleads and pacepulses for now, return to them later.
			
			// In Philips 1.03, pacedetectleads is a simple annotation
			
			// Main set of global lead measurements
			
			// TODO: In Philips 1.03, the pacemode is a list of pace modes
			//annotationMappings.put("pacemode", globalAnnotations.getPacemode());
			annotationMappings.put("pacemalf", globalAnnotations.getPacemalf());
			annotationMappings.put("pacemisc", globalAnnotations.getPacemisc());
			annotationMappings.put("ectopicrhythm", globalAnnotations.getEctopicrhythm());
			annotationMappings.put("qintdispersion", globalAnnotations.getQtintdispersion());
			annotationMappings.put("numberofcomplexes", globalAnnotations.getNumberofcomplexes());
			annotationMappings.put("numberofgroups", globalAnnotations.getNumberofgroups());
			
			String beatClassificationValues = "";
			
			// concatenate all the beat classifications, as that is how they appear in the XML
			for (Integer beatValue : globalAnnotations.getBeatclassification()) {
				beatClassificationValues = beatClassificationValues + " " + beatValue.toString();
			}
			annotationMappings.put("beatclassification", beatClassificationValues);
			int subscript = 1;
			
			// enter the qamessagecodes one by one, as that is how they are in the XML
			for(org.sierraecg.schema.TYPEmessagecode mCode : globalAnnotations.getQamessagecodes().getQamessagecode()) {
				String messageName = "qamessagecode" + subscript;
				annotationMappings.put(messageName, mCode.value());
				subscript++;
			}
			
			annotationMappings.put("qaactioncode", globalAnnotations.getQaactioncode().value());
			annotationMappings.put("pon", globalAnnotations.getPon());
			annotationMappings.put("qrson", globalAnnotations.getQrson());
			annotationMappings.put("qrsoff", globalAnnotations.getQrsoff());
			annotationMappings.put("ton", globalAnnotations.getTon());
			annotationMappings.put("toff", globalAnnotations.getToff());
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
			annotationMappings.put("qrsinitangle", globalAnnotations.getQrsinitangle());
			annotationMappings.put("qrsinitmag", globalAnnotations.getQrsinitmag());
			annotationMappings.put("qrsmaxangle", globalAnnotations.getQrsmaxangle());
			annotationMappings.put("qrsmaxmag", globalAnnotations.getQrsmaxmag());
			annotationMappings.put("qrstermangle", globalAnnotations.getQrstermangle());
			annotationMappings.put("qrstermmag", globalAnnotations.getQrstermmag());
			annotationMappings.put("qrsrotation", globalAnnotations.getQrsrotation());
			annotationMappings.put("globalreserved", globalAnnotations.getGlobalreserved());
			
			return annotationMappings;
		}
		
		private void generateWaveformAnnotations(String annotationName, String annotationValue) {
			
		}
	
}
