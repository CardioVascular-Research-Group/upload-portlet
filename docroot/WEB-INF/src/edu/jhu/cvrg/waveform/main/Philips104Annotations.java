package edu.jhu.cvrg.waveform.main;

import java.util.LinkedHashMap;

import org.cvrgrid.philips.jaxb.beans.Acquirer;
import org.cvrgrid.philips.jaxb.beans.Crossleadmeasurements;
import org.cvrgrid.philips.jaxb.beans.Dataacquisition;
import org.cvrgrid.philips.jaxb.beans.Drgcategory;
import org.cvrgrid.philips.jaxb.beans.Groupmeasurement;
import org.cvrgrid.philips.jaxb.beans.Leadmeasurement;
import org.cvrgrid.philips.jaxb.beans.Orderinfo;
import org.cvrgrid.philips.jaxb.beans.Orderinfo.Other;
import org.cvrgrid.philips.jaxb.beans.Signalcharacteristics;
import org.cvrgrid.philips.jaxb.beans.TYPEmessagecode;

// This class contains methods for retrieving annotations from the XML file.
// The class is left at the default (package level) visibility as they are not intended to
// be used by classes outside of this package.
//
// Author: Brandon Benitez

class Philips104Annotations {
	
	LinkedHashMap<String, Object> extractOrderInfo(Orderinfo orderinfoAnn) {
		LinkedHashMap<String, Object> orderAnnsMap = new LinkedHashMap<String, Object>();
		
		orderAnnsMap.put("Order Number", orderinfoAnn.getOrdernumber());
		orderAnnsMap.put("Unique Order ID", orderinfoAnn.getUniqueorderid());
		orderAnnsMap.put("Order Billing Code", orderinfoAnn.getOrderbillingcode());
		orderAnnsMap.put("Order Remarks", orderinfoAnn.getOrderremarks());
		orderAnnsMap.put("Reason For Order", orderinfoAnn.getReasonfororder());
		
		int subscript = 1;
		
		for(Drgcategory drg : orderinfoAnn.getDrgcategories().getDrgcategory()) {
			if(drg != null) {
				String messageName = "drgcategories" + subscript;
				orderAnnsMap.put(messageName, drg.getValue());
				subscript++;
			}
		}
		
		orderAnnsMap.put("Order Status", orderinfoAnn.getOrderstatus());
		orderAnnsMap.put("Inbox", orderinfoAnn.getInbox());
		
		subscript = 1;
		
		for(Other othr : orderinfoAnn.getOther()) {
			if(othr != null) {
				String messageName = "drgcategories" + subscript;
				orderAnnsMap.put(messageName, othr.getValue());
				subscript++;
			}
		}
		
		return orderAnnsMap;
	}
	
	LinkedHashMap<String, Object> extractDataAcquisition(Dataacquisition dataAnnotations) {
		LinkedHashMap<String, Object> dataMappings = new LinkedHashMap<String, Object>();
		
		dataMappings.put("Database ID", dataAnnotations.getDatabaseid());
		dataMappings.put("Modality", dataAnnotations.getModality());
		dataMappings.put("Machine", dataAnnotations.getMachine());
		
		// Now get the acquirer block in the XML
		Acquirer acquirerAnn = dataAnnotations.getAcquirer(); 
		
		dataMappings.put("Acquirer Encounter ID", acquirerAnn.getEncounterid());
		dataMappings.put("Operator", acquirerAnn.getOperator());
		dataMappings.put("Room", acquirerAnn.getRoom());
		dataMappings.put("Bed", acquirerAnn.getBed());
		dataMappings.put("Department ID", acquirerAnn.getDepartmentid());
		dataMappings.put("Department Name", acquirerAnn.getDepartmentname());
		dataMappings.put("Institution ID", acquirerAnn.getInstitutionid());
		dataMappings.put("Institution Name", acquirerAnn.getInstitutionname());
		dataMappings.put("Facility ID", acquirerAnn.getFacilityid());
		dataMappings.put("Facility Name", acquirerAnn.getFacilityname());
		dataMappings.put("Ordering Clinician", acquirerAnn.getOrderingclinician());
		dataMappings.put("Fellow", acquirerAnn.getFellow());
		dataMappings.put("Attending Clinician", acquirerAnn.getAttendingclinician());
		dataMappings.put("Referring Clinician", acquirerAnn.getReferringclinician());
		dataMappings.put("Consulting Clinician", acquirerAnn.getConsultingclinician());
		
		// Retrieve the Signal Characteristics block.  The Sampling rate and number of channels
		// information will not be gathered here, since those are being tracked elsewhere.
		
		Signalcharacteristics signalProperties = dataAnnotations.getSignalcharacteristics();
		
		dataMappings.put("Signal Resolution", signalProperties.getResolution());
		dataMappings.put("High Pass Frequency", signalProperties.getHipass());
		dataMappings.put("Low Pass Frequency", signalProperties.getLowpass());
		dataMappings.put("AC Setting", signalProperties.getAcsetting());
		dataMappings.put("Notch Filtered", signalProperties.getNotchfiltered());
		dataMappings.put("Notch Filter Frequency", signalProperties.getNotchfilterfreqs());
		dataMappings.put("Filtered ART", signalProperties.getArtfiltered());
		dataMappings.put("Acquisition Type", signalProperties.getAcquisitiontype());
		dataMappings.put("Other Acquisition Information", signalProperties.getOtheracquisitioninfo());
		dataMappings.put("Bits Per Sample", signalProperties.getBitspersample());
		dataMappings.put("Signal Offset", signalProperties.getSignaloffset());
		dataMappings.put("Signal Signed", signalProperties.getSignalsigned());
		dataMappings.put("Electrode Placement", signalProperties.getElectrodeplacement());
		dataMappings.put("Other Placement Information", signalProperties.getOtherplacementinfo());
		dataMappings.put("Number of Derived Leads", signalProperties.getDerivedleads());
		
		return dataMappings;
	}
	
