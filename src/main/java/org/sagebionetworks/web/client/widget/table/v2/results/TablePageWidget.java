package org.sagebionetworks.web.client.widget.table.v2.results;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.gwtbootstrap3.client.ui.constants.IconType;
import org.sagebionetworks.repo.model.table.ColumnModel;
import org.sagebionetworks.repo.model.table.Query;
import org.sagebionetworks.repo.model.table.QueryResultBundle;
import org.sagebionetworks.repo.model.table.Row;
import org.sagebionetworks.repo.model.table.SortDirection;
import org.sagebionetworks.repo.model.table.SortItem;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.widget.pagination.DetailedPaginationWidget;
import org.sagebionetworks.web.client.widget.pagination.PagingAndSortingListener;
import org.sagebionetworks.web.client.widget.table.KeyboardNavigationHandler;
import org.sagebionetworks.web.client.widget.table.v2.results.SortableTableHeader.HeaderClickHandler;
import org.sagebionetworks.web.client.widget.table.v2.schema.ColumnModelUtils;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

/**
 * A widget for displaying a single page of a query result.
 * 
 * @author John
 *
 */
public class TablePageWidget implements TablePageView.Presenter, IsWidget, RowSelectionListener {

	TablePageView view;
	PortalGinInjector ginInjector;
	List<ColumnModel> types;
	RowSelectionListener rowSelectionListener;
	DetailedPaginationWidget paginationWidget;
	List<RowWidget> rows;
	KeyboardNavigationHandler keyboardNavigationHandler;
	/*
	 * This flag is used to ignore selection event while this widget is causing selection changes.
	 */
	boolean isSelectionChanging;
	
	@Inject
	public TablePageWidget(TablePageView view, PortalGinInjector ginInjector, DetailedPaginationWidget paginationWidget){
		this.ginInjector = ginInjector;
		this.paginationWidget = paginationWidget;
		this.view = view;
		this.view.setPaginationWidget(paginationWidget);
	}
	
	/**
	 * Configure this page with query results.
	 * @param bundle The query results.
	 * @param query The query used to generate this page.
	 * @param isEditable Is this page editable.
	 * @param rowSelectionListener If null then selection will be disabled.
	 * @param pageChangeListener If null then pagination will be disabled.
	 */
	public void configure(QueryResultBundle bundle, Query query, boolean isEditable, RowSelectionListener rowSelectionListener, final PagingAndSortingListener pageChangeListener){
		this.rowSelectionListener = rowSelectionListener;
		// The pagination widget is only visible if a listener was provider
		if(pageChangeListener != null){
			this.paginationWidget.configure(query.getLimit(), query.getOffset(), bundle.getQueryCount(), pageChangeListener);
			view.setPaginationWidgetVisible(true);
		}else{
			view.setPaginationWidgetVisible(false);
		}
		// Map the columns to types
		types = ColumnModelUtils.buildTypesForQueryResults(bundle.getQueryResult().getQueryResults().getHeaders(), bundle.getSelectColumns());
		// setup the headers from the types
		List<IsWidget> headers = new ArrayList<IsWidget>();
		for (ColumnModel type: types) {
			// Create each header
			String headerName = type.getName();
			SortableTableHeader sth = ginInjector.createSortableTableHeader();
			HeaderClickHandler headerClickHandler = createHeaderClickHandler(
					pageChangeListener, headerName);
			sth.configure(type.getName(), headerClickHandler);
			headers.add(sth);
			SortDirection direction = getSortDirection(query, headerName);
			if(direction != null){
				if(SortDirection.ASC.equals(direction)){
					sth.setIcon(IconType.ANGLE_DOWN);
				}else{
					sth.setIcon(IconType.ANGLE_UP);
				}
			}
		}
		
		// Create a navigation handler
		if(isEditable){
			// We only need key press navigation for editors.
			keyboardNavigationHandler = ginInjector.createKeyboardNavigationHandler();
		}else{
			keyboardNavigationHandler = null;
		}

		view.setTableHeaders(headers);
		rows = new ArrayList<RowWidget>(bundle.getQueryResult().getQueryResults().getRows().size());
		// Build the rows for this table
		for(Row row: bundle.getQueryResult().getQueryResults().getRows()){
			// Create the row 
			addRow(row, isEditable);
		}
	}
	
