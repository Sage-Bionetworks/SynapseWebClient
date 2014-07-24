package org.sagebionetworks.web.client.view;

import java.util.List;
import java.util.Set;

import org.sagebionetworks.repo.model.EntityHeader;
import org.sagebionetworks.repo.model.EntityPath;
import org.sagebionetworks.repo.model.TrashedEntity;
import org.sagebionetworks.web.client.SynapsePresenter;
import org.sagebionetworks.web.client.SynapseView;
import org.sagebionetworks.web.client.widget.search.PaginationEntry;

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
	void configure(List<TrashedEntity> trashedEntities);
	
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
		 * Permanently deletes each trashed entity in the given set.
		 * @param trashedEntities The set containing the trashed entities to be deleted.
		 */
		void purgeEntities(Set<TrashedEntity> trashedEntities);
		
		/**
		 * Restores the given entity from the trash.
		 * @param trashedEntity The entity to be restored.
		 */
		void restoreEntity(TrashedEntity trashedEntity);

		/**
		 * Gets the trash.
		 */
		void getTrash(Integer offset);
		
		/**
		 * Gets the offset for pagination.
		 * @return offset
		 */
		int getOffset();
		
		List<PaginationEntry> getPaginationEntries(int nPerPage, int nPagesToShow);
		
	}
}
