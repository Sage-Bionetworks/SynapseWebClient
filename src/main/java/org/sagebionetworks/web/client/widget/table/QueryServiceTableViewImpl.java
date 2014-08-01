package org.sagebionetworks.web.client.widget.table;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.ImagePrototypeSingleton;
import org.sagebionetworks.web.client.view.CellTableProvider;
import org.sagebionetworks.web.client.view.RowData;
import org.sagebionetworks.web.client.view.table.ColumnFactory;
import org.sagebionetworks.web.shared.HeaderData;

import com.extjs.gxt.ui.client.data.BaseModelData;
import com.extjs.gxt.ui.client.data.BasePagingLoader;
import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.data.PagingLoadResult;
import com.extjs.gxt.ui.client.store.ListStore;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.SimplePager;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.Range;
import com.google.gwt.view.client.RangeChangeEvent;
import com.google.gwt.view.client.RangeChangeEvent.Handler;
import com.google.inject.Inject;

public class QueryServiceTableViewImpl extends Composite implements QueryServiceTableView {
 
//	@UiTemplate("QueryServiceTableViewImpl.ui.xml")
	public interface Binder extends UiBinder<Widget, QueryServiceTableViewImpl> {}

	@UiField
	SimplePanel tablePanel;
	
	@UiField
	SimplePanel pagerPanel;
	
	CellTable<Map<String, Object>> cellTable;
	SimplePager pager;
	
	ImagePrototypeSingleton prototype;

	// How many columns are we currently rendering
	int columnCount = 0;

	// Any column that is sortable will have sortable header.
	private List<SortableHeader> sortableHeaders = new ArrayList<SortableHeader>();
	private Presenter presenter;
	private ColumnFactory columnFactory;
	private CellTableProvider tableProvider;
	private boolean usePager = false;
	
	/**
	 * Gin will inject all of the params.
	 * 
	 * @param cellTableResource
	 */
	@Inject
	public QueryServiceTableViewImpl(final Binder uiBinder,ImagePrototypeSingleton prototype,ColumnFactory columnFactory, CellTableProvider provider) {
		// Use the xml script to load the rest of the view.
		initWidget(uiBinder.createAndBindUi(this));
		this.prototype = prototype;
		this.columnFactory = columnFactory;
		this.tableProvider = provider;
		removeAllColumns();
	}
	
	/**
	 * Public for testing
	 * @param display
	 * @param key
	 * @return
	 */
	public SortableHeader createHeader(String display, String key){
		final SortableHeader header = new SortableHeader(display, prototype, key);
		sortableHeaders.add(header);
		header.setUpdater(new ValueUpdater<String>() {
			@Override
			public void update(String value) {
				presenter.toggleSort(header.getColumnKey());
			}
		});
		return header;
	}

	/**
	 * Remove all columns from the table.
	 */
	private void removeAllColumns() {
		if(cellTable != null){
			cellTable.removeFromParent();
		}
		if(this.pager != null){
			this.pager.removeFromParent();
		}
		// Create the tables
		cellTable = tableProvider.createNewTable();
		tablePanel.add(cellTable);
		
		// Add all of the range handlers to the new table
		if(this.usePager){
			pager = tableProvider.createPager();
			pager.setDisplay(cellTable);
			this.pagerPanel.add(pager);			
		}

		// The pager will trigger these
		cellTable.addRangeChangeHandler(new Handler() {
			@Override
			public void onRangeChange(RangeChangeEvent event) {
				Range newRange = event.getNewRange();
				presenter.pageTo(newRange.getStart(), newRange.getLength());
			}
		});
		// Clear all header data
		sortableHeaders.clear();
	}

	@Override
	public void setPresenter(Presenter presenter) {
		this.presenter = presenter;
	}

	@Override
	public void setRows(RowData data) {
		// update the table
		cellTable.setRowCount(data.getTotalCount(), true);
		// Push the data into the widget.
		cellTable.setRowData(data.getOffset(), data.getRows());
		cellTable.setPageSize(data.getLimit());
		cellTable.setPageStart(data.getOffset());
		// Update the sorting
		updateSortColumns(data.getSortKey(), data.isAscending());
	}
	
	public void updateSortColumns(String sortKey, boolean ascending){
		// Set the sorting state
		for(SortableHeader header: sortableHeaders){
			// If the sort key is null then turn off sorting
			if(sortKey != null && sortKey.equals(header.getColumnKey())){
				header.setSorting(true);
				header.setSortAscending(ascending);
			}else{
				header.setSorting(false);
			}
		}
		// Need to re-draw the headers
		cellTable.redrawHeaders();
	}

	@Override
	public void showMessage(String message) {
		DisplayUtils.showInfoDialog("", message, null);
	}

	@Override
	public void setColumns(List<HeaderData> list) {
		removeAllColumns();
		// Now add each column from
		for(int i=0; i<list.size(); i++){
			HeaderData meta = list.get(i);
			// Now create the column.
			Column<Map<String, Object>, ?> column = columnFactory.createColumn(meta);
			// Add the column to the table;
			// Is this column sortable
			String sortId = meta.getSortId();
			if(sortId != null){
				// the header is a sortable object
				SortableHeader header = createHeader(meta.getDisplayName(), sortId);
				cellTable.addColumn(column, header);
			}else{
				// The header is a string
				cellTable.addColumn(column, meta.getDisplayName());
			}
		}
		// Keep the column count
		columnCount = list.size();
	}
	
	
	public int getColumnCount(){
		return columnCount;
	}

	@Override
	public void usePager(boolean use) {
		this.usePager = use;
	}
	
	/*
	 *  Methods of the view interface that are not designed for this impl
	 */
	@Override
	public void setPaginationOffsetAndLength(int offset, int length) {
		// not applicable to this View Impl	
	}

	@Override
	public void setStoreAndLoader(ListStore<BaseModelData> store, BasePagingLoader<PagingLoadResult<ModelData>> loader) {
		// not applicable to this View Impl	
	}

	@Override
	public void setDimensions(int width, int height) {
		// not applicable to this View Impl	
	}

	@Override
	public void showLoading() {
	}

	@Override
	public void showInfo(String title, String message) {
		DisplayUtils.showInfo(title, message);
	}

	@Override
	public void showErrorMessage(String message) {
		DisplayUtils.showErrorMessage(message);
	}

	@Override
	public void clear() {
	}
}
