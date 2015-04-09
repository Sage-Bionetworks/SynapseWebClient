package org.sagebionetworks.web.client.view;

import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.web.client.SynapsePresenter;
import org.sagebionetworks.web.client.SynapseView;

import com.google.gwt.event.dom.client.KeyDownHandler;
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
	
	/**
	 * Alerts the view that the password change failed
	 */
	public void passwordChangeFailed(String error);	
	
	public void updateStorageUsage(Long grandTotal);

	public void clearStorageUsageUI();
	
	public void updateNotificationCheckbox(UserProfile profile);
	
	void showNotificationEmailAddress(String primaryEmailAddress);
	void showEmailChangeSuccess(String message);
	void showEmailChangeFailed(String error);
	
	public interface Presenter extends SynapsePresenter {

		void resetPassword(String existingPassword, String newPassword);

		void goTo(Place place);
		
		void updateMyNotificationSettings(boolean sendEmailNotifications, boolean markEmailedMessagesAsRead);

		void changeApiKey();
		
		void addEmail(String emailAddress);
	}

	public void setApiKey(String apiKey);

}
