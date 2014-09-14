package org.sagebionetworks.web.client.widget.table;

import java.util.List;

import org.sagebionetworks.repo.model.file.FileHandle;
import org.sagebionetworks.repo.model.table.ColumnModel;
import org.sagebionetworks.repo.model.table.RowReferenceSet;
import org.sagebionetworks.repo.model.table.RowSet;
import org.sagebionetworks.repo.model.table.TableStatus;
import org.sagebionetworks.web.client.SynapseView;
import org.sagebionetworks.web.client.events.EntityUpdatedHandler;
import org.sagebionetworks.web.client.model.EntityBundle;
import org.sagebionetworks.web.shared.table.QueryDetails;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.IsWidget;

public interface SimpleTableWidgetView extends IsWidget, SynapseView {

	/**
	 * Set the presenter.
	 * @param presenter
	 */
	public void setPresenter(Presenter presenter);

	public void setQuery(String query);
	
	/**
	 * Presenter interface
	 */
	public interface Presenter {

		void alterCurrentQuery(QueryDetails alterDetails, AsyncCallback<RowSet> asyncCallback);

		void query(String query);

		void rerunCurrentQuery();

		void updateRow(TableModel row, AsyncCallback<RowReferenceSet> callback);
		
		void addRow();
		
		void createColumn(org.sagebionetworks.repo.model.table.ColumnModel col, AsyncCallback<String> callback);

		void updateColumnOrder(List<String> columnIds);

		void deleteRows(List<TableModel> selectedRows);

		void viewRow(List<TableModel> selectedRows);

		void getFileHandle(String rowId, String versionNumber, String colId, AsyncCallback<FileHandle> callback);

	}
	
	/**
	 * configure view
	 * @param table
	 * @param columns
	 * @param rowset 
	 * @param canEdit
	 * @param isRowQuery 
	 * @param limit 
	 * @param offset 
	 */
	public void createNewTable(EntityBundle bundle, RowSet rowset, int totalRowCount, boolean canEdit, String queryString, QueryDetails queryDetails, EntityUpdatedHandler handler);
	
	public void showTableUnavailable(TableStatus status, Integer percentComplete);

	public void showQueryProblem(String message);

	public void insertNewRow(TableModel model);

	public void createRowView(List<ColumnModel> tableColumns, RowSet rowset);
}
