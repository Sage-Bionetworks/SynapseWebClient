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
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.repo.model.ACTAccessRequirement;
import org.sagebionetworks.repo.model.AccessApproval;
import org.sagebionetworks.repo.model.AccessRequirement;
import org.sagebionetworks.repo.model.ManagedACTAccessRequirement;
import org.sagebionetworks.repo.model.TeamMembershipStatus;
import org.sagebionetworks.repo.model.TermsOfUseAccessRequirement;
import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.repo.model.UserSessionData;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.GWTWrapper;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.PlaceChanger;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.cookie.CookieProvider;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.entity.MarkdownWidget;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.client.widget.team.JoinTeamWidget;
import org.sagebionetworks.web.client.widget.team.JoinTeamWidgetView;
import org.sagebionetworks.web.client.widget.team.WizardProgressWidget;
import org.sagebionetworks.web.shared.WebConstants;
import org.sagebionetworks.web.shared.WidgetConstants;
import org.sagebionetworks.web.shared.WikiPageKey;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;
import org.sagebionetworks.web.unitclient.widget.entity.EvaluationSubmitterTest;

import com.google.gwt.user.client.rpc.AsyncCallback;

public class JoinTeamWidgetTest {

	SynapseClientAsync mockSynapseClient;
	GlobalApplicationState mockGlobalApplicationState;
	JoinTeamWidgetView mockView;
	String teamId = "123";
	JoinTeamWidget joinWidget;
	AuthenticationController mockAuthenticationController;
	Callback mockTeamUpdatedCallback;
	GWTWrapper mockGwt;
	PlaceChanger mockPlaceChanger;
	List<AccessRequirement> ars;
	UserProfile currentUserProfile;
	MarkdownWidget mockWikiPageWidget;
	WizardProgressWidget mockWizardProgress;
	TeamMembershipStatus status;
	@Mock
	CookieProvider mockCookies;
	@Mock
	SynapseAlert mockSynAlert;
	
