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
public class TablePageWidget implements TablePageView.Presenter, IsWidget {

	TablePageView view;
	PortalGinInjector ginInjector;
	
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
	public void configure(QueryResultBundle bundle, boolean isEditable){
		// Transform the query result into view data
		List<String> headers = new ArrayList<String>();
		for (String header : bundle.getQueryResults().getHeaders()) {
			headers.add(header);
		}
		view.setTableHeaders(headers);
		Map<String, ColumnModel> idToModel = ColumnModelUtils.buildMapColumnIdtoModel(bundle.getSelectColumns());
		// Map the columns to types
		List<ColumnTypeViewEnum> types = ColumnModelUtils.buildTypesForQueryResults(bundle.getQueryResults().getHeaders(), idToModel);
		// if the page is editable then the rows can be selected.
		boolean isSelectable = isEditable;
		// Build the rows for this table
		for(Row row: bundle.getQueryResults().getRows()){
			// Create the row 
			RowView rowView = ginInjector.createNewRowView();
			rowView.initializeRow(types);
			// fill out the row.
			rowView.setRowData(row.getRowId(), row.getVersionNumber(), row.getValues(), isSelectable);
			view.addRow(rowView);
		}
	}

	@Override
	public Widget asWidget() {
		return view.asWidget();
	}
	
}
