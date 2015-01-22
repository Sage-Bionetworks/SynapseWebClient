package org.sagebionetworks.web.client.widget.entity;

import java.util.HashSet;
import java.util.List;

import org.sagebionetworks.evaluation.model.Evaluation;
import org.sagebionetworks.repo.model.Reference;
import org.sagebionetworks.repo.model.TeamHeader;
import org.sagebionetworks.web.client.SynapseView;

import com.google.gwt.user.client.ui.IsWidget;

public interface EvaluationSubmitterView extends SynapseView, IsWidget {

	void setPresenter(Presenter presenter);
	void showModal1(boolean showEntityFinder, List<Evaluation> evaluations);
	void hideModal1();
	void showModal2(List<TeamHeader> availableTeams);
	void hideModal2();
	void showSubmissionAcceptedDialogs(HashSet<String> receiptMessages);
	public interface Presenter {
		//view sends back the selected entity, selected evaluation Ids (will not be empty)
		void nextClicked(Reference selectedEntity, String submissionName, List<Evaluation> evaluationIds);
		/**
		 * 
		 * @param selectedTeam If set, is the team selected to be associated with the submission.  If null, it is a submission from an individual.
		 */
		void doneClicked(String teamId);
	}
}
