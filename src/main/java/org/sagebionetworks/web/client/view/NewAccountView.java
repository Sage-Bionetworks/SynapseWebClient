package org.sagebionetworks.web.client.view;

import org.sagebionetworks.web.client.SynapseView;
import com.google.gwt.user.client.ui.IsWidget;

public interface NewAccountView extends IsWidget, SynapseView {

	/**
	 * Set this view's presenter
	 * 
	 * @param presenter
	 */
	void setPresenter(Presenter presenter);

	void markUsernameUnavailable();

	void setEmail(String email);

	void setLoading(boolean loading);

	public interface Presenter {
		void checkUsernameAvailable(String username);

		void completeRegistration(String userName, String fName, String lName, String password);
	}
}
