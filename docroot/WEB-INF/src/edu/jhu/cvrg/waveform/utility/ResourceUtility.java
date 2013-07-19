package edu.jhu.cvrg.waveform.utility;
/*
Copyright 2011, 2013 Johns Hopkins University Institute for Computational Medicine

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
* @author Chris Jurado
* 
*/
import java.io.IOException;
import java.util.List;

import javax.portlet.PortletPreferences;
import javax.portlet.PortletRequest;
import javax.portlet.ReadOnlyException;
import javax.portlet.ValidatorException;

import com.liferay.faces.portal.context.LiferayFacesContext;
import com.liferay.faces.util.helper.LongHelper;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.portal.model.Group;
import com.liferay.portal.model.User;
import com.liferay.portal.service.GroupLocalServiceUtil;
import com.liferay.portal.service.UserLocalServiceUtil;
import com.liferay.portal.theme.ThemeDisplay;

public class ResourceUtility {
	
	public static String getServerName(){
		LiferayFacesContext liferayFacesContext = LiferayFacesContext.getInstance();
		PortletRequest request = (PortletRequest)liferayFacesContext.getExternalContext().getRequest();
		
		String serverName = request.getServerName();
		if(request.getServerPort() > 0){
			serverName = serverName + ":" + String.valueOf(request.getServerPort());
		}
		
		return serverName;
	}
	
	public static String getStagingFolder(){
		return com.liferay.util.portlet.PortletProps.get("stagingFolder");
	}

	public static String getAnalysisServiceURL(){
		return com.liferay.util.portlet.PortletProps.get("analysisServiceURL");
//		LiferayFacesContext liferayFacesContext = LiferayFacesContext.getInstance();
//		PortletPreferences prefs = liferayFacesContext.getPortletPreferences();
//		return prefs.getValue("analysisServiceURL", "0");
	}
	
	public static String getNodeConversionService(){
		
		return com.liferay.util.portlet.PortletProps.get("nodeConversionService");
		
//		LiferayFacesContext liferayFacesContext = LiferayFacesContext.getInstance();
//		PortletPreferences prefs = liferayFacesContext.getPortletPreferences();
//		return prefs.getValue("nodeConversionService", "0");
	}
	
	public static String getStagingServiceMethod(){
		LiferayFacesContext liferayFacesContext = LiferayFacesContext.getInstance();
		PortletPreferences prefs = liferayFacesContext.getPortletPreferences();
		return prefs.getValue("stagingServiceMethod", "0");
	}

	public static String getStagingService(){
		LiferayFacesContext liferayFacesContext = LiferayFacesContext.getInstance();
		PortletPreferences prefs = liferayFacesContext.getPortletPreferences();
		return prefs.getValue("stagingService", "0");
	}
	
	public static String getFtpHost(){
		
		return com.liferay.util.portlet.PortletProps.get("ftpHost");
		
//		LiferayFacesContext liferayFacesContext = LiferayFacesContext.getInstance();
//		PortletPreferences prefs = liferayFacesContext.getPortletPreferences();
//		return prefs.getValue("ftpHost", "0");
	}
	
	public static String getFtpUser(){
		
		return com.liferay.util.portlet.PortletProps.get("ftpUser");
		
//		LiferayFacesContext liferayFacesContext = LiferayFacesContext.getInstance();
//		PortletPreferences prefs = liferayFacesContext.getPortletPreferences();
//		return prefs.getValue("ftpUser", "0");
	}
	
	public static String getFtpPassword(){
		
		return com.liferay.util.portlet.PortletProps.get("ftpPassword");
		
//		LiferayFacesContext liferayFacesContext = LiferayFacesContext.getInstance();
//		PortletPreferences prefs = liferayFacesContext.getPortletPreferences();
//		return prefs.getValue("ftpPassword", "0");
	}
	
	public static String getFtpRoot(){
		
		return com.liferay.util.portlet.PortletProps.get("ftpRoot");
		
//		LiferayFacesContext liferayFacesContext = LiferayFacesContext.getInstance();
//		PortletPreferences prefs = liferayFacesContext.getPortletPreferences();
//		return prefs.getValue("ftpRoot", "0");
	}
	
	public static long getPrefFolderId(){
		LiferayFacesContext liferayFacesContext = LiferayFacesContext.getInstance();
		PortletPreferences prefs = liferayFacesContext.getPortletPreferences();
		return Long.valueOf(prefs.getValue("folderid", "0"));
	}
	
	public static long getLocalDownloadFolder(){
		LiferayFacesContext liferayFacesContext = LiferayFacesContext.getInstance();
		PortletPreferences prefs = liferayFacesContext.getPortletPreferences();
		return Long.valueOf(prefs.getValue("localDownloadFolder", "0"));
	}	
	
	public static long getPrefSurveyId(){
		LiferayFacesContext liferayFacesContext = LiferayFacesContext.getInstance();
		PortletPreferences prefs = liferayFacesContext.getPortletPreferences();
		return Long.valueOf(prefs.getValue("surveyid", "0"));
	}
	