	LinkedHashMap<String, Object> extractCrossleadElements(Crossleadmeasurements crossLeadAnnotations) {
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
		annotationMappings.put("Transverse QRS Clockwise Rotation", crossLeadAnnotations.getTransqrscwrot());
		annotationMappings.put("Transverse QRS Initial Angle", crossLeadAnnotations.getTransqrsinitangle());
		annotationMappings.put("Transverse QRS Initial Magnitude", crossLeadAnnotations.getTransqrsinitmag());
		annotationMappings.put("Transverse QRS Maximum Angle", crossLeadAnnotations.getTransqrsmaxangle());
		annotationMappings.put("Transverse QRS Maximum Magnitude", crossLeadAnnotations.getTransqrsmaxmag());
		annotationMappings.put("Transverse QRS Terminal Angle", crossLeadAnnotations.getTransqrstermangle());
		annotationMappings.put("Transverse QRS Terminal Magnitude", crossLeadAnnotations.getTransqrstermmag());
		annotationMappings.put("Transverse T Clockwise Rotation", crossLeadAnnotations.getTranstcwrot());
		annotationMappings.put("Transverse T Initial Angle", crossLeadAnnotations.getTranstinitangle());
		annotationMappings.put("Transverse T Initial Magnitude", crossLeadAnnotations.getTranstinitmag());
		annotationMappings.put("Transverse T Maximum Angle", crossLeadAnnotations.getTranstmaxangle());
		annotationMappings.put("Transverse T Maximum Magnitude", crossLeadAnnotations.getTranstmaxmag());
		annotationMappings.put("Transverse T Terminal Angle", crossLeadAnnotations.getTransttermangle());
		annotationMappings.put("Transverse T Terminal Magnitude", crossLeadAnnotations.getTransttermmag());
		annotationMappings.put("Frontal P Clockwise Rotation", crossLeadAnnotations.getFrontpcwrot());
		annotationMappings.put("Frontal P Initial Angle", crossLeadAnnotations.getFrontpinitangle());
		annotationMappings.put("Frontal P Initial Magnitude", crossLeadAnnotations.getFrontpinitmag());
		annotationMappings.put("Frontal P Maximum Angle", crossLeadAnnotations.getFrontpmaxangle());
		annotationMappings.put("Frontal P Maximum Magnitude", crossLeadAnnotations.getFrontpmaxmag());
		annotationMappings.put("Frontal P Terminal Angle", crossLeadAnnotations.getFrontptermangle());
		annotationMappings.put("Frontal P Terminal Magnitude", crossLeadAnnotations.getFrontptermmag());
		annotationMappings.put("Frontal QRS Clockwise Rotation", crossLeadAnnotations.getFrontqrscwrot());
		annotationMappings.put("Frontal QRS Initial Angle", crossLeadAnnotations.getFrontqrsinitangle());
		annotationMappings.put("Frontal QRS Initial Magnitude", crossLeadAnnotations.getFrontqrsinitmag());
		annotationMappings.put("Frontal QRS Maximum Angle", crossLeadAnnotations.getFrontqrsmaxangle());
		annotationMappings.put("Frontal QRS Maximum Magnitude", crossLeadAnnotations.getFrontqrsmaxmag());
		annotationMappings.put("Frontal QRS Terminal Angle", crossLeadAnnotations.getFrontqrstermangle());
		annotationMappings.put("Frontal QRS Terminal Magnitude", crossLeadAnnotations.getFrontqrstermmag());
		annotationMappings.put("Frontal T Clockwise Rotation", crossLeadAnnotations.getFronttcwrot());
		annotationMappings.put("Frontal T Initial Angle", crossLeadAnnotations.getFronttinitangle());
		annotationMappings.put("Frontal T Initial Magnitude", crossLeadAnnotations.getFronttinitmag());
		annotationMappings.put("Front T Maximum Angle", crossLeadAnnotations.getFronttmaxangle());
		annotationMappings.put("Front T Maximum Magnitude", crossLeadAnnotations.getFronttmaxmag());
		annotationMappings.put("Front T Terminal Angle", crossLeadAnnotations.getFrontttermangle());
		annotationMappings.put("Front T Terminal Magnitude", crossLeadAnnotations.getFrontttermmag());
		annotationMappings.put("Saggital P Clockwise Rotation", crossLeadAnnotations.getSagpcwrot());
		annotationMappings.put("Saggital P Initial Angle", crossLeadAnnotations.getSagpinitangle());
		annotationMappings.put("Saggital P Initial Magnitude", crossLeadAnnotations.getSagpinitmag());
		annotationMappings.put("Saggital P Maximum Angle", crossLeadAnnotations.getSagpmaxangle());
		annotationMappings.put("Saggital P Maximum Magnitude", crossLeadAnnotations.getSagpmaxmag());
		annotationMappings.put("Saggital P Terminal Angle", crossLeadAnnotations.getSagptermangle());
		annotationMappings.put("Saggital P Terminal Magnitude", crossLeadAnnotations.getSagptermmag());
		annotationMappings.put("Saggital QRS Clockwise Rotation", crossLeadAnnotations.getSagqrscwrot());
		annotationMappings.put("Saggital QRS Initial Angle", crossLeadAnnotations.getSagqrsinitangle());
		annotationMappings.put("Saggital QRS Initial Magnitude", crossLeadAnnotations.getSagqrsinitmag());
		annotationMappings.put("Saggital QRS Maximum Angle", crossLeadAnnotations.getSagqrsmaxangle());
		annotationMappings.put("Saggital QRS Maximum Magnitude", crossLeadAnnotations.getSagqrsmaxmag());
		annotationMappings.put("Saggital QRS Terminal Angle", crossLeadAnnotations.getSagqrstermangle());
		annotationMappings.put("Saggital QRS Terminal Magnitude", crossLeadAnnotations.getSagqrstermmag());
		annotationMappings.put("Saggital T Clockwise Rotation", crossLeadAnnotations.getSagtcwrot());
		annotationMappings.put("Saggital T Initial Angle", crossLeadAnnotations.getSagtinitangle());
		annotationMappings.put("Saggital T Initial Magnitude", crossLeadAnnotations.getSagtinitmag());
		annotationMappings.put("Saggital T Maximum Angle", crossLeadAnnotations.getSagtmaxangle());
		annotationMappings.put("Saggital T Maximum Magnitude", crossLeadAnnotations.getSagtmaxmag());
		annotationMappings.put("Saggital T Terminal Angle", crossLeadAnnotations.getSagttermangle());
		annotationMappings.put("Saggital T Terminal Magnitude", crossLeadAnnotations.getSagttermmag());
		
		annotationMappings.put("preexcitation", crossLeadAnnotations.getPreexcitation());
		
		// TODO:  Get beat annotations once the schema has been changed to accomdate for them
		
		annotationMappings.put("analysiserror", crossLeadAnnotations.getAnalysiserror());
		annotationMappings.put("analysiserrormessage", crossLeadAnnotations.getAnalysiserrormessage());
		
		// TODO:  Get namedmeasurements, if any.
		
		return annotationMappings;
	}
	
