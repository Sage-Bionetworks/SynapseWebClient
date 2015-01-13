package org.sagebionetworks.web.unitclient.widget.entity;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.sagebionetworks.repo.model.ACTAccessRequirement;
import org.sagebionetworks.repo.model.PostMessageContentAccessRequirement;
import org.sagebionetworks.repo.model.TermsOfUseAccessRequirement;
import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.repo.model.UserSessionData;
import org.sagebionetworks.schema.adapter.AdapterFactory;
import org.sagebionetworks.schema.adapter.JSONObjectAdapter;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.schema.adapter.org.json.AdapterFactoryImpl;
import org.sagebionetworks.schema.adapter.org.json.JSONObjectAdapterImpl;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.PlaceChanger;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.place.LoginPlace;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.utils.APPROVAL_TYPE;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.utils.RESTRICTION_LEVEL;
import org.sagebionetworks.web.client.widget.entity.AccessRequirementDialog;
import org.sagebionetworks.web.client.widget.entity.AccessRequirementDialogView;
import org.sagebionetworks.web.client.widget.entity.JiraURLHelper;
import org.sagebionetworks.web.client.widget.entity.WikiPageWidget;
import org.sagebionetworks.web.shared.EntityWrapper;
import org.sagebionetworks.web.shared.WikiPageKey;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;

import com.google.gwt.user.client.rpc.AsyncCallback;

public class AccessRequirementDialogTest {

	SynapseClientAsync mockSynapseClient;
	AuthenticationController mockAuthenticationController;
	GlobalApplicationState mockGlobalApplicationState;
	PlaceChanger mockPlaceChanger;
	AccessRequirementDialogView mockView;
	JSONObjectAdapter jsonObjectAdapter;
	JiraURLHelper mockJiraURLHelper;
	AccessRequirementDialog widget;
	String entityId = "syn123";
	AdapterFactory adapterFactory = new AdapterFactoryImpl();
	Callback mockFinishedCallback;
	Long touAccessRequirementId = 92837L;
	TermsOfUseAccessRequirement touAR;
	Long actAccessRequirementId = 928388L;
	ACTAccessRequirement actAR;
	Callback mockImposeRestrictionCallback;
	String arText = "some terms of use";
	String actContactInfo = "shine the governance spotlight into the night sky and the ACT will be there in seconds.";
	WikiPageWidget mockWikiPageWidget;
	@Before
	public void before() throws JSONObjectAdapterException {
		mockAuthenticationController = mock(AuthenticationController.class);
		mockGlobalApplicationState = mock(GlobalApplicationState.class);
		mockPlaceChanger = mock(PlaceChanger.class);
		mockWikiPageWidget = mock(WikiPageWidget.class);
		when(mockGlobalApplicationState.getPlaceChanger()).thenReturn(mockPlaceChanger);
		mockSynapseClient = mock(SynapseClientAsync.class);
		mockView = mock(AccessRequirementDialogView.class);
		jsonObjectAdapter = new JSONObjectAdapterImpl();
		mockJiraURLHelper = mock(JiraURLHelper.class);

		UserSessionData usd = new UserSessionData();
		List<String> emailAddresses = new ArrayList<String>();
		emailAddresses.add("test@test.com");
		UserProfile up = new UserProfile();
		up.setOwnerId("101");
		up.setEmails(emailAddresses);
		usd.setProfile(up);
		
		when(mockAuthenticationController.getCurrentUserSessionData()).thenReturn(usd);
		when(mockAuthenticationController.isLoggedIn()).thenReturn(true);
		
		widget = new AccessRequirementDialog(mockView, mockSynapseClient, mockAuthenticationController, jsonObjectAdapter, mockGlobalApplicationState, mockJiraURLHelper, mockWikiPageWidget);

		touAR = new TermsOfUseAccessRequirement();
		touAR.setId(touAccessRequirementId);
		touAR.setTermsOfUse(arText);
		
		mockFinishedCallback = mock(Callback.class);
		mockImposeRestrictionCallback = mock(Callback.class);
		actAR = new ACTAccessRequirement();
		actAR.setActContactInfo(actContactInfo);
		actAR.setId(actAccessRequirementId);
		
		AsyncMockStubber.callSuccessWith(null).when(mockSynapseClient).createAccessApproval(any(EntityWrapper.class),  any(AsyncCallback.class));
		AsyncMockStubber.callSuccessWith(null).when(mockSynapseClient).createLockAccessRequirement(anyString(), any(AsyncCallback.class));
	}
	
