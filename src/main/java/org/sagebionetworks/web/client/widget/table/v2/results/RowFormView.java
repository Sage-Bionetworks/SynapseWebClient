package org.sagebionetworks.web.client.widget.table.v2.results;

import org.sagebionetworks.web.client.widget.table.v2.results.cell.Cell;
import com.google.gwt.user.client.ui.IsWidget;

/**
 * Abstraction for a table row.
 * 
 * @author Jay
 * 
 */
public interface RowFormView extends IsWidget {

	/**
	 * Business logic for this view.
	 */
	public interface Presenter {
	}

	/**
	 * Bind this view to its presenter.
	 * 
	 * @param presenter
	 */
	void setPresenter(Presenter presenter);

	/**
	 * Add a cell to the form.
	 * 
	 * @param cell
	 */
	void addCell(String labelText, Cell cell);

	void clear();
}
