package org.sagebionetworks.web.client.widget.entity.controller;

import org.sagebionetworks.web.client.widget.entity.browse.EntityFinder;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class ProvenanceListRow implements ProvenanceListRowView.Presenter, IsWidget {

	ProvenanceListRowView view;
	EntityFinder entityFinderWidget;
	
	@Inject
	public ProvenanceListRow(ProvenanceListRowView view, EntityFinder entityFinderWidget) {
		this.view = view;
		this.entityFinderWidget = entityFinderWidget;
		view.setEntityFinderWidget(entityFinderWidget.asWidget());
	}
	
	@Override
	public Widget asWidget() {
		return view.asWidget();
	}
	
	@Override
	public void configureAsEntity() {
		view.setItemText("Entity");
		view.setEntityFieldsVisible(true);
		view.setURLFieldsVisible(false);
	}
	
	@Override
	public void configureAsURL() {
		view.setItemText("URL");
		view.setEntityFieldsVisible(false);
		view.setURLFieldsVisible(true);
	}

	public void setEntityText(String entityId) {
		view.setEntityText(entityId);
	}

	public void setURLText(String title, String address) {
		view.setURLName(title);
		view.setURLAddress(address);
	}
}
