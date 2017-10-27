package org.sagebionetworks.web.client.widget.entity.browse;

import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.SynapseWidgetPresenter;

import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class FilesBrowser implements SynapseWidgetPresenter {
	
	private FilesBrowserView view;
	GlobalApplicationState globalApplicationState;
	AuthenticationController authenticationController;
	
	@Inject
	public FilesBrowser(FilesBrowserView view,
			GlobalApplicationState globalApplicationState,
			AuthenticationController authenticationController) {
		this.view = view;		
		this.globalApplicationState = globalApplicationState;
		this.authenticationController = authenticationController;
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
	public Widget asWidget() {
		return view.asWidget();
	}
	
	public void setEntityClickedHandler(CallbackP<String> callback) {
		view.setEntityClickedHandler(callback);
	}
}