	public static long getReportPrefSurveyId(){
		LiferayFacesContext liferayFacesContext = LiferayFacesContext.getInstance();
		PortletPreferences prefs = liferayFacesContext.getPortletPreferences();
		return Long.valueOf(prefs.getValue("reportSurveyid", "0"));
	}
	
	public static long getReportPrefFolderId(){
		LiferayFacesContext liferayFacesContext = LiferayFacesContext.getInstance();
		PortletPreferences prefs = liferayFacesContext.getPortletPreferences();
		return Long.valueOf(prefs.getValue("reportFolderid", "0"));
	}	

	public static void savePreferences(long surveyId){
		System.out.println("In ResourceUtility, saving Survey " + surveyId);
		storePrefs("surveyid", String.valueOf(surveyId));
	}

	private static void storePrefs(String prefName, String prefValue){
		LiferayFacesContext liferayFacesContext = LiferayFacesContext.getInstance();
		PortletPreferences prefs = liferayFacesContext.getPortletPreferences();
		try {
			prefs.setValue(prefName, prefValue);
			prefs.store();
		} catch (ReadOnlyException e) {
			printErrorMessage("Resource Utility");
			e.printStackTrace();
		} catch (ValidatorException e) {
			printErrorMessage("Resource Utility");
			e.printStackTrace();
		} catch (IOException e) {
			printErrorMessage("Resource Utility");
			e.printStackTrace();
		}
	}
	
	public static void printErrorMessage(String source){
		System.err.println("*************************** Error in " + source + " ******************************");
	}
	
	public static long getIdParameter(String param){
		LiferayFacesContext liferayFacesContext = LiferayFacesContext.getInstance();
		return LongHelper.toLong(liferayFacesContext.getExternalContext().getRequestParameterMap().get(param), 0L);
	}
	
	public static User getUser(long userId){
		User user = null;
		try {
			user = UserLocalServiceUtil.getUser(userId);
		} catch (PortalException e) {
			printErrorMessage("Resource Utility");
			e.printStackTrace();
		} catch (SystemException e) {
			printErrorMessage("Resource Utility");
			e.printStackTrace();
		}
		return user;
	}
	
	public static long getCurrentGroupId(){	
		LiferayFacesContext liferayFacesContext = LiferayFacesContext.getInstance();
		PortletRequest request = (PortletRequest)liferayFacesContext.getExternalContext().getRequest();
		ThemeDisplay themeDisplay = (ThemeDisplay)request.getAttribute(WebKeys.THEME_DISPLAY);
		return themeDisplay.getLayout().getGroupId();	
	}
	
	public static User getCurrentUser(){
		LiferayFacesContext liferayFacesContext = LiferayFacesContext.getInstance();
		User currentUser = null;
		try {
			currentUser = UserLocalServiceUtil.getUser(Long.parseLong(liferayFacesContext.getPortletRequest().getRemoteUser()));
		} catch (NumberFormatException e) {
			printErrorMessage("Resource Utility");
			e.printStackTrace();
		} catch (PortalException e) {
			printErrorMessage("Resource Utility");
			e.printStackTrace();
		} catch (SystemException e) {
			printErrorMessage("Resource Utility");
			e.printStackTrace();
		}
		return currentUser;
	}

	public static long getCurrentUserId(){
		return getCurrentUser().getUserId();
	}
	
	public static boolean isUserCommunityMember(long userId, long communityId){
		
		try {
			List<Group> userGroups = GroupLocalServiceUtil.getUserGroups(userId);
			
			for(Group group : userGroups){
				if(group.getGroupId() == communityId){
					return true;
				}
			}
		} catch (PortalException e) {
			printErrorMessage("Resource Utility");
			e.printStackTrace();
		} catch (SystemException e) {
			printErrorMessage("Resource Utility");
			e.printStackTrace();
		}
		return false;
	}
	
	public static long getGroupId(String communityName){
		long groupId = 0L;
		List<Group> groupList;
		try {
			groupList = GroupLocalServiceUtil.getGroups(0, GroupLocalServiceUtil.getGroupsCount());;
			for(Group group : groupList){
				if(group.getName().equals(communityName)){
					groupId = group.getGroupId();
				}
			}
		} catch (SystemException e) {
			printErrorMessage("Resource Utility");
			e.printStackTrace();
		}

		return groupId;
	}
	
	public static String convertToUserName(long userId){
		String userFullName = "";
		
		try {
			User user = UserLocalServiceUtil.getUser(userId);
			userFullName = user.getFullName();
		} catch (PortalException e) {
			printErrorMessage("Resource Utility");
			e.printStackTrace();
		} catch (SystemException e) {
			printErrorMessage("Resource Utility");
			e.printStackTrace();
		}
		
		return userFullName;
	}

}