	//many ways to configure this dialog.  verify UI in each case

	@Test (expected=IllegalArgumentException.class)
	public void testConfigureTouInvalid() {
		//unrecognized AR type
		PostMessageContentAccessRequirement postMessageAR = new PostMessageContentAccessRequirement();
		widget.configure(postMessageAR, 
				entityId, 
				true, /** hasAdministrativeAccess **/
				false, /** accessApproved **/ 
				null,/** imposeRestrictionCallback **/ 
				mockFinishedCallback /** finishedCallback **/
		);
	}
	
	@Test (expected=IllegalArgumentException.class)
	public void testConfigureTouInvalid2() {
		//unrecognized AR type
		when(mockAuthenticationController.isLoggedIn()).thenReturn(false);
		when(mockAuthenticationController.getCurrentUserSessionData()).thenReturn(null);
		widget.configure(touAR, 
				entityId, 
				true, /** hasAdministrativeAccess **/
				true, /** accessApproved **/ 
				null,/** imposeRestrictionCallback **/ 
				mockFinishedCallback /** finishedCallback **/
		);
	}
	
	@Test
	public void testConfigureTouNullAR() {
		widget.configure(null, 
				entityId, 
				true, 							/** hasAdministrativeAccess **/
				true, 							/** accessApproved **/ 
				mockImposeRestrictionCallback,	/** imposeRestrictionCallback **/ 
				mockFinishedCallback			/** finishedCallback **/
		);
		verify(mockView).clear();
		verify(mockView).showNoRestrictionsUI();
		verify(mockView).showOpenUI();
		verify(mockView, never()).showControlledUseUI();
		verify(mockView, never()).showApprovedHeading();
		verify(mockView, never()).showTouHeading();
		verify(mockView, never()).showActHeading();
		verify(mockView, never()).showTermsUI();
		verify(mockView, never()).setTerms(arText);
		verify(mockView, never()).showAnonymousAccessNote();
		verify(mockView).showImposeRestrictionsAllowedNote();
		verify(mockView, never()).showImposeRestrictionsNotAllowedNote();
		verify(mockView, never()).showAnonymousFlagNote();
		verify(mockView, never()).showImposeRestrictionsNotAllowedFlagNote();
		verify(mockView).showImposeRestrictionsButton();
		verify(mockView, never()).showLoginButton();
		verify(mockView).showCancelButton();
		verify(mockView, never()).showSignTermsButton();
		verify(mockView, never()).showRequestAccessFromACTButton();
		verify(mockView, never()).showCloseButton();
	}
	
	@Test
	public void testConfigureTou() {
		widget.configure(touAR, 
				entityId, 
				true, 							/** hasAdministrativeAccess **/
				true, 							/** accessApproved **/ 
				mockImposeRestrictionCallback,	/** imposeRestrictionCallback **/ 
				mockFinishedCallback			/** finishedCallback **/
		);
		verify(mockView).clear();
		verify(mockView, never()).showNoRestrictionsUI();
		verify(mockView, never()).showOpenUI();
		verify(mockView).showControlledUseUI();
		verify(mockView).showApprovedHeading();
		verify(mockView, never()).showTouHeading();
		verify(mockView, never()).showActHeading();
		verify(mockView).showTermsUI();
		verify(mockView).setTerms(arText);
		verify(mockView, never()).showWikiTermsUI();
		verify(mockView, never()).showAnonymousAccessNote();
		verify(mockView, never()).showImposeRestrictionsAllowedNote();
		verify(mockView).showImposeRestrictionsNotAllowedNote();
		verify(mockView, never()).showAnonymousFlagNote();
		verify(mockView).showImposeRestrictionsNotAllowedFlagNote();
		verify(mockView, never()).showImposeRestrictionsButton();
		verify(mockView, never()).showLoginButton();
		verify(mockView, never()).showCancelButton();
		verify(mockView, never()).showSignTermsButton();
		verify(mockView, never()).showRequestAccessFromACTButton();
		verify(mockView).showCloseButton();
	}
	

