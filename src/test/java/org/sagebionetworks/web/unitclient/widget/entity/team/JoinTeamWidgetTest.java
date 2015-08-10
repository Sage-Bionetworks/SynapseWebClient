package org.sagebionetworks.web.unitclient.widget.entity.team;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.sagebionetworks.repo.model.ACTAccessRequirement;
import org.sagebionetworks.repo.model.AccessApproval;
import org.sagebionetworks.repo.model.AccessRequirement;
import org.sagebionetworks.repo.model.PostMessageContentAccessRequirement;
import org.sagebionetworks.repo.model.TeamMembershipStatus;
import org.sagebionetworks.repo.model.TermsOfUseAccessRequirement;
import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.repo.model.UserSessionData;
import org.sagebionetworks.schema.adapter.JSONObjectAdapter;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.schema.adapter.org.json.JSONObjectAdapterImpl;
import org.sagebionetworks.web.client.GWTWrapper;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.PlaceChanger;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.entity.MarkdownWidget;
import org.sagebionetworks.web.client.widget.team.JoinTeamWidget;
import org.sagebionetworks.web.client.widget.team.JoinTeamWidgetView;
import org.sagebionetworks.web.shared.WebConstants;
import org.sagebionetworks.web.shared.WidgetConstants;
import org.sagebionetworks.web.shared.WikiPageKey;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;
import org.sagebionetworks.web.unitclient.widget.entity.EvaluationSubmitterTest;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;

public class JoinTeamWidgetTest {

	SynapseClientAsync mockSynapseClient;
	GlobalApplicationState mockGlobalApplicationState;
	JoinTeamWidgetView mockView;
	String teamId = "123";
	JoinTeamWidget joinWidget;
	AuthenticationController mockAuthenticationController;
	Callback mockTeamUpdatedCallback;
	JSONObjectAdapter jsonObjectAdapter;
	GWTWrapper mockGwt;
	PlaceChanger mockPlaceChanger;
	List<AccessRequirement> ars;
	UserProfile currentUserProfile;
	MarkdownWidget mockWikiPageWidget;
	
	@Before
	public void before() throws JSONObjectAdapterException {
		mockGlobalApplicationState = mock(GlobalApplicationState.class);
		mockSynapseClient = mock(SynapseClientAsync.class);
		mockView = mock(JoinTeamWidgetView.class);
		mockAuthenticationController = mock(AuthenticationController.class);
		mockTeamUpdatedCallback = mock(Callback.class);
		mockGwt = mock(GWTWrapper.class);
		mockWikiPageWidget = mock(MarkdownWidget.class);
		jsonObjectAdapter = new JSONObjectAdapterImpl();
		
		mockPlaceChanger = mock(PlaceChanger.class);
        when(mockGlobalApplicationState.getPlaceChanger()).thenReturn(mockPlaceChanger);
        mockAuthenticationController = mock(AuthenticationController.class);
        when(mockAuthenticationController.isLoggedIn()).thenReturn(true);
        UserSessionData currentUser = new UserSessionData();                
        currentUserProfile = new UserProfile();
        currentUserProfile.setOwnerId("1");
        currentUser.setProfile(currentUserProfile);
        when(mockAuthenticationController.getCurrentUserSessionData()).thenReturn(currentUser);
        ars = new ArrayList<AccessRequirement>();
        AsyncMockStubber.callSuccessWith(true).when(mockSynapseClient).hasAccess(anyString(), anyString(), anyString(), any(AsyncCallback.class));
        AsyncMockStubber.callSuccessWith(ars).when(mockSynapseClient).getTeamAccessRequirements(anyString(), any(AsyncCallback.class));
        
		
		joinWidget = new JoinTeamWidget(mockView, mockSynapseClient, mockGlobalApplicationState, mockAuthenticationController, jsonObjectAdapter, mockGwt, mockWikiPageWidget);
		TeamMembershipStatus status = new TeamMembershipStatus();
		status.setHasOpenInvitation(false);
		status.setCanJoin(false);
		status.setHasOpenRequest(false);
		status.setIsMember(false);
		joinWidget.configure(teamId, false, status, mockTeamUpdatedCallback, null, null, null, null, false);
		
		AsyncMockStubber.callSuccessWith(null).when(mockSynapseClient).deleteOpenMembershipRequests(anyString(), anyString(), any(AsyncCallback.class));
		AsyncMockStubber.callSuccessWith(null).when(mockSynapseClient).requestMembership(anyString(), anyString(), anyString(), anyString(), any(AsyncCallback.class));
		
		AsyncMockStubber.callSuccessWith(null).when(mockSynapseClient).createAccessApproval(any(AccessApproval.class), any(AsyncCallback.class));
		when(mockGwt.getHostPageBaseURL()).thenReturn(EvaluationSubmitterTest.HOST_PAGE_URL);
	}

	
	@Test
	public void testSimpleRequestButton() throws Exception {
		reset(mockView);
		TeamMembershipStatus status = new TeamMembershipStatus();
		String isMemberMessage = "already a member";
		String successMessage = "successfully joined";
		String buttonText = "join a team";
		String openRequestText = "you have an open request.";
		boolean isChallenge = false;
		boolean isSimpleRequestButton = true;
		joinWidget.configure(teamId, isChallenge, status, mockTeamUpdatedCallback, isMemberMessage, successMessage, buttonText, openRequestText, isSimpleRequestButton);
		verify(mockView).configure(true, status, isMemberMessage, buttonText, openRequestText, isSimpleRequestButton);
	}
	
