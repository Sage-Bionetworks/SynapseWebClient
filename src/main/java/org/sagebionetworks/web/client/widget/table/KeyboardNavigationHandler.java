package org.sagebionetworks.web.client.widget.table;

import com.google.gwt.user.client.ui.IsWidget;

/**
 * Abstraction for handler that listens to navigation key events of table editors. When a navigation
 * event occurs, focus will be passed to the appropriate editor.
 * 
 * @author jhill
 *
 */
public interface KeyboardNavigationHandler {

	/**
	 * Bind a new row to this handler. Rows should be bound in the order they appear in the table.
	 * 
	 * @param row
	 */
	public void bindRow(RowOfWidgets row);

	/**
	 * Remove a row from this handler.
	 * 
	 * @param row
	 */
	public void removeRow(RowOfWidgets row);

	/**
	 * Remove all of the rows.s
	 */
	public void removeAllRows();

	/**
	 * A horizontal row of widgets in a table.
	 */
	public interface RowOfWidgets {

		/**
		 * Get the widget at the given index.
		 * 
		 * @param index
		 * @return
		 */
		public IsWidget getWidget(int index);

		/**
		 * The number of widgets in this row.
		 * 
		 * @return
		 */
		public int getWidgetCount();
	}

}
