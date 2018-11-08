package org.sagebionetworks.web.client.view;

import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.web.client.SynapseView;

import com.google.gwt.user.client.ui.IsWidget;

public interface HomeView extends IsWidget, SynapseView {
	
	/**
	 * Set this view's presenter
	 * @param presenter
	 */
	public void setPresenter(Presenter presenter);
		
	public void refresh();
	
	public void showLoggedInUI(UserProfile profile);
	public void showRegisterUI();
	public void showLoginUI();
	public interface Presenter {
		void onUserChange();
	}
}
