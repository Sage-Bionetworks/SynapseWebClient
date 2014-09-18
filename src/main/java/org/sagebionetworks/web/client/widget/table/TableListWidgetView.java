package org.sagebionetworks.web.client.widget.table;

import java.util.List;

import org.sagebionetworks.repo.model.EntityHeader;
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
		 * Upload table button pushed.
		 */
		void onUploadTable();
				
	}

	/**
	 * Configure or reconfigure the view.
	 * @param tables
	 */
	public void configure(List<EntityHeader> tables);

	/**
	 * Add a table to the list.
	 * @param table
	 */
	public void addTable(EntityHeader table);
	
	/**
	 * Show/hide the add table button.
	 * @param enabled
	 */
	public void setAddTableVisible(boolean enabled);
	
	/**
	 * Show or hide the upload tables button.
	 * @param enabled
	 */
	public void setUploadTableVisible(boolean enabled);
	
	
}
