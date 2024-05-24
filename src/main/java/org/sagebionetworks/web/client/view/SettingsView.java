package org.sagebionetworks.web.client.view;

import com.google.gwt.place.shared.Place;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.web.client.SynapseView;
import org.sagebionetworks.web.client.utils.Callback;

public interface SettingsView extends IsWidget, SynapseView {
  /**
   * Set this view's presenter
   *
   * @param presenter
   */
  public void setPresenter(Presenter presenter);

  /**
   * Renders the view for a given presenter
   */
  public void render();

  public void updateNotificationCheckbox(UserProfile profile);

  void setEmailAddressesWidget(IsWidget w);

  public interface Presenter {
    void goTo(Place place);

    void updateMyNotificationSettings(
      boolean sendEmailNotifications,
      boolean markEmailedMessagesAsRead
    );

    void changeApiKey();

    void onEditProfile();

    void getAPIKey();

    void setShowUTCTime(boolean isUTC);

    void newVerificationSubmissionClicked();

    void editVerificationSubmissionClicked();

    void linkOrcIdClicked();

    void unbindOrcId();
  }

  public void setApiKeySettingsVisible(boolean visible);

  public void setApiKey(String apiKey);

  public void setNotificationSynAlertWidget(IsWidget asWidget);

  public void setAPISynAlertWidget(IsWidget synAlert);

  void hideAPIKey();

  void showConfirm(String message, Callback callback);

  void setShowingUTCTime();

  void setShowingLocalTime();

  void setOrcIdVisible(boolean isVisible);

  void setOrcIDLinkButtonVisible(boolean isVisible);

  void setUnbindOrcIdVisible(boolean isVisible);

  void setOrcId(String href);

  void showNotVerified();

  void setResubmitVerificationButtonVisible(boolean isVisible);

  void setVerificationSuspendedButtonVisible(boolean isVisible);

  void setVerificationRejectedButtonVisible(boolean isVisible);

  void setVerificationSubmittedButtonVisible(boolean isVisible);

  void setVerificationDetailsButtonVisible(boolean isVisible);

  void setIsCertified(boolean isCertified);
}
