package org.sagebionetworks.web.client.widget.entity.browse;

import java.util.ArrayList;
import java.util.List;

import org.sagebionetworks.repo.model.EntityChildrenRequest;
import org.sagebionetworks.repo.model.EntityChildrenResponse;
import org.sagebionetworks.repo.model.EntityType;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.SynapseJavascriptClient;
import org.sagebionetworks.web.client.cookie.CookieProvider;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.SynapseWidgetPresenter;
import org.sagebionetworks.web.client.widget.clienthelp.ContainerClientsHelp;
import org.sagebionetworks.web.client.widget.entity.file.AddToDownloadListV2;

import com.google.gwt.user.client.rpc.AsyncCallback;
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
	SynapseJavascriptClient jsClient;
	String entityId;

	@Inject
	public FilesBrowser(FilesBrowserView view, GlobalApplicationState globalApplicationState, AuthenticationController authenticationController, ContainerClientsHelp containerClientsHelp, AddToDownloadListV2 addToDownloadListV2, CookieProvider cookies, SynapseJavascriptClient jsClient) {
		this.view = view;
		this.globalApplicationState = globalApplicationState;
		this.authenticationController = authenticationController;
		this.containerClientsHelp = containerClientsHelp;
		this.addToDownloadListV2 = addToDownloadListV2;
		this.cookies = cookies;
		this.jsClient = jsClient;
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
		
		refreshHasFile();
	}
	
	public void refreshHasFile() {
		EntityChildrenRequest request = new EntityChildrenRequest();
		request.setParentId(entityId);
		request.setIncludeSumFileSizes(false);
		request.setIncludeTotalChildCount(false);
		List<EntityType> types = new ArrayList<>();
		types.add(EntityType.file);
		request.setIncludeTypes(types);
		jsClient.getEntityChildren(request, new AsyncCallback<EntityChildrenResponse>() {
			@Override
			public void onSuccess(EntityChildrenResponse result) {
				view.setHasFile(result.getPage().size() > 0);
			}
			@Override
			public void onFailure(Throwable caught) {
				view.showErrorMessage(caught.getMessage());
			}
		});
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
