package org.sagebionetworks.web.unitclient.widget.accessrequirements;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.repo.model.ACTAccessRequirement;
import org.sagebionetworks.repo.model.RestrictableObjectDescriptor;
import org.sagebionetworks.repo.model.dataaccess.ACTAccessRequirementStatus;
import org.sagebionetworks.repo.model.dataaccess.DataAccessSubmissionState;
import org.sagebionetworks.web.client.DataAccessClientAsync;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.accessrequirements.ACTAccessRequirementWidget;
import org.sagebionetworks.web.client.widget.accessrequirements.ACTAccessRequirementWidgetView;
import org.sagebionetworks.web.client.widget.accessrequirements.CreateAccessRequirementButton;
import org.sagebionetworks.web.client.widget.accessrequirements.DeleteAccessRequirementButton;
import org.sagebionetworks.web.client.widget.accessrequirements.ManageAccessButton;
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

public class ACTAccessRequirementWidgetTest {
	ACTAccessRequirementWidget widget;
	@Mock
	ACTAccessRequirementWidgetView mockView; 
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
	ACTAccessRequirement mockACTAccessRequirement;
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
	ACTAccessRequirementStatus mockDataAccessSubmissionStatus;
	@Mock
	AuthenticationController mockAuthController;
	@Mock
	UserBadge mockSubmitterUserBadge;
	
	Callback lazyLoadDataCallback;
	
	public final static String ROOT_WIKI_ID = "777";
	public final static String SUBMISSION_ID = "442";
	public final static String SUBMITTER_ID = "9";
	
	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		widget = new ACTAccessRequirementWidget(mockView, mockSynapseClient, mockWikiPageWidget, mockSynAlert, mockGinInjector, mockSubjectsWidget, mockCreateAccessRequirementButton, mockDeleteAccessRequirementButton, mockManageAccessButton, mockDataAccessClient, mockLazyLoadHelper, mockAuthController, mockSubmitterUserBadge);
		when(mockGinInjector.getCreateDataAccessRequestWizard()).thenReturn(mockCreateDataAccessRequestWizard);
		when(mockACTAccessRequirement.getSubjectIds()).thenReturn(mockSubjectIds);
		AsyncMockStubber.callSuccessWith(ROOT_WIKI_ID).when(mockSynapseClient).getRootWikiId(anyString(), anyString(), any(AsyncCallback.class));
		verify(mockLazyLoadHelper).configure(callbackCaptor.capture(), eq(mockView));
		lazyLoadDataCallback = callbackCaptor.getValue();
		AsyncMockStubber.callSuccessWith(mockDataAccessSubmissionStatus).when(mockDataAccessClient).getAccessRequirementStatus(anyString(), any(AsyncCallback.class));
		when(mockDataAccessSubmissionStatus.getSubmissionId()).thenReturn(SUBMISSION_ID);
		when(mockDataAccessSubmissionStatus.getSubmittedBy()).thenReturn(SUBMITTER_ID);
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
		AsyncMockStubber.callFailureWith(new NotFoundException()).when(mockSynapseClient).getRootWikiId(anyString(), anyString(), any(AsyncCallback.class));
		String tou = "must do things before access is allowed";
		when(mockACTAccessRequirement.getActContactInfo()).thenReturn(tou);
		widget.setRequirement(mockACTAccessRequirement);
		verify(mockView).setTerms(tou);
		verify(mockView).showTermsUI();
		verify(mockCreateAccessRequirementButton).configure(mockACTAccessRequirement);
		verify(mockDeleteAccessRequirementButton).configure(mockACTAccessRequirement);
		verify(mockManageAccessButton).configure(mockACTAccessRequirement);
		boolean isHideIfLoadError = true;
		verify(mockSubjectsWidget).configure(mockSubjectIds, isHideIfLoadError);
		verify(mockLazyLoadHelper).setIsConfigured();
	}
	@Test
	public void testSetRequirementWithWikiTerms() {
		widget.setRequirement(mockACTAccessRequirement);
		verify(mockWikiPageWidget).configure(any(WikiPageKey.class), eq(false), any(WikiPageWidget.Callback.class), eq(false));
		verify(mockView, never()).setTerms(anyString());
		verify(mockView, never()).showTermsUI();
	}
	
	@Test
	public void testSubmittedState() {
		when(mockAuthController.getCurrentUserPrincipalId()).thenReturn(SUBMITTER_ID);
		widget.setRequirement(mockACTAccessRequirement);
		when(mockDataAccessSubmissionStatus.getState()).thenReturn(DataAccessSubmissionState.SUBMITTED);
		lazyLoadDataCallback.invoke();
		verify(mockView).showUnapprovedHeading();
		verify(mockView).showRequestSubmittedMessage();
		verify(mockView).showCancelRequestButton();
	}
	

	@Test
	public void testSubmittedStateByAnotherUser() {
		when(mockAuthController.getCurrentUserPrincipalId()).thenReturn("different id");
		widget.setRequirement(mockACTAccessRequirement);
		when(mockDataAccessSubmissionStatus.getState()).thenReturn(DataAccessSubmissionState.SUBMITTED);
		lazyLoadDataCallback.invoke();
		verify(mockView).showUnapprovedHeading();
		verify(mockSubmitterUserBadge).configure(SUBMITTER_ID);
		verify(mockView).showRequestSubmittedByOtherUser();
		verify(mockView, never()).showCancelRequestButton();
	}
	
	@Test
	public void testApprovedState() {
		widget.setRequirement(mockACTAccessRequirement);
		when(mockDataAccessSubmissionStatus.getState()).thenReturn(DataAccessSubmissionState.APPROVED);
		lazyLoadDataCallback.invoke();
		verify(mockView).showApprovedHeading();
		verify(mockView).showRequestApprovedMessage();
		verify(mockView).showUpdateRequestButton();
	}
	
	@Test
	public void testRejectedState() {
		String rejectedReason = "Please sign";
		widget.setRequirement(mockACTAccessRequirement);
		when(mockDataAccessSubmissionStatus.getState()).thenReturn(DataAccessSubmissionState.REJECTED);
		when(mockDataAccessSubmissionStatus.getRejectedReason()).thenReturn(rejectedReason);
		lazyLoadDataCallback.invoke();
		verify(mockView).showUnapprovedHeading();
		verify(mockView).showRequestRejectedMessage(rejectedReason);
		verify(mockView).showUpdateRequestButton();
	}
	
	@Test
	public void testCancelledState() {
		widget.setRequirement(mockACTAccessRequirement);
		when(mockDataAccessSubmissionStatus.getState()).thenReturn(DataAccessSubmissionState.CANCELLED);
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
		when(mockDataAccessSubmissionStatus.getState()).thenReturn(DataAccessSubmissionState.APPROVED);
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
		when(mockDataAccessSubmissionStatus.getState()).thenReturn(DataAccessSubmissionState.APPROVED);
		lazyLoadDataCallback.invoke();
		
		widget.onCancelRequest();
		verify(mockDataAccessClient).cancelDataAccessSubmission(eq(SUBMISSION_ID), any(AsyncCallback.class));
		verify(mockSynAlert).handleException(ex);
	}
}
