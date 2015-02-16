package org.sagebionetworks.web.client.widget.entity;

import java.util.List;

import org.sagebionetworks.evaluation.model.Evaluation;
import org.sagebionetworks.repo.model.Reference;
import org.sagebionetworks.repo.model.Team;
import org.sagebionetworks.web.client.SynapseView;

import com.google.gwt.user.client.ui.IsWidget;

public interface EvaluationSubmitterView extends SynapseView, IsWidget {

	void setPresenter(Presenter presenter);
	void showModal1(boolean showEntityFinder, List<Evaluation> evaluations);
	void hideModal1();
	void showModal2();
	void hideModal2();
	void showSubmissionAcceptedDialogs(String receiptMessage);
	void setContributorsListVisible(boolean isVisible);
	void addEligibleContributor(String principalId);
	void addInEligibleContributor(String principalId, String reason);
	void setTeamInEligibleErrorVisible(boolean isVisible, String error);
	void clearContributors();
	void showRegisterTeamDialog(String challengeId);
	void setTeamComboBoxEnabled(boolean isEnabled);
	void showEmptyTeams();
	void clearTeams();
	void showTeams(List<Team> registeredTeams);
	public interface Presenter {
		//view sends back the selected entity, selected evaluation
		void onNextClicked(Reference selectedEntity, String submissionName, Evaluation evaluation);
		void onDoneClicked();
		void onTeamSelected(String teamName);
		void teamAdded();
		void onRegisterTeamClicked();
		void onNewTeamClicked();
		void onIndividualSubmissionOptionClicked();
		void onTeamSubmissionOptionClicked();
	}
}
