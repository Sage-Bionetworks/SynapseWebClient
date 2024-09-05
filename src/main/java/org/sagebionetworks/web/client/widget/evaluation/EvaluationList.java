package org.sagebionetworks.web.client.widget.evaluation;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import org.sagebionetworks.evaluation.model.Evaluation;
import org.sagebionetworks.web.client.widget.SynapseWidgetPresenter;

public class EvaluationList
  implements SynapseWidgetPresenter, EvaluationListView.Presenter, IsWidget {

  private EvaluationListView view;
  List<Evaluation> evaluationList;
  Evaluation selectedEvaluation;

  @Inject
  public EvaluationList(EvaluationListView view) {
    this.view = view;
    view.setPresenter(this);
  }

  public void configure(List<Evaluation> list, boolean isSelectable) {
    this.evaluationList = list;
    this.selectedEvaluation = null;

    view.configure(list, isSelectable);
  }

  public void clear() {
    configure(new ArrayList<Evaluation>(), false);
  }

  @Override
  public Widget asWidget() {
    return view.asWidget();
  }

  @Override
  public void onChangeSelectedEvaluation(Evaluation evaluation) {
    this.selectedEvaluation = evaluation;
  }

  public Evaluation getSelectedEvaluation() {
    return this.selectedEvaluation;
  }
}
