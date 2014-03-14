package org.sagebionetworks.web.client.widget.table;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.sagebionetworks.repo.model.table.ColumnModel;
import org.sagebionetworks.repo.model.table.ColumnType;
import org.sagebionetworks.repo.model.table.Row;
import org.sagebionetworks.repo.model.table.RowSet;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.SageImageBundle;
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
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
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
	SimplePanel tableLoading;
	@UiField
	SimplePanel pagerContainer;

	SageImageBundle sageImageBundle;
	CellTable<TableModel> cellTable;
	SimplePager pager;
	private List<ColumnModel> columns;
	ListHandler<TableModel> sortHandler;
	SelectionModel<TableModel> selectionModel;
	ContactDatabase database;
	Presenter presenter;
	Map<Column,ColumnModel> columnToModel;
	AsyncDataProvider<TableModel> dataProvider;
	RowSet initialLoad = null;
	QueryDetails initialDetails = null;		
	
	@Inject
	public SimpleTableWidgetViewImpl(final Binder uiBinder, SageImageBundle sageImageBundle) {
		columnToModel = new HashMap<Column, ColumnModel>();
		initWidget(uiBinder.createAndBindUi(this));

		this.sageImageBundle = sageImageBundle;
		tableLoading.add(getLoadingWidget());
		
		tableContainer.addStyleName("tableMinWidth");
		
	    // setup DataProvider for pagination/sorting
		dataProvider = new AsyncDataProvider<TableModel>() {
			@Override
			protected void onRangeChanged(HasData<TableModel> display) {
				// skip query if table was just built
				if (initialLoad != null) {
					updateData(new Range(initialDetails.getOffset().intValue(), initialDetails.getLimit().intValue()), initialLoad);
					initialLoad = null;
					initialDetails = null;
					return;
				}

				// extract sorted column
				String sortedColumnId = null;
				QueryDetails.SortDirection sortDirection = null;
				ColumnSortList sortList = cellTable.getColumnSortList();
				if (sortList.size() > 0 && sortList.get(0).getColumn() != null) {
					ColumnSortInfo columnSortInfo = sortList.get(0);
					ColumnModel model = columnToModel.get(columnSortInfo
							.getColumn());
					if (model != null) {
						sortedColumnId = model.getId();
						sortDirection = columnSortInfo.isAscending() ? SortDirection.ASC : SortDirection.DESC;
					}
				}

				final Range range = display.getVisibleRange();
				Long offset = new Long(range.getStart());
				Long limit = new Long(range.getLength());

				presenter.alterCurrentQuery(new QueryDetails(offset, limit, sortedColumnId, sortDirection), new AsyncCallback<RowSet>() {
					@Override
					public void onSuccess(RowSet rowData) {
						updateData(range, rowData);
					}
	
					@Override
					public void onFailure(Throwable caught) {
						showErrorMessage(DisplayConstants.ERROR_LOADING_QUERY_PLEASE_RETRY);
					}
				});
			}
	      
			private void updateData(final Range range, RowSet rowData) {
				// convert to Table Model (map)
				if(rowData != null && rowData.getHeaders() != null && rowData.getHeaders().size() > 0) {
			        List<TableModel> returnedData = new ArrayList<TableModel>();
			        for(Row row : rowData.getRows()) {
			        	TableModel model = new TableModel();
			        	for(int i=0; i<rowData.getHeaders().size(); i++) {				        		
			        		model.put(rowData.getHeaders().get(i), row.getValues().get(i));
			        	}
			        	returnedData.add(model);
			        }
			        updateRowData(range.getStart(), returnedData);
			        hideLoading();
				}
			}

	    };

	}
	
	@Override
	public void createNewTable(List<ColumnModel> columns, RowSet rowset, int totalRowCount, boolean canEdit, String queryString, QueryDetails queryDetails) {		
		this.columns = columns;				
		this.initialLoad = rowset;
		this.initialDetails = queryDetails;
		columnToModel.clear();
				
		setupQueryBox(queryString);			
		queryPanel.setVisible(true);		
		buildTable(queryDetails.getLimit().intValue());
		buildColumns(columns, canEdit);
		
	    cellTable.setRowCount(totalRowCount, true);
	    if(queryDetails.getOffset() != null && queryDetails.getLimit() != null) {	    	
	    	cellTable.setVisibleRange(queryDetails.getOffset().intValue(), queryDetails.getOffset().intValue() + queryDetails.getLimit().intValue());
	    } else {
	    	cellTable.setVisibleRange(0, totalRowCount);
	    }	    
	    

	    // Connect the list to the data provider.
	    dataProvider.addDataDisplay(cellTable);
 
	    // Add a ColumnSortEvent.AsyncHandler to connect sorting to the
	    // AsyncDataPRrovider.
	    AsyncHandler columnSortHandler = new AsyncHandler(cellTable);
	    cellTable.addColumnSortHandler(columnSortHandler);
	    
	    hideLoading();
	}
	
	@Override
	public void updateData(RowSet rowset, QueryDetails queryDetails) {
		// TODO Auto-generated method stub
		
	}

	
	@Override
	public void setQuery(String query) {
		queryField.setValue(query);
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
		queryField.setEnabled(false);
		tableContainer.setVisible(false);
		pagerContainer.setVisible(false);
		tableLoading.setVisible(true);
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
	private void hideLoading() {
		tableLoading.setVisible(false);
		tableContainer.setVisible(true);
		pagerContainer.setVisible(true);
		queryField.setEnabled(true);
	}
	
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
	
	private void buildTable(int pageSize) {
		cellTable = new CellTable<TableModel>(TableModel.KEY_PROVIDER);
		cellTable.setWidth("100%", true);
		cellTable.setPageSize(pageSize);
		cellTable.addStyleName("cellTable");
		cellTable.setLoadingIndicator(getLoadingWidget());
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

	private HTML getLoadingWidget() {
//		HTML widget =  new HTML(DisplayUtils.getLoadingHtml(sageImageBundle, DisplayConstants.EXECUTING_QUERY));
		HTML widget = new HTML(SafeHtmlUtils.fromSafeConstant(DisplayUtils.getIconHtml(sageImageBundle.loading31()) + " " + DisplayConstants.EXECUTING_QUERY + "..."));
		widget.addStyleName("margin-top-15 center");
		return widget;
	}
	
}
