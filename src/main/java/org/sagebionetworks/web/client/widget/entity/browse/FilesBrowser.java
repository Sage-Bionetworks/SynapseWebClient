package org.sagebionetworks.web.client.widget.entity.browse;

import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.repo.model.Folder;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.SynapseJavascriptClient;
import org.sagebionetworks.web.client.SynapseJavascriptFactory.OBJECT_TYPE;
import org.sagebionetworks.web.client.UploadView;
import org.sagebionetworks.web.client.cookie.CookieProvider;
import org.sagebionetworks.web.client.events.EntitySelectedHandler;
import org.sagebionetworks.web.client.events.EntityUpdatedEvent;
import org.sagebionetworks.web.client.events.EntityUpdatedHandler;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.SynapseWidgetPresenter;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class FilesBrowser implements FilesBrowserView.Presenter, SynapseWidgetPresenter {
	
	private FilesBrowserView view;
	
	private EntityUpdatedHandler entityUpdatedHandler;
	GlobalApplicationState globalApplicationState;
	AuthenticationController authenticationController;
	CookieProvider cookies;
	
	@Inject
	public FilesBrowser(FilesBrowserView view,
			GlobalApplicationState globalApplicationState,
			AuthenticationController authenticationController,
			CookieProvider cookies) {
		this.view = view;		
		this.globalApplicationState = globalApplicationState;
		this.authenticationController = authenticationController;
		this.cookies = cookies;
		view.setPresenter(this);
	}	
	
	/**
	 * Configure tree view with given entityId's children as start set
	 * @param entityId
	 */
	public void configure(String entityId) {
		view.clear();
		view.configure(entityId);
	}
	
	public void clear() {
		view.clear();
	}
	
	@Override
	public void fireEntityUpdatedEvent() {
		if (entityUpdatedHandler != null)
			entityUpdatedHandler.onPersistSuccess(new EntityUpdatedEvent());
	}
	
	public void setEntityUpdatedHandler(EntityUpdatedHandler handler) {
		this.entityUpdatedHandler = handler;
	}

	@Override
	public Widget asWidget() {
		view.setPresenter(this);
		return view.asWidget();
	}
	
	public void setEntitySelectedHandler(EntitySelectedHandler handler) {
		view.setEntitySelectedHandler(handler);
	}
	
	public void setEntityClickedHandler(CallbackP<String> callback) {
		view.setEntityClickedHandler(callback);
	}
}
