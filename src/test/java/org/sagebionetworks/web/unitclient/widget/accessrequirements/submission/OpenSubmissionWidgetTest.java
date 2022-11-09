package org.sagebionetworks.web.unitclient.widget.accessrequirements.submission;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import com.google.gwt.user.client.rpc.AsyncCallback;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.repo.model.ManagedACTAccessRequirement;
import org.sagebionetworks.repo.model.TermsOfUseAccessRequirement;
import org.sagebionetworks.repo.model.dataaccess.OpenSubmission;
import org.sagebionetworks.web.client.SynapseJavascriptClient;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.accessrequirements.ManagedACTAccessRequirementWidget;
import org.sagebionetworks.web.client.widget.accessrequirements.submission.OpenSubmissionWidget;
import org.sagebionetworks.web.client.widget.accessrequirements.submission.OpenSubmissionWidgetView;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.client.widget.lazyload.LazyLoadHelper;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;

public class OpenSubmissionWidgetTest {

  @Mock
  SynapseJavascriptClient mockClient;

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
    widget =
      new OpenSubmissionWidget(
        mockView,
        mockAccessRequirementWidget,
        mockClient,
        mockSynapseAlert,
        mockLazyLoadHelper
      );
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
    verify(mockClient)
      .getAccessRequirement(anyString(), any(AsyncCallback.class));
  }

  @Test
  public void testLoadAccessRequirementFailure() {
    Exception ex = new Exception();
    AsyncMockStubber
      .callFailureWith(ex)
      .when(mockClient)
      .getAccessRequirement(anyString(), any(AsyncCallback.class));
    widget.loadAccessRequirement();
    verify(mockClient)
      .getAccessRequirement(anyString(), any(AsyncCallback.class));
    InOrder inOrder = inOrder(mockSynapseAlert);
    inOrder.verify(mockSynapseAlert).clear();
    inOrder.verify(mockSynapseAlert).handleException(ex);
  }

  @Test
  public void testLoadAccessRequirementSuccessWithACTAccessRequirement() {
    ManagedACTAccessRequirement actAccessRequirement = new ManagedACTAccessRequirement();
    AsyncMockStubber
      .callSuccessWith(actAccessRequirement)
      .when(mockClient)
      .getAccessRequirement(anyString(), any(AsyncCallback.class));
    widget.loadAccessRequirement();
    verify(mockClient)
      .getAccessRequirement(anyString(), any(AsyncCallback.class));
    verify(mockSynapseAlert).clear();
    verify(mockAccessRequirementWidget)
      .setRequirement(eq(actAccessRequirement), any(Callback.class));
  }

  @Test
  public void testLoadAccessRequirementSuccessWithTermsOfUseAccessRequirement() {
    TermsOfUseAccessRequirement touAccessRequirement = new TermsOfUseAccessRequirement();
    AsyncMockStubber
      .callSuccessWith(touAccessRequirement)
      .when(mockClient)
      .getAccessRequirement(anyString(), any(AsyncCallback.class));
    widget.loadAccessRequirement();
    verify(mockClient)
      .getAccessRequirement(anyString(), any(AsyncCallback.class));
    InOrder inOrder = inOrder(mockSynapseAlert);
    inOrder.verify(mockSynapseAlert).clear();
    ArgumentCaptor<Exception> captor = ArgumentCaptor.forClass(Exception.class);
    inOrder.verify(mockSynapseAlert).handleException(captor.capture());
    Exception exception = captor.getValue();
    assertTrue(exception instanceof IllegalStateException);
    //SWC-5171: controls should be shown in OpenSubmissions (in ACTDataAccessSubmissionDashboard)
    verify(mockAccessRequirementWidget, never()).hideControls();
    verify(mockAccessRequirementWidget).setReviewAccessRequestsVisible(true);
    verifyNoMoreInteractions(mockAccessRequirementWidget);
  }

  @Test
  public void testConfigure() {
    Long numberOfSubmissions = 2L;
    String accessRequirementId = "10000";
    OpenSubmission openSubmission = new OpenSubmission();
    openSubmission.setNumberOfSubmittedSubmission(numberOfSubmissions);
    openSubmission.setAccessRequirementId(accessRequirementId);
    widget.configure(openSubmission);
    verify(mockView).setNumberOfSubmissions(numberOfSubmissions);
    verify(mockLazyLoadHelper).setIsConfigured();

    widget.loadAccessRequirement();
    verify(mockClient)
      .getAccessRequirement(eq(accessRequirementId), any(AsyncCallback.class));
  }
}
