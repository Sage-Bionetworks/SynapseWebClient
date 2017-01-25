package org.sagebionetworks.web.client.widget.table.v2.results;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.gwtbootstrap3.client.ui.constants.IconType;
import org.sagebionetworks.repo.model.table.ColumnModel;
import org.sagebionetworks.repo.model.table.FacetColumnRequest;
import org.sagebionetworks.repo.model.table.FacetColumnResult;
import org.sagebionetworks.repo.model.table.Query;
import org.sagebionetworks.repo.model.table.QueryResultBundle;
import org.sagebionetworks.repo.model.table.Row;
import org.sagebionetworks.repo.model.table.SortDirection;
import org.sagebionetworks.repo.model.table.SortItem;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.cache.ClientCache;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.pagination.DetailedPaginationWidget;
import org.sagebionetworks.web.client.widget.table.KeyboardNavigationHandler;
import org.sagebionetworks.web.client.widget.table.v2.results.facets.FacetsWidget;
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
	String tableId;
	boolean isView;
	FacetsWidget facetsWidget;
	ClientCache clientCache;
	/*
	 * This flag is used to ignore selection event while this widget is causing selection changes.
	 */
	boolean isSelectionChanging;
	
	@Inject
	public TablePageWidget(TablePageView view, 
			PortalGinInjector ginInjector, 
			DetailedPaginationWidget paginationWidget, 
			FacetsWidget facetsWidget,
			ClientCache clientCache){
		this.ginInjector = ginInjector;
		this.paginationWidget = paginationWidget;
		this.view = view;
		this.view.setPaginationWidget(paginationWidget);
		this.facetsWidget = facetsWidget;
		this.clientCache = clientCache;
		view.setFacetsWidget(facetsWidget.asWidget());
		view.setPresenter(this);
	}
	
	/**
	 * Configure this page with query results.
	 * @param bundle The query results.
	 * @param query The query used to generate this page.
	 * @param isEditable Is this page editable.
	 * @param rowSelectionListener If null then selection will be disabled.
	 * @param pageChangeListener If null then pagination will be disabled.
	 */
	public void configure(QueryResultBundle bundle, 
			Query query, 
			List<SortItem> sortList, 
			boolean isEditable, 
			boolean isView, 
			RowSelectionListener rowSelectionListener, 
			final PagingAndSortingListener pageChangeListener,
			CallbackP<FacetColumnRequest> facetChangedHandler){
		this.isView = isView;
		this.rowSelectionListener = rowSelectionListener;
		// The pagination widget is only visible if a listener was provider
		if(pageChangeListener != null){
			this.paginationWidget.configure(query.getLimit(), query.getOffset(), bundle.getQueryCount(), pageChangeListener);
			view.setPaginationWidgetVisible(true);
		}else {
			view.setPaginationWidgetVisible(false);
		}
		view.setEditorBufferVisible(isEditable);
		tableId = QueryBundleUtils.getTableId(bundle);
		// Map the columns to types
		types = ColumnModelUtils.buildTypesForQueryResults(QueryBundleUtils.getSelectFromBundle(bundle), bundle.getColumnModels());
		// setup the headers from the types
		List<IsWidget> headers = new ArrayList<IsWidget>();
		Map<String, SortItem> sortedHeaders = new HashMap<String, SortItem>();
		if (sortList != null) {
			for (SortItem sort : sortList) {
				sortedHeaders.put(sort.getColumn(), sort);
			}	
		}
		for (ColumnModel type: types) {
			// Create each header
			String headerName = type.getName();
			if(!isEditable){
				// For sorting we need click handler and to set sort direction when needed.
				SortableTableHeader sth = ginInjector.createSortableTableHeader();
				sth.configure(type.getName(), pageChangeListener);
				headers.add(sth);
				if(sortedHeaders.containsKey(headerName)) {
					SortItem sortItem = sortedHeaders.get(headerName);
					if(SortDirection.DESC.equals(sortItem.getDirection())){
						sth.setIcon(IconType.SORT_DESC);
					}else{
						sth.setIcon(IconType.SORT_ASC);
					}
				}
			}else{
				// For the static case we just set the header name.
				StaticTableHeader sth = ginInjector.createStaticTableHeader();
				sth.setHeader(headerName);
				headers.add(sth);
			}
		}
		
		// Create a navigation handler
		if(isEditable){
			// We only need key press navigation for editors.
			keyboardNavigationHandler = ginInjector.createKeyboardNavigationHandler();
		}else{
			keyboardNavigationHandler = null;
		}
		List<FacetColumnResult> facets = bundle.getFacets();
		
		boolean isFacetsSupported = !isEditable && 
				facetChangedHandler != null && 
				facets != null && 
				!facets.isEmpty();
		
		if (isFacetsSupported) {
			facetsWidget.configure(facets, facetChangedHandler, types);
		} else {
			view.setFacetsVisible(false);	
		}
		view.setTableHeaders(headers);
		rows = new ArrayList<RowWidget>(bundle.getQueryResult().getQueryResults().getRows().size());
		// Build the rows for this table
		for(Row row: bundle.getQueryResult().getQueryResults().getRows()){
			// Create the row 
			addRow(row, isEditable);
		}
		String isRecentlyModifiedView = clientCache.get(tableId + QueryResultEditorWidget.VIEW_RECENTLY_CHANGED_KEY);
		view.setViewRecentlyModifiedAlertvisible(isRecentlyModifiedView != null);
	}
	
	@Override
	public void viewRecentlyModifiedAlertDismissed() {
		clientCache.remove(tableId + QueryResultEditorWidget.VIEW_RECENTLY_CHANGED_KEY);
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
		rowWidget.configure(tableId, types, isEditor, isView, row, listner);
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
	 * Headers for this page.  If a ColumnModle has an ID then it is a real column.  If the ID is null then it is a derived column.
	 * @return
	 */
	public List<ColumnModel> extractHeaders(){
		return types;
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

	/**
	 * Is this page valid?
	 * @return
	 */
	public boolean isValid() {
		boolean isValid = true;
		for(RowWidget row: rows){
			if(!row.isValid()){
				isValid = false;
			}
		}
		return isValid;
	}
	
	public void setFacetsVisible(boolean visible) {
		view.setFacetsVisible(visible);
	}
}
