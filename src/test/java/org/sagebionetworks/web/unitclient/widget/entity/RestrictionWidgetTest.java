package org.sagebionetworks.web.unitclient.widget.entity;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.sagebionetworks.repo.model.AccessRequirement;
import org.sagebionetworks.repo.model.Data;
import org.sagebionetworks.repo.model.TermsOfUseAccessRequirement;
import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.repo.model.UserSessionData;
import org.sagebionetworks.repo.model.Versionable;
import org.sagebionetworks.repo.model.auth.UserEntityPermissions;
import org.sagebionetworks.schema.adapter.AdapterFactory;
import org.sagebionetworks.schema.adapter.JSONObjectAdapter;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.schema.adapter.org.json.AdapterFactoryImpl;
import org.sagebionetworks.schema.adapter.org.json.JSONObjectAdapterImpl;
import org.sagebionetworks.web.client.EntitySchemaCache;
import org.sagebionetworks.web.client.EntityTypeProvider;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.IconsImageBundle;
import org.sagebionetworks.web.client.PlaceChanger;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.model.EntityBundle;
import org.sagebionetworks.web.client.place.LoginPlace;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.utils.RESTRICTION_LEVEL;
import org.sagebionetworks.web.client.widget.entity.AccessRequirementDialog;
import org.sagebionetworks.web.client.widget.entity.JiraURLHelper;
import org.sagebionetworks.web.client.widget.entity.RestrictionWidget;
import org.sagebionetworks.web.client.widget.entity.RestrictionWidgetView;

import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Widget;

public class RestrictionWidgetTest {

	AuthenticationController mockAuthenticationController;
	GlobalApplicationState mockGlobalApplicationState;
	RestrictionWidgetView mockView;
	EntitySchemaCache mockSchemaCache;
	JSONObjectAdapter jsonObjectAdapter;
	EntityTypeProvider mockEntityTypeProvider;
	JiraURLHelper mockJiraURLHelper;
	AccessRequirementDialog mockAccessRequirementDialog;
	RestrictionWidget widget;
	Versionable vb;
	String entityId = "syn123";
	EntityBundle bundle;
	AdapterFactory adapterFactory = new AdapterFactoryImpl();
	List<String> emailAddresses;
	Long testAccessRequirementId = 92837L;
	Callback mockEntityUpdatedCallback;
	UserEntityPermissions mockPermissions;
	PlaceChanger mockPlaceChanger;
	
	@Before
	public void before() throws JSONObjectAdapterException {
		mockAuthenticationController = mock(AuthenticationController.class);
		mockGlobalApplicationState = mock(GlobalApplicationState.class);
		
		mockView = mock(RestrictionWidgetView.class);
		mockSchemaCache = mock(EntitySchemaCache.class);
		jsonObjectAdapter = new JSONObjectAdapterImpl();
		mockEntityTypeProvider = mock(EntityTypeProvider.class);
		mockJiraURLHelper = mock(JiraURLHelper.class);
		mockEntityUpdatedCallback = mock(Callback.class);
		UserSessionData usd = new UserSessionData();
		emailAddresses = new ArrayList<String>();
		emailAddresses.add("test@test.com");
		UserProfile up = new UserProfile();
		up.setOwnerId("101");
		up.setEmails(emailAddresses);
		usd.setProfile(up);
		mockPlaceChanger = mock(PlaceChanger.class);
		when(mockGlobalApplicationState.getPlaceChanger()).thenReturn(mockPlaceChanger);
		when(mockAuthenticationController.getCurrentUserSessionData()).thenReturn(usd);
		when(mockAuthenticationController.isLoggedIn()).thenReturn(true);
		mockAccessRequirementDialog = mock(AccessRequirementDialog.class);

		widget = new RestrictionWidget(mockView, mockAuthenticationController, mockGlobalApplicationState, mockJiraURLHelper, mockAccessRequirementDialog);

		vb = new Data();
		vb.setId(entityId);
		vb.setVersionNumber(new Long(1));
		vb.setVersionLabel("");
		vb.setVersionComment("");
		bundle = mock(EntityBundle.class, RETURNS_DEEP_STUBS);
		when(bundle.getEntity()).thenReturn(vb);
		mockPermissions = mock(UserEntityPermissions.class);
		when(bundle.getPermissions()).thenReturn(mockPermissions);
		when(mockPermissions.getCanChangePermissions()).thenReturn(true);
		
		List<AccessRequirement> accessRequirements = new ArrayList<AccessRequirement>();
		TermsOfUseAccessRequirement accessRequirement = new TermsOfUseAccessRequirement();
		accessRequirement.setId(testAccessRequirementId);
		accessRequirement.setTermsOfUse("terms of use");
		accessRequirements.add(accessRequirement);
		when(bundle.getAccessRequirements()).thenReturn(accessRequirements);
		when(bundle.getUnmetDownloadAccessRequirements()).thenReturn(accessRequirements);
				
		widget.setEntityBundle(bundle);
		widget.resetAccessRequirementCount();
	}
	
