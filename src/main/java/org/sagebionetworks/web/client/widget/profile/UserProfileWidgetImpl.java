package org.sagebionetworks.web.client.widget.profile;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import org.gwtbootstrap3.client.ui.constants.ButtonSize;
import org.gwtbootstrap3.client.ui.constants.ButtonType;
import org.gwtbootstrap3.client.ui.constants.IconType;
import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.repo.model.principal.NotificationEmail;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.PopupUtilsView;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.SynapseJavascriptClient;
import org.sagebionetworks.web.client.cache.ClientCache;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.client.widget.upload.ImageUploadWidget;

public class UserProfileWidgetImpl implements UserProfileWidget {

  UserProfileWidgetView view;
  ProfileImageWidget imageWidget;
  ImageUploadWidget fileHandleUploadWidget;
  String fileHandleId;
  UserProfile originalProfile;
  SynapseJavascriptClient jsClient;
  ClientCache clientCache;
  AuthenticationController authController;
  GlobalApplicationState globalAppState;
  SynapseAlert synAlert;
  PopupUtilsView popupUtils;
  String orcIdHref;
  Callback callback;

  @Inject
  public UserProfileWidgetImpl(
    UserProfileWidgetView view,
    ProfileImageWidget imageWidget,
    ImageUploadWidget fileHandleUploadWidget,
    SynapseJavascriptClient jsClient,
    ClientCache clientCache,
    AuthenticationController authController,
    PortalGinInjector ginInjector,
    SynapseAlert synAlert,
    PopupUtilsView popupUtils,
    GlobalApplicationState globalAppState
  ) {
    super();
    this.view = view;
    this.imageWidget = imageWidget;
    this.fileHandleUploadWidget = fileHandleUploadWidget;
    this.jsClient = jsClient;
    this.clientCache = clientCache;
    this.authController = authController;
    this.synAlert = synAlert;
    this.popupUtils = popupUtils;
    this.globalAppState = globalAppState;
    fileHandleUploadWidget.setView(ginInjector.getCroppedImageUploadView());
    fileHandleUploadWidget.addStyleName("editProfileImageButton");
    fileHandleUploadWidget.setButtonIcon(IconType.EDIT);
    fileHandleUploadWidget.setButtonText("");
    fileHandleUploadWidget.setButtonType(ButtonType.DEFAULT);
    fileHandleUploadWidget.setButtonSize(ButtonSize.SMALL);
    fileHandleUploadWidget.setVisible(false);
    imageWidget.setRemovePictureCommandVisible(false);
    this.view.addFileInputWidget(fileHandleUploadWidget);
    this.view.addImageWidget(imageWidget);
    this.view.setSynAlert(synAlert);
  }

  @Override
  public Widget asWidget() {
    return this.view.asWidget();
  }

  @Override
  public void configure(
    UserProfile profile,
    String orcIdHref,
    Callback callback
  ) {
    this.callback = callback;
    this.orcIdHref = orcIdHref;
    originalProfile = profile;
    synAlert.clear();
    view.clearEmails();
    view.setUsername(profile.getUserName());
    view.setFirstName(profile.getFirstName());
    view.setLastName(profile.getLastName());
    view.setCurrentPosition(profile.getPosition());
    view.setCurrentAffiliation(profile.getCompany());
    view.setBio(profile.getSummary());
    view.setIndustry(profile.getIndustry());
    view.setLocation(profile.getLocation());
    view.setLink(profile.getUrl());
    boolean isCurrentUserProfile = profile
      .getOwnerId()
      .equals(authController.getCurrentUserPrincipalId());
    boolean isEmailAvailable =
      profile.getEmails() != null && profile.getEmails().size() > 0;
    view.setEmailsVisible(isEmailAvailable);
    if (isEmailAvailable) {
      // find out what the primary (notification) email address is
      // SWC-5599: the first email is not the notification email address.
      if (isCurrentUserProfile) {
        jsClient.getNotificationEmail(
          new AsyncCallback<NotificationEmail>() {
            @Override
            public void onFailure(Throwable caught) {
              synAlert.handleException(caught);
            }

            public void onSuccess(NotificationEmail notificationEmail) {
              view.setEmails(profile.getEmails(), notificationEmail.getEmail());
            }
          }
        );
      } else {
        view.setEmails(profile.getEmails(), null);
      }
    }
    view.setOrcIdHref(orcIdHref);
    view.setOwnerId(profile.getOwnerId());
    view.setCanEdit(isCurrentUserProfile);
    this.fileHandleId = profile.getProfilePicureFileHandleId();
    imageWidget.configure(profile.getOwnerId(), this.fileHandleId);
  }
}
