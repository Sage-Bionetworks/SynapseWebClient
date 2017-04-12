package org.sagebionetworks.web.unitclient.widget.entity.restriction.v2;

import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.repo.model.ACTAccessRequirement;
import org.sagebionetworks.repo.model.AccessRequirement;
import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.repo.model.EntityBundle;
import org.sagebionetworks.repo.model.FileEntity;
import org.sagebionetworks.repo.model.RestrictionInformation;
import org.sagebionetworks.repo.model.RestrictionLevel;
import org.sagebionetworks.repo.model.TermsOfUseAccessRequirement;
import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.repo.model.UserSessionData;
import org.sagebionetworks.repo.model.Versionable;
import org.sagebionetworks.repo.model.auth.UserEntityPermissions;
import org.sagebionetworks.schema.adapter.org.json.JSONObjectAdapterImpl;
import org.sagebionetworks.web.client.DataAccessClientAsync;
import org.sagebionetworks.web.client.EntitySchemaCache;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.PlaceChanger;
import org.sagebionetworks.web.client.place.AccessRequirementsPlace;
import org.sagebionetworks.web.client.place.LoginPlace;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.entity.JiraURLHelper;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.client.widget.entity.restriction.v2.RestrictionWidget;
import org.sagebionetworks.web.client.widget.entity.restriction.v2.RestrictionWidgetView;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;

import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;

public class RestrictionWidgetTest {

	RestrictionWidget widget;
	
	@Mock
	AuthenticationController mockAuthenticationController;
	@Mock
	GlobalApplicationState mockGlobalApplicationState;
	@Mock
	RestrictionWidgetView mockView;
	@Mock
	JiraURLHelper mockJiraURLHelper;
	@Mock
	DataAccessClientAsync mockDataAccessClient;
	@Mock
	PlaceChanger mockPlaceChanger;
	@Mock
	SynapseAlert mockSynAlert;
	@Mock
	RestrictionInformation mockRestrictionInformation;
	@Mock
	Entity mockEntity;
	public static final String ENTITY_ID = "762";
	@Before
	public void before() {
		MockitoAnnotations.initMocks(this);
		widget = new RestrictionWidget(mockView, mockAuthenticationController, mockGlobalApplicationState, mockJiraURLHelper, mockDataAccessClient, mockSynAlert);
		
		UserSessionData usd = new UserSessionData();
		List<String> emailAddresses = new ArrayList<String>();
		emailAddresses.add("test@test.com");
		UserProfile up = new UserProfile();
		up.setOwnerId("101");
		up.setEmails(emailAddresses);
		usd.setProfile(up);
		when(mockGlobalApplicationState.getPlaceChanger()).thenReturn(mockPlaceChanger);
		when(mockAuthenticationController.getCurrentUserSessionData()).thenReturn(usd);
		when(mockAuthenticationController.isLoggedIn()).thenReturn(true);
		AsyncMockStubber.callSuccessWith(mockRestrictionInformation).when(mockDataAccessClient).getRestrictionInformation(anyString(), any(AsyncCallback.class));
		when(mockEntity.getId()).thenReturn(ENTITY_ID);
	}
	
	@Test
	public void testConstruction() {
		verify(mockView).setSynAlert(any(IsWidget.class));
		verify(mockView).setPresenter(widget);
	}
	
	@Test
	public void testGetJiraFlagUrl() {
		when(mockRestrictionInformation.getRestrictionLevel()).thenReturn(RestrictionLevel.OPEN);
		boolean canChangePermissions = false;
		widget.configure(mockEntity, canChangePermissions);
		String flagURLString = "flagURLString";
		when(mockJiraURLHelper.createFlagIssue(any(String.class),any(String.class),any(String.class))).thenReturn(flagURLString);
		assertEquals(flagURLString, widget.getJiraFlagUrl());
	}
	
	
	@Test
	public void testShowChangeLink() {
		widget.setShowChangeLink(true);
		when(mockRestrictionInformation.getRestrictionLevel()).thenReturn(RestrictionLevel.OPEN);
		boolean canChangePermissions = true;
		widget.configure(mockEntity, canChangePermissions);
		
		verify(mockView).showChangeLink();
	}
	
