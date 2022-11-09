package org.sagebionetworks.web.client.widget.evaluation;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import org.sagebionetworks.evaluation.model.Evaluation;
import org.sagebionetworks.repo.model.ACCESS_TYPE;
import org.sagebionetworks.web.client.utils.CallbackP;

public class SubmissionViewScopeEditor
  implements SubmissionViewScopeEditorView.Presenter, IsWidget {

  private SubmissionViewScopeEditorView view;
  private List<Evaluation> evaluations;
  EvaluationFinder evaluationFinder;

  @Inject
  public SubmissionViewScopeEditor(
    SubmissionViewScopeEditorView view,
    EvaluationFinder evaluationFinder
  ) {
    this.view = view;
    this.evaluationFinder = evaluationFinder;
    view.setPresenter(this);
  }

  public void configure(List<Evaluation> initEvaluations) {
    evaluations = new ArrayList<Evaluation>(initEvaluations);
    refresh();
  }

  private void refresh() {
    view.clearRows();

    for (Evaluation evaluation : evaluations) {
      view.addRow(evaluation);
    }
  }

  public List<Evaluation> getEvaluations() {
    return evaluations;
  }

  public List<String> getEvaluationIds() {
    // gather IDs
    List<String> evalIds = new ArrayList<String>();
    for (Evaluation evaluation : evaluations) {
      evalIds.add(evaluation.getId());
    }
    return evalIds;
  }

  @Override
  public void onDeleteClicked(Evaluation evaluation) {
    evaluations.remove(evaluation);
    refresh();
  }

  @Override
  public void onAddClicked() {
    // pop up evaluation queue selector
    Boolean isActiveOnly = false;

    evaluationFinder.configure(
      isActiveOnly,
      ACCESS_TYPE.READ_PRIVATE_SUBMISSION,
      new CallbackP<Evaluation>() {
        @Override
        public void invoke(Evaluation evaluation) {
          evaluations.add(evaluation);
          refresh();
        }
      }
    );
  }

  @Override
  public Widget asWidget() {
    return view.asWidget();
  }
}
