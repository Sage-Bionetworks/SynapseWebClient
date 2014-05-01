package org.sagebionetworks.web.client.widget.entity;

import java.util.List;

import org.sagebionetworks.evaluation.model.Evaluation;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.SynapseWidgetPresenter;

import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class EvaluationLinksList implements EvaluationLinksListView.Presenter, SynapseWidgetPresenter {
	
	private EvaluationLinksListView view;
	private CallbackP<Evaluation> evaluationCallback;
	
	@Inject
	public EvaluationLinksList(EvaluationLinksListView view) {
		this.view = view;
		view.setPresenter(this);
	}

	/**
	 * 
	 * @param evaluations List of evaluations to display
	 * @param evaluationCallback call back with the evaluation if it is selected
	 */
	public void configure(List<Evaluation> evaluations, CallbackP<Evaluation> evaluationCallback, String title, boolean showEvaluationIds) {
		this.evaluationCallback = evaluationCallback;
		view.configure(evaluations, title, showEvaluationIds);
	}
	
	@SuppressWarnings("unchecked")
	public void clearState() {
	}
	
	@Override
	public Widget asWidget() {
		return view.asWidget();
	}
	
	public void showErrorMessage(String message) {
		view.showErrorMessage(message);
	}
	
	public void showInfoMessage(String title, String message) {
		view.showInfo(title, message);
	}
	
	@Override
	public void evaluationClicked(Evaluation evaluation) {
		if (evaluationCallback != null)
			evaluationCallback.invoke(evaluation);
	}

	
		/*
	 * Private Methods
	 */
}
