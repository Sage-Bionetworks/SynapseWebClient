package org.sagebionetworks.web.unitclient.widget.evaluation;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.google.gwt.user.client.rpc.AsyncCallback;
import java.util.ArrayList;
import java.util.Date;
import java.util.function.Consumer;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.evaluation.model.Evaluation;
import org.sagebionetworks.web.client.ChallengeClientAsync;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.client.widget.entity.renderer.SubmitToEvaluationWidget;
import org.sagebionetworks.web.client.widget.evaluation.AdministerEvaluationsList;
import org.sagebionetworks.web.client.widget.evaluation.AdministerEvaluationsListView;
import org.sagebionetworks.web.client.widget.sharing.EvaluationAccessControlListModalWidget;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;

public class AdministerEvaluationsListTest {

  AdministerEvaluationsList evalList;

  @Mock
  AdministerEvaluationsListView mockView;

  @Mock
  ChallengeClientAsync mockChallengeClient;

  @Mock
  EvaluationAccessControlListModalWidget mockAclEditor;

  @Mock
  SynapseAlert mockSynAlert;

  @Mock
  GlobalApplicationState mockGlobalApplicationState;

  @Mock
  AuthenticationController mockAuthenticationController;

  @Mock
  SubmitToEvaluationWidget mockSubmitToEvaluationWidget;

  @Mock
  Consumer<String> mockOnEditEvaluation;

  Evaluation e1, e2;

  @Before
  public void setup() throws Exception {
    MockitoAnnotations.initMocks(this);
    evalList =
      new AdministerEvaluationsList(
        mockView,
        mockChallengeClient,
        mockAclEditor,
        mockSynAlert,
        mockGlobalApplicationState,
        mockAuthenticationController,
        mockSubmitToEvaluationWidget
      );

    ArrayList<Evaluation> evaluationResults = new ArrayList<Evaluation>();

    e1 = new Evaluation();
    e1.setId("101");
    e1.setCreatedOn(new Date());
    evaluationResults.add(e1);

    e2 = new Evaluation();
    e2.setId("102");
    e2.setCreatedOn(new Date());
    evaluationResults.add(e2);

    AsyncMockStubber
      .callSuccessWith(evaluationResults)
      .when(mockChallengeClient)
      .getSharableEvaluations(anyString(), any(AsyncCallback.class));
  }

  @Ignore // Not sure how to stub out GWT Javascript-specific module used in EvaluationJSObject
  @Test
  public void testConfigure() {
    evalList.configure("syn100", mockOnEditEvaluation);
    verify(mockChallengeClient)
      .getSharableEvaluations(anyString(), any(AsyncCallback.class));
    verify(mockView).addReactComponent(eq(e1), any());
    verify(mockView).addReactComponent(eq(e2), any());
  }

  @Test
  public void testConfigureZeroResults() {
    AsyncMockStubber
      .callSuccessWith(new ArrayList<Evaluation>())
      .when(mockChallengeClient)
      .getSharableEvaluations(anyString(), any(AsyncCallback.class));
    evalList.configure("syn100", mockOnEditEvaluation);
    verify(mockChallengeClient)
      .getSharableEvaluations(anyString(), any(AsyncCallback.class));
    verify(mockView, never()).addReactComponent(eq(e1), any());
  }

  @Test
  public void testConfigureFailure() throws Exception {
    Exception ex = new Exception("bad time");
    AsyncMockStubber
      .callFailureWith(ex)
      .when(mockChallengeClient)
      .getSharableEvaluations(anyString(), any(AsyncCallback.class));
    evalList.configure("syn100", mockOnEditEvaluation);
    verify(mockChallengeClient)
      .getSharableEvaluations(anyString(), any(AsyncCallback.class));
    verify(mockSynAlert).handleException(ex);
  }

  @Test
  public void testOnShareClicked() {
    evalList.onShareClicked(e1);
    verify(mockAclEditor).configure(eq(e1), any());
    verify(mockAclEditor).show();
  }
}
