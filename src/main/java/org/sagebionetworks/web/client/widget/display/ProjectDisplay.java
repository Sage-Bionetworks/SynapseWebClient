package org.sagebionetworks.web.client.widget.display;

import org.sagebionetworks.repo.model.EntityBundle;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.cookie.CookieProvider;
import org.sagebionetworks.web.client.events.EntityUpdatedHandler;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class ProjectDisplay implements ProjectDisplayView {
	ProjectDisplayView view;
	SynapseClientAsync synapseClient;
	SynapseAlert synAlert;
	EntityUpdatedHandler entityUpdatedHandler;
	EntityBundle entityBundle;
	CookieProvider cookies;
	
	@Inject
	public ProjectDisplay(ProjectDisplayView view,
			SynapseClientAsync synapseClient, 
			SynapseAlert synAlert, 
			CookieProvider cookies) {
		this.view = view;
		this.synapseClient = synapseClient;
		this.synAlert = synAlert;
		this.cookies = cookies;
		view.setSynAlertWidget(synAlert);
	}
	
	
	@Override
	public Widget asWidget() {
		return view.asWidget();
	}
	
	public void show() {
		view.show();
	}
	
	public void hide() {
		view.hide();
	}
	
	@Override
	public void clear() {
		synAlert.clear();
		view.clear();
	}

	@Override
	public void onSave() {
		synAlert.clear();
	}


	@Override
	public void setSynAlertWidget(IsWidget asWidget) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void setPresenter(Presenter presenter) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void configure() {
		// TODO Auto-generated method stub
		
	}
}
