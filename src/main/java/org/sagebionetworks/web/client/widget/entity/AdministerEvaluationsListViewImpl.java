package org.sagebionetworks.web.client.widget.entity;

import org.gwtbootstrap3.client.ui.Panel;
import org.gwtbootstrap3.client.ui.html.Div;
import org.gwtbootstrap3.client.ui.html.Text;
import org.sagebionetworks.evaluation.model.Evaluation;

public class AdministerEvaluationsListViewImpl extends Panel implements AdministerEvaluationsListView {

	private Presenter presenter;
	Div titleDiv = new Div();
	public AdministerEvaluationsListViewImpl() {
		titleDiv.addStyleName("highlight-title margin-left-5");
		titleDiv.add(new Text("Evaluation Queues"));
		addStyleName("min-height-400");
	}
	
	@Override
	public void addRow(Evaluation evaluation) {
		EvaluationRowWidget newRow = new EvaluationRowWidget();
		newRow.configure(evaluation, presenter);
		add(newRow.asWidget());
	}
	
	@Override 
	public void setPresenter(Presenter presenter) {
		this.presenter = presenter;
	}
	@Override
	public void clear() {
		super.clear();
		add(titleDiv);
	}
	/*
	 * Private Methods
	 */

}
