package org.sagebionetworks.web.client.widget.evaluation;

import org.sagebionetworks.evaluation.model.Evaluation;
import com.google.gwt.user.client.ui.IsWidget;

public interface SubmissionViewScopeEditorView extends IsWidget {
	void setPresenter(Presenter presenter);
	void addRow(Evaluation ev);
	void clearRows();
	void add(IsWidget w);
	
	public interface Presenter {
		void onDeleteClicked(Evaluation evaluation);
		void onAddClicked();
	}

}
