package org.sagebionetworks.web.client.widget.entity;

import java.util.List;

import org.sagebionetworks.evaluation.model.Evaluation;
import org.sagebionetworks.web.client.SynapseView;

import com.google.gwt.user.client.ui.IsWidget;

public interface EvaluationLinksListView extends IsWidget, SynapseView {

	/**
	 * Set the presenter.
	 * @param presenter
	 */
	public void setPresenter(Presenter presenter);
	public void configure(List<Evaluation> evaluations, String title);
	/**
	 * Presenter interface
	 */
	public interface Presenter {
		void evaluationClicked(Evaluation evaluation);
	}
}
