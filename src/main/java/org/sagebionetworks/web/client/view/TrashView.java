package org.sagebionetworks.web.client.view;

import org.sagebionetworks.repo.model.EntityHeader;
import org.sagebionetworks.repo.model.EntityPath;
import org.sagebionetworks.repo.model.TrashedEntity;
import org.sagebionetworks.web.client.SynapsePresenter;
import org.sagebionetworks.web.client.SynapseView;

import com.google.gwt.user.client.ui.IsWidget;

public interface TrashView extends IsWidget, SynapseView {
	/**
	 * Set this view's Presenter
	 * @param presenter
	 */
	void setPresenter(Presenter presenter);
	
	// TODO: Declare methods. Presenter methods like "setUsername".
	void displayTrashedEntity(TrashedEntity trashedEntity);
	void removeDisplayTrashedEntity(TrashedEntity trashedEntity);
	
	
	public interface Presenter extends SynapsePresenter {
		/**
		 * Permanently deletes all Entities in the trash.
		 */
		void purgeAll();
		
		/**
		 * Permanently deletes the given trashed entity.
		 * @param trashedEntity The entity to be deleted.
		 */
		void purgeEntity(TrashedEntity trashedEntity);
		
		/**
		 * Restores the given entity from the trash.
		 * @param trashedEntity The entity to be restored.
		 */
		void restoreEntity(TrashedEntity trashedEntity);

		/**
		 * Gets the trash.
		 */
		void getTrash();
		
	}
}
