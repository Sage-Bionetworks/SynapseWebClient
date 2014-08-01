package org.sagebionetworks.web.client.widget.entity.browse;

import org.sagebionetworks.web.client.SynapseView;
import org.sagebionetworks.web.client.UploadView;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.utils.CallbackP;

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
	public void configure(String entityId, boolean canEdit);

	/**
	 * Configure the view with the parent id and title
	 * @param entityId
	 * @param title
	 */
	public void configure(String entityId, boolean canEdit, String title);

	public void refreshTreeView(String entityId);
	
	public void showUploadDialog(String entityId);
	public void showFolderEditDialog(String folderEntityId);
	public void showQuizInfoDialog(CallbackP<Boolean> callback);
	
	/**
	 * Presenter interface
	 */
	public interface Presenter {
		void addFolderClicked();
		void uploadButtonClicked();
		void updateFolderName(String newFolderName, String folderEntityId);
		void deleteFolder(String folderEntityId, boolean skipTrashCan);
		void fireEntityUpdatedEvent();

	}

}
