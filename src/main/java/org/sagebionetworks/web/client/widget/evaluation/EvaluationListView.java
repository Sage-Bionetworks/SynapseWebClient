package org.sagebionetworks.web.client.widget.evaluation;

import java.util.List;
import org.sagebionetworks.evaluation.model.Evaluation;
import org.sagebionetworks.web.client.SynapseView;
import com.google.gwt.user.client.ui.IsWidget;

public interface EvaluationListView extends IsWidget, SynapseView {
	void configure(List<Evaluation> evaluationList);

	Integer getSelectedEvaluationIndex();

	void setSelectedEvaluationIndex(int i);
}
