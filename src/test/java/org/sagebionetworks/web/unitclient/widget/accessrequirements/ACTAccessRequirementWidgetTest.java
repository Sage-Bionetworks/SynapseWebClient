package org.sagebionetworks.web.unitclient.widget.accessrequirements;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.sagebionetworks.web.shared.WebConstants.GRANT_ACCESS_REQUEST_COMPONENT_ID;
import static org.sagebionetworks.web.shared.WebConstants.ISSUE_PRIORITY_MINOR;
import static org.sagebionetworks.web.shared.WebConstants.REQUEST_ACCESS_ISSUE_COLLECTOR_URL;
import static org.sagebionetworks.web.shared.WebConstants.REQUEST_ACCESS_ISSUE_DESCRIPTION;
import static org.sagebionetworks.web.shared.WebConstants.REQUEST_ACCESS_ISSUE_SUMMARY;
import java.util.Collections;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.sagebionetworks.repo.model.ACTAccessRequirement;
import org.sagebionetworks.repo.model.RestrictableObjectDescriptor;
import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.repo.model.dataaccess.BasicAccessRequirementStatus;
import org.sagebionetworks.web.client.DataAccessClientAsync;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.PopupUtilsView;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.SynapseJavascriptClient;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.accessrequirements.ACTAccessRequirementWidget;
import org.sagebionetworks.web.client.widget.accessrequirements.ACTAccessRequirementWidgetView;
import org.sagebionetworks.web.client.widget.accessrequirements.ConvertACTAccessRequirementButton;
import org.sagebionetworks.web.client.widget.accessrequirements.CreateAccessRequirementButton;
import org.sagebionetworks.web.client.widget.accessrequirements.DeleteAccessRequirementButton;
import org.sagebionetworks.web.client.widget.accessrequirements.ReviewAccessorsButton;
import org.sagebionetworks.web.client.widget.accessrequirements.SubjectsWidget;
import org.sagebionetworks.web.client.widget.accessrequirements.requestaccess.CreateDataAccessRequestWizard;
import org.sagebionetworks.web.client.widget.entity.WikiPageWidget;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.client.widget.lazyload.LazyLoadHelper;
import org.sagebionetworks.web.client.widget.user.UserBadge;
import org.sagebionetworks.web.shared.WikiPageKey;
import org.sagebionetworks.web.shared.exceptions.NotFoundException;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;

@RunWith(MockitoJUnitRunner.class)
public class ACTAccessRequirementWidgetTest {
	public static final String SUBJECT_OBJECT_ID = "syn981612";
	public static final Long ACCESS_REQUIREMENT_ID = 7654L;
	public static final String USER_EMAIL = "email@email.com";
	public static final String USER_ID = "123";
	public static final String USERNAME = "Clue";
	public static final String FIRST_NAME = "Professor";
	public static final String LAST_NAME = "Plum";

	ACTAccessRequirementWidget widget;
	@Mock
	ACTAccessRequirementWidgetView mockView;
	@Mock
	SynapseJavascriptClient mockJsClient;
	@Mock
	DataAccessClientAsync mockDataAccessClient;
	@Mock
	WikiPageWidget mockWikiPageWidget;
	@Mock
	SynapseAlert mockSynAlert;
	@Mock
	PortalGinInjector mockGinInjector;
	@Mock
	CreateDataAccessRequestWizard mockCreateDataAccessRequestWizard;
	@Mock
	ACTAccessRequirement mockACTAccessRequirement;
	@Mock
	CreateAccessRequirementButton mockCreateAccessRequirementButton;
	@Mock
	DeleteAccessRequirementButton mockDeleteAccessRequirementButton;
	@Mock
	SubjectsWidget mockSubjectsWidget;
	@Mock
	LazyLoadHelper mockLazyLoadHelper;
	@Mock
	List<RestrictableObjectDescriptor> mockSubjectIds;
	@Captor
	ArgumentCaptor<Callback> callbackCaptor;
	@Mock
	BasicAccessRequirementStatus mockDataAccessSubmissionStatus;
	@Mock
	AuthenticationController mockAuthController;
	@Mock
	UserBadge mockSubmitterUserBadge;
	@Mock
	PopupUtilsView mockPopupUtils;
	@Mock
	UserProfile mockProfile;
	@Mock
	ReviewAccessorsButton mockManageAccessButton;
	@Mock
	ConvertACTAccessRequirementButton mockConvertACTAccessRequirementButton;
	@Mock
	Callback mockRefreshCallback;
	@Mock
	RestrictableObjectDescriptor mockRestrictableObjectDescriptor;
	@Mock
	SynapseJSNIUtils mockJsniUtils;
	Callback lazyLoadDataCallback;

