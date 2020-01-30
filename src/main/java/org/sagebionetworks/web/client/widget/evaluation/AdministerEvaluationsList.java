package org.sagebionetworks.web.client.widget.evaluation;

import static org.sagebionetworks.web.client.ServiceEntryPointUtils.fixServiceEntryPoint;
import java.util.List;
import org.sagebionetworks.evaluation.model.Evaluation;
import org.sagebionetworks.web.client.ChallengeClientAsync;
import org.sagebionetworks.web.client.widget.SynapseWidgetPresenter;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.client.widget.evaluation.EvaluationRowWidget.EvaluationActionHandler;
import org.sagebionetworks.web.client.widget.sharing.EvaluationAccessControlListModalWidget;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class AdministerEvaluationsList implements SynapseWidgetPresenter, EvaluationActionHandler {

	private ChallengeClientAsync challengeClient;
	private AdministerEvaluationsListView view;
	private EvaluationAccessControlListModalWidget aclEditor;
	private SynapseAlert synAlert;
	private String entityId;
	private EvaluationEditorModal evalEditor;

	@Inject
	public AdministerEvaluationsList(AdministerEvaluationsListView view, ChallengeClientAsync challengeClient, EvaluationAccessControlListModalWidget aclEditor, EvaluationEditorModal evalEditor, SynapseAlert synAlert) {
		this.challengeClient = challengeClient;
		fixServiceEntryPoint(challengeClient);
		this.aclEditor = aclEditor;
		this.view = view;
		this.synAlert = synAlert;
		this.evalEditor = evalEditor;
		view.add(evalEditor);
		view.add(aclEditor);
		view.setPresenter(this);
		view.add(synAlert);
	}

	/**
	 * 
	 * @param evaluations List of evaluations to display
	 * @param evaluationCallback call back with the evaluation if it is selected
	 */
	public void configure(String entityId) {
		this.entityId = entityId;
		view.clearRows();
		synAlert.clear();
		challengeClient.getSharableEvaluations(entityId, new AsyncCallback<List<Evaluation>>() {
			@Override
			public void onSuccess(List<Evaluation> evaluations) {
				for (Evaluation evaluation : evaluations) {
					view.addRow(evaluation);
				}
			}

			@Override
			public void onFailure(Throwable caught) {
				synAlert.handleException(caught);
			}
		});
	}

	public void refresh() {
		configure(entityId);
	}

	@Override
	public void onEditClicked(Evaluation evaluation) {
		// configure and show modal for editing evaluation
		evalEditor.configure(evaluation, () -> {
			refresh();
		});
		evalEditor.show();
	}

	@Override
	public void onShareClicked(Evaluation evaluation) {
		aclEditor.configure(evaluation, null);
		aclEditor.show();
	}

	@Override
	public void onDeleteClicked(Evaluation evaluation) {
		challengeClient.deleteEvaluation(evaluation.getId(), new AsyncCallback<Void>() {
			@Override
			public void onSuccess(Void result) {
				refresh();
			}

			@Override
			public void onFailure(Throwable caught) {
				synAlert.handleException(caught);
			}
		});
	}

	@Override
	public Widget asWidget() {
		return view.asWidget();
	}
}
