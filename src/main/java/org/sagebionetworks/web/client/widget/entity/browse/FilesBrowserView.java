package org.sagebionetworks.web.client.widget.entity.browse;

import org.sagebionetworks.web.client.SynapseView;
import org.sagebionetworks.web.client.UploadView;
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
	void configure(String entityId, boolean canCertifiedUserAddChild);

	public void refreshTreeView(String entityId);
	
	public void showFolderEditDialog(final String folderEntityId);
	
	/**
	 * Presenter interface
	 */
	public interface Presenter {
		void addFolderClicked();
		void uploadButtonClicked();
		void updateFolderName(String newFolderName);
		void deleteFolder(boolean skipTrashCan);
		void fireEntityUpdatedEvent();
	}

}
