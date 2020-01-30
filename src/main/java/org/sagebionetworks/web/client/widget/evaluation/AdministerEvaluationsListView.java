package org.sagebionetworks.web.client.widget.evaluation;

import org.sagebionetworks.evaluation.model.Evaluation;
import org.sagebionetworks.web.client.widget.evaluation.EvaluationRowWidget.EvaluationActionHandler;
import com.google.gwt.user.client.ui.IsWidget;

public interface AdministerEvaluationsListView extends IsWidget {
	void setPresenter(EvaluationActionHandler presenter);

	void addRow(Evaluation ev);

	void clearRows();

	void add(IsWidget w);
}
