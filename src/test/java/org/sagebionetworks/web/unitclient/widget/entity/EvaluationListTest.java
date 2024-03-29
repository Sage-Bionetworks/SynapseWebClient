package org.sagebionetworks.web.unitclient.widget.entity;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.sagebionetworks.evaluation.model.Evaluation;
import org.sagebionetworks.web.client.widget.evaluation.EvaluationList;
import org.sagebionetworks.web.client.widget.evaluation.EvaluationListView;
import org.sagebionetworks.web.shared.exceptions.RestServiceException;

public class EvaluationListTest {

  EvaluationListView mockView;
  EvaluationList widget;

  @Before
  public void before() {
    mockView = mock(EvaluationListView.class);
    widget = new EvaluationList(mockView);
  }

  @Test
  public void testGetSelectedEvaluationsEmpty() throws RestServiceException {
    // configure with no evaluations
    List<Evaluation> emptyList = new ArrayList<Evaluation>();
    boolean isSelectable = true;
    widget.configure(emptyList, isSelectable);
    assertNull(widget.getSelectedEvaluation());
  }

  @Test
  public void testGetSingleSelectedEvaluation() throws RestServiceException {
    // if single evaluation, then it is always returned
    Evaluation singleEvaluation = new Evaluation();
    singleEvaluation.setId("1");
    List<Evaluation> evaluations = new ArrayList<Evaluation>();
    evaluations.add(singleEvaluation);
    boolean isSelectable = true;
    widget.configure(evaluations, isSelectable);
    widget.onChangeSelectedEvaluation(singleEvaluation);
    assertEquals(singleEvaluation, widget.getSelectedEvaluation());
  }

  @Test
  public void testGetSelectedEvaluationsNoneSelected()
    throws RestServiceException {
    // user selected none
    List<Evaluation> evaluations = new ArrayList<Evaluation>();
    evaluations.add(new Evaluation());
    evaluations.add(new Evaluation());
    boolean isSelectable = true;
    widget.configure(evaluations, isSelectable);
    assertNull(widget.getSelectedEvaluation());
  }

  @Test
  public void testGetSelectedEvaluationsSomeSelected()
    throws RestServiceException {
    // user selected a subset of the evaluations
    List<Evaluation> evaluations = new ArrayList<Evaluation>();
    evaluations.add(new Evaluation());
    Evaluation eval2 = new Evaluation();
    eval2.setId("2");
    evaluations.add(eval2);
    boolean isSelectable = true;
    widget.configure(evaluations, isSelectable);
    widget.onChangeSelectedEvaluation(eval2);

    assertEquals(eval2, widget.getSelectedEvaluation());
  }

  @Test
  public void testNotSelectable() throws RestServiceException {
    // user selected a subset of the evaluations
    List<Evaluation> evaluations = new ArrayList<Evaluation>();
    evaluations.add(new Evaluation());
    boolean isSelectable = false;

    widget.configure(evaluations, isSelectable);

    verify(mockView).configure(evaluations, isSelectable);
    assertNull(widget.getSelectedEvaluation());
  }
}
