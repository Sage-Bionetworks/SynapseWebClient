package org.sagebionetworks.web.unitclient.widget.entity;

import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import com.google.gwt.event.shared.EventBus;
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
import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.repo.model.entitybundle.v2.EntityBundle;
import org.sagebionetworks.repo.model.table.EntityView;
import org.sagebionetworks.repo.model.table.SubmissionView;
import org.sagebionetworks.web.client.SynapseJavascriptClient;
import org.sagebionetworks.web.client.events.EntityUpdatedEvent;
import org.sagebionetworks.web.client.jsinterop.SubmissionViewScopeEditorModalProps;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.client.widget.evaluation.EvaluationList;
import org.sagebionetworks.web.client.widget.evaluation.SubmissionViewScopeEditorModalWidget;
import org.sagebionetworks.web.client.widget.table.modal.fileview.SubmissionViewScopeWidget;
import org.sagebionetworks.web.client.widget.table.modal.fileview.SubmissionViewScopeWidgetView;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;

@RunWith(MockitoJUnitRunner.class)
public class SubmissionViewScopeWidgetTest {

  @Mock
  SubmissionViewScopeWidgetView mockView;

  @Mock
  SynapseJavascriptClient mockJsClient;

  @Mock
  EvaluationList mockViewScopeWidget;

  @Mock
  SubmissionViewScopeEditorModalWidget mockEditScopeWidget;

  @Mock
  SynapseAlert mockSynAlert;

  @Mock
  EventBus mockEventBus;

  @Mock
  EntityBundle mockEntityBundle;

  @Mock
  SubmissionView mockSubmissionView;

  @Mock
  EntityView mockEntityView;

  @Mock
  Evaluation mockEvaluation1;

  @Mock
  Evaluation mockEvaluation2;

  @Captor
  ArgumentCaptor<List<Evaluation>> evaluationListCaptor;

  SubmissionViewScopeWidget widget;
  public static final String EVAL_ID_1 = "1111";
  public static final String EVAL_ID_2 = "2222";

  @Before
  public void before() {
    widget =
      new SubmissionViewScopeWidget(
        mockView,
        mockJsClient,
        mockViewScopeWidget,
        mockEditScopeWidget,
        mockSynAlert,
        mockEventBus
      );
    when(mockEntityBundle.getEntity()).thenReturn(mockSubmissionView);
    when(mockEvaluation1.getId()).thenReturn(EVAL_ID_1);
    when(mockEvaluation2.getId()).thenReturn(EVAL_ID_2);
    List<String> scopeIds = new ArrayList<String>();
    scopeIds.add(EVAL_ID_1);
    scopeIds.add(EVAL_ID_2);
    when(mockSubmissionView.getScopeIds()).thenReturn(scopeIds);
    List<Evaluation> evaluations = new ArrayList<Evaluation>();
    evaluations.add(mockEvaluation1);
    evaluations.add(mockEvaluation2);
    AsyncMockStubber
      .callSuccessWith(evaluations)
      .when(mockJsClient)
      .getEvaluations(
        anyBoolean(),
        any(ACCESS_TYPE.class),
        anyList(),
        anyInt(),
        anyInt(),
        any(AsyncCallback.class)
      );
    AsyncMockStubber
      .callSuccessWith(mockSubmissionView)
      .when(mockJsClient)
      .updateEntity(
        any(Entity.class),
        anyString(),
        anyBoolean(),
        any(AsyncCallback.class)
      );
  }

  @Test
  public void testConstructor() {
    verify(mockView).setPresenter(widget);
    verify(mockView).setSynAlert(any(IsWidget.class));
    verify(mockView).setSubmissionViewScopeEditor(any(IsWidget.class));
    verify(mockView).setEvaluationListWidget(any(IsWidget.class));
  }

  @Test
  public void testConfigure() {
    boolean isEditable = false;
    boolean expectedIsSelectable = false;

    widget.configure(mockEntityBundle, isEditable);

    verify(mockSynAlert).clear();
    verify(mockViewScopeWidget).clear();
    verify(mockView).setVisible(true);
    verify(mockView).setEditButtonVisible(isEditable);
    verify(mockViewScopeWidget)
      .configure(evaluationListCaptor.capture(), eq(expectedIsSelectable));
    verifyEvaluationListContainsMockEvaluations(
      evaluationListCaptor.getValue()
    );
  }

  private void verifyEvaluationListContainsMockEvaluations(
    List<Evaluation> evaluationList
  ) {
    assertEquals(2, evaluationList.size());
    assertTrue(evaluationList.contains(mockEvaluation1));
    assertTrue(evaluationList.contains(mockEvaluation2));
  }

  @Test
  public void testConfigureNotSubmissionView() {
    when(mockEntityBundle.getEntity()).thenReturn(mockEntityView);

    widget.configure(mockEntityBundle, true);

    verify(mockView).setVisible(false);
  }

  @Test
  public void testOnEditAndOnUpdate() {
    boolean isEditable = true;

    widget.configure(mockEntityBundle, isEditable);
    widget.onEdit();

    ArgumentCaptor<
      SubmissionViewScopeEditorModalProps.Callback
    > onUpdateArgumentCaptor = ArgumentCaptor.forClass(
      SubmissionViewScopeEditorModalProps.Callback.class
    );
    ArgumentCaptor<
      SubmissionViewScopeEditorModalProps.Callback
    > onCancelArgumentCaptor = ArgumentCaptor.forClass(
      SubmissionViewScopeEditorModalProps.Callback.class
    );
    verify(mockEditScopeWidget)
      .configure(
        eq(mockEntityBundle.getEntity().getId()),
        onUpdateArgumentCaptor.capture(),
        onCancelArgumentCaptor.capture()
      );
    onUpdateArgumentCaptor.getValue().run();
    verify(mockEditScopeWidget).setOpen(false);
    verify(mockEventBus).fireEvent(any(EntityUpdatedEvent.class));
  }

  @Test
  public void testOnEditAndOnCancel() {
    boolean isEditable = true;

    widget.configure(mockEntityBundle, isEditable);
    widget.onEdit();

    ArgumentCaptor<
      SubmissionViewScopeEditorModalProps.Callback
    > onUpdateArgumentCaptor = ArgumentCaptor.forClass(
      SubmissionViewScopeEditorModalProps.Callback.class
    );
    ArgumentCaptor<
      SubmissionViewScopeEditorModalProps.Callback
    > onCancelArgumentCaptor = ArgumentCaptor.forClass(
      SubmissionViewScopeEditorModalProps.Callback.class
    );
    verify(mockEditScopeWidget)
      .configure(
        eq(mockEntityBundle.getEntity().getId()),
        onUpdateArgumentCaptor.capture(),
        onCancelArgumentCaptor.capture()
      );
    onCancelArgumentCaptor.getValue().run();
    verify(mockEditScopeWidget).setOpen(false);
    verify(mockEventBus, never()).fireEvent(any(EntityUpdatedEvent.class));
  }
}
