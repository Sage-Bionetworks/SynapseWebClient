package org.sagebionetworks.web.client;

import org.gwtbootstrap3.client.ui.constants.IconType;
import org.sagebionetworks.web.client.utils.Callback;

import com.google.gwt.user.client.Window;

public class PopupUtilsViewImpl implements PopupUtilsView {

	@Override
	public void showInfo(String message) {
		DisplayUtils.showInfo(message);
	}
	
	@Override
	public void showInfo(String message, String href, String buttonText, IconType iconType) {
		DisplayUtils.showInfo(message, href, buttonText, iconType);
	}

	@Override
	public void showError(String message, Integer timeout) {
		DisplayUtils.showError(message, timeout);
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
