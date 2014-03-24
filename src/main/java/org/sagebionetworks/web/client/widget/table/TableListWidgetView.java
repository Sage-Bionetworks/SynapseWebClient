package org.sagebionetworks.web.client.widget.table;

import java.util.List;

import org.sagebionetworks.repo.model.EntityHeader;
import org.sagebionetworks.repo.model.table.TableEntity;
import org.sagebionetworks.web.client.SynapseView;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.IsWidget;

public interface TableListWidgetView extends IsWidget, SynapseView {

	/**
	 * Set the presenter.
	 * @param presenter
	 */
	public void setPresenter(Presenter presenter);

	/**
	 * Presenter interface
	 */
	public interface Presenter {

		void createTableEntity(String name);

		void getTableDetails(EntityHeader table, AsyncCallback<TableEntity> asyncCallback);
				
	}

	public void configure(List<EntityHeader> tables, boolean canEdit, boolean showAddTable);

	public void showLoadingError();

	public void addTable(EntityHeader table);
	
	
}
