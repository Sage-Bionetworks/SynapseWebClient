package org.sagebionetworks.web.unitclient.widget.accessrequirements;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.repo.model.ManagedACTAccessRequirement;
import org.sagebionetworks.repo.model.RestrictableObjectDescriptor;
import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.repo.model.dataaccess.ManagedACTAccessRequirementStatus;
import org.sagebionetworks.repo.model.dataaccess.SubmissionState;
import org.sagebionetworks.repo.model.dataaccess.SubmissionStatus;
import org.sagebionetworks.web.client.DataAccessClientAsync;
import org.sagebionetworks.web.client.DateTimeUtils;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.SynapseJavascriptClient;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.accessrequirements.CreateAccessRequirementButton;
import org.sagebionetworks.web.client.widget.accessrequirements.DeleteAccessRequirementButton;
import org.sagebionetworks.web.client.widget.accessrequirements.IntendedDataUseReportButton;
import org.sagebionetworks.web.client.widget.accessrequirements.ManagedACTAccessRequirementWidget;
import org.sagebionetworks.web.client.widget.accessrequirements.ManagedACTAccessRequirementWidgetView;
import org.sagebionetworks.web.client.widget.accessrequirements.ReviewAccessRequestsButton;
import org.sagebionetworks.web.client.widget.accessrequirements.ReviewAccessorsButton;
import org.sagebionetworks.web.client.widget.accessrequirements.SubjectsWidget;
import org.sagebionetworks.web.client.widget.accessrequirements.requestaccess.CreateDataAccessRequestWizard;
import org.sagebionetworks.web.client.widget.entity.WikiPageWidget;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.client.widget.lazyload.LazyLoadHelper;
import org.sagebionetworks.web.client.widget.table.modal.wizard.ModalWizardWidget.WizardCallback;
import org.sagebionetworks.web.client.widget.user.UserBadge;
import org.sagebionetworks.web.shared.WikiPageKey;
import org.sagebionetworks.web.shared.exceptions.NotFoundException;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;

public class ManagedACTAccessRequirementWidgetTest {
	ManagedACTAccessRequirementWidget widget;
	@Mock
	ManagedACTAccessRequirementWidgetView mockView;
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
	ManagedACTAccessRequirement mockACTAccessRequirement;
	@Mock
	CreateAccessRequirementButton mockCreateAccessRequirementButton;
	@Mock
	DeleteAccessRequirementButton mockDeleteAccessRequirementButton;
	@Mock
	ReviewAccessRequestsButton mockReviewAccessRequestsButton;
	@Mock
	SubjectsWidget mockSubjectsWidget;
	@Mock
	LazyLoadHelper mockLazyLoadHelper;
	@Mock
	List<RestrictableObjectDescriptor> mockSubjectIds;
	@Captor
	ArgumentCaptor<Callback> callbackCaptor;
	@Mock
	ManagedACTAccessRequirementStatus mockDataAccessSubmissionStatus;
	@Mock
	AuthenticationController mockAuthController;
	@Mock
	UserBadge mockSubmitterUserBadge;
	@Mock
	UserProfile mockProfile;
	@Mock
	SubmissionStatus mockSubmissionStatus;
	@Mock
	SynapseJSNIUtils mockJsniUtils;
	@Mock
	DateTimeUtils mockDateTimeUtils;
	@Mock
	ReviewAccessorsButton mockManageAccessButton;
	@Mock
	Callback mockRefreshCallback;
	@Mock
	RestrictableObjectDescriptor mockSubject;
	@Mock
	IntendedDataUseReportButton mockIduReportButton;
	Callback lazyLoadDataCallback;