	@Test
	public void testConfigureWikiTou() {
		touAR.setTermsOfUse(null);
		widget.configure(touAR, 
				entityId, 
				true, 							/** hasAdministrativeAccess **/
				true, 							/** accessApproved **/ 
				mockImposeRestrictionCallback,	/** imposeRestrictionCallback **/ 
				mockFinishedCallback			/** finishedCallback **/
		);
		verify(mockView).clear();
		verify(mockView, never()).showNoRestrictionsUI();
		verify(mockView, never()).showOpenUI();
		verify(mockView).showControlledUseUI();
		verify(mockView).showApprovedHeading();
		verify(mockView, never()).showTouHeading();
		verify(mockView, never()).showActHeading();
		verify(mockView, never()).showTermsUI();
		verify(mockView).showWikiTermsUI();
		verify(mockWikiPageWidget).configure(any(WikiPageKey.class), anyBoolean(), any(WikiPageWidget.Callback.class), anyBoolean());
		verify(mockView, never()).showAnonymousAccessNote();
		verify(mockView, never()).showImposeRestrictionsAllowedNote();
		verify(mockView).showImposeRestrictionsNotAllowedNote();
		verify(mockView, never()).showAnonymousFlagNote();
		verify(mockView).showImposeRestrictionsNotAllowedFlagNote();
		verify(mockView, never()).showImposeRestrictionsButton();
		verify(mockView, never()).showLoginButton();
		verify(mockView, never()).showCancelButton();
		verify(mockView, never()).showSignTermsButton();
		verify(mockView, never()).showRequestAccessFromACTButton();
		verify(mockView).showCloseButton();
	}
	
	@Test
	public void testConfigureTouNotApproved() {
		widget.configure(touAR, 
				entityId, 
				true, 							/** hasAdministrativeAccess **/
				false, 							/** accessApproved **/ 
				mockImposeRestrictionCallback,	/** imposeRestrictionCallback **/ 
				mockFinishedCallback			/** finishedCallback **/
		);
		verify(mockView).clear();
		verify(mockView, never()).showNoRestrictionsUI();
		verify(mockView, never()).showOpenUI();
		verify(mockView).showControlledUseUI();
		verify(mockView, never()).showApprovedHeading();
		verify(mockView).showTouHeading();
		verify(mockView, never()).showActHeading();
		verify(mockView).showTermsUI();
		verify(mockView).setTerms(arText);
		verify(mockView, never()).showAnonymousAccessNote();
		verify(mockView, never()).showImposeRestrictionsAllowedNote();
		verify(mockView).showImposeRestrictionsNotAllowedNote();
		verify(mockView, never()).showAnonymousFlagNote();
		verify(mockView).showImposeRestrictionsNotAllowedFlagNote();
		verify(mockView, never()).showImposeRestrictionsButton();
		verify(mockView, never()).showLoginButton();
		verify(mockView).showCancelButton();
		verify(mockView).showSignTermsButton();
		verify(mockView, never()).showRequestAccessFromACTButton();
		verify(mockView, never()).showCloseButton();
	}
	
	@Test
	public void testConfigureTouAnonymous() {
		when(mockAuthenticationController.isLoggedIn()).thenReturn(false);
		when(mockAuthenticationController.getCurrentUserSessionData()).thenReturn(null);

		widget.configure(touAR, 
				entityId, 
				true, 							/** hasAdministrativeAccess **/
				false, 							/** accessApproved **/ 
				mockImposeRestrictionCallback,	/** imposeRestrictionCallback **/ 
				mockFinishedCallback			/** finishedCallback **/
		);
		verify(mockView).clear();
		verify(mockView, never()).showNoRestrictionsUI();
		verify(mockView, never()).showOpenUI();
		verify(mockView).showControlledUseUI();
		verify(mockView, never()).showApprovedHeading();
		verify(mockView).showTouHeading();
		verify(mockView, never()).showActHeading();
		verify(mockView).showTermsUI();
		verify(mockView).setTerms(arText);
		verify(mockView).showAnonymousAccessNote();
		verify(mockView, never()).showImposeRestrictionsAllowedNote();
		verify(mockView).showImposeRestrictionsNotAllowedNote();
		verify(mockView).showAnonymousFlagNote();
		verify(mockView, never()).showImposeRestrictionsNotAllowedFlagNote();
		verify(mockView, never()).showImposeRestrictionsButton();
		verify(mockView).showLoginButton();
		verify(mockView).showCancelButton();
		verify(mockView, never()).showSignTermsButton();
		verify(mockView, never()).showRequestAccessFromACTButton();
		verify(mockView, never()).showCloseButton();
	}
	
