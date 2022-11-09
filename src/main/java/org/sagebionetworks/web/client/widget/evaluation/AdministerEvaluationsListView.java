package org.sagebionetworks.web.client.widget.evaluation;

import com.google.gwt.user.client.ui.IsWidget;
import org.sagebionetworks.evaluation.model.Evaluation;
import org.sagebionetworks.web.client.jsinterop.EvaluationCardProps;
import org.sagebionetworks.web.client.widget.evaluation.EvaluationRowWidget.EvaluationActionHandler;

public interface AdministerEvaluationsListView extends IsWidget {
  void setPresenter(EvaluationActionHandler presenter);

  void addRow(Evaluation ev);

  void clearRows();

  void add(IsWidget w);

  void addReactComponent(Evaluation evaluation, EvaluationCardProps props);
}
