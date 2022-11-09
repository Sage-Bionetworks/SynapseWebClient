package org.sagebionetworks.web.client.widget.profile;

import com.google.gwt.event.dom.client.KeyDownHandler;
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
import org.sagebionetworks.web.client.ValidationUtils;
import org.sagebionetworks.web.client.cache.ClientCache;
import org.sagebionetworks.web.client.place.Profile;
import org.sagebionetworks.web.client.place.Synapse.ProfileArea;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.client.widget.upload.ImageUploadWidget;
import org.sagebionetworks.web.shared.WebConstants;

public class UserProfileEditorWidgetImpl
  implements UserProfileEditorWidget, UserProfileEditorWidgetView.Presenter {

  public static final String CONFIRM_SAVE_BEFORE_GOTO_SETTINGS_TITLE =
    "Would you like to save your profile changes?";
  public static final String CONFIRM_SAVE_BEFORE_GOTO_SETTINGS_MESSAGE =
    "Select OK to save the changes to your Profile and go to the Settings tab to change your password, or select Cancel to continue editing your Profile.";
  public static final String PLEASE_ENTER_A_VALID_URL =
    "Please enter a valid URL";
  public static final String PLEASE_SELECT_A_FILE = "Please select a file";
  public static final String CAN_ONLY_INCLUDE =
    "Can only include letters, numbers, dot (.), dash (-), and underscore (_)";
  public static final String MUST_BE_AT_LEAST_3_CHARACTERS =
    "Must be at least 3 characters";
  public static final String SEE_ERRORS_ABOVE = "See errors above.";

  UserProfileEditorWidgetView view;
  ProfileImageWidget imageWidget;
  ImageUploadWidget fileHandleUploadWidget;
  String fileHandleId;
  Callback uploadCompleteCallback;
  UserProfile originalProfile;
  SynapseJavascriptClient jsClient;
  ClientCache clientCache;
  AuthenticationController authController;
  GlobalApplicationState globalAppState;
  SynapseAlert synAlert;
  PopupUtilsView popupUtils;
  String orcIdHref;
  Callback callback;
  boolean goToAccountSettingsAfterSave = false;
  boolean isEditing = false;

  @Inject
  public UserProfileEditorWidgetImpl(
    UserProfileEditorWidgetView view,
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
    this.view.setPresenter(this);
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
    goToAccountSettingsAfterSave = false;
    view.hideUsernameError();
    view.hideLinkError();
    synAlert.clear();
    view.setUsername(profile.getUserName());
    view.setFirstName(profile.getFirstName());
    view.setLastName(profile.getLastName());
    view.setCurrentPosition(profile.getPosition());
    view.setCurrentAffiliation(profile.getCompany());
    view.setBio(profile.getSummary());
    view.setIndustry(profile.getIndustry());
    view.setLocation(profile.getLocation());
    view.setLink(profile.getUrl());
    if (profile.getEmails() != null && profile.getEmails().size() > 0) {
      // find out what the primary (notification) email address is
      // SWC-5599: the first email is not the notification email address.
      jsClient.getNotificationEmail(
        new AsyncCallback<NotificationEmail>() {
          @Override
          public void onFailure(Throwable caught) {
            synAlert.handleException(caught);
          }

          public void onSuccess(NotificationEmail notificationEmail) {
            view.setEmail(notificationEmail.getEmail());
          }
        }
      );
    }
    view.setOrcIdHref(orcIdHref);
    view.setOwnerId(profile.getOwnerId());
    setIsEditingMode(false);
    view.setCanEdit(
      profile.getOwnerId().equals(authController.getCurrentUserPrincipalId())
    );
    this.fileHandleId = profile.getProfilePicureFileHandleId();
    imageWidget.configure(profile.getOwnerId(), this.fileHandleId);
    imageWidget.setRemovePictureCallback(
      new Callback() {
        @Override
        public void invoke() {
          setNewFileHandle(null);
        }
      }
    );
    fileHandleUploadWidget.configure(fileUploaded -> {
      setNewFileHandle(fileUploaded.getFileHandleId());
    });
  }

  @Override
  public boolean isValid() {
    view.hideUsernameError();
    view.hideLinkError();
    boolean valid = true;
    // username
    String username = view.getUsername();
    if (!ValidationUtils.isValidUsername(username)) {
      valid = false;
      if (username.length() < 3) {
        view.showUsernameError(MUST_BE_AT_LEAST_3_CHARACTERS);
      } else {
        view.showUsernameError(CAN_ONLY_INCLUDE);
      }
    }
    // link
    String link = view.getLink();
    if (link != null && !"".equals(link.trim())) {
      if (!ValidationUtils.isValidUrl(link, true)) {
        valid = false;
        view.showLinkError(PLEASE_ENTER_A_VALID_URL);
      }
    }
    return valid;
  }

  @Override
  public String getFirstName() {
    return view.getFirstName();
  }

  @Override
  public String getImageId() {
    return fileHandleId;
  }

  @Override
  public String getLastName() {
    return view.getLastName();
  }

  @Override
  public String getUsername() {
    return view.getUsername();
  }

  @Override
  public String getPosition() {
    return view.getCurrentPosition();
  }

  @Override
  public String getCompany() {
    return view.getCurrentAffiliation();
  }

  @Override
  public String getIndustry() {
    return view.getIndustry();
  }

  @Override
  public String getLocation() {
    return view.getLocation();
  }

  @Override
  public String getUrl() {
    return view.getLink();
  }

  @Override
  public String getSummary() {
    return view.getBio();
  }

  public void setNewFileHandle(String fileHandleId) {
    this.fileHandleId = fileHandleId;
    this.fileHandleUploadWidget.reset();
    this.imageWidget.configure(this.fileHandleId);
    if (uploadCompleteCallback != null) {
      uploadCompleteCallback.invoke();
    }
  }

  @Override
  public void addKeyDownHandler(KeyDownHandler keyDownHandler) {
    view.addKeyDownHandlerToFields(keyDownHandler);
  }

  @Override
  public void setUploadingCallback(Callback startedUploadingCallback) {
    fileHandleUploadWidget.setUploadingCallback(startedUploadingCallback);
  }

  @Override
  public void setUploadingCompleteCallback(Callback uploadCompleteCallback) {
    this.uploadCompleteCallback = uploadCompleteCallback;
  }

  @Override
  public void onCancel() {
    // revert changes
    configure(originalProfile, orcIdHref, callback);
  }

  @Override
  public void onSave() {
    synAlert.clear();
    // First validate the view
    if (!isValid()) {
      synAlert.showError(SEE_ERRORS_ABOVE);
      return;
    }
    // Update the profile from the editor
    updateProfileFromEditor();
    // update the profile
    jsClient.updateMyUserProfile(
      originalProfile,
      new AsyncCallback<UserProfile>() {
        @Override
        public void onSuccess(UserProfile updateProfile) {
          // clear entry from the client cache
          clientCache.remove(
            originalProfile.getOwnerId() + WebConstants.USER_PROFILE_SUFFIX
          );
          // update the profile in the user session data
          authController.updateCachedProfile(originalProfile);
          setIsEditingMode(false);
          callback.invoke();
          if (goToAccountSettingsAfterSave) {
            globalAppState
              .getPlaceChanger()
              .goTo(
                new Profile(originalProfile.getOwnerId(), ProfileArea.SETTINGS)
              );
          }
        }

        @Override
        public void onFailure(Throwable caught) {
          synAlert.handleException(caught);
          view.resetSaveButtonState();
        }
      }
    );
  }

  @Override
  public void setIsEditingMode(boolean isEditing) {
    this.isEditing = isEditing;
    globalAppState.setIsEditing(isEditing);
    fileHandleUploadWidget.setVisible(isEditing);
    view.setEditMode(isEditing);
  }

  @Override
  public boolean isEditingMode() {
    return isEditing;
  }

  /**
   * Update the profile from the view.
   *
   * @return
   */
  public UserProfile updateProfileFromEditor() {
    originalProfile.setProfilePicureFileHandleId(getImageId());
    originalProfile.setUserName(getUsername());
    originalProfile.setFirstName(getFirstName());
    originalProfile.setLastName(getLastName());
    originalProfile.setPosition(getPosition());
    originalProfile.setCompany(getCompany());
    originalProfile.setIndustry(getIndustry());
    originalProfile.setLocation(getLocation());
    originalProfile.setUrl(getUrl());
    originalProfile.setSummary(getSummary());
    originalProfile.setDisplayName(
      originalProfile.getFirstName() + " " + originalProfile.getLastName()
    );
    return originalProfile;
  }
}
