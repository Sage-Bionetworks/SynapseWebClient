package org.sagebionetworks.web.client.widget.table.v2.schema;

/**
 * Editor add more business logic to the column model process.
 * 
 * @author John
 *
 */
public interface ColumnModelTableRowEditor extends ColumnModelTableRow {

	/**
	 * Control for this view.
	 * @author John
	 *
	 */
	public interface TypePresenter{
		
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
	 * @param visible
	 */
	public void setSizeFieldVisible(boolean visible);
}
