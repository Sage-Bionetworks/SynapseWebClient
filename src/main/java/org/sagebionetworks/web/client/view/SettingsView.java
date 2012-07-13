package org.sagebionetworks.web.client.view;

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
	
	/**
	 * Alerts the view that the password change failed
	 */
	public void passwordChangeFailed();	
	
	/**
	 * Show the user that their email has been sent
	 */
	public void showRequestPasswordEmailSent();
	
	/**
	 * Alerts the view that the password request email failed to send.
	 */
	public void requestPasswordEmailFailed();
	
	public void refreshHeader();
	
	public interface Presenter extends SynapsePresenter {

		void resetPassword(String existingPassword, String newPassword);

		void createSynapsePassword();
		
		void goTo(Place place);
	}


}
