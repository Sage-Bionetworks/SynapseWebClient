package org.sagebionetworks.web.client.view.users;

import org.sagebionetworks.web.client.SynapseView;

import com.google.gwt.place.shared.Place;
import com.google.gwt.user.client.ui.IsWidget;

public interface RegisterAccountView extends IsWidget, SynapseView {
	
	/**
	 * Set this view's presenter
	 * @param presenter
	 */
	public void setPresenter(Presenter presenter);

	/**
	 * The default register view
	 */
	public void showDefault();
	
	/**
	 * The account was created view
	 */
	public void showAccountCreated();
	
	public void showErrorMessage(String message);
	
	public void clear();
	public void markUsernameUnavailable();
	public void markEmailUnavailable();
	
	
	public interface Presenter {	
		void goTo(Place place);
		
		void registerUser(String username, String email, String firstName, String lastName);
		
		public void checkUsernameAvailable(String username);
		public void checkEmailAvailable(String email);

	}

	public void showAccountCreationFailed();


}
