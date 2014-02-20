package org.sagebionetworks.web.client.widget.table;

import java.util.List;

import org.sagebionetworks.repo.model.table.ColumnModel;
import org.sagebionetworks.repo.model.table.TableEntity;
import org.sagebionetworks.web.client.SynapseView;

import com.google.gwt.user.client.ui.IsWidget;

public interface CompleteTableWidgetView extends IsWidget, SynapseView {

	/**
	 * Set the presenter.
	 * @param presenter
	 */
	public void setPresenter(Presenter presenter);

	/**
	 * Presenter interface
	 */
	public interface Presenter {

		void query(String query);

		void createColumn(org.sagebionetworks.repo.model.table.ColumnModel col);

		void updateColumnOrder(List<String> columnIds);
				
	}

	public void configure(TableEntity table, List<ColumnModel> columns, String queryString, boolean canEdit);
	
	
}
