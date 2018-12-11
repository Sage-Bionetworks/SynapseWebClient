package org.sagebionetworks.web.client.widget.entity.controller;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
/**
 * Abstraction for the view 
 *
 */
public interface StuAlertView extends IsWidget {
	
	/**
	 * Show info to the user.
	 * @param string
	 */
	void showInfo(String message);
	/**
	 * Clears all state in the view (makes all components invisible).
	 */
	void clearState();
	
	void setPresenter(Presenter p);

	/**
	 * Presenter interface
	 */
	public interface Presenter {
		void onRequestAccess();
	}

	void show403();
	void showRequestAccessUI();
	void hideRequestAccessUI();
	void showRequestAccessButtonLoading();
	void show404();
	
	void setSynAlert(Widget w);
	void setVisible(boolean visible);
	
}
