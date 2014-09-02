package org.sagebionetworks.web.client.view;

import org.sagebionetworks.web.client.SynapsePresenter;
import org.sagebionetworks.web.client.SynapseView;

import com.google.gwt.user.client.ui.IsWidget;

public interface NewAccountView extends IsWidget, SynapseView {
	
	/**
	 * Set this view's presenter
	 * @param presenter
	 */
	void setPresenter(Presenter presenter);	
	void markUsernameUnavailable();
	
	public interface Presenter extends SynapsePresenter {
		void checkUsernameAvailable(String username);
		void completeRegistration(String userName, String fName, String lName, String password);
	}

}
