package org.sagebionetworks.web.client.widget.login;

import com.google.gwt.user.client.ui.IsWidget;

public interface PasswordStrengthWidgetView extends IsWidget {

	/**
	 * Set the presenter.
	 * @param presenter
	 */
	public void setPresenter(Presenter presenter);
	
	
	/**
	 * Presenter interface
	 */
	public interface Presenter {
	}
	
	void setVisible(boolean visible);
	void showWeakPasswordUI(String reason);
	void showFairPasswordUI(String reason);
	void showGoodPasswordUI();
	void showStrongPasswordUI();
}
