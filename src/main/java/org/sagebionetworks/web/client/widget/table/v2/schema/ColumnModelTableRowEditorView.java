package org.sagebionetworks.web.client.widget.table.v2.schema;

import org.sagebionetworks.web.client.widget.table.KeyboardNavigationHandler;
import org.sagebionetworks.web.client.widget.table.v2.results.cell.CellEditor;

/**
 * Editor add more business logic to the column model process.
 * 
 * @author John
 *
 */
public interface ColumnModelTableRowEditorView extends ColumnModelTableRow, KeyboardNavigationHandler.RowOfWidgets {

	/**
	 * Control for this view.
	 * 
	 * @author John
	 *
	 */
	public interface TypePresenter {

		/**
		 * Called when the type changes.
		 */
		public void onTypeChanged();
	}

	/**
	 * Bind this view to its presenter.
	 * 
	 * @param presenter
	 */
	public void setTypePresenter(TypePresenter presenter);

	/**
	 * Set the size field to be visible
	 * 
	 * @param visible
	 */
	public void setSizeFieldVisible(boolean visible);

	/**
	 * Set the editor to be used for default values.
	 * 
	 * @param defaultEditor
	 */
	public void setDefaultEditor(CellEditor defaultEditor);


	/**
	 * Clear the editor to be used for default values.
	 * 
	 * @param defaultEditor
	 */
	public void setDefaultEditorVisible(boolean visible);

	/**
	 * Set an error for a name.
	 * 
	 * @param string
	 */
	public void setNameError(String string);

	/**
	 * Clear an error for a name.
	 */
	public void clearNameError();

	/**
	 * Set an error on the size of a column.
	 * 
	 * @param string
	 */
	public void setSizeError(String string);

	/**
	 * Clear a size error.
	 */
	public void clearSizeError();

	/**
	 * Validate the default editor.
	 * 
	 * @return
	 */
	public boolean validateDefault();

	/**
	 * Show/hid the restrict values editor
	 * 
	 * @param showRestrictValues
	 */
	public void setRestrictValuesVisible(boolean showRestrictValues);

	/**
	 * Show/hide the facets editor
	 * 
	 * @param showFacetTypes
	 */
	public void setFacetVisible(boolean showFacetTypes);

	public void setFacetValues(String... items);

	void setToBeDefaultFileViewColumn();

}
