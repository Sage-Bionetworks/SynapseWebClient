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
import org.sagebionetworks.repo.model.UserSessionData;
import org.sagebionetworks.repo.model.dataaccess.ManagedACTAccessRequirementStatus;
import org.sagebionetworks.repo.model.dataaccess.SubmissionState;
import org.sagebionetworks.repo.model.dataaccess.SubmissionStatus;
import org.sagebionetworks.web.client.DataAccessClientAsync;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.accessrequirements.CreateAccessRequirementButton;
import org.sagebionetworks.web.client.widget.accessrequirements.DeleteAccessRequirementButton;
import org.sagebionetworks.web.client.widget.accessrequirements.ManageAccessButton;
import org.sagebionetworks.web.client.widget.accessrequirements.ManagedACTAccessRequirementWidget;
import org.sagebionetworks.web.client.widget.accessrequirements.ManagedACTAccessRequirementWidgetView;
import org.sagebionetworks.web.client.widget.accessrequirements.SubjectsWidget;
import org.sagebionetworks.web.client.widget.accessrequirements.requestaccess.CreateDataAccessRequestWizard;
import org.sagebionetworks.web.client.widget.entity.WikiPageWidget;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.client.widget.lazyload.LazyLoadHelper;
import org.sagebionetworks.web.client.widget.table.modal.wizard.ModalWizardWidget.WizardCallback;
import org.sagebionetworks.web.client.widget.user.UserBadge;
import org.sagebionetworks.web.shared.WikiPageKey;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;

public class ManagedACTAccessRequirementWidgetTest {
	ManagedACTAccessRequirementWidget widget;
	@Mock
	ManagedACTAccessRequirementWidgetView mockView; 
	@Mock
	SynapseClientAsync mockSynapseClient;
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
	ManageAccessButton mockManageAccessButton;
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
	UserSessionData mockUserSessionData;
	@Mock
	UserProfile mockProfile;
	@Mock
	SubmissionStatus mockSubmissionStatus;
	@Mock
	SynapseJSNIUtils mockJsniUtils;
	
	Callback lazyLoadDataCallback;
	
