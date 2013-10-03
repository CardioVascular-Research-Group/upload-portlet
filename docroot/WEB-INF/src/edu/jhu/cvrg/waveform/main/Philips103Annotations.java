package edu.jhu.cvrg.waveform.main;


import org.sierraecg.*;
import org.sierraecg.schema.*;

import java.util.LinkedHashMap;

// This class contains methods for retrieving annotations from the XML file.
// The class is left at the default (package level) visibility as they are not intended to
// be used by classes outside of this package.
//
// Author: Brandon Benitez
class Philips103Annotations {
	
	public LinkedHashMap<String, Object> extractOrderInfo(Orderinfo orderinfoAnn) {
		LinkedHashMap<String, Object> orderAnnsMap = new LinkedHashMap<String, Object>();
		
		orderAnnsMap.put("Order Number", orderinfoAnn.getEncounterid());
		orderAnnsMap.put("Operator ID", orderinfoAnn.getOperatorid());
		orderAnnsMap.put("Order Number", orderinfoAnn.getOrdernumber());
		orderAnnsMap.put("Viper Unique Order ID", orderinfoAnn.getViperuniqueorderid());
		orderAnnsMap.put("Ordering Clinician Name", orderinfoAnn.getOrderingclinicianname());
		orderAnnsMap.put("Ordering Clinician UPIN", orderinfoAnn.getOrderingclinicianUPIN());
		orderAnnsMap.put("Reason For Order", orderinfoAnn.getReasonfororder());
		
		int subscript = 1;
		
		for(Drgcategory drg : orderinfoAnn.getDrgcategories().getDrgcategory()) {
			if(drg != null) {
				String messageName = "drgcategories" + subscript;
				orderAnnsMap.put(messageName, drg.getValue());
				subscript++;
			}
		}
		
		return orderAnnsMap;
	}
	
	LinkedHashMap<String, Object> extractDataAcquisition(Dataacquisition dataAnnotations) {
		LinkedHashMap<String, Object> dataMappings = new LinkedHashMap<String, Object>();
		
		dataMappings.put("Database ID", dataAnnotations.getEmsdatabaseid());
		dataMappings.put("Machine", dataAnnotations.getMachine());
		
		// Now get the acquirer block in the XML
		Acquirer acquirerAnn = dataAnnotations.getAcquirer(); 
		
		dataMappings.put("Acquirer Encounter ID", acquirerAnn.getEncounterid());
		dataMappings.put("Acquirer Operator ID", acquirerAnn.getOperatorid());
		dataMappings.put("Editing Operator ID", acquirerAnn.getEditingoperatorid());
		dataMappings.put("Room", acquirerAnn.getRoom());
		dataMappings.put("Department ID", acquirerAnn.getDepartmentid());
		dataMappings.put("Department Name", acquirerAnn.getDepartmentname());
		dataMappings.put("Institution ID", acquirerAnn.getInstitutionid());
		dataMappings.put("Institution Name", acquirerAnn.getInstitutionname());
		dataMappings.put("Institution Location ID", acquirerAnn.getInstitutionlocationid());
		dataMappings.put("Institution Location Name", acquirerAnn.getInstitutionlocationname());
		dataMappings.put("Ordering Clinician", acquirerAnn.getOrderingclinicianname());
		dataMappings.put("Ordering Clinician UPIN", acquirerAnn.getOrderingclinicianUPIN());
		dataMappings.put("Consulting Clinician", acquirerAnn.getReviewingclinician());
		
		// Retrieve the Signal Characteristics block.  The Sampling rate and number of channels
		// information will not be gathered here, since those are being tracked elsewhere.
		
		Signalcharacteristics signalProperties = dataAnnotations.getSignalcharacteristics();
		
		dataMappings.put("Signal Resolution", signalProperties.getSignalresolution());
		dataMappings.put("AC Setting", signalProperties.getAcsetting());
		dataMappings.put("Acquisition Type", signalProperties.getAcquisitiontype());
		dataMappings.put("Bits Per Sample", signalProperties.getBitspersample());
		dataMappings.put("Signal Offset", signalProperties.getSignaloffset());
		dataMappings.put("Signal Signed", signalProperties.getSignalsigned());
		dataMappings.put("Lead Set", signalProperties.getLeadset());
		
		return dataMappings;
	}

