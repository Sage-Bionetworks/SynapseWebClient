package org.sagebionetworks.web.client.widget.entity.browse;

import org.sagebionetworks.web.client.SynapseView;
import org.sagebionetworks.web.client.UploadView;
import org.sagebionetworks.web.client.events.EntitySelectedHandler;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.entity.EntityTreeItem;

import com.google.gwt.user.client.ui.IsWidget;

public interface FilesBrowserView extends IsWidget, SynapseView, UploadView {

	/**
	 * Set the presenter.
	 * @param presenter
	 */
	public void setPresenter(Presenter presenter);
	
	/**
	 * Configure the view with the parent id
	 * @param entityId
	 */
	void configure(String entityId);

	public void refreshTreeView(String entityId);

	public void setEntitySelectedHandler(EntitySelectedHandler handler);
	public void setEntityClickedHandler(CallbackP<String> callback);
	/**
	 * Presenter interface
	 */
	public interface Presenter {
		void fireEntityUpdatedEvent();
	}
}
