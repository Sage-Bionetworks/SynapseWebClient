package org.sagebionetworks.web.client.widget.evaluation;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import java.util.List;
import org.sagebionetworks.evaluation.model.Evaluation;
import org.sagebionetworks.repo.model.ACCESS_TYPE;
import org.sagebionetworks.web.client.SynapseJavascriptClient;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.client.widget.pagination.BasicPaginationWidget;
import org.sagebionetworks.web.client.widget.pagination.PageChangeListener;

public class EvaluationFinder
  implements EvaluationFinderView.Presenter, IsWidget, PageChangeListener {

  public static final String NO_EVALUATIONS_FOUND =
    "No evaluations were found.";

  public static final String NO_EVALUATION_SELECTED =
    "Please select an evaluation.";

  private EvaluationFinderView view;
  private SynapseAlert synAlert;
  SynapseJavascriptClient jsClient;
  EvaluationList evaluationList;
  Boolean isActiveOnly;
  ACCESS_TYPE accessType;
  CallbackP<Evaluation> evaluationSelectedCallback;
  BasicPaginationWidget paginationWidget;
  public static final Long DEFAULT_EVALUATION_LIMIT = 20L;
  public static final Long DEFAULT_OFFSET = 0L;

  @Inject
  public EvaluationFinder(
    EvaluationFinderView view,
    BasicPaginationWidget paginationWidget,
    SynapseAlert synAlert,
    SynapseJavascriptClient jsClient,
    EvaluationList evaluationList
  ) {
    this.view = view;
    this.synAlert = synAlert;
    this.jsClient = jsClient;
    this.evaluationList = evaluationList;
    this.paginationWidget = paginationWidget;
    view.setPresenter(this);
    view.setSynAlert(synAlert.asWidget());
    view.setEvaluationList(evaluationList.asWidget());
    view.setPaginationWidget(paginationWidget);
  }

  @Override
  public void onPageChange(final Long newOffset) {
    synAlert.clear();

    jsClient.getEvaluations(
      isActiveOnly,
      accessType,
      null,
      DEFAULT_EVALUATION_LIMIT.intValue(),
      newOffset.intValue(),
      new AsyncCallback<List<Evaluation>>() {
        @Override
        public void onSuccess(List<Evaluation> results) {
          if (results.isEmpty() && newOffset.equals(DEFAULT_OFFSET)) {
            // no evaluations found!
            synAlert.showError(NO_EVALUATIONS_FOUND);
            return;
          }
          long rowCount = new Integer(results.size()).longValue();
          paginationWidget.configure(
            DEFAULT_EVALUATION_LIMIT,
            newOffset,
            rowCount,
            EvaluationFinder.this
          );
          boolean isSelectable = true;
          evaluationList.configure(results, isSelectable);
        }

        @Override
        public void onFailure(Throwable caught) {
          synAlert.handleException(caught);
        }
      }
    );
  }

  public void configure(
    Boolean isActiveOnly,
    ACCESS_TYPE accessType,
    CallbackP<Evaluation> evaluationSelectedCallback
  ) {
    this.isActiveOnly = isActiveOnly;
    this.accessType = accessType;
    this.evaluationSelectedCallback = evaluationSelectedCallback;
    onPageChange(DEFAULT_OFFSET);
    view.show();
  }

  @Override
  public void onOk() {
    Evaluation evaluation = evaluationList.getSelectedEvaluation();
    if (evaluation == null) {
      synAlert.showError(NO_EVALUATION_SELECTED);
      return;
    }
    evaluationSelectedCallback.invoke(evaluation);
    view.hide();
  }

  public Widget asWidget() {
    return view.asWidget();
  }
}