		LinkedHashMap<String, String> extractGlobalElements(Globalmeasurements globalAnnotations) {
			LinkedHashMap<String, String> annotationMappings = new LinkedHashMap<String, String>();
			
			// The mapping will need to be filled manually since currently we cannot get a list of all of the elements
			// Do not do complex annotation measurements yet.  Stick with the simple ones.
			
			// TODO:  Skipping pacedetectleads and pacepulses for now, return to them later.
			// In Philips 1.03, pacedetectleads is a simple annotation
			
			// Main set of global lead measurements
			
			// TODO: In Philips 1.03, the pacemode is a list of pace modes, return to this later
			//annotationMappings.put("pacemode", globalAnnotations.getPacemode());
			
			annotationMappings.put("pacemalf", globalAnnotations.getPacemalf());
			annotationMappings.put("pacemisc", globalAnnotations.getPacemisc());
			annotationMappings.put("Ectopic Rhythm", globalAnnotations.getEctopicrhythm());
			annotationMappings.put("QT Interval Dispersion", globalAnnotations.getQtintdispersion());
			annotationMappings.put("Number of Complexes", globalAnnotations.getNumberofcomplexes());
			annotationMappings.put("Number of Groups", globalAnnotations.getNumberofgroups());
			
			String beatClassificationValues = "";
			
			// Only one additional check for null is need in the 1.03 version, 
			// as most of these elements are required in that version of the schema
			// The ones that are not are already taken care of in the function that calls this one.
			
			// concatenate all the beat classifications, as that is how they appear in the XML
			for (Integer beatValue : globalAnnotations.getBeatclassification()) {
				beatClassificationValues = beatClassificationValues + " " + beatValue.toString();
			}
			annotationMappings.put("beatclassification", beatClassificationValues);
			int subscript = 1;
			
			// enter the qamessagecodes one by one, as that is how they are in the XML
			for(org.sierraecg.schema.TYPEmessagecode mCode : globalAnnotations.getQamessagecodes().getQamessagecode()) {
				if(mCode != null) {
					String messageName = "qamessagecode" + subscript;
					annotationMappings.put(messageName, mCode.value());
					subscript++;
				}
			}
			
			annotationMappings.put("qaactioncode", globalAnnotations.getQaactioncode().value());
			annotationMappings.put("P Onset", globalAnnotations.getPon());
			annotationMappings.put("QRS Onset", globalAnnotations.getQrson());
			annotationMappings.put("QRS Offset", globalAnnotations.getQrsoff());
			annotationMappings.put("T Onset", globalAnnotations.getTon());
			annotationMappings.put("T Offset", globalAnnotations.getToff());
			annotationMappings.put("P Frontal Axis", globalAnnotations.getPfrontaxis());
			annotationMappings.put("P Horizontal Axis", globalAnnotations.getPhorizaxis());
			annotationMappings.put("i40 Frontal Axis", globalAnnotations.getI40Frontaxis());
			annotationMappings.put("i40 Horizontal Axis", globalAnnotations.getI40Horizaxis());
			annotationMappings.put("QRS Frontal Axis", globalAnnotations.getQrsfrontaxis());
			annotationMappings.put("QRS Horizontal Axis", globalAnnotations.getQrshorizaxis());
			annotationMappings.put("t40 Frontal Axis", globalAnnotations.getT40Frontaxis());
			annotationMappings.put("t40 Horizontal Axis", globalAnnotations.getT40Horizaxis());
			annotationMappings.put("ST Frontal Axis", globalAnnotations.getStfrontaxis());
			annotationMappings.put("ST Horizontal Axis", globalAnnotations.getSthorizaxis());
			annotationMappings.put("T Frontal Axis", globalAnnotations.getTfrontaxis());
			annotationMappings.put("T Horizontal Axis", globalAnnotations.getThorizaxis());
			annotationMappings.put("Atrial Rate", globalAnnotations.getAtrialrate());
			annotationMappings.put("Minimum Ventricular Rate", globalAnnotations.getLowventrate());
			annotationMappings.put("Mean Ventricular Rate", globalAnnotations.getMeanventrate());
			annotationMappings.put("High Ventricular Rate", globalAnnotations.getHighventrate());
			annotationMappings.put("Mean PR Interval", globalAnnotations.getMeanprint());
			annotationMappings.put("Mean PR Segment", globalAnnotations.getMeanprseg());
			annotationMappings.put("Mean QRS Duration", globalAnnotations.getMeanqrsdur());
			annotationMappings.put("Mean QT Interval", globalAnnotations.getMeanqtint());
			annotationMappings.put("Mean QT Corrected", globalAnnotations.getMeanqtc());
			annotationMappings.put("Delta Wave Count", globalAnnotations.getDeltawavecount());
			annotationMappings.put("Delta Wave Percent", globalAnnotations.getDeltawavepercent());
			annotationMappings.put("bigeminycount", globalAnnotations.getBigeminycount());
			annotationMappings.put("bigeminystring", globalAnnotations.getBigeminystring());
			annotationMappings.put("trigeminycount", globalAnnotations.getTrigeminycount());
			annotationMappings.put("trigeminystring", globalAnnotations.getTrigeminystring());
			annotationMappings.put("wenckcount", globalAnnotations.getWenckcount());
			annotationMappings.put("wenckstring", globalAnnotations.getWenckstring());
			annotationMappings.put("flutterfibcount", globalAnnotations.getFlutterfibcount());
			annotationMappings.put("QRS Initial Angle", globalAnnotations.getQrsinitangle());
			annotationMappings.put("QRS Initial Magnitude", globalAnnotations.getQrsinitmag());
			annotationMappings.put("QRS Maximum Angle", globalAnnotations.getQrsmaxangle());
			annotationMappings.put("QRS Maximum Magnitude", globalAnnotations.getQrsmaxmag());
			annotationMappings.put("QRS Terminal Angle", globalAnnotations.getQrstermangle());
			annotationMappings.put("QRS Terminal Magnitude", globalAnnotations.getQrstermmag());
			annotationMappings.put("QRS Rotation", globalAnnotations.getQrsrotation());
			annotationMappings.put("globalreserved", globalAnnotations.getGlobalreserved());
			
			return annotationMappings;
		}
		
