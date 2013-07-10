package org.sagebionetworks.web.client.widget.entity;

import java.util.List;

import org.sagebionetworks.evaluation.model.Evaluation;
import org.sagebionetworks.repo.model.Reference;
import org.sagebionetworks.web.client.SynapseView;

public interface EvaluationSubmitterView extends SynapseView {

	void setPresenter(Presenter presenter);
	void popupSelector(boolean showEntityFinder, List<Evaluation> evaluations, List<String> submitterAliases);
	
	public interface Presenter {
		//view sends back the selected entity, selected evaluation Ids (will not be empty), and selected submitter alias (will not be null)
		void submitToEvaluations(Reference selectedEntity, List<String> evaluationIds, String submitterAlias);
	}

}