	private SortDirection getSortDirection(Query query, String header){
		if(query != null){
			if(query.getSort() != null){
				for(SortItem item: query.getSort()){
					if(item.getColumn().equals(header)){
						return item.getDirection();
					}
				}
			}
		}
		return null;
	}

	/**
	 * Create HeaderClickHandler for a given header.
	 * @param pageChangeListener
	 * @param header
	 * @return
	 */
	private HeaderClickHandler createHeaderClickHandler(final PagingAndSortingListener pageChangeListener,
			final String header) {
		// Null if we were not passed a PagingAndSortingListener.
		HeaderClickHandler headerClickHandler = null;
		if(pageChangeListener != null){
			headerClickHandler = new HeaderClickHandler(){
				@Override
				public void onHeaderClicked() {
					pageChangeListener.onToggleSort(header);
				}};
		}
		return headerClickHandler;
	}

	/**
	 * @param types
	 * @param isSelectable
	 * @param row
	 */
	private void addRow(Row row, boolean isEditor) {
		// Create a new row and configure it with the data.
		RowWidget rowWidget = ginInjector.createRowWidget();
		// We only listen to selection changes on the row if one was provided.
		RowSelectionListener listner = null;
		if(rowSelectionListener != null){
			listner = this;
		}
		rowWidget.configure(types, isEditor, row, listner);
		rows.add(rowWidget);
		view.addRow(rowWidget);
		if(keyboardNavigationHandler != null){
			this.keyboardNavigationHandler.bindRow(rowWidget);
		}
	}

	@Override
	public Widget asWidget() {
		return view.asWidget();
	}

	/**
	 * Add a new row to the table.
	 */
	public void onAddNewRow() {
		addRow(new Row(), true);
	}

	/**
	 * Toggle selection.
	 */
	public void onToggleSelect() {
		if(isOneRowOrMoreRowsSelected()){
			onSelectNone();
		}else{
			onSelectAll();
		}
	}

	/**
	 * Delete the selected rows
	 */
	public void onDeleteSelected() {
		Iterator<RowWidget> it = this.rows.iterator();
		while(it.hasNext()){
			RowWidget row = it.next();
			if(row.isSelected()){
				view.removeRow(row);
				it.remove();
				if(this.keyboardNavigationHandler != null){
					this.keyboardNavigationHandler.removeRow(row);
				}
			}
		}
		onSelectionChanged();
	}

	/**
	 * Select no rows.
	 */
	public void onSelectNone() {
		setAllSelect(false);
	}

	/**
	 * Select all rows.
	 */
	public void onSelectAll() {
		setAllSelect(true);
	}
	
	/**
	 * Change all sections.
	 * @param isSelected
	 */
	private void setAllSelect(boolean isSelected){
		try{
			this.isSelectionChanging = true;
			for(RowWidget row: rows){
				row.setSelected(isSelected);
			}
		}finally{
			this.isSelectionChanging = false;
		}
		onSelectionChanged();
	}
	
	/**
	 * Returns true if one or more rows are selected. False if no rows are selected.
	 * @return
	 */
	public boolean isOneRowOrMoreRowsSelected(){
		for(RowWidget row: rows){
			if(row.isSelected()){
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Extract a copy of the rows in this widget according to the current state.
	 * 
	 * @return
	 */
	public List<Row> extractRowSet(){
		List<Row> copy = new ArrayList<Row>(rows.size());
		for(RowWidget rowWidget: rows){
			Row row = rowWidget.getRow();
			copy.add(row);
		}
		return copy;
	}
	
	/**
	 * Extract the list of headers (ColumnModel ids), from this page.
	 * Note: Values can be null for columns that are aggregate functions.
	 * @return
	 */
	public List<String> extractHeaders(){
		List<String> headers = new ArrayList<String>(types.size());
		for(ColumnModel cm: types){
			headers.add(cm.getId());
		}
		return headers;
	}
	
	/**
	 * Called when a row changes its selection.
	 */
	public void onSelectionChanged(){
		// Only send out the message if selection is not in the process of changing.
		if(!this.isSelectionChanging && this.rowSelectionListener != null){
			this.rowSelectionListener.onSelectionChanged();
		}
	}
}
