package org.sagebionetworks.web.client;

import org.gwtbootstrap3.extras.notify.client.ui.NotifySettings;
import org.sagebionetworks.web.client.utils.Callback;

public interface PopupUtilsView {
	void showInfo(String title, String message);
	void notify(String title, String message, NotifySettings settings);
	void showError(String title, String message, Integer timeout);
	void showErrorMessage(String message);
	void showErrorMessage(String title, String message);
	void showInfoDialog(
			String title, 
			String message,
			Callback okCallback
			);
	void showConfirmDialog(
			String title, 
			String message,
			Callback yesCallback,
			Callback noCallback
			);
	void showConfirmDialog(
			String title, 
			String message,
			Callback yesCallback
			);
	void openInNewWindow(String url);
}
