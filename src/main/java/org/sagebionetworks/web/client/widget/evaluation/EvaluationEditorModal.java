package org.sagebionetworks.web.client.widget.evaluation;

import org.sagebionetworks.evaluation.model.Evaluation;
import org.sagebionetworks.web.client.ChallengeClientAsync;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class EvaluationEditorModal implements EvaluationEditorModalView.Presenter {
	
	private EvaluationEditorModalView view;
	private ChallengeClientAsync synapseClient;
	private Evaluation evaluation;
	private SynapseAlert synAlert;
	private Callback evaluationUpdatedCallback;
	
	@Inject
	public EvaluationEditorModal(EvaluationEditorModalView view, 
			ChallengeClientAsync synapseClient,
			SynapseAlert synAlert) {
		this.view = view;
		this.synapseClient = synapseClient;
		this.synAlert = synAlert;
		view.setPresenter(this);
	}	
	
	public void configure(Evaluation evaluation, Callback evaluationUpdatedCallback) {
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
		synapseClient.updateEvaluation(evaluation, new AsyncCallback<Void>() {
			@Override
			public void onSuccess(Void result) {
				if (evaluationUpdatedCallback != null) {
					evaluationUpdatedCallback.invoke();
				}
			}
			
			@Override
			public void onFailure(Throwable caught) {
				synAlert.handleException(caught);
			}
		});
	}
	
	public Widget asWidget() {
		view.setPresenter(this);			
		return view.asWidget();
	}
		
}
