package org.sagebionetworks.web.client.view.users;

import org.sagebionetworks.web.client.SynapseView;

import com.google.gwt.user.client.ui.IsWidget;

public interface RegisterWidgetView extends IsWidget, SynapseView {

	/**
	 * Set the presenter.
	 * @param presenter
	 */
	void setPresenter(Presenter presenter);

	/**
	 * Presenter interface
	 */
	public interface Presenter {
		void registerUser(String email);		
	}
	void enableRegisterButton(boolean enable);
	void setVisible(boolean isVisible);

}
