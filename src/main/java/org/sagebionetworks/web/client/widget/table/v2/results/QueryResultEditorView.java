package org.sagebionetworks.web.client.widget.table.v2.results;

import com.google.gwt.user.client.ui.IsWidget;

/**
 * An abstraction for a view that allows a single query result page to be edited.
 * 
 * @author John
 *
 */
public interface QueryResultEditorView extends IsWidget {
	
	/**
	 * All business logic for this widget goes here.
	 *
	 */
	public interface Presenter {

		/**
		 * Add a row to the table.
		 */
		void onAddRow();

		/**
		 * Toggle the selected rows.
		 */
		void onToggleSelect();

		/**
		 * Select all rows.
		 */
		void onSelectAll();

		/**
		 * Select no rows.
		 */
		void onSelectNone();

		/**
		 * Delete the selected rows.
		 */
		void onDeleteSelected();
		
	}
	
	/**
	 * Bind the presenter to the view.
	 * @param presenter
	 */
	public void setPresenter(Presenter presenter);

	/**
	 * This widget contains the table of data for the page.
	 * 
	 * @param pageWidget
	 */
	public void setTablePageWidget(TablePageWidget pageWidget);

}
