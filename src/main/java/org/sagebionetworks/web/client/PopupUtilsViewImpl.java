package org.sagebionetworks.web.client;

import org.gwtbootstrap3.extras.notify.client.ui.NotifySettings;
import org.sagebionetworks.web.client.utils.Callback;

import com.google.gwt.user.client.Window;

public class PopupUtilsViewImpl implements PopupUtilsView {

	@Override
	public void showInfo(String title, String message) {
		DisplayUtils.showInfo(title, message);
	}

	@Override
	public void notify(String title, String message, NotifySettings settings) {
		DisplayUtils.notify(title, message, settings);
	}

	@Override
	public void showError(String title, String message, Integer timeout) {
		DisplayUtils.showError(title, message, timeout);
	}

	@Override
	public void showErrorMessage(String message) {
		DisplayUtils.showErrorMessage(message);
	}

	@Override
	public void showErrorMessage(String title, String message) {
		DisplayUtils.showErrorMessage(title, message);		
	}

	@Override
	public void showInfoDialog(String title, String message, Callback okCallback) {
		DisplayUtils.showInfoDialog(title, message, okCallback);
	}

	@Override
	public void showConfirmDialog(String title, String message, Callback yesCallback, Callback noCallback) {
		DisplayUtils.showConfirmDialog(title, message, yesCallback, noCallback);;
	}

	@Override
	public void showConfirmDialog(String title, String message, Callback yesCallback) {
		DisplayUtils.showConfirmDialog(title, message, yesCallback);
	}
	
	@Override
	public void openInNewWindow(String url) {
		Window.open(url, "_blank", "");	
	}
	
	@Override
	public void showConfirmDelete(String message, Callback callback) {
		DisplayUtils.confirmDelete(message, callback);
	}
}