	LinkedHashMap<String, Object> extractGroupMeasurements(Groupmeasurement groupMeasurements) {
		LinkedHashMap<String, Object> annotationMappings = new LinkedHashMap<String, Object>();
		
		annotationMappings.put("Member Count", groupMeasurements.getMembercount());
		annotationMappings.put("Member Percent", groupMeasurements.getMemberpercent());
		annotationMappings.put("Longest Run", groupMeasurements.getLongestrun());
		annotationMappings.put("mean QRS Duration", groupMeasurements.getMeanqrsdur());
		annotationMappings.put("Minimum Ventricular Rate", groupMeasurements.getLowventrate());
		annotationMappings.put("Mean Ventricular Rate", groupMeasurements.getMeanventrate());
		annotationMappings.put("Maximum Ventricular Rate", groupMeasurements.getHighventrate());
		annotationMappings.put("Ventricular Rate Standard Deviation", groupMeasurements.getVentratestddev());
		annotationMappings.put("Mean RR Interval", groupMeasurements.getMeanrrint());
		annotationMappings.put("Atrial Rate", groupMeasurements.getAtrialrate());
		annotationMappings.put("Atrial Rate Standard Deviation", groupMeasurements.getAtrialratestddev());
		annotationMappings.put("avgpcount", groupMeasurements.getAvgpcount());
		annotationMappings.put("notavgbeats", groupMeasurements.getNotavgpbeats());
		annotationMappings.put("Minimum PR Interval", groupMeasurements.getLowprint());
		annotationMappings.put("Mean PR Interval", groupMeasurements.getMeanprint());
		annotationMappings.put("High PR Interval", groupMeasurements.getHighprint());
		annotationMappings.put("PR Interval Standard Deviation", groupMeasurements.getPrintstddev());
		annotationMappings.put("Mean PR Segment", groupMeasurements.getMeanprseg());
		annotationMappings.put("Mean QT Interval", groupMeasurements.getMeanqtint());
		annotationMappings.put("Mean QT Segment", groupMeasurements.getMeanqtseg());
		annotationMappings.put("comppausecount", groupMeasurements.getComppausecount());
		
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
