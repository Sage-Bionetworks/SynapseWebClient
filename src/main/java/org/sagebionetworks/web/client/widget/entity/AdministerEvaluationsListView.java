package org.sagebionetworks.web.client.widget.entity;

import org.sagebionetworks.evaluation.model.Evaluation;
import org.sagebionetworks.web.client.widget.entity.EvaluationRowWidget.EvaluationActionHandler;

import com.google.gwt.user.client.ui.IsWidget;

public interface AdministerEvaluationsListView extends IsWidget {

	/**
	 * Set the presenter.
	 * @param presenter
	 */
	void setPresenter(Presenter presenter);
	void addRow(Evaluation ev);
	void clear();
	void add(IsWidget w);
	
	/**
	 * Presenter interface
	 */
	public interface Presenter extends EvaluationActionHandler {
	}
}
