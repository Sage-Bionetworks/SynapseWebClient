package org.sagebionetworks.web.client.widget.evaluation;

import com.google.gwt.user.client.ui.IsWidget;
import java.util.List;
import org.sagebionetworks.evaluation.model.Evaluation;
import org.sagebionetworks.web.client.SynapseView;

public interface EvaluationListView extends IsWidget, SynapseView {
  void configure(List<Evaluation> evaluationList, boolean isSelectable);

  Integer getSelectedEvaluationIndex();

  void setSelectedEvaluationIndex(int i);
}