	@Test
	public void testGetJiraFlagUrl() {
		String flagURLString = "flagURLString";
		when(mockJiraURLHelper.createFlagIssue(any(String.class),any(String.class),any(String.class))).thenReturn(flagURLString);
		assertEquals(flagURLString, widget.getJiraFlagUrl());
	}
	
	@Test
	public void testGetRestrictionLevel() {
		assertEquals(RESTRICTION_LEVEL.RESTRICTED, widget.getRestrictionLevel());
	}
	
	private void setupEmptyAccessRequirements() {
		List<AccessRequirement> accessRequirements = new ArrayList<AccessRequirement>();
		when(bundle.getAccessRequirements()).thenReturn(accessRequirements);
		when(bundle.getUnmetDownloadAccessRequirements()).thenReturn(accessRequirements);
		widget.setEntityBundle(bundle);
		widget.resetAccessRequirementCount();
	}
	
	@Test
	public void testShowVerifySensitiveDataDialog() {
		//set up so that it has no requirements
		setupEmptyAccessRequirements();
		
		//show nothing if empty and no admin privs (should not get into this state)
		widget.showNextAccessRequirement(false);
		verify(mockAccessRequirementDialog, never()).show();
		verify(mockView, never()).showVerifyDataSensitiveDialog();

		//should show verification of sensitive data dialog if we have admin privileges
		widget.showNextAccessRequirement(true);
		verify(mockAccessRequirementDialog, never()).show();
		verify(mockView).showVerifyDataSensitiveDialog();
	}
	
	
	@Test
	public void testConfigureLoggedInControlled() {
		//is logged in, with our tou restriction set up in before(), has admin access (can change)
		widget.configure(bundle, 
				true,						//showChangeLink 
				true,						//showIfProject 
				true,						//showFlagLink 
				mockEntityUpdatedCallback);	//entityUpdated
		
		verify(mockView).clear();
		verify(mockView).showControlledUseUI();
		verify(mockView).showUnmetRequirementsIcon();
		//has existing terms, has "show" link
		verify(mockView).showShowLink(any(ClickHandler.class));
		verify(mockView).showFlagUI();
		
		verify(mockView).setAccessRequirementDialog(any(Widget.class));
	}
	
	@Test
	public void testConfigureAnonymousControlled() {
		when(mockAuthenticationController.isLoggedIn()).thenReturn(false);

		//anonymous, with our tou restriction set up in before()
		widget.configure(bundle, 
				true,						//showChangeLink 
				true,						//showIfProject 
				true,						//showFlagLink 
				mockEntityUpdatedCallback);	//entityUpdated
		
		verify(mockView).clear();
		verify(mockView).showControlledUseUI();
		verify(mockView).showUnmetRequirementsIcon();

		verify(mockView).showShowLink(any(ClickHandler.class));
		verify(mockView).showAnonymousFlagUI();
		
		verify(mockView).setAccessRequirementDialog(any(Widget.class));
	}
	
	@Test
	public void testConfigureLoggedInControlledMet() {
		when(bundle.getUnmetDownloadAccessRequirements()).thenReturn(new ArrayList<AccessRequirement>());
		
		//is logged in, with our tou restriction set up in before(), has admin access (can change)
		widget.configure(bundle, 
				true,						//showChangeLink 
				true,						//showIfProject 
				true,						//showFlagLink 
				mockEntityUpdatedCallback);	//entityUpdated
		
		verify(mockView).clear();
		verify(mockView).showControlledUseUI();
		verify(mockView).showMetRequirementsIcon();
	}
	@Test
	public void testConfigureLoggedInOpen() {
		//no access restrictions, logged in, has admin access (can change)
		setupEmptyAccessRequirements();
		widget.configure(bundle, 
				true,						//showChangeLink 
				true,						//showIfProject 
				true,						//showFlagLink 
				mockEntityUpdatedCallback);	//entityUpdated
		
		verify(mockView).clear();
		verify(mockView).showNoRestrictionsUI();

		verify(mockView).showChangeLink(any(ClickHandler.class));
		verify(mockView).showFlagUI();
		
		verify(mockView).setAccessRequirementDialog(any(Widget.class));
	}
	
