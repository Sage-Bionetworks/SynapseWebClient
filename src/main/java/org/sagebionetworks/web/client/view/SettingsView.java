package org.sagebionetworks.web.client.view;

import org.gwtbootstrap3.extras.bootbox.client.callback.ConfirmCallback;
import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.web.client.SynapsePresenter;
import org.sagebionetworks.web.client.SynapseView;

import com.google.gwt.place.shared.Place;
import com.google.gwt.user.client.ui.IsWidget;

public interface SettingsView extends IsWidget, SynapseView {
	
	/**
	 * Set this view's presenter
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
	
	public void updateStorageUsage(Long grandTotal);

	public void clearStorageUsageUI();
	
	public void updateNotificationCheckbox(UserProfile profile);
	
	void showNotificationEmailAddress(String primaryEmailAddress);
	void showEmailChangeSuccess(String message);
	
	public interface Presenter extends SynapsePresenter {

		void resetPassword(String existingPassword, String newPassword);

		void goTo(Place place);
		
		void updateMyNotificationSettings(boolean sendEmailNotifications, boolean markEmailedMessagesAsRead);

		void changeApiKey();
		
		void addEmail(String emailAddress);
		void onEditProfile();
		void getAPIKey();

		void changePassword();
	}

	public void setApiKey(String apiKey);


	public void setNotificationSynAlertWidget(IsWidget asWidget);

	public void setAddressSynAlertWidget(IsWidget asWidget);

	public void setAPISynAlertWidget(IsWidget synAlert);
	
	public void setPasswordSynAlertWidget(IsWidget synAlert);
	
	void hideAPIKey();
	void showConfirm(String message, ConfirmCallback callback);

	String getPassword1Field();

	String getCurrentPasswordField();

	String getPassword2Field();

	void setCurrentPasswordInError(boolean inError);

	void setPassword1InError(boolean inError);

	void setPassword2InError(boolean inError);

	void setChangePasswordEnabled(boolean isEnabled);

	void resetChangePasswordUI();
}
