package org.sagebionetworks.web.client.widget.entity.controller;

import org.sagebionetworks.web.client.PortalGinInjector;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class ProvenanceListWidget implements ProvenanceListWidgetView.Presenter {

	ProvenanceListWidgetView view;
	PortalGinInjector ginInjector;
	
	@Inject
	public ProvenanceListWidget(ProvenanceListWidgetView view, PortalGinInjector ginInjector) {
		this.view = view;
		this.ginInjector = ginInjector;
		view.setPresenter(this);
	}
	
	@Override
	public void addEntityRow() {
		ProvenanceListRow newRow = ginInjector.getProvenanceListRow();
		newRow.configureAsEntity();
		view.addRow(newRow.asWidget());
	}
	
	@Override
	public void loadEntityRow(String entityId) {
		ProvenanceListRow newRow = ginInjector.getProvenanceListRow();
		newRow.configureAsEntity();
		newRow.setEntityText(entityId);
		view.addRow(newRow.asWidget());
	}
	
	@Override
	public void addURLRow() {
		ProvenanceListRow newRow = ginInjector.getProvenanceListRow();
		newRow.configureAsURL();
		view.addRow(newRow.asWidget());
	}
	

	@Override
	public void loadURLRow(String title, String address) {
		ProvenanceListRow newRow = ginInjector.getProvenanceListRow();
		newRow.configureAsURL();
		newRow.setURLText(title, address);
		view.addRow(newRow.asWidget());
	}
	
	@Override
	public Widget asWidget() {
		return view.asWidget();
	}
}
