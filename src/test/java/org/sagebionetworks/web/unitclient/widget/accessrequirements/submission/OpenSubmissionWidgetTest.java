package org.sagebionetworks.web.unitclient.widget.accessrequirements.submission;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.repo.model.ManagedACTAccessRequirement;
import org.sagebionetworks.repo.model.TermsOfUseAccessRequirement;
import org.sagebionetworks.repo.model.dataaccess.OpenSubmission;
import org.sagebionetworks.web.client.DataAccessClientAsync;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.accessrequirements.ManagedACTAccessRequirementWidget;
import org.sagebionetworks.web.client.widget.accessrequirements.submission.OpenSubmissionWidget;
import org.sagebionetworks.web.client.widget.accessrequirements.submission.OpenSubmissionWidgetView;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.client.widget.lazyload.LazyLoadHelper;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class OpenSubmissionWidgetTest {
	@Mock
	DataAccessClientAsync mockClient;
	@Mock
	SynapseAlert mockSynapseAlert;
	@Mock
	OpenSubmissionWidgetView mockView;
	@Mock
	LazyLoadHelper mockLazyLoadHelper;
	@Mock
	ManagedACTAccessRequirementWidget mockAccessRequirementWidget;

	OpenSubmissionWidget widget;

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		widget = new OpenSubmissionWidget(mockView, mockAccessRequirementWidget, mockClient, mockSynapseAlert, mockLazyLoadHelper);

	}

	@Test
	public void testConstruction() {
		verify(mockView).setSynAlert(mockSynapseAlert);
		verify(mockView).setACTAccessRequirementWidget(mockAccessRequirementWidget);
		verify(mockView).setPresenter(widget);

		ArgumentCaptor<Callback> captor = ArgumentCaptor.forClass(Callback.class);
		verify(mockLazyLoadHelper).configure(captor.capture(), eq(mockView));

		Callback callback = captor.getValue();
		callback.invoke();
		verify(mockClient).getAccessRequirement(anyLong(), any(AsyncCallback.class));
	}

	@Test
	public void testLoadAccessRequirementFailure() {
		Exception ex = new Exception();
		AsyncMockStubber.callFailureWith(ex).when(mockClient).getAccessRequirement(anyLong(), any(AsyncCallback.class));
		widget.loadAccessRequirement();
		verify(mockClient).getAccessRequirement(anyLong(), any(AsyncCallback.class));
		InOrder inOrder = inOrder(mockSynapseAlert);
		inOrder.verify(mockSynapseAlert).clear();
		inOrder.verify(mockSynapseAlert).handleException(ex);
	}

	@Test
	public void testLoadAccessRequirementSuccessWithACTAccessRequirement() {
		ManagedACTAccessRequirement actAccessRequirement = new ManagedACTAccessRequirement();
		AsyncMockStubber.callSuccessWith(actAccessRequirement).when(mockClient).getAccessRequirement(anyLong(), any(AsyncCallback.class));
		widget.loadAccessRequirement();
		verify(mockClient).getAccessRequirement(anyLong(), any(AsyncCallback.class));
		verify(mockSynapseAlert).clear();
		verify(mockAccessRequirementWidget).setRequirement(eq(actAccessRequirement), any(Callback.class));
	}

	@Test
	public void testLoadAccessRequirementSuccessWithTermsOfUseAccessRequirement() {
		TermsOfUseAccessRequirement touAccessRequirement = new TermsOfUseAccessRequirement();
		AsyncMockStubber.callSuccessWith(touAccessRequirement).when(mockClient).getAccessRequirement(anyLong(), any(AsyncCallback.class));
		widget.loadAccessRequirement();
		verify(mockClient).getAccessRequirement(anyLong(), any(AsyncCallback.class));
		InOrder inOrder = inOrder(mockSynapseAlert);
		inOrder.verify(mockSynapseAlert).clear();
		ArgumentCaptor<Exception> captor = ArgumentCaptor.forClass(Exception.class);
		inOrder.verify(mockSynapseAlert).handleException(captor.capture());
		Exception exception = captor.getValue();
		assertTrue(exception instanceof IllegalStateException);
		verify(mockAccessRequirementWidget).hideButtons();
		verify(mockAccessRequirementWidget).setReviewAccessRequestsVisible(true);
		verifyNoMoreInteractions(mockAccessRequirementWidget);
	}

	@Test
	public void testConfigure() {
		Long numberOfSubmissions = 2L;
		Long accessRequirementId = 10000L;
		OpenSubmission openSubmission = new OpenSubmission();
		openSubmission.setNumberOfSubmittedSubmission(numberOfSubmissions);
		openSubmission.setAccessRequirementId(accessRequirementId.toString());
		widget.configure(openSubmission);
		verify(mockView).setNumberOfSubmissions(numberOfSubmissions);
		verify(mockLazyLoadHelper).setIsConfigured();

		widget.loadAccessRequirement();
		verify(mockClient).getAccessRequirement(eq(accessRequirementId), any(AsyncCallback.class));
	}
}
