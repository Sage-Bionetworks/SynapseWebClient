package org.sagebionetworks.web.client.widget.entity;

import java.util.List;

import org.sagebionetworks.repo.model.EntityHeader;
import org.sagebionetworks.web.client.widget.SynapseWidgetPresenter;

import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class MyEvaluationEntitiesList implements MyEvaluationEntitiesListView.Presenter, SynapseWidgetPresenter {
	
	private MyEvaluationEntitiesListView view;
	
	@Inject
	public MyEvaluationEntitiesList(MyEvaluationEntitiesListView view) {
		this.view = view;
		view.setPresenter(this);
	}
	
	public void configure(List<EntityHeader> entities) {
		view.configure(entities);
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
