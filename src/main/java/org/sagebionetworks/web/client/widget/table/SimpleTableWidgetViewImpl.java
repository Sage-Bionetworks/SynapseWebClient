package org.sagebionetworks.web.client.widget.table;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.sagebionetworks.repo.model.table.ColumnModel;
import org.sagebionetworks.repo.model.table.ColumnType;
import org.sagebionetworks.repo.model.table.RowSet;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.shared.table.QueryDetails;
import org.sagebionetworks.web.shared.table.QueryDetails.SortDirection;

import com.google.gwt.cell.client.CheckboxCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.ColumnSortEvent.AsyncHandler;
import com.google.gwt.user.cellview.client.ColumnSortEvent.ListHandler;
import com.google.gwt.user.cellview.client.ColumnSortList;
import com.google.gwt.user.cellview.client.ColumnSortList.ColumnSortInfo;
import com.google.gwt.user.cellview.client.SimplePager;
import com.google.gwt.user.cellview.client.SimplePager.TextLocation;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.AsyncDataProvider;
import com.google.gwt.view.client.DefaultSelectionEventManager;
import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.MultiSelectionModel;
import com.google.gwt.view.client.Range;
import com.google.gwt.view.client.SelectionModel;
import com.google.inject.Inject;

public class SimpleTableWidgetViewImpl extends Composite implements SimpleTableWidgetView {
	public interface Binder extends UiBinder<Widget, SimpleTableWidgetViewImpl> {	}

	@UiField
	HTMLPanel queryPanel;
	@UiField
	TextBox queryField;
	@UiField
	SimplePanel queryButtonContainer;
	@UiField
	SimplePanel tableContainer;
	@UiField
	SimplePanel pagerContainer;

	CellTable<TableModel> cellTable;
	SimplePager pager;
	private List<ColumnModel> columns;
	ListHandler<TableModel> sortHandler;
	SelectionModel<TableModel> selectionModel;
	ContactDatabase database;
	Presenter presenter;
	Map<Column,ColumnModel> columnToModel;
	
	@Inject
	public SimpleTableWidgetViewImpl(final Binder uiBinder) {
		columnToModel = new HashMap<Column, ColumnModel>();
		initWidget(uiBinder.createAndBindUi(this));
		
		tableContainer.addStyleName("tableMinWidth");
	}
	
	@Override
	public void configure(List<ColumnModel> columns, RowSet rowset, int totalRowCount, boolean canEdit, String queryString, QueryDetails queryDetails) {		
		this.columns = columns;					
		columnToModel.clear();
				
		setupQueryBox(queryString);			
		queryPanel.setVisible(true);		
		buildTable();
		buildColumns(columns, canEdit);
		
	    cellTable.setRowCount(totalRowCount, true); // TODO : do this asynchronously from the view?
	    if(queryDetails.getOffset() != null && queryDetails.getLimit() != null) {	    	
	    	cellTable.setVisibleRange(queryDetails.getOffset(), queryDetails.getOffset() + queryDetails.getLimit());
	    } else {
	    	cellTable.setVisibleRange(0, totalRowCount);
	    }	    
	    
	    // setup DataProvider for pagination/sorting
	    AsyncDataProvider<TableModel> dataProvider = new AsyncDataProvider<TableModel>() {
	      @Override
	      protected void onRangeChanged(HasData<TableModel> display) {
	        final Range range = display.getVisibleRange();

	        // extract sorted column
	        String sortedColumnId = null;
	        QueryDetails.SortDirection sortDirection = null;
	        ColumnSortList sortList = cellTable.getColumnSortList();
	        if(sortList.size() > 0 && sortList.get(0).getColumn() != null) {
	        	ColumnSortInfo columnSortInfo = sortList.get(0);
	        	ColumnModel model = columnToModel.get(columnSortInfo.getColumn());
	        	if(model != null) {
	        		sortedColumnId = model.getId();	        	
	        		sortDirection = columnSortInfo.isAscending() ? SortDirection.ASC : SortDirection.DESC;
	        	}
	        }
        	
	        int offset = range.getStart();
	        int limit = range.getLength();
	        
	        
	        // TODO: call presenter for new data range
	        presenter.alterCurrentQuery(new QueryDetails(offset, limit, sortedColumnId, sortDirection));
	        List<TableModel> returnedData = null;
	        cellTable.setRowData(range.getStart(), returnedData);
	      }
	    };

	    // Connect the list to the data provider.
	    dataProvider.addDataDisplay(cellTable);
 
	    // Add a ColumnSortEvent.AsyncHandler to connect sorting to the
	    // AsyncDataPRrovider.
	    AsyncHandler columnSortHandler = new AsyncHandler(cellTable);
	    cellTable.addColumnSortHandler(columnSortHandler);

	    // Add Row Data
	    
		
		// TODO : REMOVE THIS
		// Add the CellList to the adapter in the database.
//		database = ContactDatabase.get();
//		database.addDataDisplay(cellTable);
	}
	