		LinkedHashMap<String, Object> extractGroupMeasurements(Groupmeasurement annotation) {
			LinkedHashMap<String, Object> annotationMappings = new LinkedHashMap<String, Object>();
			
			annotationMappings.put("Member Count", annotation.getMembercount());
			annotationMappings.put("Member Percent", annotation.getMemberpercent());
			annotationMappings.put("Longest Run", annotation.getLongestrun());
			annotationMappings.put("Mean QRS Duration", annotation.getMeanqrsdur());
			annotationMappings.put("Minimum Ventricular Rate", annotation.getLowventrate());
			annotationMappings.put("Mean Ventricular Rate", annotation.getMeanventrate());
			annotationMappings.put("Maximum Ventricular Rate", annotation.getHighventrate());
			annotationMappings.put("Ventricular Rate Standard Deviation", annotation.getVentratestddev());
			annotationMappings.put("Mean RR Interval", annotation.getMeanrrint());
			annotationMappings.put("Atrial Rate", annotation.getAtrialrate());
			annotationMappings.put("Atrial Rate Standard Deviation", annotation.getAtrialratestddev());
			annotationMappings.put("avgpcount", annotation.getAvgpcount());
			annotationMappings.put("notavgbeats", annotation.getNotavgpbeats());
			annotationMappings.put("Minimum PR Interval", annotation.getLowprint());
			annotationMappings.put("Mean PR Interval", annotation.getMeanprint());
			annotationMappings.put("High PR Interval", annotation.getHighprint());
			annotationMappings.put("PR Interval Standard Deviation", annotation.getPrintstddev());
			annotationMappings.put("Mean PR Segment", annotation.getMeanprseg());
			annotationMappings.put("Mean QT Interval", annotation.getMeanqtint());
			annotationMappings.put("Mean QT Segment", annotation.getMeanqtseg());
			annotationMappings.put("groupreserved", annotation.getGroupreserved());
			
			return annotationMappings;
			
		}
		
