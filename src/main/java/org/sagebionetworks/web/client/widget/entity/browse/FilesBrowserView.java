package org.sagebionetworks.web.client.widget.entity.browse;

import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.web.client.widget.SynapseWidgetView;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.IsWidget;

public interface FilesBrowserView extends IsWidget, SynapseWidgetView {

	/**
	 * Set the presenter.
	 * @param presenter
	 */
	public void setPresenter(Presenter presenter);
	
	/**
	 * Configure the view with the parent id
	 * @param entityId
	 */
	public void configure(String entityId);

	/**
	 * Configure the view with the parent id and title
	 * @param entityId
	 * @param title
	 */
	public void configure(String entityId, String title);

	public void refreshTreeView(String entityId);
	
	/**
	 * Presenter interface
	 */
	public interface Presenter {

		void createFolder(String name);

		void createEntityForUpload(AsyncCallback<Entity> asyncCallback);

		void fireEntityUpdatedEvent();

		void renameChildToFilename(String entityId);

	}


}
