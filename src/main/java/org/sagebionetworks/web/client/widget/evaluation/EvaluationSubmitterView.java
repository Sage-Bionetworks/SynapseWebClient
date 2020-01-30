package org.sagebionetworks.web.client.widget.evaluation;

import java.util.List;
import org.sagebionetworks.evaluation.model.Evaluation;
import org.sagebionetworks.repo.model.Reference;
import org.sagebionetworks.repo.model.Team;
import org.sagebionetworks.web.client.SynapseView;
import org.sagebionetworks.web.shared.FormParams;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;

public interface EvaluationSubmitterView extends SynapseView, IsWidget {

	void setPresenter(Presenter presenter);

	void showModal1(boolean isEntitySet, FormParams formParams, List<Evaluation> evaluations);

	void hideModal1();

	void showModal2();

	void hideModal2();

	void showSubmissionAcceptedDialogs(String receiptMessage);

	void addEligibleContributor(String principalId);

	void addInEligibleContributor(String principalId, String reason);

	void setTeamInEligibleError(String error);

	void clearContributors();

	void showRegisterTeamDialog(String challengeId);

	void showEmptyTeams();

	void clearTeams();

	void showTeamsUI(List<Team> registeredTeams);

	void hideTeamsUI();

	void setIndividualSubmissionActive();

	void setTeamSubmissionActive();

	void setContributorsLoading(boolean isVisible);

	void setNextButtonLoading();

	void resetNextButton();

	void setSubmitButtonLoading();

	void resetSubmitButton();

	void setEvaluationListVisible(boolean visible);

	public interface Presenter {
		// view sends back the selected entity, selected evaluation
		void onNextClicked(Reference selectedEntity, String submissionName, Evaluation evaluation);

		void onDoneClicked();

		void onTeamSelected(int index);

		void onRegisterTeamClicked();

		void onNewTeamClicked();

		void onIndividualSubmissionOptionClicked();

		void onTeamSubmissionOptionClicked();

		void onDockerCommitNextButton();

		void refreshRegisteredTeams();
	}

	void setChallengesSynAlertWidget(Widget synAlert);

	void setTeamSelectSynAlertWidget(Widget synAlert);

	void setContributorsSynAlertWidget(Widget synAlert);

	void showDockerCommitModal();

	void hideDockerCommitModal();

	void setDockerCommitList(Widget widget);

	void setDockerCommitSynAlert(Widget widget);
}
