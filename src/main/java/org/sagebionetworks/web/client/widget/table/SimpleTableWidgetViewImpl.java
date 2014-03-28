package org.sagebionetworks.web.client.widget.table;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.sagebionetworks.repo.model.table.ColumnModel;
import org.sagebionetworks.repo.model.table.ColumnType;
import org.sagebionetworks.repo.model.table.Row;
import org.sagebionetworks.repo.model.table.RowReferenceSet;
import org.sagebionetworks.repo.model.table.RowSet;
import org.sagebionetworks.repo.model.table.TableState;
import org.sagebionetworks.repo.model.table.TableStatus;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.DisplayUtils.ButtonType;
import org.sagebionetworks.web.client.SageImageBundle;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.shared.table.QueryDetails;
import org.sagebionetworks.web.shared.table.QueryDetails.SortDirection;

import com.google.gwt.cell.client.CheckboxCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
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
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Label;
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
	private static final String HAS_ERROR_HAS_FEEDBACK = "has-error has-feedback";

	public interface Binder extends UiBinder<Widget, SimpleTableWidgetViewImpl> {	}
	
	@UiField
	HTMLPanel queryPanel;
	@UiField
	TextBox queryField;
	@UiField
	DivElement queryFieldContainer;
	@UiField
	Label queryFeedback;
	@UiField
	SimplePanel queryButtonContainer;
	@UiField
	SimplePanel tableContainer;
	@UiField
	SimplePanel tableLoading;
	@UiField
	SimplePanel pagerContainer;
	@UiField
	SimplePanel errorMessage;

	SageImageBundle sageImageBundle;
	CellTable<TableModel> cellTable;
	MySimplePager pager;
	private List<ColumnModel> columns;
	ListHandler<TableModel> sortHandler;
	SelectionModel<TableModel> selectionModel;
	ContactDatabase database;
	Presenter presenter;
	Map<Column,ColumnModel> columnToModel;
	AsyncDataProvider<TableModel> dataProvider;
	RowSet initialLoad = null;
	QueryDetails initialDetails = null;	
	int timerRemainingSec;
	TableModel newRow = null;
	SynapseJSNIUtils jsniUtils;
	
	List<TableModel> currentPage;
	
	@Inject
	public SimpleTableWidgetViewImpl(final Binder uiBinder, SageImageBundle sageImageBundle, SynapseJSNIUtils jsniUtils) {
		this.sageImageBundle = sageImageBundle;
		this.jsniUtils = jsniUtils;
		
		columnToModel = new HashMap<Column, ColumnModel>();
		initWidget(uiBinder.createAndBindUi(this));

		this.sageImageBundle = sageImageBundle;
		tableLoading.add(getLoadingWidget());
		
		tableContainer.addStyleName("tableMinWidth");
		
	    // setup DataProvider for pagination/sorting
		dataProvider = createAsyncDataProvider();

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
	public void setQuery(String query) {
		queryField.setValue(query);
	}

	@Override
	public void insertNewRow(TableModel model) {
		newRow = model;
		currentPage.add(0, model);
		cellTable.setRowData(currentPage); // causes onRangeChange event
	}

	@Override
	public void showInfo(String title, String message) {
		DisplayUtils.showInfo(title, message);
	}

	@Override
	public void showErrorMessage(String message) {
		hideLoading();
		DisplayUtils.showErrorMessage(message);
	}

	@Override
	public void showLoading() {
		queryField.setEnabled(false);
		tableContainer.setVisible(false);
		pagerContainer.setVisible(false);
		tableLoading.setVisible(true);
		errorMessage.setVisible(false);
		queryFeedback.setVisible(false);
	}
	
	@Override
	public void clear() {
	}

	@Override
	public void setPresenter(Presenter presenter) {
		this.presenter = presenter;
	}

	@Override
	public void showTableUnavailable(TableStatus status, Integer percentComplete) {
		hideLoading();
		tableContainer.setVisible(false);
		pagerContainer.setVisible(false);
		
		FlowPanel jumbo = new FlowPanel();
		jumbo.addStyleName("jumbotron");
		
		String tableUnavailableHeading = "<h1>" + DisplayConstants.TABLE_UNAVAILABLE + "</h1>";
		if(status != null && status.getState() != null) {
			String stateStr = "<strong>"+ status.getState() +"</strong>";
			if(status.getState() == TableState.PROCESSING) {
				jumbo.add(new HTML(tableUnavailableHeading + "<p>"+ stateStr +": "+ DisplayConstants.TABLE_PROCESSING_DESCRIPTION +"</p>"));
				if(percentComplete != null) {
					HTML progress = new HTML();
					progress.setHTML("<div class=\"progress-bar progress-bar-success\" role=\"progressbar\" aria-valuenow=\""+ percentComplete +"\" aria-valuemin=\"0\" aria-valuemax=\"100\" style=\"width: "+percentComplete+"%\">"+percentComplete+"% Complete</div>");
					progress.addStyleName("progress progress-striped active");
					jumbo.add(progress);
				}
				
				FlowPanel tryAgain = new FlowPanel();
				
				timerRemainingSec = 10;
				final HTML timerLabel = new HTML(getWaitingString(timerRemainingSec));
				final Timer timer = new Timer() {					
					@Override
					public void run() {
						timerRemainingSec--;
						if(timerRemainingSec <= 0) {
							cancel();
							presenter.retryCurrentQuery();
							return;
						}
						timerLabel.setHTML(getWaitingString(timerRemainingSec));
					}
				};
				timer.scheduleRepeating(1000);

				Button btn = DisplayUtils.createButton(DisplayConstants.TRY_NOW, ButtonType.DEFAULT);
				btn.addStyleName("btn-lg left");
				btn.addClickHandler(new ClickHandler() {					
					@Override
					public void onClick(ClickEvent event) {
						timer.cancel();
						presenter.retryCurrentQuery();
					}
				});
				tryAgain.add(btn);
				tryAgain.add(timerLabel);				
				jumbo.add(tryAgain);
			} else if(status.getState() == TableState.PROCESSING_FAILED) {
				jumbo.add(new HTML(tableUnavailableHeading + "<p>"+ stateStr +": "+ DisplayConstants.ERROR_GENERIC_NOTIFY +"</p>"));
			} else {
				handleGeneric(jumbo, tableUnavailableHeading);
			}
		} else {
			handleGeneric(jumbo, tableUnavailableHeading);
		}
		
		
		errorMessage.clear();
		errorMessage.setWidget(jumbo);
		errorMessage.setVisible(true);
	}

	@Override
	public void showQueryProblem(QueryProblem problem, String message) {
		hideLoading();
		if(problem == QueryProblem.UNRECOGNIZED_COLUMN) 
			queryFeedback.setText("Unrecognized column: " + message);
		else 
			queryFeedback.setText("Query Error: " + message);
		queryFeedback.setVisible(true);
		queryFieldContainer.addClassName(HAS_ERROR_HAS_FEEDBACK);
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
				runQuery();
			}
		});
		
		queryButtonContainer.setWidget(queryBtn);
		queryField.setValue(queryString);
		queryField.getElement().setId("inputError2");
		queryField.addKeyDownHandler(new KeyDownHandler() {				
			@Override
			public void onKeyDown(KeyDownEvent event) {
				if (event.getNativeEvent().getKeyCode() == KeyCodes.KEY_ENTER) {
					runQuery();
	            }					
			}
		});				
	}

	private void runQuery() {
		queryFieldContainer.removeClassName(HAS_ERROR_HAS_FEEDBACK);
		queryField.setEnabled(false);
		presenter.query(queryField.getValue());
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
		pager = new MySimplePager(TextLocation.CENTER, pagerResources, false, 0, true);
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
		RowUpdater rowUpdater = new RowUpdater() {			
			@Override
			public void updateRow(TableModel row, AsyncCallback<RowReferenceSet> callback) {
				presenter.updateRow(row, callback);			
				
			}
		};
		
		// attempt to get view width
		int windowWidth = Window.getClientWidth();
		
		
		for(ColumnModel model : columns) {
			Column<TableModel, ?> column = TableViewUtils.getColumn(model, sortHandler, canEdit, rowUpdater, cellTable, this);
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

	private AsyncDataProvider<TableModel> createAsyncDataProvider() {
		return new AsyncDataProvider<TableModel>() {
			@Override
			protected void onRangeChanged(HasData<TableModel> display) {
				// skip query if table was just built
				if (initialLoad != null) {
					updateData(new Range(initialDetails.getOffset().intValue(), initialDetails.getLimit().intValue()), initialLoad);
					initialLoad = null;
					initialDetails = null;
					return;
				}

				// skip query if a new row was inserted
				if(newRow != null) {
					newRow = null;
					return;
				}
				
				// extract sorted column
				String sortedColumnName = null;
				QueryDetails.SortDirection sortDirection = null;
				ColumnSortList sortList = cellTable.getColumnSortList();
				if (sortList.size() > 0 && sortList.get(0).getColumn() != null) {
					ColumnSortInfo columnSortInfo = sortList.get(0);
					ColumnModel model = columnToModel.get(columnSortInfo
							.getColumn());
					if (model != null) {
						sortedColumnName = model.getName();
						sortDirection = columnSortInfo.isAscending() ? SortDirection.ASC : SortDirection.DESC;
					}
				}

				final Range range = display.getVisibleRange();
				Long offset = new Long(range.getStart());
				Long limit = new Long(range.getLength());
				
				presenter.alterCurrentQuery(new QueryDetails(offset, limit, sortedColumnName, sortDirection), new AsyncCallback<RowSet>() {
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
			        currentPage = new ArrayList<TableModel>();
			        for(Row row : rowData.getRows()) {			        				        	
			        	currentPage.add(TableUtils.convertRowToModel(rowData.getHeaders(), row));
			        }
			        updateRowData(range.getStart(), currentPage);
			        hideLoading();
				}
			}
	    };
	}

	
	private HTML getLoadingWidget() {
		HTML widget = new HTML(SafeHtmlUtils.fromSafeConstant(DisplayUtils.getIconHtml(sageImageBundle.loading31()) + " " + DisplayConstants.EXECUTING_QUERY + "..."));
		widget.addStyleName("margin-top-15 center");
		return widget;
	}

	private String getWaitingString(int remainingSec) {
		return "&nbsp;" + DisplayConstants.WAITING + " " + remainingSec + "s...";
	}

	private void handleGeneric(FlowPanel jumbo, String tableUnavailableHeading) {
		jumbo.add(new HTML(tableUnavailableHeading + "<p>"+ DisplayConstants.TABLE_UNAVAILABLE_GENERIC +"</p>"));
	}

}