	@Test
	public void testNormalRequestButton() throws Exception {
		reset(mockView);
		TeamMembershipStatus status = new TeamMembershipStatus();
		String isMemberMessage = "already a member";
		String successMessage = "successfully joined";
		String buttonText = "join a team";
		String openRequestText = "you have an open request.";
		boolean isChallenge = true;
		boolean isSimpleRequestButton = false;
		joinWidget.configure(teamId, isChallenge, status, mockTeamUpdatedCallback, isMemberMessage, successMessage, buttonText, openRequestText, isSimpleRequestButton);
		verify(mockView).configure(true, status, isMemberMessage, buttonText, openRequestText, isSimpleRequestButton);
	}
	
	@Test
	public void testJoinRequestStep1() throws Exception {
		WikiPageKey challengeInfoKey = mock(WikiPageKey.class);
		joinWidget.sendJoinRequestStep0();
		joinWidget.sendJoinRequestStep1(challengeInfoKey);
		verify(mockView).showChallengeInfoPage(any(UserProfile.class), eq(challengeInfoKey), any(Callback.class));
		verify(mockView, times(2)).setButtonsEnabled(anyBoolean());
	}
	

	@SuppressWarnings("unchecked")
	@Test
	public void testJoinRequestStep2WithRestriction() throws Exception {
		TermsOfUseAccessRequirement terms = new TermsOfUseAccessRequirement();
		terms.setTermsOfUse("terms have been set");
		ars.add(terms);
        
        joinWidget.sendJoinRequestStep0();
		verify(mockSynapseClient).getTeamAccessRequirements(anyString(), any(AsyncCallback.class));
		verify(mockView).showTermsOfUseAccessRequirement(anyString(), any(Callback.class));
	}
	
