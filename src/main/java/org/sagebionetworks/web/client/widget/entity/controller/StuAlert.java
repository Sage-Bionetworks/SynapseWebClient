package org.sagebionetworks.web.client.widget.entity.controller;

import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import org.sagebionetworks.web.client.GWTWrapper;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.shared.exceptions.ForbiddenException;
import org.sagebionetworks.web.shared.exceptions.NotFoundException;

public class StuAlert {

  StuAlertView view;
  String entityId;
  Long entityVersion;
  SynapseAlert synAlert;
  GWTWrapper gwt;
  AuthenticationController authController;

  @Inject
  public StuAlert(
    StuAlertView view,
    SynapseAlert synAlert,
    GWTWrapper gwt,
    AuthenticationController authController
  ) {
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
    entityVersion = null;
  }

  public void show403() {
    show403(null, null);
  }

  public void show403(String entityId, Long entityVersion) {
    clear();
    this.entityId = entityId;
    this.entityVersion = entityVersion;
    view.show403(entityId, entityVersion, authController.isLoggedIn());
  }

  public void show404() {
    show404(null, null);
  }

  public void show404(String entityId, Long entityVersion) {
    clear();
    this.entityId = entityId;
    this.entityVersion = entityVersion;
    view.show404(entityId, entityVersion, authController.isLoggedIn());
  }

  public String getEntityId() {
    return entityId;
  }

  public void handleException(Throwable ex) {
    clear();
    // if it's something that Stu recognizes, then he should handle it.
    if (ex instanceof ForbiddenException) {
      view.show403(null, null, authController.isLoggedIn());
    } else if (ex instanceof NotFoundException) {
      view.show404(null, null, authController.isLoggedIn());
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
