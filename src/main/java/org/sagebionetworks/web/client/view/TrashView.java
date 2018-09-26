package org.sagebionetworks.web.client.view;

import java.util.List;
import java.util.Set;

import org.sagebionetworks.repo.model.TrashedEntity;
import org.sagebionetworks.web.client.SynapseView;
import org.sagebionetworks.web.client.widget.search.PaginationEntry;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;

public interface TrashView extends IsWidget, SynapseView {
	/**
	 * Set this view's Presenter
	 * @param presenter
	 */
	void setPresenter(Presenter presenter);
	
	void displayTrashedEntity(TrashedEntity trashedEntity);
	void removeDisplayTrashedEntity(TrashedEntity trashedEntity);
	void configure(List<TrashedEntity> trashedEntities);
	void displayEmptyTrash();
	void refreshTable();
	void displayFailureMessage(String title, String message);
	
	public interface Presenter {
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
		 * Fetches and displays the trash.
		 */
		void getTrash(Integer offset);
		
		/**
		 * Gets the offset for pagination.
		 * @return offset
		 */
		int getOffset();
		
		/**
		 * Gets the pagination entries of trashed entities.
		 * @param nPerPage
		 * @param nPagesToShow
		 * @return
		 */
		List<PaginationEntry> getPaginationEntries(int nPerPage, int nPagesToShow);
		
	}

	void setSynAlertWidget(Widget asWidget);
}
