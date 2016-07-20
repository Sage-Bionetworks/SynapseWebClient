package org.sagebionetworks.web.client.widget.entity;

import java.util.List;

import org.sagebionetworks.evaluation.model.Evaluation;
import org.sagebionetworks.web.client.ChallengeClientAsync;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.SynapseWidgetPresenter;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.client.widget.sharing.EvaluationAccessControlListModalWidget;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class AdministerEvaluationsList implements SynapseWidgetPresenter, AdministerEvaluationsListView.Presenter {
	
	private ChallengeClientAsync challengeClient;
	private AdministerEvaluationsListView view;
	private EvaluationAccessControlListModalWidget aclEditor;
	private SynapseAlert synAlert;
	
	@Inject
	public AdministerEvaluationsList(
			AdministerEvaluationsListView view, 
			ChallengeClientAsync challengeClient,
			EvaluationAccessControlListModalWidget aclEditor,
			SynapseAlert synAlert) {
		this.challengeClient = challengeClient;
		this.aclEditor = aclEditor;
		this.view = view;
		this.synAlert = synAlert;
		view.setPresenter(this);
	}

	/**
	 * 
	 * @param evaluations List of evaluations to display
	 * @param evaluationCallback call back with the evaluation if it is selected
	 */
	public void configure(String entityId, final CallbackP<Boolean> isChallengeCallback) {
		view.clear();
		challengeClient.getSharableEvaluations(entityId, new AsyncCallback<List<Evaluation>>() {
			@Override
			public void onSuccess(List<Evaluation> evaluations) {
				for (Evaluation evaluation : evaluations) {
					view.addRow(evaluation);
				}
				if (isChallengeCallback != null)
					isChallengeCallback.invoke(evaluations.size() > 0);
			}
			
			@Override
			public void onFailure(Throwable caught) {
				synAlert.handleException(caught);
				view.add(synAlert);
			}
		});
		
	}
	
	@Override
	public void onEditClicked(Evaluation evaluation) {
		//TODO: new modal for editing evaluation
		aclEditor.configure(evaluation);
		aclEditor.showSharing(new Callback() {
			@Override
			public void invoke() {
			}
		});
	}
	
	@Override
	public void onShareClicked(Evaluation evaluation) {
		aclEditor.configure(evaluation);
		aclEditor.showSharing(new Callback() {
			@Override
			public void invoke() {
			}
		});
	}
	
	
	@Override
	public Widget asWidget() {
		return view.asWidget();
	}
	
}
