package edu.jhu.cvrg.waveform.callbacks;
/*
Copyright 2013 Johns Hopkins University Institute for Computational Medicine

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/
/**
* @author Chris Jurado, Mike Shipway
* 
*/
import java.util.Map;

import org.apache.axiom.om.OMElement;
import org.apache.axis2.client.async.AxisCallback;
import org.apache.axis2.context.MessageContext;

import edu.jhu.cvrg.waveform.utility.ServerUtility;
import edu.jhu.cvrg.waveform.utility.WebServiceUtility;

public abstract class SvcAxisCallback implements AxisCallback{
	protected boolean verbose = false;
	protected ServerUtility util = new ServerUtility(verbose);

	public void onComplete() {

	}

	public void onFault(MessageContext msgContext) {

	}

	public void onMessage(MessageContext msgContext) {
		
		OMElement omMessage = msgContext.getEnvelope().getBody().getFirstElement();
		
		Map<String, Object> paramMap = WebServiceUtility.buildParamMap(omMessage);
		String sJobID = (String) paramMap.get("jobID");
		
		completeProcess(paramMap, sJobID);
	}
	
	protected abstract void completeProcess(Map<String, Object> paramMap, String sJobID);
	

	public void onError(Exception ex) {

	}	
}
