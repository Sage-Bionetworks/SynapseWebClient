package org.sagebionetworks.web.client.widget.evaluation;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import org.sagebionetworks.evaluation.model.Evaluation;
import org.sagebionetworks.web.client.widget.SynapseWidgetPresenter;

public class EvaluationList implements SynapseWidgetPresenter, IsWidget {

  private EvaluationListView view;
  List<Evaluation> evaluationList;

  @Inject
  public EvaluationList(EvaluationListView view) {
    this.view = view;
  }

  public void configure(List<Evaluation> list, boolean isSelectable) {
    this.evaluationList = list;
    view.configure(list, isSelectable);
  }

  public void clear() {
    configure(new ArrayList<Evaluation>(), false);
  }

  @Override
  public Widget asWidget() {
    return view.asWidget();
  }

  public Evaluation getSelectedEvaluation() {
    if (evaluationList.size() == 1) {
      return evaluationList.get(0);
    } // else
    Integer selectedEvaluationIndex = view.getSelectedEvaluationIndex();
    if (selectedEvaluationIndex == null) return null;

    return evaluationList.get(selectedEvaluationIndex);
  }
}
