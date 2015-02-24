package org.sagebionetworks.web.client.widget.entity;

import java.util.ArrayList;
import java.util.List;

import org.sagebionetworks.evaluation.model.Evaluation;
import org.sagebionetworks.schema.adapter.AdapterFactory;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.client.ChallengeClientAsync;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.SynapseWidgetPresenter;
import org.sagebionetworks.web.shared.exceptions.UnknownErrorException;

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
		challengeClient.getSharableEvaluations(entityId, new AsyncCallback<ArrayList<String>>() {
			
			@Override
			public void onSuccess(ArrayList<String> results) {					
				try {	
					List<Evaluation> evaluations = new ArrayList<Evaluation>();
					for(String eh : results) {
						evaluations.add(new Evaluation(adapterFactory.createNew(eh)));
					}
					view.configure(evaluations);
					if (isChallengeCallback != null)
						isChallengeCallback.invoke(evaluations.size() > 0);
				} catch (JSONObjectAdapterException e) {
					onFailure(new UnknownErrorException(DisplayConstants.ERROR_INCOMPATIBLE_CLIENT_VERSION));
				}
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