	@Before
	public void before() throws JSONObjectAdapterException {
		MockitoAnnotations.initMocks(this);
		mockGlobalApplicationState = mock(GlobalApplicationState.class);
		mockSynapseClient = mock(SynapseClientAsync.class);
		mockView = mock(JoinTeamWidgetView.class);
		mockAuthenticationController = mock(AuthenticationController.class);
		mockTeamUpdatedCallback = mock(Callback.class);
		mockGwt = mock(GWTWrapper.class);
		mockWikiPageWidget = mock(MarkdownWidget.class);
		mockWizardProgress = mock(WizardProgressWidget.class);
        mockAuthenticationController = mock(AuthenticationController.class);
		mockPlaceChanger = mock(PlaceChanger.class);

        currentUserProfile = new UserProfile();
        ars = new ArrayList<AccessRequirement>();
        
        currentUserProfile.setOwnerId("1");
        when(mockAuthenticationController.getCurrentUserProfile()).thenReturn(currentUserProfile);
        when(mockGlobalApplicationState.getPlaceChanger()).thenReturn(mockPlaceChanger);
        when(mockAuthenticationController.isLoggedIn()).thenReturn(true);
        AsyncMockStubber.callSuccessWith(true).when(mockSynapseClient).hasAccess(anyString(), anyString(), anyString(), any(AsyncCallback.class));
        AsyncMockStubber.callSuccessWith(ars).when(mockSynapseClient).getTeamAccessRequirements(anyString(), any(AsyncCallback.class));
        
		
		joinWidget = new JoinTeamWidget(mockView, mockSynapseClient, mockGlobalApplicationState, mockAuthenticationController, mockGwt, mockWikiPageWidget, mockWizardProgress,
				mockSynAlert, mockCookies);
		status = new TeamMembershipStatus();
		status.setHasOpenInvitation(false);
		status.setCanJoin(false);
		status.setHasOpenRequest(false);
		status.setIsMember(false);
		status.setMembershipApprovalRequired(false);
		status.setHasUnmetAccessRequirement(false);
		joinWidget.configure(teamId, false, status, mockTeamUpdatedCallback, null, null, null, null, false);
		
		AsyncMockStubber.callSuccessWith(status).when(mockSynapseClient).requestMembership(anyString(), anyString(), anyString(), anyString(), any(Date.class), any(AsyncCallback.class));
		
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
//		joinWidget.configure(teamId, isChallenge, status, mockTeamUpdatedCallback, isMemberMessage, successMessage, buttonText, openRequestText, isSimpleRequestButton);
//		verify(mockView).configure(true, status, isMemberMessage, buttonText, openRequestText, isSimpleRequestButton);
	}
	
	
	@Test
	public void testNormalRequestButton() throws Exception {
		reset(mockView);
		String isMemberMessage = "already a member";
		String successMessage = "successfully joined";
		String buttonText = "join a team";
		String openRequestText = "you have an open request.";
		boolean isChallenge = true;
		boolean isSimpleRequestButton = false;
		joinWidget.configure(teamId, isChallenge, status, mockTeamUpdatedCallback, isMemberMessage, successMessage, buttonText, openRequestText, isSimpleRequestButton);
//		verify(mockView).configure(true, status, isMemberMessage, buttonText, openRequestText, isSimpleRequestButton);
	}
	
	
	@Test
	public void testJoinRequestStep1() throws Exception {
		WikiPageKey challengeInfoKey = mock(WikiPageKey.class);
		joinWidget.sendJoinRequestStep0();
		verify(mockView).setAccessRequirementsLinkVisible(false);
		verify(mockView, never()).setAccessRequirementsLinkVisible(true);
		joinWidget.sendJoinRequestStep1(challengeInfoKey);
		verify(mockView).setJoinWizardCallback(any(Callback.class));
		verify(mockWikiPageWidget).loadMarkdownFromWikiPage(any(WikiPageKey.class),eq(false));
		verify(mockView).setCurrentWizardContent(mockWikiPageWidget);
		verify(mockView, times(2)).setButtonsEnabled(anyBoolean());
	}
	

