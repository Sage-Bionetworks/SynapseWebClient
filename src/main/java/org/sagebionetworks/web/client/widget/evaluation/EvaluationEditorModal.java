package org.sagebionetworks.web.client.widget.evaluation;

import static org.sagebionetworks.web.client.ServiceEntryPointUtils.fixServiceEntryPoint;
import java.util.Date;
import org.sagebionetworks.evaluation.model.Evaluation;
import org.sagebionetworks.evaluation.model.EvaluationStatus;
import org.sagebionetworks.evaluation.model.SubmissionQuota;
import org.sagebionetworks.web.client.ChallengeClientAsync;
import org.sagebionetworks.web.client.DateTimeUtils;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.client.widget.user.UserBadge;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class EvaluationEditorModal implements EvaluationEditorModalView.Presenter, IsWidget {
	private EvaluationEditorModalView view;
	private ChallengeClientAsync challengeClient;
	private Evaluation evaluation;
	private SynapseAlert synAlert;
	private Callback evaluationUpdatedCallback;
	private boolean isCreate;
	private AsyncCallback<Void> rpcCallback;
	private UserBadge createdByBadge;
	private AuthenticationController authController;
	private DateTimeUtils dateTimeUtils;

	@Inject
	public EvaluationEditorModal(EvaluationEditorModalView view, ChallengeClientAsync challengeClient, SynapseAlert synAlert, UserBadge createdByBadge, AuthenticationController authController, DateTimeUtils dateTimeUtils) {
		this.view = view;
		this.challengeClient = challengeClient;
		this.authController = authController;
		fixServiceEntryPoint(challengeClient);
		this.synAlert = synAlert;
		this.dateTimeUtils = dateTimeUtils;
		this.createdByBadge = createdByBadge;
		view.setCreatedByWidget(createdByBadge);
		rpcCallback = new AsyncCallback<Void>() {
			@Override
			public void onSuccess(Void result) {
				EvaluationEditorModal.this.view.hide();
				if (evaluationUpdatedCallback != null) {
					evaluationUpdatedCallback.invoke();
				}
			}

			@Override
			public void onFailure(Throwable caught) {
				EvaluationEditorModal.this.synAlert.handleException(caught);
			}
		};
		view.setSynAlert(synAlert.asWidget());
		view.setPresenter(this);
	}

	public void configure(String entityId, Callback evaluationUpdatedCallback) {
		Evaluation newEvaluation = new Evaluation();
		newEvaluation.setContentSource(entityId);
		newEvaluation.setStatus(EvaluationStatus.OPEN);
		newEvaluation.setOwnerId(authController.getCurrentUserPrincipalId());
		newEvaluation.setCreatedOn(new Date());
		configure(newEvaluation, evaluationUpdatedCallback, true);
	}

	public void configure(Evaluation evaluation, Callback evaluationUpdatedCallback) {
		configure(evaluation, evaluationUpdatedCallback, false);
	}

	private void configure(Evaluation evaluation, Callback evaluationUpdatedCallback, boolean isCreate) {
		view.clear();
		this.isCreate = isCreate;
		this.evaluation = evaluation;
		this.evaluationUpdatedCallback = evaluationUpdatedCallback;
		view.setEvaluationName(evaluation.getName());
		view.setSubmissionInstructionsMessage(evaluation.getSubmissionInstructionsMessage());
		view.setSubmissionReceiptMessage(evaluation.getSubmissionReceiptMessage());
		createdByBadge.configure(evaluation.getOwnerId());
		view.setDescription(evaluation.getDescription());
		view.setCreatedOn(dateTimeUtils.getDateString(evaluation.getCreatedOn()));
		if (evaluation.getQuota() != null) {
			SubmissionQuota quota = evaluation.getQuota();
			if (quota.getFirstRoundStart() != null) {
				view.setRoundStart(quota.getFirstRoundStart());
			}
			if (quota.getNumberOfRounds() != null) {
				view.setNumberOfRounds(quota.getNumberOfRounds());
			}
			if (quota.getRoundDurationMillis() != null) {
				view.setRoundDuration(quota.getRoundDurationMillis());
			}
			if (quota.getSubmissionLimit() != null) {
				view.setSubmissionLimit(quota.getSubmissionLimit());
			}
		}
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
		evaluation.setDescription(view.getDescription());
		// quota
		Double submissionLimit = view.getSubmissionLimit();
		Double numberOfRounds = view.getNumberOfRounds();
		Double roundDuration = view.getRoundDuration();
		Date roundStart = view.getRoundStart();
		boolean isUserTryingToSetQuota = submissionLimit != null || numberOfRounds != null || roundDuration != null || roundStart != null;
		if (isUserTryingToSetQuota) {
			// user is attempting to set quota
			SubmissionQuota newQuota = new SubmissionQuota();
			if (submissionLimit != null) {
				newQuota.setSubmissionLimit(submissionLimit.longValue());
			}
			if (roundStart != null) {
				newQuota.setFirstRoundStart(roundStart);
			}
			if (numberOfRounds != null) {
				newQuota.setNumberOfRounds(numberOfRounds.longValue());
			}
			if (roundDuration != null) {
				newQuota.setRoundDurationMillis(roundDuration.longValue());
			}
			evaluation.setQuota(newQuota);
		} else {
			evaluation.setQuota(null);
		}

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
