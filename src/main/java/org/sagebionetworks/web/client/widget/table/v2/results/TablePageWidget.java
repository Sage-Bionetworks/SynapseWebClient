package org.sagebionetworks.web.client.widget.table.v2.results;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.sagebionetworks.repo.model.table.ColumnModel;
import org.sagebionetworks.repo.model.table.QueryResultBundle;
import org.sagebionetworks.repo.model.table.Row;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.widget.table.v2.schema.ColumnModelUtils;
import org.sagebionetworks.web.client.widget.table.v2.schema.ColumnTypeViewEnum;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

/**
 * A widget for displaying a single page or a query result.
 * 
 * @author John
 *
 */
public class TablePageWidget implements TablePageView.Presenter, RowView.Presenter, IsWidget {

	TablePageView view;
	PortalGinInjector ginInjector;
	List<ColumnTypeViewEnum> types;
	RowSelectionListener rowSelectionListener;
	
	@Inject
	public TablePageWidget(TablePageView view, PortalGinInjector ginInjector){
		this.ginInjector = ginInjector;
		this.view = view;
	}
	
	/**
	 * Configure this page with query results.
	 * 
	 * @param bundle
	 * @param isEditable
	 */
	public void configure(QueryResultBundle bundle, boolean isEditable, RowSelectionListener rowSelectionListener){
		this.rowSelectionListener = rowSelectionListener;
		// Transform the query result into view data
		List<String> headers = new ArrayList<String>();
		for (String header : bundle.getQueryResults().getHeaders()) {
			headers.add(header);
		}
		view.setTableHeaders(headers);
		Map<String, ColumnModel> idToModel = ColumnModelUtils.buildMapColumnIdtoModel(bundle.getSelectColumns());
		// Map the columns to types
		types = ColumnModelUtils.buildTypesForQueryResults(bundle.getQueryResults().getHeaders(), idToModel);
		// if the page is editable then the rows can be selected.
		boolean isSelectable = isEditable;
		// Build the rows for this table
		for(Row row: bundle.getQueryResults().getRows()){
			// Create the row 
			addRow(types, isSelectable, row, isEditable);
		}
	}

	/**
	 * @param types
	 * @param isSelectable
	 * @param row
	 */
	public RowView addRow(List<ColumnTypeViewEnum> types, boolean isSelectable, Row row, boolean isEditor) {
		RowView rowView = ginInjector.createNewRowView();
		rowView.initializeRow(types, isEditor);
		// fill out the row.
		rowView.setRowData(row.getRowId(), row.getVersionNumber(), row.getValues(), isSelectable);
		view.addRow(rowView);
		rowView.setPresenter(this);
		return rowView;
	}

	@Override
	public Widget asWidget() {
		return view.asWidget();
	}

	/**
	 * Add a new row to the table.
	 */
	public void addNewRow() {
		// Add a new row to the page.
		Row newRow = new Row();
		List<String> values = new ArrayList<String>(types.size());
		newRow.setValues(values);
		// Set empty strings for all values
		for(ColumnTypeViewEnum type: types){
			values.add("");
		}
		addRow(types, true, newRow, true);
	}

	@Override
	public void onSelectedChanged(RowView selected) {
		if(this.rowSelectionListener != null){
			this.rowSelectionListener.onSelectedChanged(selected);
		}
	}
	
}
