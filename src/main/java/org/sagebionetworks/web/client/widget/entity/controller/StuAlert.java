package org.sagebionetworks.web.client.widget.entity.controller;

import org.sagebionetworks.web.client.GWTWrapper;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.shared.exceptions.ForbiddenException;
import org.sagebionetworks.web.shared.exceptions.NotFoundException;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class StuAlert {
	StuAlertView view;
	String entityId;
	SynapseAlert synAlert;
	GWTWrapper gwt;
	AuthenticationController authController;

	@Inject
	public StuAlert(StuAlertView view, SynapseAlert synAlert, GWTWrapper gwt, AuthenticationController authController) {
		this.view = view;
		this.synAlert = synAlert;
		this.authController = authController;
		this.gwt = gwt;
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
		}
	}

	public void show404() {
		clear();
		view.show404();
	}

	public String getEntityId() {
		return entityId;
	}

	public void handleException(Throwable ex) {
		clear();
		// if it's something that Stu recognizes, then he should handle it.
		if (ex instanceof ForbiddenException) {
			if (!authController.isLoggedIn()) {
				synAlert.showLogin();
			} else {
				view.show403();
			}
		} else if (ex instanceof NotFoundException) {
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