	@Test
	public void testConfigureAct() {
		actAR.setOpenJiraIssue(true);
		widget.configure(actAR, 
				entityId, 
				true, 							/** hasAdministrativeAccess **/
				false, 							/** accessApproved **/ 
				mockImposeRestrictionCallback,	/** imposeRestrictionCallback **/ 
				mockFinishedCallback			/** finishedCallback **/
		);
		verify(mockView).clear();
		verify(mockView, never()).showNoRestrictionsUI();
		verify(mockView, never()).showOpenUI();
		verify(mockView).showControlledUseUI();
		verify(mockView, never()).showApprovedHeading();
		verify(mockView, never()).showTouHeading();
		verify(mockView).showActHeading();
		verify(mockView).showTermsUI();
		verify(mockView).setTerms(actContactInfo);
		verify(mockView, never()).showAnonymousAccessNote();
		verify(mockView, never()).showImposeRestrictionsAllowedNote();
		verify(mockView).showImposeRestrictionsNotAllowedNote();
		verify(mockView, never()).showAnonymousFlagNote();
		verify(mockView).showImposeRestrictionsNotAllowedFlagNote();
		verify(mockView, never()).showImposeRestrictionsButton();
		verify(mockView, never()).showLoginButton();
		verify(mockView).showCancelButton();
		verify(mockView, never()).showSignTermsButton();
		verify(mockView).showRequestAccessFromACTButton();
		verify(mockView, never()).showCloseButton();
	}
	
	@Test
	public void testConfigureActNullOpenJiraIssue() {
		actAR.setOpenJiraIssue(null);
		widget.configure(actAR, 
				entityId, 
				true, 							/** hasAdministrativeAccess **/
				false, 							/** accessApproved **/ 
				mockImposeRestrictionCallback,	/** imposeRestrictionCallback **/ 
				mockFinishedCallback			/** finishedCallback **/
		);
		verify(mockView).clear();
		verify(mockView, never()).showNoRestrictionsUI();
		verify(mockView, never()).showOpenUI();
		verify(mockView).showControlledUseUI();
		verify(mockView, never()).showApprovedHeading();
		verify(mockView, never()).showTouHeading();
		verify(mockView).showActHeading();
		verify(mockView).showTermsUI();
		verify(mockView).setTerms(actContactInfo);
		verify(mockView, never()).showAnonymousAccessNote();
		verify(mockView, never()).showImposeRestrictionsAllowedNote();
		verify(mockView).showImposeRestrictionsNotAllowedNote();
		verify(mockView, never()).showAnonymousFlagNote();
		verify(mockView).showImposeRestrictionsNotAllowedFlagNote();
		verify(mockView, never()).showImposeRestrictionsButton();
		verify(mockView, never()).showLoginButton();
		verify(mockView).showCancelButton();
		verify(mockView, never()).showSignTermsButton();
		verify(mockView).showRequestAccessFromACTButton();
		verify(mockView, never()).showCloseButton();
	}
	
	@Test
	public void testConfigureActNoRequestAccess() {
		actAR.setOpenJiraIssue(false);
		widget.configure(actAR, 
				entityId, 
				true, 							/** hasAdministrativeAccess **/
				false, 							/** accessApproved **/ 
				mockImposeRestrictionCallback,	/** imposeRestrictionCallback **/ 
				mockFinishedCallback			/** finishedCallback **/
		);
		verify(mockView).clear();
		verify(mockView, never()).showNoRestrictionsUI();
		verify(mockView, never()).showOpenUI();
		verify(mockView).showControlledUseUI();
		verify(mockView, never()).showApprovedHeading();
		verify(mockView, never()).showTouHeading();
		verify(mockView).showActHeading();
		verify(mockView).showTermsUI();
		verify(mockView).setTerms(actContactInfo);
		verify(mockView, never()).showAnonymousAccessNote();
		verify(mockView, never()).showImposeRestrictionsAllowedNote();
		verify(mockView).showImposeRestrictionsNotAllowedNote();
		verify(mockView, never()).showAnonymousFlagNote();
		verify(mockView).showImposeRestrictionsNotAllowedFlagNote();
		verify(mockView, never()).showImposeRestrictionsButton();
		verify(mockView, never()).showLoginButton();
		verify(mockView, never()).showCancelButton();
		verify(mockView, never()).showSignTermsButton();
		verify(mockView, never()).showRequestAccessFromACTButton();
		verify(mockView).showCloseButton();
	}
	