	public final static String ROOT_WIKI_ID = "777";

	@Before
	public void setUp() throws Exception {
		widget = new ACTAccessRequirementWidget(mockView, mockJsClient, mockWikiPageWidget, mockSynAlert, mockGinInjector, mockSubjectsWidget, mockCreateAccessRequirementButton, mockDeleteAccessRequirementButton, mockDataAccessClient, mockLazyLoadHelper, mockAuthController, mockManageAccessButton, mockConvertACTAccessRequirementButton, mockJsniUtils);
		when(mockGinInjector.getCreateDataAccessRequestWizard()).thenReturn(mockCreateDataAccessRequestWizard);
		when(mockACTAccessRequirement.getSubjectIds()).thenReturn(mockSubjectIds);
		AsyncMockStubber.callSuccessWith(ROOT_WIKI_ID).when(mockJsClient).getRootWikiPageKey(anyString(), anyString(), any(AsyncCallback.class));
		verify(mockLazyLoadHelper).configure(callbackCaptor.capture(), eq(mockView));
		lazyLoadDataCallback = callbackCaptor.getValue();
		AsyncMockStubber.callSuccessWith(mockDataAccessSubmissionStatus).when(mockDataAccessClient).getAccessRequirementStatus(anyString(), any(AsyncCallback.class));
		when(mockDataAccessSubmissionStatus.getIsApproved()).thenReturn(false);
		when(mockAuthController.getCurrentUserProfile()).thenReturn(mockProfile);
		when(mockProfile.getOwnerId()).thenReturn(USER_ID);
		when(mockProfile.getEmails()).thenReturn(Collections.singletonList(USER_EMAIL));
		when(mockProfile.getFirstName()).thenReturn(FIRST_NAME);
		when(mockProfile.getLastName()).thenReturn(LAST_NAME);
		when(mockProfile.getUserName()).thenReturn(USERNAME);
		when(mockSubjectIds.get(anyInt())).thenReturn(mockRestrictableObjectDescriptor);
		when(mockAuthController.isLoggedIn()).thenReturn(true);
		when(mockACTAccessRequirement.getId()).thenReturn(ACCESS_REQUIREMENT_ID);
		when(mockRestrictableObjectDescriptor.getId()).thenReturn(SUBJECT_OBJECT_ID);
	}

	@Test
	public void testConstruction() {
		verify(mockView).setPresenter(widget);
		verify(mockView).setWikiTermsWidget(any(Widget.class));
		verify(mockView).setEditAccessRequirementWidget(any(Widget.class));
		verify(mockWikiPageWidget).setModifiedCreatedByHistoryVisible(false);
	}

