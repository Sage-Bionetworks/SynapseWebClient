package org.sagebionetworks.web.client.widget.table;

import java.util.List;

import org.sagebionetworks.repo.model.entity.query.EntityQueryResult;
import org.sagebionetworks.repo.model.entity.query.SortDirection;
import org.sagebionetworks.web.client.SynapseView;
import org.sagebionetworks.web.client.widget.pagination.PaginationWidget;

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
		void onSort(String sortColumnName, SortDirection sortDirection);
	}

	/**
	 * Configure or reconfigure the view.
	 * @param tables
	 */
	public void configure(List<EntityQueryResult> tables);
	
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
	 * Add the pagination widget to the view.
	 * @param paginationWidget
	 */
	public void addPaginationWidget(PaginationWidget paginationWidget);

	/**
	 * Show or hide the pagination widget.
	 * @param b
	 */
	public void showPaginationVisible(boolean visible);

	/**
	 * Add the modal dialog to the view.
	 * @param uploadTableModalWidget
	 */
	public void addUploadTableModal(IsWidget uploadTableModalWidget);

	public void addWizard(IsWidget wizard);
	void resetSortUI();
	
}
