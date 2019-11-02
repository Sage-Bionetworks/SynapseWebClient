package org.sagebionetworks.web.client.widget.table.v2.schema;

import com.google.gwt.user.client.ui.IsWidget;

/**
 * Abstraction for a
 * <tr>
 * representation of a ColumnModel
 * 
 * @author John
 *
 */
public interface ColumnModelTableRow extends ColumnModelView, IsWidget {

	/**
	 * Control for this view.
	 * 
	 * @author John
	 *
	 */
	public interface SelectionPresenter {

		/**
		 * Called when selection changes.
		 */
		public void selectionChanged(boolean isSelected);
	}


	/**
	 * Set the selection selection state of this row
	 * 
	 * @param select
	 */
	public void setSelected(boolean select);

	/**
	 * Is this row selected?
	 */
	public boolean isSelected();

	/**
	 * Delete this row.
	 */
	public void delete();

	/**
	 * Set the selection presenter for this view.
	 * 
	 * @param selectionPresenter
	 */
	public void setSelectionPresenter(SelectionPresenter selectionPresenter);

	/**
	 * Show/hide the select check box.
	 * 
	 * @param visible
	 */
	public void setSelectVisible(boolean visible);

}
