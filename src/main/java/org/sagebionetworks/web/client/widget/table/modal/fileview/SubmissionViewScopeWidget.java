package org.sagebionetworks.web.client.widget.table.modal.fileview;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import org.sagebionetworks.evaluation.model.Evaluation;
import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.repo.model.entitybundle.v2.EntityBundle;
import org.sagebionetworks.repo.model.table.SubmissionView;
import org.sagebionetworks.web.client.SynapseJavascriptClient;
import org.sagebionetworks.web.client.events.EntityUpdatedEvent;
import org.sagebionetworks.web.client.widget.SynapseWidgetPresenter;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.client.widget.evaluation.EvaluationFinder;
import org.sagebionetworks.web.client.widget.evaluation.EvaluationList;
import org.sagebionetworks.web.client.widget.evaluation.SubmissionViewScopeEditor;

/**
 * All business logic for viewing and editing the SubmissionView scope.
 *
 * Scope Widget - these are the UI output elements in this widget:
 *
 * @author Jay
 *
 */
public class SubmissionViewScopeWidget
  implements SynapseWidgetPresenter, SubmissionViewScopeWidgetView.Presenter {

  boolean isEditable;
  SubmissionViewScopeWidgetView view;
  SynapseJavascriptClient jsClient;
  EntityBundle bundle;
  EvaluationList viewScopeWidget;
  SubmissionViewScopeEditor editScopeWidget;
  SynapseAlert synAlert;
  SubmissionView currentView;
  EventBus eventBus;
  List<Evaluation> evaluationsList = new ArrayList<Evaluation>();
  int jobKey = 0;

  /**
   * New presenter with its view.
   *
   * @param view
   */
  @Inject
  public SubmissionViewScopeWidget(
    SubmissionViewScopeWidgetView view,
    SynapseJavascriptClient jsClient,
    EvaluationList viewScopeWidget,
    SubmissionViewScopeEditor editScopeWidget,
    SynapseAlert synAlert,
    EventBus eventBus
  ) {
    this.jsClient = jsClient;
    this.view = view;
    this.viewScopeWidget = viewScopeWidget;
    this.editScopeWidget = editScopeWidget;
    this.synAlert = synAlert;
    this.eventBus = eventBus;
    view.setPresenter(this);
    view.setSubmissionViewScopeEditor(editScopeWidget);
    view.setEvaluationListWidget(viewScopeWidget.asWidget());
    view.setSynAlert(synAlert.asWidget());
  }

  public void configure(EntityBundle bundle, boolean isEditable) {
    this.isEditable = isEditable;
    this.bundle = bundle;
    boolean isVisible = bundle.getEntity() instanceof SubmissionView;
    jobKey++;
    if (isVisible) {
      int currentJobKey = jobKey;
      currentView = (SubmissionView) bundle.getEntity();
      viewScopeWidget.clear();
      evaluationsList.clear();
      synAlert.clear();
      AsyncCallback<Void> cb = new AsyncCallback<Void>() {
        @Override
        public void onFailure(Throwable caught) {
          if (currentJobKey != jobKey) {
            return;
          }
          synAlert.handleException(caught);
        }

        @Override
        public void onSuccess(Void result) {
          if (currentJobKey != jobKey) {
            return;
          }
          boolean isSelectable = false;
          viewScopeWidget.configure(evaluationsList, isSelectable);
        }
      };
      getAllEvaluations(currentView.getScopeIds(), 0, cb);
      view.setEditButtonVisible(isEditable);
    }
    view.setVisible(isVisible);
  }

  private void getAllEvaluations(
    List<String> scopeIds,
    int currentOffset,
    AsyncCallback<Void> cb
  ) {
    jsClient.getEvaluations(
      null,
      null,
      scopeIds,
      EvaluationFinder.DEFAULT_EVALUATION_LIMIT.intValue(),
      currentOffset,
      new AsyncCallback<List<Evaluation>>() {
        @Override
        public void onFailure(Throwable caught) {
          cb.onFailure(caught);
        }

        @Override
        public void onSuccess(List<Evaluation> result) {
          evaluationsList.addAll(result);
          if (result.size() == EvaluationFinder.DEFAULT_EVALUATION_LIMIT) {
            // keep looking for more results
            getAllEvaluations(
              scopeIds,
              currentOffset +
              EvaluationFinder.DEFAULT_EVALUATION_LIMIT.intValue(),
              cb
            );
          } else {
            cb.onSuccess(null);
          }
        }
      }
    );
  }

  @Override
  public Widget asWidget() {
    return view.asWidget();
  }

  @Override
  public void onSave() {
    // update scope
    synAlert.clear();
    List<String> newScopeIds = editScopeWidget.getEvaluationIds();
    if (newScopeIds.isEmpty()) {
      synAlert.showError(CreateTableViewWizardStep1.EMPTY_SCOPE_MESSAGE);
      return;
    }
    view.setLoading(true);
    currentView.setScopeIds(newScopeIds);
    jsClient.updateEntity(
      currentView,
      null,
      null,
      new AsyncCallback<Entity>() {
        @Override
        public void onSuccess(Entity entity) {
          view.setLoading(false);
          view.hideModal();
          eventBus.fireEvent(new EntityUpdatedEvent(entity.getId()));
        }

        @Override
        public void onFailure(Throwable caught) {
          view.setLoading(false);
          synAlert.handleException(caught);
        }
      }
    );
  }

  @Override
  public void onEdit() {
    editScopeWidget.configure(evaluationsList);
    view.showModal();
  }
}
