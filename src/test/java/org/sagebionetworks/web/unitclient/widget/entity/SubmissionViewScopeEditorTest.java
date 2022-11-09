package org.sagebionetworks.web.unitclient.widget.entity;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.sagebionetworks.evaluation.model.Evaluation;
import org.sagebionetworks.repo.model.ACCESS_TYPE;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.evaluation.EvaluationFinder;
import org.sagebionetworks.web.client.widget.evaluation.SubmissionViewScopeEditor;
import org.sagebionetworks.web.client.widget.evaluation.SubmissionViewScopeEditorView;

@RunWith(MockitoJUnitRunner.class)
public class SubmissionViewScopeEditorTest {

  @Mock
  SubmissionViewScopeEditorView mockView;

  @Mock
  EvaluationFinder mockEvaluationFinder;

  @Mock
  Evaluation mockEvaluation1;

  @Mock
  Evaluation mockEvaluation2;

  List<Evaluation> evaluationList;

  @Captor
  ArgumentCaptor<CallbackP<Evaluation>> callbackPEvaluationCaptor;

  SubmissionViewScopeEditor widget;
  public static final String EVAL_ID = "1111";

  @Before
  public void before() {
    widget = new SubmissionViewScopeEditor(mockView, mockEvaluationFinder);
    evaluationList = Collections.singletonList(mockEvaluation1);
    when(mockEvaluation1.getId()).thenReturn(EVAL_ID);
  }

  @Test
  public void testConfigureAddDelete() {
    widget.configure(evaluationList);

    verify(mockView).clearRows();
    verify(mockView).addRow(mockEvaluation1);
    assertEquals(1, widget.getEvaluations().size());
    assertEquals(mockEvaluation1, widget.getEvaluations().get(0));
    assertEquals(1, widget.getEvaluationIds().size());
    assertEquals(EVAL_ID, widget.getEvaluationIds().get(0));
  }

  @Test
  public void testOnAdd() {
    widget.configure(evaluationList);
    widget.onAddClicked();

    boolean expectedIsActiveOnly = false;
    ACCESS_TYPE expectedAccessType = ACCESS_TYPE.READ_PRIVATE_SUBMISSION;
    verify(mockEvaluationFinder)
      .configure(
        eq(expectedIsActiveOnly),
        eq(expectedAccessType),
        callbackPEvaluationCaptor.capture()
      );

    CallbackP<Evaluation> callback = callbackPEvaluationCaptor.getValue();
    // simulate user selected to add mockEvaluation2
    callback.invoke(mockEvaluation2);

    verify(mockView, times(2)).clearRows();
    verify(mockView, times(2)).addRow(mockEvaluation1);
    verify(mockView).addRow(mockEvaluation2);
    assertEquals(2, widget.getEvaluations().size());
    assertEquals(mockEvaluation1, widget.getEvaluations().get(0));
    assertEquals(mockEvaluation2, widget.getEvaluations().get(1));
  }

  @Test
  public void testOnDelete() {
    widget.configure(evaluationList);

    widget.onDeleteClicked(mockEvaluation1);
    assertEquals(0, widget.getEvaluations().size());
  }
}