	public final static String ROOT_WIKI_ID = "777";
	public final static String SUBMISSION_ID = "442";
	public final static String SUBMITTER_ID = "9";
	
	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		widget = new ManagedACTAccessRequirementWidget(mockView, mockSynapseClient, mockWikiPageWidget, mockSynAlert, mockGinInjector, mockSubjectsWidget, mockCreateAccessRequirementButton, mockDeleteAccessRequirementButton, mockManageAccessButton, mockDataAccessClient, mockLazyLoadHelper, mockAuthController, mockSubmitterUserBadge, mockJsniUtils);
		when(mockGinInjector.getCreateDataAccessRequestWizard()).thenReturn(mockCreateDataAccessRequestWizard);
		when(mockACTAccessRequirement.getSubjectIds()).thenReturn(mockSubjectIds);
		AsyncMockStubber.callSuccessWith(ROOT_WIKI_ID).when(mockSynapseClient).getRootWikiId(anyString(), anyString(), any(AsyncCallback.class));
		verify(mockLazyLoadHelper).configure(callbackCaptor.capture(), eq(mockView));
		lazyLoadDataCallback = callbackCaptor.getValue();
		AsyncMockStubber.callSuccessWith(mockDataAccessSubmissionStatus).when(mockDataAccessClient).getAccessRequirementStatus(anyString(), any(AsyncCallback.class));
		when(mockDataAccessSubmissionStatus.getCurrentSubmissionStatus()).thenReturn(mockSubmissionStatus);
		when(mockSubmissionStatus.getSubmissionId()).thenReturn(SUBMISSION_ID);
		when(mockSubmissionStatus.getSubmittedBy()).thenReturn(SUBMITTER_ID);
		when(mockAuthController.getCurrentUserSessionData()).thenReturn(mockUserSessionData);
		when(mockUserSessionData.getProfile()).thenReturn(mockProfile);
		when(mockProfile.getEmails()).thenReturn(Collections.singletonList("email@email.com"));
		when(mockSubjectIds.get(anyInt())).thenReturn(new RestrictableObjectDescriptor());
	}

	@Test
	public void testConstruction() {
		verify(mockView).setPresenter(widget);
		verify(mockView).setWikiTermsWidget(any(Widget.class));
		verify(mockView).setEditAccessRequirementWidget(any(Widget.class));
		verify(mockWikiPageWidget).setModifiedCreatedByHistoryVisible(false);
	}

	@Test
	public void testSetRequirementWithWikiTerms() {
		widget.setRequirement(mockACTAccessRequirement);
		verify(mockWikiPageWidget).configure(any(WikiPageKey.class), eq(false), any(WikiPageWidget.Callback.class), eq(false));
	}
	
	@Test
	public void testSubmittedState() {
		when(mockAuthController.getCurrentUserPrincipalId()).thenReturn(SUBMITTER_ID);
		widget.setRequirement(mockACTAccessRequirement);
		when(mockSubmissionStatus.getState()).thenReturn(SubmissionState.SUBMITTED);
		lazyLoadDataCallback.invoke();
		verify(mockView).showUnapprovedHeading();
		verify(mockView).showRequestSubmittedMessage();
		verify(mockView).showCancelRequestButton();
	}
	

	@Test
	public void testSubmittedStateByAnotherUser() {
		when(mockAuthController.getCurrentUserPrincipalId()).thenReturn("different id");
		widget.setRequirement(mockACTAccessRequirement);
		when(mockSubmissionStatus.getState()).thenReturn(SubmissionState.SUBMITTED);
		lazyLoadDataCallback.invoke();
		verify(mockView).showUnapprovedHeading();
		verify(mockSubmitterUserBadge).configure(SUBMITTER_ID);
		verify(mockView).showRequestSubmittedByOtherUser();
		verify(mockView, never()).showCancelRequestButton();
	}
	
	@Test
	public void testApprovedState() {
		widget.setRequirement(mockACTAccessRequirement);
		when(mockSubmissionStatus.getState()).thenReturn(SubmissionState.APPROVED);
		lazyLoadDataCallback.invoke();
		verify(mockView).showApprovedHeading();
		verify(mockView).showRequestApprovedMessage();
		verify(mockView).showUpdateRequestButton();
		verify(mockView, never()).showExpirationDate(anyString());
	}
	
	@Test
	public void testApprovedStateWithExpiration() {
		String friendlyDate = "June 9th, 2018";
		widget.setRequirement(mockACTAccessRequirement);
		when(mockDataAccessSubmissionStatus.getExpiredOn()).thenReturn(new Date());
		when(mockJsniUtils.getLongFriendlyDate(any(Date.class))).thenReturn(friendlyDate);
		when(mockSubmissionStatus.getState()).thenReturn(SubmissionState.APPROVED);
		lazyLoadDataCallback.invoke();
		verify(mockView).showApprovedHeading();
		verify(mockView).showRequestApprovedMessage();
		verify(mockView).showUpdateRequestButton();
		verify(mockView).showExpirationDate(friendlyDate);
	}
	
	@Test
	public void testRejectedState() {
		String rejectedReason = "Please sign";
		widget.setRequirement(mockACTAccessRequirement);
		when(mockSubmissionStatus.getState()).thenReturn(SubmissionState.REJECTED);
		when(mockSubmissionStatus.getRejectedReason()).thenReturn(rejectedReason);
		lazyLoadDataCallback.invoke();
		verify(mockView).showUnapprovedHeading();
		verify(mockView).showRequestRejectedMessage(rejectedReason);
		verify(mockView).showUpdateRequestButton();
	}
	
	@Test
	public void testCancelledState() {
		widget.setRequirement(mockACTAccessRequirement);
		when(mockSubmissionStatus.getState()).thenReturn(SubmissionState.CANCELLED);
		lazyLoadDataCallback.invoke();
		verify(mockView).showUnapprovedHeading();
		verify(mockView).showRequestAccessButton();
	}
	
	@Test
	public void testGetSubmissionStatusError() {
		Exception ex = new Exception();
		AsyncMockStubber.callFailureWith(ex).when(mockDataAccessClient).getAccessRequirementStatus(anyString(), any(AsyncCallback.class));
		widget.setRequirement(mockACTAccessRequirement);
		lazyLoadDataCallback.invoke();
		verify(mockSynAlert).handleException(ex);
	}
	
	@Test
	public void testCancel() {
		AsyncMockStubber.callSuccessWith(null).when(mockDataAccessClient).cancelDataAccessSubmission(anyString(), any(AsyncCallback.class));
		
		widget.setRequirement(mockACTAccessRequirement);
		when(mockSubmissionStatus.getState()).thenReturn(SubmissionState.APPROVED);
		lazyLoadDataCallback.invoke();
		
		widget.onCancelRequest();
		verify(mockDataAccessClient).cancelDataAccessSubmission(eq(SUBMISSION_ID), any(AsyncCallback.class));
		//refreshes status after cancel
		verify(mockDataAccessClient, times(2)).getAccessRequirementStatus(anyString(), any(AsyncCallback.class));
	}
	
	@Test
	public void testCancelFailure() {
		Exception ex = new Exception();
		AsyncMockStubber.callFailureWith(ex).when(mockDataAccessClient).cancelDataAccessSubmission(anyString(), any(AsyncCallback.class));
		
		widget.setRequirement(mockACTAccessRequirement);
		when(mockSubmissionStatus.getState()).thenReturn(SubmissionState.APPROVED);
		lazyLoadDataCallback.invoke();
		
		widget.onCancelRequest();
		verify(mockDataAccessClient).cancelDataAccessSubmission(eq(SUBMISSION_ID), any(AsyncCallback.class));
		verify(mockSynAlert).handleException(ex);
	}
	
	@Test
	public void testRequestAccess() {
		when(mockDataAccessSubmissionStatus.getCurrentSubmissionStatus()).thenReturn(null);
		widget.setRequirement(mockACTAccessRequirement);
		lazyLoadDataCallback.invoke();
		
		widget.onRequestAccess();
		verify(mockCreateDataAccessRequestWizard).configure(mockACTAccessRequirement);
		verify(mockCreateDataAccessRequestWizard).showModal(any(WizardCallback.class));
	}
}