	private void buildTable() {
		cellTable = new CellTable<TableModel>(TableModel.KEY_PROVIDER);
		cellTable.setWidth("100%", true);
		cellTable.setPageSize(15);
		cellTable.addStyleName("cellTable");
		tableContainer.setWidget(cellTable);

		// Do not refresh the headers and footers every time the data is
		// updated.
//		cellTable.setAutoHeaderRefreshDisabled(true);
//		cellTable.setAutoFooterRefreshDisabled(true);

		// Attach a column sort handler to the ListDataProvider to sort the
		// list.
		sortHandler = new ListHandler<TableModel>(ContactDatabase.get().getDataProvider().getList());
		cellTable.addColumnSortHandler(sortHandler);

		// Create a Pager to control the table.
		SimplePager.Resources pagerResources = GWT.create(SimplePager.Resources.class);
		pager = new SimplePager(TextLocation.CENTER, pagerResources, false, 0, true);
		pager.setDisplay(cellTable);
		pagerContainer.setWidget(pager);

		// Add a selection model so we can select cells.
		selectionModel = new MultiSelectionModel<TableModel>(TableModel.KEY_PROVIDER);
		cellTable.setSelectionModel(selectionModel, DefaultSelectionEventManager.<TableModel> createCheckboxManager());
		
	}

	private void buildColumns(List<ColumnModel> columns, boolean canEdit) {
		// checkbox selector
		Column<TableModel, Boolean> checkColumn = new Column<TableModel, Boolean>(
				new CheckboxCell(true, false)) {
			@Override
			public Boolean getValue(TableModel object) {
				// Get the value from the selection model.
				return selectionModel.isSelected(object);
			}		
		}; 
		cellTable.addColumn(checkColumn, SafeHtmlUtils.fromSafeConstant("<br/>"));
		cellTable.setColumnWidth(checkColumn, 40, Unit.PX);

		int nPercentageCols = columns.size();
		for(ColumnModel model : columns) {
			// TODO : uncomment and replace below when we have DATE column type
			if(model.getColumnType() == ColumnType.BOOLEAN) nPercentageCols--;	
//			if(model.getColumnType() == ColumnType.BOOLEAN || model.getColumnType() == ColumnType.DATE) nPercentageCols--;
		}		
		
		// Table's columns
		for(ColumnModel model : columns) {
			Column<TableModel, ?> column = ColumnUtils.getColumn(model, sortHandler, canEdit);
			cellTable.addColumn(column, model.getName());
			columnToModel.put(column, model);
			
			// TODO : just have a fixed width for each column and let table scroll horizontally?
			if(model.getColumnType() == ColumnType.BOOLEAN) {
				cellTable.setColumnWidth(column, 100, Unit.PX);
//			} else if(model.getColumnType() == ColumnType.DATE) {
//				cellTable.setColumnWidth(column, 140, Unit.PX);								
			} else {
				cellTable.setColumnWidth(column, (100/nPercentageCols), Unit.PCT);			
			}
		}
	}

	
	/*
	 * SynapseView methods
	 */
	@Override
	public void showInfo(String title, String message) {
		DisplayUtils.showInfo(title, message);
	}

	@Override
	public void showErrorMessage(String message) {
		DisplayUtils.showErrorMessage(message);
	}

	@Override
	public void showLoading() {
	}
	
	@Override
	public void clear() {
	}

	@Override
	public void setPresenter(Presenter presenter) {
		this.presenter = presenter;
	}

	
	/*
	 * Private Methods
	 */
	private void setupQueryBox(String queryString) {
		// setup query
		Button queryBtn = DisplayUtils.createButton(DisplayConstants.QUERY);
		queryBtn.addStyleName("btn-block");
		queryBtn.addClickHandler(new ClickHandler() {			
			@Override
			public void onClick(ClickEvent event) {
				presenter.query(queryField.getValue());
			}
		});
		queryButtonContainer.setWidget(queryBtn);
		queryField.setValue(queryString);
	}

	
}
