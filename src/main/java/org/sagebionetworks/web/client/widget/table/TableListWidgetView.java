package org.sagebionetworks.web.client.widget.table;

import org.sagebionetworks.repo.model.EntityHeader;
import org.sagebionetworks.repo.model.entity.Direction;
import org.sagebionetworks.repo.model.entity.SortBy;
import org.sagebionetworks.web.client.SynapseView;

import com.google.gwt.user.client.ui.IsWidget;

public interface TableListWidgetView extends IsWidget, SynapseView {

	/**
	 * Set the presenter.
	 * @param presenter
	 */
	public void setPresenter(Presenter presenter);

	/**
	 * Presenter interface
	 */
	public interface Presenter {
		
		/**
		 * Add table button pushed
		 */
		void onAddTable();

		
		/**
		 * Add file view button pushed
		 */
		void onAddFileView();
		
		/**
		 * Upload table button pushed.
		 */
		void onUploadTable();
		/**
		 * Report when a table is clicked
		 */
		void onTableClicked(String entityId);
		
		/**
		 * called on sort
		 * @param sortColumnName
		 * @param sortDirection
		 */
		void onSort(SortBy sortColumn, Direction sortDirection);
	}
	void clearTableWidgets();
	void addTableListItem(EntityHeader header);
	void setLoadMoreWidget(IsWidget w);
	
	/**
	 * Show/hide the add table button.
	 * @param enabled
	 */
	public void setAddTableVisible(boolean enabled);
	
	/**
	 * Show/hide the add file view button.
	 * @param visible
	 */
	public void setAddFileViewVisible(boolean visible);
	
	
	/**
	 * Show or hide the upload tables button.
	 * @param enabled
	 */
	public void setUploadTableVisible(boolean enabled);
	
	/**
	 * Add the create table modal to the page.
	 * @param createTableModal
	 */
	public void addCreateTableModal(IsWidget createTableModal);


	/**
	 * Add the modal dialog to the view.
	 * @param uploadTableModalWidget
	 */
	public void addUploadTableModal(IsWidget uploadTableModalWidget);

	public void addWizard(IsWidget wizard);
	void resetSortUI();
	
}
