package org.sagebionetworks.web.client.widget.evaluation;

import com.google.gwt.user.client.ui.IsWidget;
import org.sagebionetworks.evaluation.model.Evaluation;
import org.sagebionetworks.web.client.jsinterop.EvaluationCardProps;

public interface AdministerEvaluationsListView extends IsWidget {
  void clearRows();

  void add(IsWidget w);

  void addReactComponent(Evaluation evaluation, EvaluationCardProps props);
}