		LinkedHashMap<String, Object> extractLeadMeasurements(Leadmeasurement leadMeasurements) {
			LinkedHashMap<String, Object> annotationMappings = new LinkedHashMap<String, Object>();
			
			annotationMappings.put("P Amplitude", leadMeasurements.getPamp());
			annotationMappings.put("P Duration", leadMeasurements.getPdur());
			annotationMappings.put("P Area", leadMeasurements.getParea());
			annotationMappings.put("P' Amplitude", leadMeasurements.getPpamp());
			annotationMappings.put("P' Duration", leadMeasurements.getPpdur());
			annotationMappings.put("P'' Duration", leadMeasurements.getPpppdur());
			annotationMappings.put("P' Area", leadMeasurements.getPparea());
			annotationMappings.put("P'' Area", leadMeasurements.getPppparea());
			annotationMappings.put("Q Amplitude", leadMeasurements.getQamp());
			annotationMappings.put("Q Duration", leadMeasurements.getQdur());
			annotationMappings.put("R Amplitude", leadMeasurements.getRamp());
			annotationMappings.put("R Duration", leadMeasurements.getRdur());
			annotationMappings.put("S Amplitude", leadMeasurements.getSamp());
			annotationMappings.put("S Duration", leadMeasurements.getSdur());
			annotationMappings.put("R' Amplitude", leadMeasurements.getRpamp());
			annotationMappings.put("R' Duration", leadMeasurements.getRpdur());
			annotationMappings.put("S' Amplitude", leadMeasurements.getSpamp());
			annotationMappings.put("S' Duration", leadMeasurements.getSpdur());
			annotationMappings.put("Ventricular Activation Time", leadMeasurements.getVat());
			annotationMappings.put("QRS Amplitude", leadMeasurements.getQrsppk());
			annotationMappings.put("QRS Duration", leadMeasurements.getQrsdur());
			annotationMappings.put("QRS Area", leadMeasurements.getQrsarea());
			annotationMappings.put("ST Segment Onset", leadMeasurements.getSton());
			annotationMappings.put("ST Segment Midpoint", leadMeasurements.getStmid());
			annotationMappings.put("ST Segment Offset", leadMeasurements.getStend());
			annotationMappings.put("ST Segment Duration", leadMeasurements.getStdur());
			annotationMappings.put("ST Segment Slope", leadMeasurements.getStslope());
			annotationMappings.put("ST Segment Shape", leadMeasurements.getStshape());
			annotationMappings.put("T Amplitude", leadMeasurements.getTamp());
			annotationMappings.put("T Duration", leadMeasurements.getTdur());
			annotationMappings.put("T Area", leadMeasurements.getTarea());
			annotationMappings.put("T' Amplitude", leadMeasurements.getTpamp());
			annotationMappings.put("T'' Amplitude", leadMeasurements.getTptpdur());
			annotationMappings.put("T' Duration", leadMeasurements.getTpdur());
			annotationMappings.put("T' Area", leadMeasurements.getTparea());
			annotationMappings.put("T'' Area", leadMeasurements.getTptparea());
			annotationMappings.put("PR Interval", leadMeasurements.getPrint());
			annotationMappings.put("PR Segment", leadMeasurements.getPrseg());
			annotationMappings.put("QT Interval", leadMeasurements.getQtint());
			
			return annotationMappings;
		}
	
}
