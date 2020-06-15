package org.sagebionetworks.web.client.widget.evaluation;

import java.util.ArrayList;
import java.util.List;
import org.sagebionetworks.evaluation.model.Evaluation;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class SubmissionViewScope implements SubmissionViewScopeView.Presenter, IsWidget {

	private SubmissionViewScopeView view;
	private List<Evaluation> evaluations;
	
	@Inject
	public SubmissionViewScope(SubmissionViewScopeView view) {
		this.view = view;
		view.setPresenter(this);
	}

	public void configure(List<Evaluation> initEvaluations) {
		evaluations = new ArrayList<Evaluation>(initEvaluations);
		refresh();
	}

	private void refresh( ) {
		view.clearRows();
		
		for (Evaluation evaluation : evaluations) {
			view.addRow(evaluation);
		}
	}
	public List<Evaluation> getEvaluations() {
		return evaluations;
	}
	
	@Override
	public void onDeleteClicked(Evaluation evaluation) {
		evaluations.remove(evaluation);
		refresh();
	}
	
	@Override
	public void onAddClicked() {
		// TODO: pop up evaluation queue selector
		
	}

	@Override
	public Widget asWidget() {
		return view.asWidget();
	}
}