	@Test
	public void testSetRequirementWithContactInfoTerms() {
		AsyncMockStubber.callFailureWith(new NotFoundException()).when(mockJsClient).getRootWikiPageKey(anyString(), anyString(), any(AsyncCallback.class));
		String tou = "must do things before access is allowed";
		when(mockACTAccessRequirement.getActContactInfo()).thenReturn(tou);
		widget.setRequirement(mockACTAccessRequirement, mockRefreshCallback);
		verify(mockView).setTerms(tou);
		verify(mockView).showTermsUI();
		verify(mockCreateAccessRequirementButton).configure(mockACTAccessRequirement, mockRefreshCallback);
		verify(mockDeleteAccessRequirementButton).configure(mockACTAccessRequirement, mockRefreshCallback);
		verify(mockSubjectsWidget).configure(mockSubjectIds);
		verify(mockLazyLoadHelper).setIsConfigured();
	}

	@Test
	public void testSetRequirementWithWikiTerms() {
		widget.setRequirement(mockACTAccessRequirement, mockRefreshCallback);
		verify(mockWikiPageWidget).configure(any(WikiPageKey.class), eq(false), any(WikiPageWidget.Callback.class));
		verify(mockView, never()).setTerms(anyString());
		verify(mockView, never()).showTermsUI();
	}

	@Test
	public void testApprovedState() {
		widget.setRequirement(mockACTAccessRequirement, mockRefreshCallback);
		when(mockDataAccessSubmissionStatus.getIsApproved()).thenReturn(true);
		lazyLoadDataCallback.invoke();
		verify(mockView).showApprovedHeading();
		verify(mockView).showRequestApprovedMessage();
	}

	@Test
	public void testAnonymous() {
		when(mockAuthController.isLoggedIn()).thenReturn(false);
		widget.setRequirement(mockACTAccessRequirement, mockRefreshCallback);
		lazyLoadDataCallback.invoke();
		verify(mockView).showUnapprovedHeading();
		verify(mockView).showLoginButton();
	}

	@Test
	public void testUnApprovedStateNoOpenJira() {
		when(mockACTAccessRequirement.getOpenJiraIssue()).thenReturn(false);
		when(mockDataAccessSubmissionStatus.getIsApproved()).thenReturn(false);
		widget.setRequirement(mockACTAccessRequirement, mockRefreshCallback);
		lazyLoadDataCallback.invoke();
		verify(mockView).showUnapprovedHeading();
		verify(mockView, never()).showRequestAccessButton();
	}

	@Test
	public void testUnApprovedStateNullOpenJira() {
		when(mockACTAccessRequirement.getOpenJiraIssue()).thenReturn(null);
		when(mockDataAccessSubmissionStatus.getIsApproved()).thenReturn(false);
		widget.setRequirement(mockACTAccessRequirement, mockRefreshCallback);
		lazyLoadDataCallback.invoke();
		verify(mockView).showUnapprovedHeading();
		verify(mockView, never()).showRequestAccessButton();
	}

	@Test
	public void testUnApprovedStateNoDataAccessRequestWithOpenJiraIssue() {
		widget.setRequirement(mockACTAccessRequirement, mockRefreshCallback);
		when(mockACTAccessRequirement.getOpenJiraIssue()).thenReturn(true);
		when(mockDataAccessSubmissionStatus.getIsApproved()).thenReturn(false);
		lazyLoadDataCallback.invoke();
		verify(mockView).showUnapprovedHeading();
		verify(mockView).showRequestAccessButton();
	}

	@Test
	public void testRequestAccess() {
		widget.setRequirement(mockACTAccessRequirement, mockRefreshCallback);
		lazyLoadDataCallback.invoke();

		widget.onRequestAccess();

		verify(mockJsniUtils).showJiraIssueCollector(REQUEST_ACCESS_ISSUE_SUMMARY, REQUEST_ACCESS_ISSUE_DESCRIPTION, REQUEST_ACCESS_ISSUE_COLLECTOR_URL, USER_ID, DisplayUtils.getDisplayName(FIRST_NAME, LAST_NAME, USERNAME), USER_EMAIL, SUBJECT_OBJECT_ID, GRANT_ACCESS_REQUEST_COMPONENT_ID, ACCESS_REQUIREMENT_ID.toString(), ISSUE_PRIORITY_MINOR);
	}
}