	public final static String ROOT_WIKI_ID = "777";
	public final static String SUBMISSION_ID = "442";
	public final static String SUBMITTER_ID = "9";

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		widget = new ManagedACTAccessRequirementWidget(mockView, mockJsClient, mockWikiPageWidget, mockSynAlert, mockGinInjector, mockSubjectsWidget, mockCreateAccessRequirementButton, mockDeleteAccessRequirementButton, mockReviewAccessRequestsButton, mockIduReportButton, mockDataAccessClient, mockLazyLoadHelper, mockAuthController, mockSubmitterUserBadge, mockDateTimeUtils, mockManageAccessButton);
		when(mockGinInjector.getCreateDataAccessRequestWizard()).thenReturn(mockCreateDataAccessRequestWizard);
		when(mockACTAccessRequirement.getSubjectIds()).thenReturn(mockSubjectIds);
		AsyncMockStubber.callSuccessWith(ROOT_WIKI_ID).when(mockJsClient).getRootWikiPageKey(anyString(), anyString(), any(AsyncCallback.class));
		verify(mockLazyLoadHelper).configure(callbackCaptor.capture(), eq(mockView));
		lazyLoadDataCallback = callbackCaptor.getValue();
		AsyncMockStubber.callSuccessWith(mockDataAccessSubmissionStatus).when(mockDataAccessClient).getAccessRequirementStatus(anyString(), any(AsyncCallback.class));
		when(mockDataAccessSubmissionStatus.getCurrentSubmissionStatus()).thenReturn(mockSubmissionStatus);
		when(mockSubmissionStatus.getSubmissionId()).thenReturn(SUBMISSION_ID);
		when(mockSubmissionStatus.getSubmittedBy()).thenReturn(SUBMITTER_ID);
		when(mockAuthController.getCurrentUserProfile()).thenReturn(mockProfile);
		when(mockProfile.getEmails()).thenReturn(Collections.singletonList("email@email.com"));
		when(mockSubjectIds.get(anyInt())).thenReturn(new RestrictableObjectDescriptor());
		when(mockAuthController.isLoggedIn()).thenReturn(true);
	}

	@Test
	public void testConstruction() {
		verify(mockView).setPresenter(widget);
		verify(mockView).setWikiTermsWidget(any(Widget.class));
		verify(mockView).setEditAccessRequirementWidget(any(Widget.class));
		verify(mockWikiPageWidget).setModifiedCreatedByHistoryVisible(false);
		verify(mockView).setIDUReportButton(mockIduReportButton);
	}

	@Test
	public void testSetRequirement() {
		widget.setRequirement(mockACTAccessRequirement, mockRefreshCallback);

		verify(mockCreateAccessRequirementButton).configure(eq(mockACTAccessRequirement), any(Callback.class));
		verify(mockDeleteAccessRequirementButton).configure(eq(mockACTAccessRequirement), any(Callback.class));
		verify(mockIduReportButton).configure(mockACTAccessRequirement);
		verify(mockReviewAccessRequestsButton).configure(mockACTAccessRequirement);
		verify(mockManageAccessButton).configure(mockACTAccessRequirement);
		verify(mockSubjectsWidget).configure(mockSubjectIds);
		verify(mockLazyLoadHelper).setIsConfigured();
	}

	@Test
	public void testSetRequirementWithWikiTerms() {
		widget.setRequirement(mockACTAccessRequirement, mockRefreshCallback);
		verify(mockWikiPageWidget).configure(any(WikiPageKey.class), eq(false), any(WikiPageWidget.Callback.class));
		verify(mockView).setWikiTermsWidgetVisible(true);
	}

	@Test
	public void testSubmittedState() {
		when(mockAuthController.getCurrentUserPrincipalId()).thenReturn(SUBMITTER_ID);
		widget.setRequirement(mockACTAccessRequirement, mockRefreshCallback);
		when(mockSubmissionStatus.getState()).thenReturn(SubmissionState.SUBMITTED);
		lazyLoadDataCallback.invoke();
		verify(mockView).showUnapprovedHeading();
		verify(mockView).showRequestSubmittedMessage();
		verify(mockView).showCancelRequestButton();
	}


	@Test
	public void testSubmittedStateByAnotherUser() {
		when(mockAuthController.getCurrentUserPrincipalId()).thenReturn("different id");
		widget.setRequirement(mockACTAccessRequirement, mockRefreshCallback);
		when(mockSubmissionStatus.getState()).thenReturn(SubmissionState.SUBMITTED);
		lazyLoadDataCallback.invoke();
		verify(mockView).showUnapprovedHeading();
		verify(mockSubmitterUserBadge).configure(SUBMITTER_ID);
		verify(mockView).showRequestSubmittedByOtherUser();
		verify(mockView, never()).showCancelRequestButton();
	}

	@Test
	public void testApprovedState() {
		widget.setRequirement(mockACTAccessRequirement, mockRefreshCallback);
		when(mockDataAccessSubmissionStatus.getIsApproved()).thenReturn(true);
		when(mockSubmissionStatus.getState()).thenReturn(SubmissionState.APPROVED);
		lazyLoadDataCallback.invoke();
		verify(mockView).showApprovedHeading();
		verify(mockView).showRequestApprovedMessage();
		verify(mockView).showUpdateRequestButton();
		verify(mockView, never()).showExpirationDate(anyString());
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
	public void testApprovedStateWithExpiration() {
		String friendlyDate = "June 9th, 2018";
		widget.setRequirement(mockACTAccessRequirement, mockRefreshCallback);
		when(mockDataAccessSubmissionStatus.getExpiredOn()).thenReturn(new Date());
		when(mockDateTimeUtils.getLongFriendlyDate(any(Date.class))).thenReturn(friendlyDate);
		when(mockSubmissionStatus.getState()).thenReturn(SubmissionState.APPROVED);
		when(mockDataAccessSubmissionStatus.getIsApproved()).thenReturn(true);
		lazyLoadDataCallback.invoke();
		verify(mockView).showApprovedHeading();
		verify(mockView).showRequestApprovedMessage();
		verify(mockView).showUpdateRequestButton();
		verify(mockView).showExpirationDate(friendlyDate);
	}

	@Test
	public void testApprovedStateWithExpirationZero() {
		widget.setRequirement(mockACTAccessRequirement, mockRefreshCallback);
		when(mockDataAccessSubmissionStatus.getExpiredOn()).thenReturn(new Date(0));
		when(mockSubmissionStatus.getState()).thenReturn(SubmissionState.APPROVED);
		when(mockDataAccessSubmissionStatus.getIsApproved()).thenReturn(true);
		lazyLoadDataCallback.invoke();
		verify(mockView, never()).showExpirationDate(anyString());
	}

	@Test
	public void testRejectedState() {
		String rejectedReason = "Please sign";
		widget.setRequirement(mockACTAccessRequirement, mockRefreshCallback);
		when(mockSubmissionStatus.getState()).thenReturn(SubmissionState.REJECTED);
		when(mockSubmissionStatus.getRejectedReason()).thenReturn(rejectedReason);
		lazyLoadDataCallback.invoke();
		verify(mockView).showUnapprovedHeading();
		verify(mockView).showRequestRejectedMessage(rejectedReason);
		verify(mockView).showUpdateRequestButton();
	}

	@Test
	public void testCancelledState() {
		when(mockDataAccessSubmissionStatus.getIsApproved()).thenReturn(false);
		widget.setRequirement(mockACTAccessRequirement, mockRefreshCallback);
		when(mockSubmissionStatus.getState()).thenReturn(SubmissionState.CANCELLED);
		lazyLoadDataCallback.invoke();
		verify(mockView).showUnapprovedHeading();
		verify(mockView).showRequestAccessButton();
	}

	@Test
	public void testCancelledStatePreviouslyApproved() {
		// see SWC-3686
		when(mockDataAccessSubmissionStatus.getIsApproved()).thenReturn(true);
		widget.setRequirement(mockACTAccessRequirement, mockRefreshCallback);
		when(mockSubmissionStatus.getState()).thenReturn(SubmissionState.CANCELLED);
		lazyLoadDataCallback.invoke();
		verify(mockView).showApprovedHeading();
		verify(mockView).showRequestApprovedMessage();
		verify(mockView).showUpdateRequestButton();
	}

	@Test
	public void testGetSubmissionStatusError() {
		Exception ex = new Exception();
		AsyncMockStubber.callFailureWith(ex).when(mockDataAccessClient).getAccessRequirementStatus(anyString(), any(AsyncCallback.class));
		widget.setRequirement(mockACTAccessRequirement, mockRefreshCallback);
		lazyLoadDataCallback.invoke();
		verify(mockSynAlert).handleException(ex);
	}

	@Test
	public void testCancel() {
		AsyncMockStubber.callSuccessWith(null).when(mockDataAccessClient).cancelDataAccessSubmission(anyString(), any(AsyncCallback.class));

		widget.setRequirement(mockACTAccessRequirement, mockRefreshCallback);
		when(mockSubmissionStatus.getState()).thenReturn(SubmissionState.APPROVED);
		lazyLoadDataCallback.invoke();

		widget.onCancelRequest();
		verify(mockDataAccessClient).cancelDataAccessSubmission(eq(SUBMISSION_ID), any(AsyncCallback.class));
		// refreshes status after cancel
		verify(mockDataAccessClient, times(2)).getAccessRequirementStatus(anyString(), any(AsyncCallback.class));
	}

	@Test
	public void testCancelFailure() {
		Exception ex = new Exception();
		AsyncMockStubber.callFailureWith(ex).when(mockDataAccessClient).cancelDataAccessSubmission(anyString(), any(AsyncCallback.class));

		widget.setRequirement(mockACTAccessRequirement, mockRefreshCallback);
		when(mockSubmissionStatus.getState()).thenReturn(SubmissionState.APPROVED);
		lazyLoadDataCallback.invoke();

		widget.onCancelRequest();
		verify(mockDataAccessClient).cancelDataAccessSubmission(eq(SUBMISSION_ID), any(AsyncCallback.class));
		verify(mockSynAlert).handleException(ex);
	}

	@Test
	public void testRequestAccess() {
		when(mockDataAccessSubmissionStatus.getCurrentSubmissionStatus()).thenReturn(null);
		widget.setRequirement(mockACTAccessRequirement, mockRefreshCallback);
		widget.setTargetSubject(mockSubject);
		lazyLoadDataCallback.invoke();

		widget.onRequestAccess();
		verify(mockCreateDataAccessRequestWizard).configure(mockACTAccessRequirement, mockSubject);
		verify(mockCreateDataAccessRequestWizard).showModal(any(WizardCallback.class));
	}

	@Test
	public void testNoWiki() {
		AsyncMockStubber.callFailureWith(new NotFoundException()).when(mockJsClient).getRootWikiPageKey(anyString(), anyString(), any(AsyncCallback.class));
		widget.setRequirement(mockACTAccessRequirement, mockRefreshCallback);
		verify(mockView).setWikiTermsWidgetVisible(false);
		verify(mockView, never()).setWikiTermsWidgetVisible(true);
	}
}
