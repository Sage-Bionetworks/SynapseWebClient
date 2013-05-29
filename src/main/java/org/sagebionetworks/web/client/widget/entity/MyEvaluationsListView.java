package org.sagebionetworks.web.client.widget.entity;

import java.util.List;

import org.sagebionetworks.evaluation.model.Evaluation;

import com.google.gwt.user.client.ui.IsWidget;

public interface MyEvaluationsListView extends IsWidget {

	/**
	 * Set the presenter.
	 * @param presenter
	 */
	public void setPresenter(Presenter presenter);
	public void configure(List<Evaluation> evaluations);
	/**
	 * Presenter interface
	 */
	public interface Presenter {
	}
}
