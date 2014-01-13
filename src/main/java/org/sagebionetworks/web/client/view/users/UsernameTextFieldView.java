package org.sagebionetworks.web.client.view.users;

import org.sagebionetworks.web.client.SynapseView;

import com.google.gwt.user.client.ui.IsWidget;

public interface UsernameTextFieldView extends IsWidget, SynapseView {

	/**
	 * Set the presenter.
	 * @param presenter
	 */
	void setPresenter(Presenter presenter);
	
	void configure(String initUsername);

	boolean validate();
	void setIsUniqueUsername(boolean isUnique);
	
	/**
	 * Presenter interface
	 */
	public interface Presenter {
		void configure(String initUsername);
		boolean validate();
		void validateUsername(String username);
	}
}