	@Test
	public void testHideChangeLink1() {
		widget.setShowChangeLink(false);
		when(mockRestrictionInformation.getRestrictionLevel()).thenReturn(RestrictionLevel.OPEN);
		boolean canChangePermissions = true;
		widget.configure(mockEntity, canChangePermissions);
		
		verify(mockView, never()).showChangeLink();
	}

	@Test
	public void testHideChangeLink2() {
		widget.setShowChangeLink(true);
		when(mockRestrictionInformation.getRestrictionLevel()).thenReturn(RestrictionLevel.RESTRICTED_BY_TERMS_OF_USE);
		boolean canChangePermissions = true;
		widget.configure(mockEntity, canChangePermissions);
		
		verify(mockView, never()).showChangeLink();
	}
	@Test
	public void testHideChangeLink3() {
		widget.setShowChangeLink(true);
		when(mockRestrictionInformation.getRestrictionLevel()).thenReturn(RestrictionLevel.OPEN);
		boolean canChangePermissions = false;
		widget.configure(mockEntity, canChangePermissions);
		
		verify(mockView, never()).showChangeLink();
	}
	
	@Test
	public void testConfigureLoggedInControlled() {
		widget.setShowFlagLink(true);
		when(mockRestrictionInformation.getRestrictionLevel()).thenReturn(RestrictionLevel.CONTROLLED_BY_ACT);
		when(mockRestrictionInformation.getHasUnmetAccessRequirement()).thenReturn(true);
		
		boolean canChangePermissions = true;
		widget.configure(mockEntity, canChangePermissions);
		
		verify(mockView).clear();
		verify(mockView).showControlledUseUI();
		verify(mockView).showUnmetRequirementsIcon();
		//has existing terms, has "show" link
		verify(mockView).showShowUnmetLink();
		verify(mockView).showFlagUI();
	}
	
	@Test
	public void testConfigureAnonymousControlled() {
		widget.setShowFlagLink(true);
		when(mockAuthenticationController.isLoggedIn()).thenReturn(false);

		when(mockRestrictionInformation.getRestrictionLevel()).thenReturn(RestrictionLevel.CONTROLLED_BY_ACT);
		when(mockRestrictionInformation.getHasUnmetAccessRequirement()).thenReturn(true);
		
		boolean canChangePermissions = true;
		widget.configure(mockEntity, canChangePermissions);
		
		verify(mockView).clear();
		verify(mockView).showControlledUseUI();
		verify(mockView).showUnmetRequirementsIcon();

		verify(mockView).showShowUnmetLink();
		verify(mockView).showAnonymousFlagUI();
	}
	
	@Test
	public void testConfigureLoggedInControlledMet() {
		when(mockRestrictionInformation.getRestrictionLevel()).thenReturn(RestrictionLevel.CONTROLLED_BY_ACT);
		when(mockRestrictionInformation.getHasUnmetAccessRequirement()).thenReturn(false);
		
		boolean canChangePermissions = true;
		widget.configure(mockEntity, canChangePermissions);
		
		verify(mockView).clear();
		verify(mockView).showControlledUseUI();
		verify(mockView).showMetRequirementsIcon();
		verify(mockView).showShowLink();
		
		widget.linkClicked();
		verify(mockPlaceChanger).goTo(any(AccessRequirementsPlace.class));
	}
	@Test
	public void testConfigureLoggedInOpen() {
		widget.setShowChangeLink(true);
		widget.setShowFlagLink(true);
		when(mockRestrictionInformation.getRestrictionLevel()).thenReturn(RestrictionLevel.OPEN);
		boolean canChangePermissions = true;
		widget.configure(mockEntity, canChangePermissions);
		
		verify(mockView).clear();
		verify(mockView).showNoRestrictionsUI();

		verify(mockView).showChangeLink();
		verify(mockView).showFlagUI();
	}
	
