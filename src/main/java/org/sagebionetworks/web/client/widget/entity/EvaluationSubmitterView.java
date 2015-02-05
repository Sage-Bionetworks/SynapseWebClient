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
	boolean isIndividual();
	void setContributorsListVisible(boolean isVisible);
	void addEligibleContributor(String principalId);
	void addInEligibleContributor(String principalId, String reason);
	void clearContributors();
	void setTeams(List<Team> registeredTeams);
	void showRegisterTeamDialog(String challengeId);
	
	public interface Presenter {
		//view sends back the selected entity, selected evaluation
		void nextClicked(Reference selectedEntity, String submissionName, Evaluation evaluation);
		void doneClicked();
		void teamSelected(String teamName);
		void teamAdded();
		void registerMyTeamLinkClicked();
		void createNewTeamClicked();
	}
}
