package org.sagebionetworks.web.client.widget.table.v2;

import org.sagebionetworks.repo.model.table.ColumnModel;

import com.google.gwt.user.client.ui.IsWidget;

public interface ColumnModelForm extends IsWidget {
	
	/**
	 * Setup the view with a column model.
	 * 
	 * @param columnModel
	 */
	public void setColumnModel(ColumnModel columnModel);
	
	/**
	 * Get the current ColumnModel for the view.
	 * 
	 * @return
	 */
	public ColumnModel getColumnModel();

}
