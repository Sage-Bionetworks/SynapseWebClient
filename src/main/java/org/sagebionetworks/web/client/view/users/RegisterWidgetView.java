package org.sagebionetworks.web.client.view.users;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;

public interface RegisterWidgetView extends IsWidget {

	/**
	 * Set the presenter.
	 * @param presenter
	 */
	void setPresenter(Presenter presenter);
	void setSynAlert(Widget w);
	/**
	 * Presenter interface
	 */
	public interface Presenter {
		void registerUser(String email);		
	}
	void enableRegisterButton(boolean enable);
	void setVisible(boolean isVisible);
	void setInlineUI(boolean isInline);
	void setEmail(String email);
	void setEmailSentAlert(boolean isVisible);
	void clear();
}
