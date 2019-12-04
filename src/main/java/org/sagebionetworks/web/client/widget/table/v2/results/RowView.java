package org.sagebionetworks.web.client.widget.table.v2.results;

import org.sagebionetworks.web.client.widget.table.v2.results.cell.Cell;
import com.google.gwt.user.client.ui.IsWidget;

/**
 * Abstraction for a table row.
 * 
 * @author John
 * 
 */
public interface RowView extends IsWidget {

	/**
	 * Business logic for this view.
	 */
	public interface Presenter {
		/**
		 * Called when the row's selection changes.
		 */
		public void onSelectionChanged();

	}

	/**
	 * Is this row selected.
	 * 
	 * @return
	 */
	public boolean isSelected();

	/**
	 * Is this row selected?
	 */
	public void setSelected(boolean isSelected);

	/**
	 * Bind this view to its presenter.
	 * 
	 * @param presenter
	 */
	public void setPresenter(Presenter presenter);

	/**
	 * Add a cell to this row.
	 * 
	 * @param cell
	 */
	public void addCell(Cell cell);

	/**
	 * Show or hide the selection.
	 * 
	 * @param b
	 */
	public void setSelectVisible(boolean visible);

}