	@Test
	public void testInvitedToTeamWithAccessRequirements() throws Exception {
		// set up the team membership status so that the current user has been invited to a team (that requires membership approval) that has unmet access requirements.
		reset(mockView);
		TeamMembershipStatus mockStatus = mock(TeamMembershipStatus.class);
		when(mockStatus.getIsMember()).thenReturn(false);
		when(mockStatus.getCanJoin()).thenReturn(false);
		when(mockStatus.getHasOpenRequest()).thenReturn(false);
		when(mockStatus.getMembershipApprovalRequired()).thenReturn(true);
		when(mockStatus.getHasOpenInvitation()).thenReturn(true);
		when(mockStatus.getHasUnmetAccessRequirement()).thenReturn(true);
		
		String isMemberMessage = "already a member";
		String successMessage = "successfully joined";
		String buttonText = "join a team";
		String openRequestText = "you have an open request.";
		boolean isChallenge = false;
		boolean isSimpleRequestButton = true;
		joinWidget.configure(teamId, isChallenge, mockStatus, mockTeamUpdatedCallback, isMemberMessage, successMessage, buttonText, openRequestText, isSimpleRequestButton);
		
		verify(mockView).setJoinButtonsText(buttonText);
		verify(mockView).setRequestOpenText(openRequestText);
		verify(mockView).setUserPanelVisible(true);
		verify(mockView).setAcceptInviteButtonVisible(true);
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testJoinRequestStep2WithRestriction() throws Exception {
		String termsText = "terms have been set";
		TermsOfUseAccessRequirement terms = new TermsOfUseAccessRequirement();
		terms.setTermsOfUse(termsText);
		ars.add(terms);
        joinWidget.sendJoinRequestStep0();
		verify(mockSynapseClient).getTeamAccessRequirements(anyString(), any(AsyncCallback.class));
		verify(mockView).setAccessRequirementHTML(termsText);
		verify(mockView).setJoinWizardCallback(any(Callback.class));
		verify(mockView).setCurrentWizardPanelVisible(false);
		verify(mockView).setJoinWizardPrimaryButtonText("Accept");
	}
	
	

	@Test
	public void testJoinRequestStep2WithNullRestrictionText() throws Exception {
		TermsOfUseAccessRequirement terms = new TermsOfUseAccessRequirement();
		terms.setId(1L);
		ars.add(terms);
		joinWidget.sendJoinRequestStep0();
		verify(mockView).setButtonsEnabled(true);
		verify(mockView).showJoinWizard();
		verify(mockWizardProgress).configure(Mockito.anyInt(), Mockito.anyInt());
		verify(mockSynapseClient).getTeamAccessRequirements(anyString(), any(AsyncCallback.class));		
		verify(mockView).setAccessRequirementHTML("");
		verify(mockView).setJoinWizardCallback(any(Callback.class));
		verify(mockView).setCurrentWizardPanelVisible(true);
		verify(mockView).setJoinWizardPrimaryButtonText("Accept");
		verify(mockView, times(2)).setAccessRequirementsLinkVisible(false);
		verify(mockView).setCurrentWizardContent(mockWikiPageWidget);
		verify(mockWikiPageWidget).loadMarkdownFromWikiPage(any(WikiPageKey.class),eq(true));
	}
	
	@Test
	public void testJoinRequestStep2WithNullActRestrictionText() throws Exception {
		ACTAccessRequirement terms = new ACTAccessRequirement();
		terms.setId(1L);
		ars.add(terms);
		joinWidget.sendJoinRequestStep0();
		verify(mockView).setButtonsEnabled(true);
		verify(mockView).showJoinWizard();
		verify(mockWizardProgress).configure(Mockito.anyInt(), Mockito.anyInt());
		verify(mockSynapseClient).getTeamAccessRequirements(anyString(), any(AsyncCallback.class));		
		verify(mockView).setAccessRequirementHTML("");
		verify(mockView).setJoinWizardCallback(any(Callback.class));
		verify(mockView).setCurrentWizardPanelVisible(true);
		verify(mockView).setJoinWizardPrimaryButtonText("Continue");
		verify(mockView).setAccessRequirementsLinkVisible(true);
		verify(mockView).setCurrentWizardContent(mockWikiPageWidget);
		verify(mockWikiPageWidget).loadMarkdownFromWikiPage(any(WikiPageKey.class),eq(true));
	}
	
	@Test
	public void testJoinRequestStep2WithManagedActRestrictionText() throws Exception {
		ManagedACTAccessRequirement terms = new ManagedACTAccessRequirement();
		terms.setId(1L);
		ars.add(terms);
		joinWidget.sendJoinRequestStep0();
		verify(mockView).setButtonsEnabled(true);
		verify(mockView).showJoinWizard();
		verify(mockWizardProgress).configure(Mockito.anyInt(), Mockito.anyInt());
		verify(mockSynapseClient).getTeamAccessRequirements(anyString(), any(AsyncCallback.class));		
		verify(mockView).setAccessRequirementHTML("");
		verify(mockView).setJoinWizardCallback(any(Callback.class));
		verify(mockView).setCurrentWizardPanelVisible(true);
		verify(mockView).setJoinWizardPrimaryButtonText("Continue");
		verify(mockView).setAccessRequirementsLinkVisible(true);
		verify(mockView).setCurrentWizardContent(mockWikiPageWidget);
		verify(mockWikiPageWidget).loadMarkdownFromWikiPage(any(WikiPageKey.class),eq(true));
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testJoinRequestStep2WithACTRestriction() throws Exception {
		ACTAccessRequirement actAR = new ACTAccessRequirement();
		actAR.setActContactInfo("terms");
		ars.add(actAR);
        joinWidget.sendJoinRequestStep0();
		verify(mockSynapseClient).getTeamAccessRequirements(anyString(), any(AsyncCallback.class));
		verify(mockView).setAccessRequirementHTML(anyString());
		verify(mockView).setJoinWizardCallback(any(Callback.class));
		verify(mockView).setCurrentWizardPanelVisible(false);
		verify(mockView).setJoinWizardPrimaryButtonText("Continue");
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testJoinRequestStep2NoRestriction() throws Exception {
		joinWidget.sendJoinRequestStep0();
		verify(mockSynapseClient).getTeamAccessRequirements(anyString(), any(AsyncCallback.class));
		//no ARs shown...
		verify(mockView, never()).showJoinWizard();
		verify(mockView).hideJoinWizard();
		//no accessRequirements, so else should not be executed
		verify(mockWizardProgress, never()).configure(Mockito.anyInt(), Mockito.anyInt());
		verify(mockSynapseClient).requestMembership(anyString(), anyString(), anyString(), eq(EvaluationSubmitterTest.HOST_PAGE_URL), eq((Date)null), any(AsyncCallback.class));
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testGetTotalPageCount() throws Exception {
		boolean isChallengeSignup = true;
        joinWidget.configure(teamId, isChallengeSignup, status, mockTeamUpdatedCallback, null, null, null, null, false);
        joinWidget.sendJoinRequestStep0();
        //one page for the AR
        Assert.assertEquals(1, joinWidget.getTotalPageCount());
        
        isChallengeSignup = false;
        joinWidget.configure(teamId, isChallengeSignup, status, mockTeamUpdatedCallback, null, null, null, null, false);
        joinWidget.sendJoinRequestStep0();
        //0 pages
        Assert.assertEquals(0, joinWidget.getTotalPageCount());
		

        //Now test with an access requirement.  First as part of a challenge, then outside of challenge 
        TermsOfUseAccessRequirement terms = new TermsOfUseAccessRequirement();
        terms.setTermsOfUse("terms of use");
        ars.add(terms);
        
        isChallengeSignup = true;
        joinWidget.configure(teamId, isChallengeSignup, status, mockTeamUpdatedCallback, null, null, null, null, false);
        joinWidget.sendJoinRequestStep0();
        //One page for challenge info, one page for the AR
        Assert.assertEquals(2, joinWidget.getTotalPageCount());
        
        isChallengeSignup = false;
        joinWidget.configure(teamId, isChallengeSignup, status, mockTeamUpdatedCallback, null, null, null, null, false);
        joinWidget.sendJoinRequestStep0();
        //One page for challenge info
        Assert.assertEquals(1, joinWidget.getTotalPageCount());
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testJoinRequestStep2Failure() throws Exception {
		Exception ex =new Exception("unhandled exception");
		AsyncMockStubber.callFailureWith(ex).when(mockSynapseClient).getTeamAccessRequirements(anyString(), any(AsyncCallback.class));
		joinWidget.sendJoinRequestStep0();
		verify(mockSynAlert).handleException(ex);
		joinWidget.sendJoinRequestStep2();
		verify(mockSynapseClient).getTeamAccessRequirements(anyString(), any(AsyncCallback.class));
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testJoinRequestStep3() throws Exception {
		joinWidget.sendJoinRequestStep3();
		verify(mockSynapseClient).requestMembership(anyString(), anyString(), anyString(), eq(EvaluationSubmitterTest.HOST_PAGE_URL), eq((Date)null), any(AsyncCallback.class));
		verify(mockView).showInfo(anyString());
		//verify that team updated callback is invoked
		verify(mockTeamUpdatedCallback).invoke();
	}

	
	@SuppressWarnings("unchecked")
	@Test
	public void testJoinRequestStep3Failure() throws Exception {
		Exception ex = new Exception("unhandled exception");
		AsyncMockStubber.callFailureWith(ex).when(mockSynapseClient).requestMembership(anyString(), anyString(),anyString(), anyString(), any(Date.class), any(AsyncCallback.class));
		joinWidget.sendJoinRequest("");
		verify(mockSynAlert).handleException(ex);
		verify(mockSynapseClient).requestMembership(anyString(), anyString(), anyString(), eq(EvaluationSubmitterTest.HOST_PAGE_URL), eq((Date)null), any(AsyncCallback.class));
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testJoinRequestStep3WikiRefresh() throws Exception {
		//configure using wiki widget renderer version
		Date now = new Date();
		Map<String, String> descriptor = new HashMap<String, String>();
		descriptor.put(WidgetConstants.TEAM_ID_KEY, teamId);
		descriptor.put(WidgetConstants.JOIN_WIDGET_SHOW_PROFILE_FORM_KEY, Boolean.TRUE.toString());
		Integer requestExpiresInXDays = 5;
		descriptor.put(WidgetConstants.JOIN_WIDGET_REQUEST_EXPIRES_IN_X_DAYS_KEY, requestExpiresInXDays.toString());
		Callback mockWidgetRefreshRequired = mock(Callback.class);
		joinWidget.configure(null, descriptor, mockWidgetRefreshRequired, null);
		joinWidget.sendJoinRequestStep3();
		ArgumentCaptor<Date> dateCaptor = ArgumentCaptor.forClass(Date.class);
		verify(mockSynapseClient).requestMembership(anyString(), anyString(), anyString(), eq(EvaluationSubmitterTest.HOST_PAGE_URL), dateCaptor.capture(), any(AsyncCallback.class));
		verify(mockView).showInfo(anyString());
		//verify that wiki page refresh is invoked
		verify(mockWidgetRefreshRequired).invoke();
		
		//since the gwtWrapper.addDaysToDate() does nothing, the date will be unchanged.
		verify(mockGwt).addDaysToDate(any(Date.class), eq(requestExpiresInXDays));
		Date expireDate = dateCaptor.getValue();
		assertTrue(expireDate.getTime() >= now.getTime());
		assertTrue(expireDate.getTime() <= new Date().getTime());
	}
	
	
	@Test
	public void testShowChallengeInfoParam() throws Exception {
		Map<String, String> descriptor = new HashMap<String, String>();
		descriptor.put(WidgetConstants.TEAM_ID_KEY, teamId);
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
		verify(mockView).setJoinWizardCallback(callbackArg.capture());
		
		//manually invoke the okbutton callback
		callbackArg.getValue().invoke();
		
        //it should try to sign this type of ar
        verify(mockSynapseClient).createAccessApproval(any(AccessApproval.class), any(AsyncCallback.class));
        verify(mockView).hideJoinWizard();
	}
	
	
	@Test
	public void testSetLicenseAcceptedACT() throws Exception {
		//single ToU
		ACTAccessRequirement ar = new ACTAccessRequirement();
		ar.setActContactInfo("terms");
		ars.add(ar);
		joinWidget.sendJoinRequestStep0();
		//verify we showTermsOfUseAccessRequirement
		ArgumentCaptor<Callback> callbackArg = ArgumentCaptor.forClass(Callback.class);
		verify(mockView).setJoinWizardCallback(callbackArg.capture());
		
		//manually invoke the okbutton callback
		callbackArg.getValue().invoke();
				
        //verify it does not try to sign, we just continue
        verify(mockSynapseClient, never()).createAccessApproval(any(AccessApproval.class), any(AsyncCallback.class));
        verify(mockView).hideJoinWizard();
	}

	@Test
	public void testSetLicenseAcceptedInvalidType() throws Exception {
		//single ToU
		AccessRequirement ar = mock(AccessRequirement.class);
		ars.add(ar);
		verify(mockSynAlert).showError(anyString());
		joinWidget.sendJoinRequestStep0();
		//verify we show an error for an unrecognized ar
		verify(mockSynAlert, times(2)).showError(anyString());
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
