package org.sagebionetworks.web.client.widget.licenseddownloader;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.constants.IconType;
import org.sagebionetworks.web.client.DisplayUtils;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class LicensedDownloaderViewImpl implements LicensedDownloaderView {

	private Presenter presenter;
	
	/*
	 * Constructors
	 */
	@Inject
	public LicensedDownloaderViewImpl() {
	}

	/*
	 * View Impls
	 */
	@Override
	public void newWindow(String directDownloadURL) {
		DisplayUtils.newWindow(directDownloadURL, "", "");
	}

	@Override
	public void setPresenter(Presenter presenter) {
		this.presenter = presenter;
	}

	@Override
	public void showLoading() {
	}
	
	@Override
	public void clear() {
	}
	
	@Override
	public void showErrorMessage(String message) {
		DisplayUtils.showErrorMessage(message);
	}

	@Override
	public void showInfo(String title, String message) {
		DisplayUtils.showInfo(title, message);
	}
}
