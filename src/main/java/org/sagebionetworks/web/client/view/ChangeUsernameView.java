package org.sagebionetworks.web.client.view;

import org.sagebionetworks.web.client.SynapsePresenter;
import org.sagebionetworks.web.client.SynapseView;

import com.google.gwt.user.client.ui.IsWidget;

public interface ChangeUsernameView extends IsWidget, SynapseView {
	
	/**
	 * Set this view's presenter
	 * @param presenter
	 */
	void setPresenter(Presenter presenter);	
	
	void showUsernameInvalid();

	void showSetUsernameError(Throwable t);
	
	public interface Presenter extends SynapsePresenter {
		void setUsername(String newUsername);
	}

}