	@Test
	public void testJoinRequestStep2WithNullRestrictionText() throws Exception {
		TermsOfUseAccessRequirement terms = new TermsOfUseAccessRequirement();
		terms.setId(1L);
		ars.add(terms);
		joinWidget.sendJoinRequestStep0();
		verify(mockSynapseClient).getTeamAccessRequirements(anyString(), any(AsyncCallback.class));
		verify(mockWikiPageWidget).loadMarkdownFromWikiPage(any(WikiPageKey.class),eq(true), eq(true));
		verify(mockView).showWikiAccessRequirement(any(Widget.class), any(Callback.class));
	}

	
	@SuppressWarnings("unchecked")
	@Test
	public void testJoinRequestStep2WithACTRestriction() throws Exception {
		ars.add(new ACTAccessRequirement());
        
        joinWidget.sendJoinRequestStep0();
		verify(mockSynapseClient).getTeamAccessRequirements(anyString(), any(AsyncCallback.class));
		verify(mockView).showACTAccessRequirement(anyString(), any(Callback.class));
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testJoinRequestStep2NoRestriction() throws Exception {
		joinWidget.sendJoinRequestStep0();
		verify(mockSynapseClient).getTeamAccessRequirements(anyString(), any(AsyncCallback.class));
		//no ARs shown...
		verify(mockView, times(0)).showTermsOfUseAccessRequirement(anyString(), any(Callback.class));
		verify(mockSynapseClient).requestMembership(anyString(), anyString(), anyString(), eq(EvaluationSubmitterTest.HOST_PAGE_URL), any(AsyncCallback.class));
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testGetTotalPageCount() throws Exception {
		boolean isChallengeSignup = true;
        joinWidget.configure(teamId, isChallengeSignup, null, mockTeamUpdatedCallback, null, null, null, null, false);
        joinWidget.sendJoinRequestStep0();
        //one page for the AR
        Assert.assertEquals(1, joinWidget.getTotalPageCount());
        
        isChallengeSignup = false;
        joinWidget.configure(teamId, isChallengeSignup, null, mockTeamUpdatedCallback, null, null, null, null, false);
        joinWidget.sendJoinRequestStep0();
        //0 pages
        Assert.assertEquals(0, joinWidget.getTotalPageCount());
		

        //Now test with an access requirement.  First as part of a challenge, then outside of challenge 
        TermsOfUseAccessRequirement terms = new TermsOfUseAccessRequirement();
        terms.setTermsOfUse("terms of use");
        ars.add(terms);
        
        isChallengeSignup = true;
        joinWidget.configure(teamId, isChallengeSignup, null, mockTeamUpdatedCallback, null, null, null, null, false);
        joinWidget.sendJoinRequestStep0();
        //One page for challenge info, one page for the AR
        Assert.assertEquals(2, joinWidget.getTotalPageCount());
        
        isChallengeSignup = false;
        joinWidget.configure(teamId, isChallengeSignup, null, mockTeamUpdatedCallback, null, null, null, null, false);
        joinWidget.sendJoinRequestStep0();
        //One page for challenge info
        Assert.assertEquals(1, joinWidget.getTotalPageCount());
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testJoinRequestStep2Failure() throws Exception {
		AsyncMockStubber.callFailureWith(new Exception("unhandled exception")).when(mockSynapseClient).getTeamAccessRequirements(anyString(), any(AsyncCallback.class));
		joinWidget.sendJoinRequestStep0();
		joinWidget.sendJoinRequestStep2();
		verify(mockSynapseClient).getTeamAccessRequirements(anyString(), any(AsyncCallback.class));
		verify(mockView).showErrorMessage(anyString());
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testJoinRequestStep3() throws Exception {
		joinWidget.sendJoinRequestStep3();
		verify(mockSynapseClient).requestMembership(anyString(), anyString(), anyString(), eq(EvaluationSubmitterTest.HOST_PAGE_URL), any(AsyncCallback.class));
		verify(mockView).showInfo(anyString(), anyString());
		//verify that team updated callback is invoked
		verify(mockTeamUpdatedCallback).invoke();
	}

	
	@SuppressWarnings("unchecked")
	@Test
	public void testJoinRequestStep3Failure() throws Exception {
		AsyncMockStubber.callFailureWith(new Exception("unhandled exception")).when(mockSynapseClient).requestMembership(anyString(), anyString(),anyString(), anyString(), any(AsyncCallback.class));
		joinWidget.sendJoinRequest("", false);
		verify(mockSynapseClient).requestMembership(anyString(), anyString(), anyString(), eq(EvaluationSubmitterTest.HOST_PAGE_URL), any(AsyncCallback.class));
		verify(mockView).showErrorMessage(anyString());
	}
	@SuppressWarnings("unchecked")
	@Test
	public void testJoinRequestStep3WikiRefresh() throws Exception {
		//configure using wiki widget renderer version
		Map<String, String> descriptor = new HashMap<String, String>();
		descriptor.put(WidgetConstants.JOIN_WIDGET_TEAM_ID_KEY, teamId);
		descriptor.put(WidgetConstants.JOIN_WIDGET_SHOW_PROFILE_FORM_KEY, Boolean.TRUE.toString());
		Callback mockWidgetRefreshRequired = mock(Callback.class);
		joinWidget.configure(null, descriptor, mockWidgetRefreshRequired, null);
		joinWidget.sendJoinRequestStep3();
		verify(mockSynapseClient).requestMembership(anyString(), anyString(), anyString(), eq(EvaluationSubmitterTest.HOST_PAGE_URL), any(AsyncCallback.class));
		verify(mockView).showInfo(anyString(), anyString());
		//verify that wiki page refresh is invoked
		verify(mockWidgetRefreshRequired).invoke();
	}
	
	@Test
	public void testShowChallengeInfoParam() throws Exception {
		Map<String, String> descriptor = new HashMap<String, String>();
		descriptor.put(WidgetConstants.JOIN_WIDGET_TEAM_ID_KEY, teamId);
		descriptor.put(WidgetConstants.JOIN_WIDGET_SHOW_PROFILE_FORM_KEY, Boolean.TRUE.toString());
		Callback mockWidgetRefreshRequired = mock(Callback.class);
		joinWidget.configure(null, descriptor, mockWidgetRefreshRequired, null);
		Assert.assertTrue(joinWidget.isChallengeSignup());
		
		descriptor.put(WidgetConstants.JOIN_WIDGET_SHOW_PROFILE_FORM_KEY, Boolean.FALSE.toString());
		joinWidget.configure(null, descriptor, mockWidgetRefreshRequired, null);
		Assert.assertFalse(joinWidget.isChallengeSignup());
		
		//if both params are defined, the new param is used
		descriptor.put(WebConstants.JOIN_WIDGET_IS_CHALLENGE_KEY, Boolean.TRUE.toString());
		joinWidget.configure(null, descriptor, mockWidgetRefreshRequired, null);
		Assert.assertTrue(joinWidget.isChallengeSignup());
		
		descriptor.remove(WidgetConstants.JOIN_WIDGET_SHOW_PROFILE_FORM_KEY);
		descriptor.put(WebConstants.JOIN_WIDGET_IS_CHALLENGE_KEY, Boolean.FALSE.toString());
		joinWidget.configure(null, descriptor, mockWidgetRefreshRequired, null);
		Assert.assertFalse(joinWidget.isChallengeSignup());
	}

	@Test
	public void testSetLicenseAccepted() throws Exception {
		//single ToU
		TermsOfUseAccessRequirement ar = new TermsOfUseAccessRequirement();
		ar.setTermsOfUse("some terms");
		ars.add(ar);
		joinWidget.sendJoinRequestStep0();
		//verify we showTermsOfUseAccessRequirement
		ArgumentCaptor<Callback> callbackArg = ArgumentCaptor.forClass(Callback.class);
		verify(mockView).showTermsOfUseAccessRequirement(anyString(), callbackArg.capture());
		
		//manually invoke the okbutton callback
		callbackArg.getValue().invoke();
		
        //it should try to sign this type of ar
        verify(mockSynapseClient).createAccessApproval(any(AccessApproval.class), any(AsyncCallback.class));
        verify(mockView).hideJoinWizard();
	}
	
	@Test
	public void testSetLicenseAcceptedACT() throws Exception {
		//single ToU
		AccessRequirement ar = new ACTAccessRequirement();
		ars.add(ar);
		joinWidget.sendJoinRequestStep0();
		//verify we showTermsOfUseAccessRequirement
		ArgumentCaptor<Callback> callbackArg = ArgumentCaptor.forClass(Callback.class);
		verify(mockView).showACTAccessRequirement(anyString(), callbackArg.capture());
		
		//manually invoke the okbutton callback
		callbackArg.getValue().invoke();
				
        //verify it does not try to sign, we just continue
        verify(mockSynapseClient, never()).createAccessApproval(any(AccessApproval.class), any(AsyncCallback.class));
        verify(mockView).hideJoinWizard();
	}

	
	@Test
	public void testSetLicenseAcceptedPostMessage() throws Exception {
		//single ToU
		AccessRequirement ar = new PostMessageContentAccessRequirement();
		ars.add(ar);
		joinWidget.sendJoinRequestStep0();
		//verify we showTermsOfUseAccessRequirement
		ArgumentCaptor<Callback> callbackArg = ArgumentCaptor.forClass(Callback.class);
		verify(mockView).showPostMessageContentAccessRequirement(anyString(), callbackArg.capture());
		
		//manually invoke the okbutton callback
		callbackArg.getValue().invoke();
				
        //verify it does not try to sign, we just continue
        verify(mockSynapseClient).createAccessApproval(any(AccessApproval.class), any(AsyncCallback.class));
        verify(mockView).hideJoinWizard();
	}
	
	@Test
	public void testSetLicenseAcceptedInvalidType() throws Exception {
		//single ToU
		AccessRequirement ar = mock(AccessRequirement.class);
		ars.add(ar);
		joinWidget.sendJoinRequestStep0();
		//verify we show an error for an unrecognized ar
		verify(mockView).showErrorMessage(anyString());
	}
	
	@Test
	public void testIsRecognizedSite() {
		if (JoinTeamWidget.EXTRA_INFO_URL_WHITELIST.length > 0) {
			String url = JoinTeamWidget.EXTRA_INFO_URL_WHITELIST[0];
			//test the whitelist
			//recognize project datasphere
			//verify case insensitive
			assertTrue(JoinTeamWidget.isRecognizedSite(url.toUpperCase()));
			assertTrue(JoinTeamWidget.isRecognizedSite(url.toLowerCase()));
			
			//but not other sites
			assertFalse(JoinTeamWidget.isRecognizedSite("http://mpmdev.ondemand.sas.com/projectdatasphere/html/registration/challenge")); //not https
			assertFalse(JoinTeamWidget.isRecognizedSite("https://www.jayhodgson.com/projectdatasphere/"));
		}
	}
	
	@Test
	public void testenhancePostMessageUrl() {
		if (JoinTeamWidget.EXTRA_INFO_URL_WHITELIST.length > 0) {
			String url = JoinTeamWidget.EXTRA_INFO_URL_WHITELIST[0];
			String firstName = "Luke";
			String lastName = "Skywalker";
			String ownerId = "628";
			List<String> emails = new ArrayList<String>();
			String email = "MidichloriansSaturation@bigfoot.com";
			emails.add(email);
			when(mockGwt.encodeQueryString(email)).thenReturn(email);
			when(mockGwt.encodeQueryString(firstName)).thenReturn(firstName);
			when(mockGwt.encodeQueryString(lastName)).thenReturn(lastName);
			when(mockGwt.encodeQueryString(ownerId)).thenReturn(ownerId);
			
			currentUserProfile.setFirstName(firstName);
			currentUserProfile.setLastName(lastName);
			currentUserProfile.setOwnerId(ownerId);
			currentUserProfile.setEmails(emails);
			
			//test setup configures us as logged in
			String enhancedUrl = joinWidget.enhancePostMessageUrl(url);
			assertTrue(enhancedUrl.contains(firstName));
			assertTrue(enhancedUrl.contains(lastName));
			assertTrue(enhancedUrl.contains(ownerId));
			assertTrue(enhancedUrl.contains(email));
		}
	}
	
	@Test
	public void testEnhancePostMessageUrlNotLoggedIn() {
		if (JoinTeamWidget.EXTRA_INFO_URL_WHITELIST.length > 0) { 
			//test setup configures us as logged in
			String url = JoinTeamWidget.EXTRA_INFO_URL_WHITELIST[0];
			when(mockAuthenticationController.isLoggedIn()).thenReturn(false);
			assertEquals(url, joinWidget.enhancePostMessageUrl(url));
		}
	}

	@Test
	public void testGetEncodedParamValueIfDefined() {
		//return the param key if null value
		assertEquals(WebConstants.USER_ID_PARAM+"=&", joinWidget.getEncodedParamValue(WebConstants.USER_ID_PARAM, null, "&"));
		//or empty string value
		assertEquals(WebConstants.USER_ID_PARAM+"=&", joinWidget.getEncodedParamValue(WebConstants.USER_ID_PARAM, "", "&"));
		
		//if defined, return <paramkey>=<url-encoded-value><suffix>
		when(mockGwt.encodeQueryString("bar")).thenReturn("encodedbar");
		assertEquals(WebConstants.USER_ID_PARAM+"=encodedbar&", joinWidget.getEncodedParamValue(WebConstants.USER_ID_PARAM, "bar", "&"));
	}
	
}
