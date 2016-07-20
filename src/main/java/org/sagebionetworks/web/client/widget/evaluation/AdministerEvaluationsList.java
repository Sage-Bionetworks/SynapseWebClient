package org.sagebionetworks.web.client.widget.evaluation;

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
	private String entityId;
	private CallbackP<Boolean> isChallengeCallback;
	private EvaluationEditorModal evalEditor;
	
	@Inject
	public AdministerEvaluationsList(
			AdministerEvaluationsListView view, 
			ChallengeClientAsync challengeClient,
			EvaluationAccessControlListModalWidget aclEditor,
			EvaluationEditorModal evalEditor,
			SynapseAlert synAlert) {
		this.challengeClient = challengeClient;
		this.aclEditor = aclEditor;
		this.view = view;
		this.synAlert = synAlert;
		this.evalEditor = evalEditor;
		view.setPresenter(this);
	}

	/**
	 * 
	 * @param evaluations List of evaluations to display
	 * @param evaluationCallback call back with the evaluation if it is selected
	 */
	public void configure(String entityId, final CallbackP<Boolean> isChallengeCallback) {
		this.entityId = entityId;
		this.isChallengeCallback = isChallengeCallback;
		view.clear();
		challengeClient.getSharableEvaluations(entityId, new AsyncCallback<List<Evaluation>>() {
			@Override
			public void onSuccess(List<Evaluation> evaluations) {
				for (Evaluation evaluation : evaluations) {
					view.addRow(evaluation);
				}
				if (isChallengeCallback != null)
					isChallengeCallback.invoke(evaluations.size() > 0);
				view.add(evalEditor.asWidget());
				view.add(aclEditor.asWidget());
			}
			
			@Override
			public void onFailure(Throwable caught) {
				synAlert.handleException(caught);
				view.add(synAlert);
			}
		});
	}
	
	public void refresh() {
		configure(entityId, isChallengeCallback);
	}
	
	@Override
	public void onEditClicked(Evaluation evaluation) {
		//configure and show modal for editing evaluation
		evalEditor.configure(evaluation, new Callback() {
			@Override
			public void invoke() {
				refresh();
			}
		});
		evalEditor.show();
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
