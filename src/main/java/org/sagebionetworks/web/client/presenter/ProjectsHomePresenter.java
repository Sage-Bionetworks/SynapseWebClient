package org.sagebionetworks.web.client.presenter;

import org.sagebionetworks.schema.adapter.AdapterFactory;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.place.ProjectsHome;
import org.sagebionetworks.web.client.place.Synapse;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.view.ProjectsHomeView;
import org.sagebionetworks.web.shared.exceptions.ConflictException;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.inject.Inject;

public class ProjectsHomePresenter extends AbstractActivity implements ProjectsHomeView.Presenter, Presenter<ProjectsHome>{
		
	private ProjectsHome place;
	private ProjectsHomeView view;
	private GlobalApplicationState globalApplicationState;
	private AuthenticationController authenticationController;
	private SynapseClientAsync synapseClient;
	AdapterFactory adapterFactory;
	
	@Inject
	public ProjectsHomePresenter(ProjectsHomeView view,
			GlobalApplicationState globalApplicationState,		
			AuthenticationController authenticationController, 
			SynapseClientAsync synapseClient, 
			AdapterFactory adapterFactory) {
		this.view = view;
		this.globalApplicationState = globalApplicationState;
		this.authenticationController = authenticationController;
		this.synapseClient = synapseClient;
		this.adapterFactory = adapterFactory;
		
		view.setPresenter(this);
	}

	@Override
	public void start(AcceptsOneWidget panel, EventBus eventBus) {
		// Install the view
		panel.setWidget(view);
	}

	@Override
	public void setPlace(ProjectsHome place) {
		this.place = place;
		view.setPresenter(this);
	}

	@Override
    public String mayStop() {
        view.clear();
        return null;
    }

	@Override
	public void createProject(final String name) {
		CreateEntityUtil.createProject(name, synapseClient, adapterFactory, globalApplicationState, authenticationController, new AsyncCallback<String>() {
			@Override
			public void onSuccess(String newProjectId) {
				view.showInfo(DisplayConstants.LABEL_PROJECT_CREATED, name);
				globalApplicationState.getPlaceChanger().goTo(new Synapse(newProjectId));						
			}
			
			@Override
			public void onFailure(Throwable caught) {
				if(caught instanceof ConflictException) {
					view.showErrorMessage(DisplayConstants.WARNING_PROJECT_NAME_EXISTS);
				} else {
					if(!DisplayUtils.handleServiceException(caught, globalApplicationState.getPlaceChanger(), authenticationController.isLoggedIn())) {					
						view.showErrorMessage(DisplayConstants.ERROR_GENERIC_RELOAD);
					} 
				}
			}
		});
	}
	
}