	@Test
	public void testConfigureAnonymousOpen() {
		//no access restrictions, anonymous
		widget.setShowFlagLink(true);
		when(mockAuthenticationController.isLoggedIn()).thenReturn(false);
		when(mockRestrictionInformation.getRestrictionLevel()).thenReturn(RestrictionLevel.OPEN);
		
		boolean canChangePermissions = true;
		widget.configure(mockEntity, canChangePermissions);
		
		verify(mockView).clear();
		verify(mockView).showNoRestrictionsUI();

		verify(mockView, never()).showChangeLink();
		verify(mockView).showAnonymousFlagUI();
	}
	
	@Test
	public void testImposeRestrictionClickedNoSelectionNulls() {
		when(mockView.isNoHumanDataRadioSelected()).thenReturn(null);
		when(mockView.isYesHumanDataRadioSelected()).thenReturn(null);
		widget.imposeRestrictionOkClicked();
		verify(mockView).showErrorMessage(anyString());
		widget.imposeRestrictionCancelClicked();
		verify(mockView).setImposeRestrictionModalVisible(false);
	}

	@Test
	public void testImposeRestrictionClickedNoSelection2() {
		when(mockView.isNoHumanDataRadioSelected()).thenReturn(false);
		when(mockView.isYesHumanDataRadioSelected()).thenReturn(false);
		widget.imposeRestrictionOkClicked();
		verify(mockView).showErrorMessage(anyString());
		widget.imposeRestrictionCancelClicked();
		verify(mockView).setImposeRestrictionModalVisible(false);
	}
	
	@Test
	public void testImposeRestrictionClickedNoIsSelected() {
		when(mockView.isNoHumanDataRadioSelected()).thenReturn(true);
		when(mockView.isYesHumanDataRadioSelected()).thenReturn(false);
		widget.imposeRestrictionOkClicked();
		verify(mockView).showErrorMessage(anyString());
		widget.imposeRestrictionCancelClicked();
		verify(mockView).setImposeRestrictionModalVisible(false);
	}
	
	@Test
	public void testImposeRestrictionClickedYesIsSelected() {
		when(mockView.isNoHumanDataRadioSelected()).thenReturn(false);
		when(mockView.isYesHumanDataRadioSelected()).thenReturn(true);
		when(mockRestrictionInformation.getRestrictionLevel()).thenReturn(RestrictionLevel.OPEN);
		boolean canChangePermissions = true;
		widget.configure(mockEntity, canChangePermissions);
		widget.imposeRestrictionOkClicked();
		verify(mockView).showLoading();
		verify(mockView).setImposeRestrictionModalVisible(false);
		verify(mockDataAccessClient).createLockAccessRequirement(eq(ENTITY_ID), any(AsyncCallback.class));
	}

	@Test
	public void testImposeRestrictionCancelButtonClicked() {
		when(mockView.isNoHumanDataRadioSelected()).thenReturn(false);
		when(mockView.isYesHumanDataRadioSelected()).thenReturn(true);
		widget.imposeRestrictionCancelClicked();
		verify(mockView, Mockito.never()).showLoading();
		verify(mockDataAccessClient, never()).createLockAccessRequirement(anyString(), any(AsyncCallback.class));
		verify(mockView).setImposeRestrictionModalVisible(false);
	}

	@Test
	public void testFlagData() {
		widget.flagData();
		verify(mockView).open(anyString());
	}
	
	@Test
	public void testAnonymousFlagModalOkClicked() {
		widget.anonymousFlagModalOkClicked();
		verify(mockPlaceChanger).goTo(isA(LoginPlace.class));
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
