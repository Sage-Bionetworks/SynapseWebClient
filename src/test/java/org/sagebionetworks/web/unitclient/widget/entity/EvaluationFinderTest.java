package org.sagebionetworks.web.unitclient.widget.entity;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.sagebionetworks.web.client.widget.evaluation.EvaluationFinder.DEFAULT_EVALUATION_LIMIT;
import static org.sagebionetworks.web.client.widget.evaluation.EvaluationFinder.DEFAULT_OFFSET;
import static org.sagebionetworks.web.client.widget.evaluation.EvaluationFinder.NO_EVALUATIONS_FOUND;
import static org.sagebionetworks.web.client.widget.evaluation.EvaluationFinder.NO_EVALUATION_SELECTED;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.IsWidget;
import java.util.ArrayList;
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
import org.sagebionetworks.web.client.SynapseJavascriptClient;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.client.widget.evaluation.EvaluationFinder;
import org.sagebionetworks.web.client.widget.evaluation.EvaluationFinderView;
import org.sagebionetworks.web.client.widget.evaluation.EvaluationList;
import org.sagebionetworks.web.client.widget.pagination.BasicPaginationWidget;
import org.sagebionetworks.web.client.widget.pagination.PageChangeListener;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;

@RunWith(MockitoJUnitRunner.class)
public class EvaluationFinderTest {

  @Mock
  EvaluationFinderView mockView;

  @Mock
  BasicPaginationWidget mockPaginationWidget;

  @Mock
  SynapseAlert mockSynAlert;

  @Mock
  SynapseJavascriptClient mockJsClient;

  @Mock
  EvaluationList mockEvaluationListWidget;

  @Mock
  Evaluation mockEvaluation1;

  @Mock
  Evaluation mockEvaluation2;

  @Mock
  CallbackP<Evaluation> mockSelectedEvaluationCallback;

  List<Evaluation> evaluationListPage;

  @Captor
  ArgumentCaptor<CallbackP<Evaluation>> callbackPEvaluationCaptor;

  EvaluationFinder widget;
  public static final String EVAL_ID_1 = "1111";
  public static final String EVAL_ID_2 = "2222";

  @Before
  public void before() {
    widget =
      new EvaluationFinder(
        mockView,
        mockPaginationWidget,
        mockSynAlert,
        mockJsClient,
        mockEvaluationListWidget
      );
    evaluationListPage = new ArrayList<Evaluation>();
    when(mockEvaluation1.getId()).thenReturn(EVAL_ID_1);
    when(mockEvaluation2.getId()).thenReturn(EVAL_ID_2);
    AsyncMockStubber
      .callSuccessWith(evaluationListPage)
      .when(mockJsClient)
      .getEvaluations(
        anyBoolean(),
        any(ACCESS_TYPE.class),
        anyList(),
        anyInt(),
        anyInt(),
        any(AsyncCallback.class)
      );
  }

  @Test
  public void testConstructor() {
    verify(mockView).setPresenter(widget);
    verify(mockView).setSynAlert(any(IsWidget.class));
    verify(mockView).setEvaluationList(any(IsWidget.class));
    verify(mockView).setPaginationWidget(any(IsWidget.class));
  }

  @Test
  public void testConfigure() {
    evaluationListPage.add(mockEvaluation1);
    boolean isActiveOnly = true;
    ACCESS_TYPE access = ACCESS_TYPE.READ_PRIVATE_SUBMISSION;

    widget.configure(isActiveOnly, access, mockSelectedEvaluationCallback);

    verify(mockSynAlert).clear();
    verify(mockJsClient)
      .getEvaluations(
        eq(isActiveOnly),
        eq(access),
        eq(null),
        eq(DEFAULT_EVALUATION_LIMIT.intValue()),
        eq(DEFAULT_OFFSET.intValue()),
        any(AsyncCallback.class)
      );
    // set up success in Before, with non-empty result (in this test)
    long expectedRowCount = evaluationListPage.size();
    verify(mockPaginationWidget)
      .configure(
        DEFAULT_EVALUATION_LIMIT,
        DEFAULT_OFFSET,
        expectedRowCount,
        widget
      );
    boolean expectedIsSelectable = true;
    verify(mockEvaluationListWidget)
      .configure(evaluationListPage, expectedIsSelectable);
    verify(mockView).show();

    // try clicking Ok before a selection has been made
    widget.onOk();
    verify(mockSynAlert).showError(NO_EVALUATION_SELECTED);

    // set up a selected evaluation to test the happy path
    when(mockEvaluationListWidget.getSelectedEvaluation())
      .thenReturn(mockEvaluation1);
    widget.onOk();
    verify(mockSelectedEvaluationCallback).invoke(mockEvaluation1);
    verify(mockView).hide();
  }

  @Test
  public void testConfigureNoResults() {
    boolean isActiveOnly = true;
    ACCESS_TYPE access = ACCESS_TYPE.READ_PRIVATE_SUBMISSION;

    widget.configure(isActiveOnly, access, mockSelectedEvaluationCallback);

    verify(mockJsClient)
      .getEvaluations(
        eq(isActiveOnly),
        eq(access),
        eq(null),
        eq(DEFAULT_EVALUATION_LIMIT.intValue()),
        eq(DEFAULT_OFFSET.intValue()),
        any(AsyncCallback.class)
      );
    verify(mockSynAlert).showError(NO_EVALUATIONS_FOUND);
    verify(mockPaginationWidget, never())
      .configure(
        anyLong(),
        anyLong(),
        anyLong(),
        any(PageChangeListener.class)
      );
    verify(mockEvaluationListWidget, never())
      .configure(anyList(), anyBoolean());
  }

  @Test
  public void testConfigureError() {
    boolean isActiveOnly = true;
    ACCESS_TYPE access = ACCESS_TYPE.READ_PRIVATE_SUBMISSION;
    Exception ex = new Exception();
    AsyncMockStubber
      .callFailureWith(ex)
      .when(mockJsClient)
      .getEvaluations(
        anyBoolean(),
        any(ACCESS_TYPE.class),
        anyList(),
        anyInt(),
        anyInt(),
        any(AsyncCallback.class)
      );

    widget.configure(isActiveOnly, access, mockSelectedEvaluationCallback);

    verify(mockSynAlert).handleException(ex);
  }
}
