package org.sagebionetworks.web.client.view;

import org.sagebionetworks.repo.model.UserSessionData;
import org.sagebionetworks.web.client.SynapsePresenter;
import org.sagebionetworks.web.client.SynapseView;
import org.sagebionetworks.web.client.widget.login.AcceptTermsOfUseCallback;

import com.google.gwt.place.shared.Place;
import com.google.gwt.user.client.ui.IsWidget;

public interface LoginView extends IsWidget, SynapseView {
	
	void setPresenter(Presenter loginPresenter);	
	
	void showLoggingInLoader();
	
	void hideLoggingInLoader();
	
	void showLogout();
	
	void showLogin();	
	
	void showTermsOfUse(String content, AcceptTermsOfUseCallback callback);
	
	public interface Presenter extends SynapsePresenter {
		void goTo(Place place);
		void goToLastPlace();
		void setNewUser(UserSessionData newUser);
    }
	
}