	@Test
	public void testGetRestrictionLevel() {
		assertEquals(RESTRICTION_LEVEL.OPEN, widget.getRestrictionLevel());
	}
	
	@Test
	public void testGetApprovalType() {
		assertEquals(APPROVAL_TYPE.NONE, widget.getApprovalType());
	}
	
	private void standardConfigure() {
		widget.configure(touAR, 
				entityId, 
				true, 							/** hasAdministrativeAccess **/
				false, 							/** accessApproved **/ 
				mockImposeRestrictionCallback,	/** imposeRestrictionCallback **/ 
				mockFinishedCallback			/** finishedCallback **/
		);
	}
	
	@Test
	public void testFinished() {
		standardConfigure();
		
		widget.finished();
		verify(mockFinishedCallback).invoke();
	}
	
	@Test
	public void testFlagClicked() {
		//also tests getJiraFlagUrl
		standardConfigure();
		
		String flagURLString = "flagURLString";
		when(mockJiraURLHelper.createFlagIssue(any(String.class),any(String.class),any(String.class))).thenReturn(flagURLString);
		assertEquals(flagURLString, widget.getJiraFlagUrl());
		
		widget.flagClicked();
		verify(mockView).hideModal();
		verify(mockView).open(flagURLString);
	}
	
	@Test
	public void testRequestACTClicked() {
		//also tests getJiraRequestAccessUrl
		standardConfigure();
		
		String requestAccessURLString = "requestAccessURLString";
		when(mockJiraURLHelper.createRequestAccessIssue(any(String.class),any(String.class),any(String.class),any(String.class),any(String.class))).thenReturn(requestAccessURLString);
		assertEquals(requestAccessURLString, widget.getJiraRequestAccessUrl());
		
		widget.requestACTClicked();
		verify(mockView).hideModal();
		verify(mockView).open(requestAccessURLString);
	}
	
	@Test
	public void testLoginClicked() {
		standardConfigure();
		
		widget.loginClicked();
		verify(mockView).hideModal();
		verify(mockPlaceChanger).goTo(any(LoginPlace.class));
	}
	
	@Test
	public void testCancelClicked() {
		standardConfigure();
		
		widget.cancelClicked();
		verify(mockFinishedCallback).invoke();
	}
	
	@Test
	public void testSignTermsOfUseClicked() throws JSONObjectAdapterException {
		standardConfigure();
		widget.signTermsOfUseClicked();
		
		verify(mockSynapseClient).createAccessApproval(any(EntityWrapper.class),  any(AsyncCallback.class));
		verify(mockFinishedCallback).invoke();
	}
	
	@Test
	public void testSignTermsOfUseClickedFailure() throws JSONObjectAdapterException {
		String errorMessage = "this should be passed to the view";
		AsyncMockStubber.callFailureWith(new Exception(errorMessage)).when(mockSynapseClient).createAccessApproval(any(EntityWrapper.class),  any(AsyncCallback.class));
		standardConfigure();
		widget.signTermsOfUseClicked();
		
		verify(mockSynapseClient).createAccessApproval(any(EntityWrapper.class),  any(AsyncCallback.class));
		verify(mockView).showErrorMessage(errorMessage);
	}
	
	@Test
	public void testImposeRestriction() throws JSONObjectAdapterException {
		standardConfigure();
		widget.imposeRestrictionClicked();
		
		verify(mockSynapseClient).createLockAccessRequirement(anyString(), any(AsyncCallback.class));
		verify(mockImposeRestrictionCallback).invoke();
	}
	
	@Test
	public void testImposeRestrictionFailure() throws JSONObjectAdapterException {
		String errorMessage = "failed to impose restriction";
		standardConfigure();
		AsyncMockStubber.callFailureWith(new Exception(errorMessage)).when(mockSynapseClient).createLockAccessRequirement(anyString(), any(AsyncCallback.class));
		widget.imposeRestrictionClicked();
		
		verify(mockSynapseClient).createLockAccessRequirement(anyString(), any(AsyncCallback.class));
		verify(mockImposeRestrictionCallback, never()).invoke();
		verify(mockView).showErrorMessage(errorMessage);
	}
	
}
