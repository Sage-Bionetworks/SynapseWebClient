package org.sagebionetworks.web.client.widget.entity;

import java.util.List;

import org.sagebionetworks.evaluation.model.Evaluation;
import org.sagebionetworks.web.client.widget.SynapseWidgetPresenter;

import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class EvaluationList implements EvaluationListView.Presenter,
		SynapseWidgetPresenter {

	private EvaluationListView view;
	
	@Inject
	public EvaluationList(EvaluationListView view) {
		this.view = view;
		view.setPresenter(this);
	}
	
	@Override
	public void configure(List<Evaluation> list) {
		view.configure(list);
	}
	
	@Override
	public Widget asWidget() {
		return view.asWidget();
	}
	
	@Override
	public List<String> getSelectedEvaluationIds() {
		return view.getSelectedEvaluationIds();
	}
}
