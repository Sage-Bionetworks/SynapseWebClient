package org.sagebionetworks.web.client.widget.entity.controller;

import static org.sagebionetworks.web.client.ServiceEntryPointUtils.fixServiceEntryPoint;

import org.sagebionetworks.repo.model.UserSessionData;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.GWTWrapper;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.shared.exceptions.ForbiddenException;
import org.sagebionetworks.web.shared.exceptions.NotFoundException;
import org.sagebionetworks.web.shared.exceptions.ReadOnlyModeException;
import org.sagebionetworks.web.shared.exceptions.SynapseDownException;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
public class StuAlert implements StuAlertView.Presenter  {
	SynapseClientAsync synapseClient;
	StuAlertView view;
	String entityId;
	SynapseAlert synAlert;
	GWTWrapper gwt;
	AuthenticationController authController;
	
	@Inject
	public StuAlert(
			StuAlertView view,
			SynapseClientAsync synapseClient,
			SynapseAlert synAlert,
			GWTWrapper gwt,
			AuthenticationController authController
			) {
		this.view = view;
		this.synapseClient = synapseClient;
		fixServiceEntryPoint(synapseClient);
		this.synAlert = synAlert;
		this.authController = authController;
		this.gwt = gwt;
		view.setPresenter(this);
		view.setSynAlert(synAlert.asWidget());
	}

	public Widget asWidget() {
		return view.asWidget();
	}
	
	
	public void clear() {
		synAlert.clear();
		view.clearState();
		entityId = null;
	}
	
	public void show403() {
		clear();
		view.show403();
	}
	
	public void show403(String entityId) {
		show403();
		this.entityId = entityId;
		if (!authController.isLoggedIn()) {
			synAlert.showLogin();
		} else {
			view.showRequestAccessUI();	
		}
	}
	
	public void show404() {
		clear();
		view.show404();
	}
	
	public void onRequestAccess() {
		synAlert.clear();
		view.showRequestAccessButtonLoading();
		String userDisplayName = DisplayUtils.getDisplayName(authController.getCurrentUserProfile());
		String message = userDisplayName + " has requested access to an entity that you own. \nTo grant access, please visit " + gwt.getHostPageBaseURL() + "#!Synapse:" + entityId + " to change the Share settings.\n  More information about " + userDisplayName + " can be found by visiting " + gwt.getHostPageBaseURL() + "#!Profile:" + authController.getCurrentUserPrincipalId() + ".";
		synapseClient.sendMessageToEntityOwner(entityId, "Requesting access to " + entityId, message, gwt.getHostPageBaseURL(), new AsyncCallback<String>() {
			@Override
			public void onSuccess(String result) {
				view.showInfo("Request sent");
				view.hideRequestAccessUI();
			}
			@Override
			public void onFailure(Throwable caught) {
				synAlert.handleException(caught);
			}
		});
	}
	
	public String getEntityId() {
		return entityId;
	}
	
	public void handleException(Throwable ex) {
		clear();
		//if it's something that Stu recognizes, then he should handle it.
		if(ex instanceof ForbiddenException) {			
			if(!authController.isLoggedIn()) {
				synAlert.showLogin();
			} else {
				view.show403();
			}
		} else if(ex instanceof NotFoundException) {
			view.show404();
		} else {
			synAlert.handleException(ex);	
		}
		view.setVisible(true);
	}
	
	public void showError(String error) {
		clear();
		synAlert.showError(error);
		view.setVisible(true);
	}
	
	public void showLogin() {
		clear();
		synAlert.showLogin();
		view.setVisible(true);
	}
	
	public boolean isUserLoggedIn() {
		return synAlert.isUserLoggedIn();
	}
}
