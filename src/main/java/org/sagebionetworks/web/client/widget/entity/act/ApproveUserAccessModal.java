package org.sagebionetworks.web.client.widget.entity.act;

import org.sagebionetworks.evaluation.model.Evaluation;
import org.sagebionetworks.evaluation.model.EvaluationStatus;
import org.sagebionetworks.web.client.ChallengeClientAsync;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class ApproveUserAccessModal implements ApproveUserAccessModalView.Presenter, IsWidget {
	
	private ApproveUserAccessModalView view;
	private ChallengeClientAsync challengeClient;
	private Evaluation evaluation;
	private SynapseAlert synAlert;
	private Callback evaluationUpdatedCallback;
	private boolean isCreate;
	private AsyncCallback<Void> rpcCallback;
	@Inject
	public ApproveUserAccessModal(ApproveUserAccessModalView view, 
			ChallengeClientAsync challengeClient,
			SynapseAlert synAlert) {
		this.view = view;
		this.challengeClient = challengeClient;
		this.synAlert = synAlert;
		
		rpcCallback = new AsyncCallback<Void>() {
			@Override
			public void onSuccess(Void result) {
				ApproveUserAccessModal.this.view.hide();
				if (evaluationUpdatedCallback != null) {
					evaluationUpdatedCallback.invoke();
				}
			}
			
			@Override
			public void onFailure(Throwable caught) {
				ApproveUserAccessModal.this.synAlert.handleException(caught);
			}
		};
		view.setSynAlert(synAlert.asWidget());
		view.setPresenter(this);
	}
	
	public void configure(String entityId, Callback evaluationUpdatedCallback) {
		Evaluation newEvaluation = new Evaluation();
		newEvaluation.setContentSource(entityId);
		newEvaluation.setStatus(EvaluationStatus.OPEN);
		configure(newEvaluation, evaluationUpdatedCallback, true);
	}
	
	
	public void configure(Evaluation evaluation, Callback evaluationUpdatedCallback) {
		configure(evaluation, evaluationUpdatedCallback, false);
	}
	
	private void configure(Evaluation evaluation, Callback evaluationUpdatedCallback, boolean isCreate) {
		this.isCreate = isCreate;
		this.evaluation = evaluation;
		this.evaluationUpdatedCallback = evaluationUpdatedCallback;
		view.setEvaluationName(evaluation.getName());
		view.setSubmissionInstructionsMessage(evaluation.getSubmissionInstructionsMessage());
		view.setSubmissionReceiptMessage(evaluation.getSubmissionReceiptMessage());
	}
	
	public void show() {
		view.show();
	}
	
	@Override
	public void onSave() {
		synAlert.clear();
		
		evaluation.setName(view.getEvaluationName());
		evaluation.setSubmissionInstructionsMessage(view.getSubmissionInstructionsMessage());
		evaluation.setSubmissionReceiptMessage(view.getSubmissionReceiptMessage());
		
		if (isCreate) {
			createEvaluationFromView();
		} else {
			updateEvaluationFromView();
			
		}
	}
	
	public void updateEvaluationFromView() {
		challengeClient.updateEvaluation(evaluation, rpcCallback);
	}
	
	public void createEvaluationFromView() {
		challengeClient.createEvaluation(evaluation, rpcCallback);
	}
	
	public Widget asWidget() {
		view.setPresenter(this);			
		return view.asWidget();
	}
		
}
