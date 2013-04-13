package org.sagebionetworks.web.client.utils;

import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.widget.Alert;
import org.sagebionetworks.web.client.widget.Alert.AlertType;

import com.extjs.gxt.ui.client.widget.MessageBox;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;

/**
 * Extracted from DisplayUtils.
 * @author jmhill
 *
 */
public class AlertUtils {

	
	/**
	 * Shows an info message to the user in the "Global Alert area".
	 * For more precise control over how the message appears,
	 * use the {@link displayGlobalAlert(Alert)} method.
	 * @param title
	 * @param message
	 */
	public static void showInfo(String title, String message) {
		Alert alert = new Alert(title, message);
		alert.setAlertType(AlertType.Info);
		alert.setTimeout(4000);
		displayGlobalAlert(alert);
	}
	
	/**
	 * The preferred method for creating new global alerts.  For a
	 * default 'info' type alert, you can also use {@link showInfo(String, String)}
	 * @param alert
	 */
	public static void displayGlobalAlert(Alert alert) {
		Element container = DOM.getElementById(DisplayUtils.ALERT_CONTAINER_ID);
		DOM.insertChild(container, alert.getElement(), 0);
	}


	public static void showErrorMessage(String message) {
		MessageBox.info(DisplayConstants.TITLE_ERROR, message, null);
	}
}
