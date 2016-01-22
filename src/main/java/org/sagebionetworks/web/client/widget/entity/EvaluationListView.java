package org.sagebionetworks.web.client.widget.entity;

import java.util.List;

import org.sagebionetworks.evaluation.model.Evaluation;
import org.sagebionetworks.web.client.SynapseView;

import com.google.gwt.user.client.ui.IsWidget;

public interface EvaluationListView extends IsWidget, SynapseView {

	/**
	 * Set the presenter.
	 * @param presenter
	 */
	void setPresenter(Presenter presenter);
		
	/**
	 * Presenter interface
	 */
	public interface Presenter {
		void configure(List<Evaluation> list);
		Evaluation getSelectedEvaluation();
	}

	void configure(List<Evaluation> evaluationList);
	Integer getSelectedEvaluationIndex();
	void setSelectedEvaluationIndex(int i);
}
