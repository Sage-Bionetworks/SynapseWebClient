package org.sagebionetworks.web.client.view;

import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.web.client.SynapseView;
import org.sagebionetworks.web.client.utils.Callback;
import com.google.gwt.place.shared.Place;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;

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

	/**
	 * Shows the user that their password change succeeded
	 */
	public void showPasswordChangeSuccess();

	public void updateNotificationCheckbox(UserProfile profile);

	void setSubscriptionsListWidget(Widget w);

	void setSubscriptionsVisible(boolean visible);

	void setEmailAddressesWidget(IsWidget w);

	public interface Presenter {

		void resetPassword(String existingPassword, String newPassword);

		void goTo(Place place);

		void updateMyNotificationSettings(boolean sendEmailNotifications, boolean markEmailedMessagesAsRead);

		void changeApiKey();

		void onEditProfile();

		void getAPIKey();

		void changePassword();

		void setShowUTCTime(boolean isUTC);

		void newVerificationSubmissionClicked();

		void editVerificationSubmissionClicked();

		void linkOrcIdClicked();

		void unbindOrcId();
	}

	public void setApiKey(String apiKey);

	public void setNotificationSynAlertWidget(IsWidget asWidget);

	public void setAPISynAlertWidget(IsWidget synAlert);

	public void setPasswordSynAlertWidget(IsWidget synAlert);

	void hideAPIKey();

	void showConfirm(String message, Callback callback);

	String getPassword1Field();

	String getCurrentPasswordField();

	String getPassword2Field();

	void setCurrentPasswordInError(boolean inError);

	void setPassword1InError(boolean inError);

	void setPassword2InError(boolean inError);

	void setChangePasswordEnabled(boolean isEnabled);

	void resetChangePasswordUI();

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
