package org.sagebionetworks.web.client.widget.entity;

import java.util.List;

import org.sagebionetworks.evaluation.model.Evaluation;
import org.sagebionetworks.schema.adapter.AdapterFactory;
import org.sagebionetworks.web.client.ChallengeClientAsync;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.SynapseWidgetPresenter;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class AdministerEvaluationsList implements SynapseWidgetPresenter, AdministerEvaluationsListView.Presenter {
	
	private ChallengeClientAsync challengeClient;
	private AdapterFactory adapterFactory;
	private AdministerEvaluationsListView view;
	
	@Inject
	public AdministerEvaluationsList(AdministerEvaluationsListView view, ChallengeClientAsync challengeClient, AdapterFactory adapterFactory) {
		this.challengeClient = challengeClient;
		this.adapterFactory = adapterFactory;
		this.view = view;
	}

	/**
	 * 
	 * @param evaluations List of evaluations to display
	 * @param evaluationCallback call back with the evaluation if it is selected
	 */
	public void configure(String entityId, final CallbackP<Boolean> isChallengeCallback) {
		challengeClient.getSharableEvaluations(entityId, new AsyncCallback<List<Evaluation>>() {
			
			@Override
			public void onSuccess(List<Evaluation> evaluations) {					
				view.configure(evaluations);
				if (isChallengeCallback != null)
					isChallengeCallback.invoke(evaluations.size() > 0);
			}
			
			@Override
			public void onFailure(Throwable caught) {
				view.showErrorMessage(caught.getMessage());
			}
		});
		
	}
	
	
	@SuppressWarnings("unchecked")
	public void clearState() {
	}
	
	@Override
	public Widget asWidget() {
		return view.asWidget();
	}
	

		/*
	 * Private Methods
	 */
}