	@Test
	public void testConfigureAnonymousOpen() {
		//no access restrictions, anonymous
		when(mockAuthenticationController.isLoggedIn()).thenReturn(false);
		setupEmptyAccessRequirements();
		widget.configure(bundle, 
				true,						//showChangeLink 
				true,						//showIfProject 
				true,						//showFlagLink 
				mockEntityUpdatedCallback);	//entityUpdated
		
		verify(mockView).clear();
		verify(mockView).showNoRestrictionsUI();

		verify(mockView, never()).showChangeLink(any(ClickHandler.class));
		verify(mockView).showAnonymousFlagUI();
		
		verify(mockView).setAccessRequirementDialog(any(Widget.class));
	}
	
	@Test
	public void testEntityUpdatedCallback() {
		Callback mockEntityUpdatedCallback = mock(Callback.class);
		widget.setEntityUpdated(mockEntityUpdatedCallback);
		//show nothing if empty and no admin privs (should not get into this state)
		//in before, we set up a single unmet access requirement
		widget.showNextAccessRequirement(false);
		verify(mockEntityUpdatedCallback, never()).invoke();
		verify(mockAccessRequirementDialog).show();
		
		widget.showNextAccessRequirement(false);
		//we have shown all access requirements, should now update bundle
		verify(mockEntityUpdatedCallback).invoke();
		
	}
	
	@Test
	public void testImposeRestrictionClickedNoSelectionNulls() {
		when(mockView.isNoHumanDataRadioSelected()).thenReturn(null);
		when(mockView.isYesHumanDataRadioSelected()).thenReturn(null);
		widget.imposeRestrictionClicked();
		verify(mockView).showErrorMessage(anyString());
	}

	@Test
	public void testImposeRestrictionClickedNoSelection2() {
		when(mockView.isNoHumanDataRadioSelected()).thenReturn(false);
		when(mockView.isYesHumanDataRadioSelected()).thenReturn(false);
		widget.imposeRestrictionClicked();
		verify(mockView).showErrorMessage(anyString());
	}
	
	@Test
	public void testImposeRestrictionClickedNoIsSelected() {
		when(mockView.isNoHumanDataRadioSelected()).thenReturn(true);
		when(mockView.isYesHumanDataRadioSelected()).thenReturn(false);
		widget.imposeRestrictionClicked();
		verify(mockView).showErrorMessage(anyString());
	}
	
	@Test
	public void testImposeRestrictionClickedYesIsSelected() {
		when(mockView.isNoHumanDataRadioSelected()).thenReturn(false);
		when(mockView.isYesHumanDataRadioSelected()).thenReturn(true);
		widget.imposeRestrictionClicked();
		verify(mockView).showLoading();
		verify(mockAccessRequirementDialog).imposeRestriction(anyString(), any(Callback.class));
	}

	@Test
	public void testFlagData() {
		widget.flagData();
		verify(mockView).open(anyString());
	}
	
	@Test
	public void testAnonymousFlagModalOkClicked() {
		widget.anonymousFlagModalOkClicked();
		verify(mockPlaceChanger).goTo(any(LoginPlace.class));
	}
	
	@Test
	public void testReportIssueClicked() {
		widget.reportIssueClicked();
		verify(mockView).showFlagModal();
	}
	
	@Test
	public void testAnonymousReportIssueClicked() {
		widget.anonymousReportIssueClicked();
		verify(mockView).showAnonymousFlagModal();
	}
	
	@Test
	public void testNotHumanDataClicked() {
		widget.notHumanDataClicked();
		verify(mockView).setImposeRestrictionOkButtonEnabled(false);
		verify(mockView).setNotSensitiveHumanDataMessageVisible(true);
	}
	@Test
	public void testYesHumanDataClicked() {
		widget.yesHumanDataClicked();
		verify(mockView).setImposeRestrictionOkButtonEnabled(true);
		verify(mockView).setNotSensitiveHumanDataMessageVisible(false);
	}
}
