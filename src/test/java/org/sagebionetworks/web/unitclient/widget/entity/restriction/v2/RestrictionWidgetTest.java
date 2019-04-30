package org.sagebionetworks.web.unitclient.widget.entity.restriction.v2;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.repo.model.RestrictableObjectType;
import org.sagebionetworks.repo.model.RestrictionInformationResponse;
import org.sagebionetworks.repo.model.RestrictionLevel;
import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.repo.model.UserSessionData;
import org.sagebionetworks.web.client.DataAccessClientAsync;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.PlaceChanger;
import org.sagebionetworks.web.client.SynapseJavascriptClient;
import org.sagebionetworks.web.client.place.LoginPlace;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.asynch.IsACTMemberAsyncHandler;
import org.sagebionetworks.web.client.widget.entity.JiraURLHelper;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.client.widget.entity.restriction.v2.RestrictionWidget;
import org.sagebionetworks.web.client.widget.entity.restriction.v2.RestrictionWidgetView;
import org.sagebionetworks.web.client.widget.footer.Footer;
import org.sagebionetworks.web.client.widget.footer.VersionState;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.IsWidget;

public class RestrictionWidgetTest {

	RestrictionWidget widget;
	@Mock
	AuthenticationController mockAuthenticationController;
	@Mock
	RestrictionWidgetView mockView;
	@Mock
	DataAccessClientAsync mockDataAccessClient;
	@Mock
	SynapseAlert mockSynAlert;
	@Mock
	RestrictionInformationResponse mockRestrictionInformation;
	@Mock
	Entity mockEntity;
	@Mock
	IsACTMemberAsyncHandler mockIsACTMemberAsyncHandler;
	@Mock
	SynapseJavascriptClient mockSynapseJavascriptClient;
	@Captor
	ArgumentCaptor<CallbackP<Boolean>> callbackPCaptor;
	public static final String ENTITY_ID = "syn762";
	@Mock
	UserProfile mockUserProfile;
	
	public static final String OWNER_ID = "282711";
	public static final String FIRST_NAME = "Bob";
	public static final String LAST_NAME = "Vance";
	public static final String USERNAME = "bvance";
	public static final String EMAIL = "bob@vancerefrigeration.com";
	
	@Before
	public void before() {
		MockitoAnnotations.initMocks(this);
		widget = new RestrictionWidget(
				mockView, 
				mockAuthenticationController, 
				mockDataAccessClient, 
				mockSynAlert, 
				mockIsACTMemberAsyncHandler,
				mockSynapseJavascriptClient);
		when(mockAuthenticationController.getCurrentUserProfile()).thenReturn(mockUserProfile);
		when(mockAuthenticationController.isLoggedIn()).thenReturn(true);
		AsyncMockStubber.callSuccessWith(mockRestrictionInformation).when(mockSynapseJavascriptClient).getRestrictionInformation(anyString(), any(RestrictableObjectType.class), any(AsyncCallback.class));
		when(mockEntity.getId()).thenReturn(ENTITY_ID);
		when(mockUserProfile.getEmails()).thenReturn(Collections.singletonList(EMAIL));
		when(mockUserProfile.getFirstName()).thenReturn(FIRST_NAME);
		when(mockUserProfile.getLastName()).thenReturn(LAST_NAME);
		when(mockUserProfile.getUserName()).thenReturn(USERNAME);
		when(mockUserProfile.getOwnerId()).thenReturn(OWNER_ID);
	}
	
	private void verifyIsACTMember(boolean isACT) {
		verify(mockIsACTMemberAsyncHandler).isACTActionAvailable(callbackPCaptor.capture());
		callbackPCaptor.getValue().invoke(isACT);
	}
	
	@Test
	public void testConstruction() {
		verify(mockView).setSynAlert(any(IsWidget.class));
		verify(mockView).setPresenter(widget);
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
		verify(mockView).showFlagUI();
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
		verify(mockView).open(anyString());
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
		verify(mockView).showFlagUI();
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
		// no-op, just hide the dialog
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
	public void testReportIssueClicked() {
		when(mockRestrictionInformation.getRestrictionLevel()).thenReturn(RestrictionLevel.OPEN);
		widget.configure(mockEntity, true);
		
		widget.reportIssueClicked();
		
		verify(mockView).showJiraIssueCollector(OWNER_ID, DisplayUtils.getDisplayName(FIRST_NAME, LAST_NAME, USERNAME), EMAIL, ENTITY_ID);
	}
	
	@Test
	public void testNotHumanDataClicked() {
		widget.notHumanDataClicked();
		verify(mockView).setNotSensitiveHumanDataMessageVisible(true);
	}
	@Test
	public void testYesHumanDataClicked() {
		widget.yesHumanDataClicked();
		verify(mockView).setNotSensitiveHumanDataMessageVisible(false);
	}
	@Test
	public void testChangeLinkNotACT() {
		when(mockRestrictionInformation.getRestrictionLevel()).thenReturn(RestrictionLevel.OPEN);
		boolean canChangePermissions = true;
		widget.configure(mockEntity, canChangePermissions);
		
		widget.changeClicked();
		verifyIsACTMember(false);
		verify(mockView).showVerifyDataSensitiveDialog();
	}
	@Test
	public void testChangeLinkIsACT() {
		when(mockRestrictionInformation.getRestrictionLevel()).thenReturn(RestrictionLevel.OPEN);
		boolean canChangePermissions = true;
		widget.configure(mockEntity, canChangePermissions);
		
		widget.changeClicked();
		verifyIsACTMember(true);
		verify(mockView).open(anyString());
	}
}
