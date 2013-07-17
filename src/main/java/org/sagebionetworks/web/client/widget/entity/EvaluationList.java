package org.sagebionetworks.web.client.widget.entity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.sagebionetworks.evaluation.model.Evaluation;
import org.sagebionetworks.web.client.widget.SynapseWidgetPresenter;

import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class EvaluationList implements EvaluationListView.Presenter,
		SynapseWidgetPresenter {

	private EvaluationListView view;
	private Map<String, Evaluation> id2Evaluation;
	@Inject
	public EvaluationList(EvaluationListView view) {
		this.view = view;
		view.setPresenter(this);
	}
	
	@Override
	public void configure(List<Evaluation> list) {
		id2Evaluation = new HashMap<String, Evaluation>();
		for (Evaluation evaluation : list) {
			id2Evaluation.put(evaluation.getId(), evaluation);
		}
		view.configure(list);
	}
	
	@Override
	public Widget asWidget() {
		return view.asWidget();
	}
	
	@Override
	public List<Evaluation> getSelectedEvaluations() {
		return view.getSelectedEvaluations();
	}
	
	@Override
	public Evaluation getEvaluation(String evaluationId) {
		return id2Evaluation.get(evaluationId);
	}
}
