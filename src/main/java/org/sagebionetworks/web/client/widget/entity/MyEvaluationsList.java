package org.sagebionetworks.web.client.widget.entity;

import java.util.List;

import org.sagebionetworks.repo.model.EntityHeader;
import org.sagebionetworks.web.client.widget.SynapseWidgetPresenter;

import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class MyEvaluationsList implements MyEvaluationsListView.Presenter, SynapseWidgetPresenter {
	
	private MyEvaluationsListView view;
	
	@Inject
	public MyEvaluationsList(MyEvaluationsListView view) {
		this.view = view;
		view.setPresenter(this);
	}
	
	public void configure(List<EntityHeader> evaluations) {
		view.configure(evaluations);
	}
	
	@SuppressWarnings("unchecked")
	public void clearState() {
	}
	
	@Override
	public Widget asWidget() {
		return view.asWidget();
	}

		/*
	 * Private Methods
	 */
}
