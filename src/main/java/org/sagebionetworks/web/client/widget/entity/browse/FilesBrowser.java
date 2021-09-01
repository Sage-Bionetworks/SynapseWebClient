package org.sagebionetworks.web.client.widget.entity.browse;

import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.cookie.CookieProvider;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.SynapseWidgetPresenter;
import org.sagebionetworks.web.client.widget.clienthelp.ContainerClientsHelp;
import org.sagebionetworks.web.client.widget.entity.file.AddToDownloadListV2;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class FilesBrowser implements SynapseWidgetPresenter, FilesBrowserView.Presenter {

	private FilesBrowserView view;
	GlobalApplicationState globalApplicationState;
	AuthenticationController authenticationController;
	ContainerClientsHelp containerClientsHelp;
	AddToDownloadListV2 addToDownloadListV2;
	CookieProvider cookies;
	String entityId;

	@Inject
	public FilesBrowser(FilesBrowserView view, GlobalApplicationState globalApplicationState, AuthenticationController authenticationController, ContainerClientsHelp containerClientsHelp, AddToDownloadListV2 addToDownloadListV2, CookieProvider cookies) {
		this.view = view;
		this.globalApplicationState = globalApplicationState;
		this.authenticationController = authenticationController;
		this.containerClientsHelp = containerClientsHelp;
		this.addToDownloadListV2 = addToDownloadListV2;
		this.cookies = cookies;
		view.setPresenter(this);
	}

	/**
	 * Configure tree view with given entityId's children as start set
	 * 
	 * @param entityId
	 */
	public void configure(String entityId) {
		this.entityId = entityId;
		view.clear();
		view.configure(entityId);
	}

	public void clear() {
		view.clear();
	}

	@Override
	public Widget asWidget() {
		return view.asWidget();
	}

	public void setEntityClickedHandler(CallbackP<String> callback) {
		view.setEntityClickedHandler(callback);
	}

	@Override
	public void onProgrammaticDownloadOptions() {
		containerClientsHelp.configureAndShow(entityId);
	}

	@Override
	public void onAddToDownloadList() {
		view.setAddToDownloadList(addToDownloadListV2);
		addToDownloadListV2.configure(entityId);
	}
	public void setActionMenu(IsWidget w) {
		view.setActionMenu(w);
	}
}
