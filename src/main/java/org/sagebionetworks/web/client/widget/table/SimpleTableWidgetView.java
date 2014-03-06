package org.sagebionetworks.web.client.widget.table;

import java.util.List;

import org.sagebionetworks.repo.model.table.ColumnModel;
import org.sagebionetworks.repo.model.table.RowSet;
import org.sagebionetworks.web.client.SynapseView;
import org.sagebionetworks.web.shared.table.QueryDetails;

import com.google.gwt.user.client.ui.IsWidget;

public interface SimpleTableWidgetView extends IsWidget, SynapseView {

	/**
	 * Set the presenter.
	 * @param presenter
	 */
	public void setPresenter(Presenter presenter);

	/**
	 * Presenter interface
	 */
	public interface Presenter {
				
	}
	
	/**
	 * configure view
	 * @param table
	 * @param columns
	 * @param rowset 
	 * @param canEdit
	 * @param limit 
	 * @param offset 
	 */
	public void configure(List<ColumnModel> columns, RowSet rowset, boolean canEdit, QueryDetails queryDetails);
	
}
