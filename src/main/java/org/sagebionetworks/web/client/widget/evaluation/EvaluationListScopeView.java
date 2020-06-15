package org.sagebionetworks.web.client.widget.evaluation;

import org.sagebionetworks.evaluation.model.Evaluation;
import com.google.gwt.user.client.ui.IsWidget;

public interface EvaluationListScopeView extends IsWidget {
	void setPresenter(Presenter presenter);

	void addRow(Evaluation ev);
	void clearRows();
	
	public interface Presenter {
		void onDeleteClicked(Evaluation evaluation);
		void onAddClicked();
	}

}
