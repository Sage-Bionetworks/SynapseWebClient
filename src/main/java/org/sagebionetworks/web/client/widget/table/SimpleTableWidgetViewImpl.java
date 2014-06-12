package org.sagebionetworks.web.client.widget.table;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.sagebionetworks.repo.model.file.FileHandle;
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
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.SageImageBundle;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.utils.BootstrapTable;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.ListCreatorViewWidget;
import org.sagebionetworks.web.shared.table.QueryDetails;
import org.sagebionetworks.web.shared.table.QueryDetails.SortDirection;

import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.MessageBoxEvent;
import com.extjs.gxt.ui.client.widget.Dialog;
import com.extjs.gxt.ui.client.widget.MessageBox;
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
import com.google.gwt.user.client.ui.InlineHTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.AsyncDataProvider;
import com.google.gwt.view.client.DefaultSelectionEventManager;
import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.MultiSelectionModel;
import com.google.gwt.view.client.Range;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SelectionModel;
import com.google.inject.Inject;

public class SimpleTableWidgetViewImpl extends Composite implements SimpleTableWidgetView {
	private static final String HAS_ERROR_HAS_FEEDBACK = "has-error has-feedback";
	private static int sequence = 0;
	
	public interface Binder extends UiBinder<Widget, SimpleTableWidgetViewImpl> {	}
	
	@UiField
	HTMLPanel buttonToolbar;
	@UiField
	SimplePanel columnEditorPanel;
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
	@UiField
	HTMLPanel allRowContainer;

	FlowPanel addColumnPanel;
	List<ColumnDetailsPanel> columnPanelOrder;
	private boolean columnEditorBuilt = false;
	
	SageImageBundle sageImageBundle;
	CellTable<TableModel> cellTable;
	MySimplePager pager;
	private List<ColumnModel> columns;
	SelectionModel<TableModel> selectionModel;
	Presenter presenter;
	Map<Column,ColumnModel> columnToModel;
	AsyncDataProvider<TableModel> dataProvider;
	RowSet initialLoad = null;
	QueryDetails initialDetails = null;	
	TableModel newRow = null;
	SynapseJSNIUtils jsniUtils;	
	List<TableModel> currentPage;
	Button showColumnsBtn;
	Button addRowBtn;
	Button deleteRowBtn;
	Button viewRowBtn;
	
	@Inject
	public SimpleTableWidgetViewImpl(final Binder uiBinder, SageImageBundle sageImageBundle, SynapseJSNIUtils jsniUtils, PortalGinInjector ginInjector) {
		this.sageImageBundle = sageImageBundle;
		this.jsniUtils = jsniUtils;
		TableViewUtils.ginInjector = ginInjector;
		
		columnToModel = new HashMap<Column, ColumnModel>();
		initWidget(uiBinder.createAndBindUi(this));

		this.sageImageBundle = sageImageBundle;
		tableLoading.add(getLoadingWidget());
		
		tableContainer.addStyleName("tableMinWidth");
		
	    // setup DataProvider for pagination/sorting
		dataProvider = createAsyncDataProvider();

	}
	
	@Override
	public void createNewTable(String tableEntityId, List<ColumnModel> columns, RowSet rowset,
			int totalRowCount, boolean canEdit, String queryString,
			QueryDetails queryDetails) {		
		this.columns = columns;				
		this.initialLoad = rowset;
		this.initialDetails = queryDetails;
		columnToModel.clear();

		// Render Table			
		columnEditorBuilt = false; // clear out old column editor view
		setupTableEditorToolbar(columns);
		setDefaultToolbarButtonVisibility(canEdit);
		
		// special cases display user instructions instead of empty table
		if(columns == null || (columns != null && columns.size() == 0)) {
			showAddColumnsView(canEdit);
			return;
		}
		
		setupQueryBox(queryString);			
		queryPanel.setVisible(true);		
		buildTable(queryDetails, totalRowCount, canEdit);
		buildColumns(tableEntityId, columns, canEdit);			
		hideLoading();		 	    
	}
	

	private void setDefaultToolbarButtonVisibility(boolean canEdit) {
		showColumnsBtn.setVisible(canEdit);
		addRowBtn.setVisible(canEdit);
		deleteRowBtn.setVisible(false);
		viewRowBtn.setVisible(false);
	}

	@Override
	public void createRowView(List<ColumnModel> columns, RowSet rowset) {
		// Render Row
		buildRowView(columns, rowset);
		tableContainer.setVisible(false);
		allRowContainer.setVisible(true);
		hideLoading();
	}

	
	private void buildRowView(List<ColumnModel> columns, RowSet rowset) {
		allRowContainer.clear();
		Button btn = DisplayUtils.createIconButton(DisplayConstants.BACK_TO_TABLE, ButtonType.DEFAULT, "glyphicon-chevron-left");
		btn.addClickHandler(new ClickHandler() {			
			@Override
			public void onClick(ClickEvent event) {				
				presenter.rerunCurrentQuery();
			}
		});
		allRowContainer.add(btn);
		
		final Map<String,ColumnModel> idToCol = new HashMap<String, ColumnModel>();
		for(ColumnModel col : columns) idToCol.put(col.getId(), col);

		FlowPanel panel = new FlowPanel();
		panel.addStyleName("panel panel-default panel-body margin-top-15");
		BootstrapTable table = new BootstrapTable();
		table.setWidth("100%");		
		table.getColumnFormatter().setWidth(0, "10%");
		table.getColumnFormatter().setWidth(1, "90%");
		if(rowset != null && rowset.getHeaders() != null && rowset.getRows() != null && rowset.getHeaders().size() > 0 && rowset.getRows().size() > 0) {			
			List<String> headers = rowset.getHeaders();
			List<String> headerNamesRow = new ArrayList<String>();
			headerNamesRow.add(DisplayConstants.COLUMN);
			headerNamesRow.add(DisplayConstants.VALUE);			
			List<List<String>> tableHeaderRows = new ArrayList<List<String>>();
			tableHeaderRows.add(headerNamesRow);
			table.setHeaders(tableHeaderRows);			

			// add data to table
			for(int i=0; i<headers.size(); i++) {
				table.setHTML(i, 0, "<span class=\"strong\">"+idToCol.get(headers.get(i)).getName()+"</span>");
				ColumnModel col = idToCol.get(headers.get(i));
				String value = rowset.getRows().get(0).getValues().get(i);
				if(value != null) {
					if(col != null && col.getColumnType() == ColumnType.DATE) {											
						table.setText(i, 1, TableViewUtils.getDateStringTableFormat(new Date(Long.parseLong(value))));
					} else if(col != null && col.getColumnType() == ColumnType.FILEHANDLEID) {					
						// TODO : render filehandle
						//table.setWidget(i, 1, new Image...);
					} else {
						// regular string
						table.setText(i, 1, value);
					}
				} else {
					table.setText(i,  1, "");
				}
			}
			panel.add(table);
		} else {
			// show empty
			panel.add(new HTML("<h3>" + DisplayConstants.ROW_IS_EMPTY + "</h3>"));
		}	
		allRowContainer.add(panel);
	}

	private void showAddColumnsView(boolean canEdit) {
		hideLoading();		
		pagerContainer.setVisible(false);		
		if(addRowBtn != null) addRowBtn.setEnabled(false);
		FlowPanel addAColumnPanel = new FlowPanel();
		addAColumnPanel.addStyleName("alert alert-info");
		String str = "This Table does not contain any Columns.";
		if(canEdit) str += " You can add columns in the \"Column Details\" section above.";
		addAColumnPanel.add(new HTML(str));
		
		tableContainer.setWidget(addAColumnPanel);		
	}

	@Override
	public void setQuery(String query) {
		queryField.setValue(query);
	}

	@Override
	public void insertNewRow(TableModel model) {
		newRow = model;
		if(currentPage == null) currentPage = new ArrayList<TableModel>();
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
		allRowContainer.setVisible(false);
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
				
				TimedRetryWidget tryAgain = new TimedRetryWidget();
				tryAgain.configure(10, new Callback() {
					
					@Override
					public void invoke() {
						presenter.rerunCurrentQuery();
					}
				});
			
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
	public void showQueryProblem(String message) {
		hideLoading();
		queryFeedback.setText("Query Error - " + message);
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
	
	private void buildTable(QueryDetails queryDetails, int totalRowCount, final boolean canEdit) {
		cellTable = new CellTable<TableModel>(TableModel.KEY_PROVIDER);
		cellTable.setWidth("100%", true);
		cellTable.setPageSize(queryDetails.getLimit().intValue());
		cellTable.addStyleName("cellTable");
		cellTable.setLoadingIndicator(getLoadingWidget());
		tableContainer.setWidget(cellTable);

		// Do not refresh the headers and footers every time the data is
		// updated.
		cellTable.setAutoHeaderRefreshDisabled(true);
		cellTable.setAutoFooterRefreshDisabled(true);

		// Create a Pager to control the table.
		SimplePager.Resources pagerResources = GWT.create(SimplePager.Resources.class);
		pager = new MySimplePager(TextLocation.CENTER, pagerResources, false, 0, true);
		pager.setDisplay(cellTable);
		pagerContainer.setWidget(pager);

		// Add a selection model so we can select cells.
		selectionModel = new MultiSelectionModel<TableModel>(TableModel.KEY_PROVIDER);
		cellTable.setSelectionModel(selectionModel, DefaultSelectionEventManager.<TableModel> createCheckboxManager());
		selectionModel.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
			@Override
			public void onSelectionChange(SelectionChangeEvent event) {				
				setSelectionButtonVisibility(getSelectedRows(), canEdit);				
			}			
		});
		
	    // Add a ColumnSortEvent.AsyncHandler to connect sorting to the
	    // AsyncDataPRrovider.
		AsyncHandler columnSortHandler = new AsyncHandler(cellTable);
	    cellTable.addColumnSortHandler(columnSortHandler);	    
	    
	    cellTable.setRowCount(totalRowCount, true);
	    if(queryDetails.getOffset() != null && queryDetails.getLimit() != null) {	    	
	    	cellTable.setVisibleRange(queryDetails.getOffset().intValue(), queryDetails.getOffset().intValue() + queryDetails.getLimit().intValue());
	    } else {
	    	cellTable.setVisibleRange(0, totalRowCount);
	    }	    
	    

	    // Connect the list to the data provider.
	    dataProvider.addDataDisplay(cellTable);

	}

	/**
	 * Control the visibility of buttons depending upon selection state
	 * @param selected
	 * @param canEdit
	 */
	private void setSelectionButtonVisibility(List<TableModel> selected, boolean canEdit) {
		deleteRowBtn.setVisible(canEdit && selected != null && selected.size() > 0);
		viewRowBtn.setVisible(selected != null && selected.size() == 1);
	}
	
	/**
	 * Create an AsyncDataProvider that updates the Data in the view when requested
	 * @return
	 */
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
						sortedColumnName = TableUtils.escapeColumnName(model.getName());
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

	/**
	 * Build the GWT CellTable Columns from a list of ColumnModels
	 * @param columns
	 * @param canEdit
	 */
	private void buildColumns(String tableEntityId, List<ColumnModel> columns, boolean canEdit) {
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

		// Table's columns
		RowUpdater rowUpdater = new RowUpdater() {			
			@Override
			public void updateRow(TableModel row, AsyncCallback<RowReferenceSet> callback) {
				presenter.updateRow(row, callback);			
				
			}
		};
		
		// attempt to get view width
		boolean isFixedWidth = TableViewUtils.isAllFixedWidthColumns(columns, Window.getClientWidth());
		int nPercentageCols = columns.size();		
		tableContainer.removeStyleName("overflow-x-auto");
		if(!isFixedWidth) {
			for(ColumnModel model : columns) {
				if(model.getColumnType() == ColumnType.BOOLEAN || model.getColumnType() == ColumnType.DATE) nPercentageCols--;
			}					
		} else {
			tableContainer.addStyleName("overflow-x-auto");
		}
				
		for(ColumnModel model : columns) {
			Column<TableModel, ?> column = TableViewUtils.getColumn(tableEntityId, model, canEdit, rowUpdater, cellTable, this, jsniUtils);
			cellTable.addColumn(column, model.getName());
			columnToModel.put(column, model);
			
			if(isFixedWidth) {
				cellTable.setColumnWidth(column, TableViewUtils.getColumnDisplayWidth(model.getColumnType()), Unit.PX);
			} else {
				// always fix boolean and date
				if(model.getColumnType() == ColumnType.BOOLEAN || model.getColumnType() == ColumnType.DATE) {
					cellTable.setColumnWidth(column, TableViewUtils.getColumnDisplayWidth(model.getColumnType()), Unit.PX);
				} else {
					cellTable.setColumnWidth(column, (100/nPercentageCols), Unit.PCT);			
				}
			}
		}
	}
	
	private HTML getLoadingWidget() {
		HTML widget = new HTML(SafeHtmlUtils.fromSafeConstant(DisplayUtils.getIconHtml(sageImageBundle.loading31()) + " " + DisplayConstants.EXECUTING_QUERY + "..."));
		widget.addStyleName("margin-top-15 center");
		return widget;
	}

	private void handleGeneric(FlowPanel jumbo, String tableUnavailableHeading) {
		jumbo.add(new HTML(tableUnavailableHeading + "<p>"+ DisplayConstants.TABLE_UNAVAILABLE_GENERIC +"</p>"));
	}

	
	/**
	 * ----- Column Editor Methods -----
	 */
	/**
	 * Sets up the top level editing toolbar
	 * @param columns
	 */
	private void setupTableEditorToolbar(final List<ColumnModel> columns) {
		buttonToolbar.clear();

		showColumnsBtn = DisplayUtils.createIconButton(DisplayConstants.COLUMN_DETAILS, ButtonType.DEFAULT, "glyphicon-th-list");
		showColumnsBtn.addStyleName("margin-right-5");
		showColumnsBtn.addClickHandler(new ClickHandler() {			
			@Override
			public void onClick(ClickEvent event) {
				if(!columnEditorBuilt) columnEditorPanel.setWidget(buildColumnsEditor(columns));
				columnEditorPanel.setVisible( columnEditorPanel.isVisible() ? false : true ); 
			}
		});
		
		addRowBtn = DisplayUtils.createIconButton(DisplayConstants.ADD_ROW, ButtonType.DEFAULT, "glyphicon-plus");
		addRowBtn.addStyleName("margin-right-5");
		addRowBtn.addClickHandler(new ClickHandler() {			
			@Override
			public void onClick(ClickEvent event) {
		    	presenter.addRow();  
			}
		});
		
		deleteRowBtn = DisplayUtils.createButton(DisplayConstants.DELETE_SELECTED, ButtonType.DANGER);
		deleteRowBtn.addStyleName("margin-right-5");
		deleteRowBtn.addClickHandler(new ClickHandler() {			
			@Override
			public void onClick(ClickEvent event) {
				DisplayUtils.showConfirmDialog(DisplayConstants.DELETE_SELECTED, DisplayConstants.CONFIRM_DELETE_SELECTED, 
						new Callback() {
							@Override
							public void invoke() {
								presenter.deleteRows(getSelectedRows());
							}
						});
			}
		});		
		
		viewRowBtn = DisplayUtils.createButton(DisplayConstants.VIEW_ROW, ButtonType.DEFAULT);
		viewRowBtn.addStyleName("margin-right-5");
		viewRowBtn.addClickHandler(new ClickHandler() {			
			@Override
			public void onClick(ClickEvent event) {
		    	presenter.viewRow(getSelectedRows());
			}
		});
		
		buttonToolbar.add(showColumnsBtn);		
		buttonToolbar.add(addRowBtn);
		buttonToolbar.add(viewRowBtn);
		buttonToolbar.add(deleteRowBtn);
	}
	
	/**
	 * Get the currently selected rows
	 * @return
	 */
	private List<TableModel> getSelectedRows() {
		List<TableModel> selected = new ArrayList<TableModel>();		
		if(selectionModel != null && dataProvider != null && dataProvider.getDataDisplays() != null) {
			for(HasData<TableModel> hd : dataProvider.getDataDisplays()) {
				for(TableModel model : hd.getVisibleItems()) {
					if(selectionModel.isSelected(model)) {
						selected.add(model);
					}					
				}
			}
		}		
		return selected;
	}

	
	/**
	 * Builds a widget for the column editor/view panel
	 * @param columns
	 * @return
	 */
	private Widget buildColumnsEditor(List<ColumnModel> columns) {
		FlowPanel parent = new FlowPanel();
		parent.addStyleName("panel-group");
		String accordionId = "accordion-" + ++sequence;
		parent.getElement().setId(accordionId);
		
		// add header
		parent.add(new HTML("<h4>" + DisplayConstants.COLUMN_DETAILS + "</h4>"));
		
		final FlowPanel allColumnsPanel = new FlowPanel();
		columnPanelOrder = new ArrayList<ColumnDetailsPanel>();
		for(int i=0; i<columns.size(); i++) {
			final ColumnModel col = columns.get(i);			
			final ColumnDetailsPanel columnPanel = new ColumnDetailsPanel(accordionId, col, "contentId" + ++sequence);
			
			columnPanel.getMoveUp().addClickHandler(new ClickHandler() {				
				@Override
				public void onClick(ClickEvent event) {
					// swap columns
					int formerIdx = columnPanelOrder.indexOf(columnPanel);
					TableViewUtils.swapColumns(columnPanelOrder, allColumnsPanel, columnPanel, formerIdx, formerIdx-1, presenter);
				}

			});
			columnPanel.getMoveDown().addClickHandler(new ClickHandler() {				
				@Override
				public void onClick(ClickEvent event) {
					// swap columns
					int formerIdx = columnPanelOrder.indexOf(columnPanel);
					TableViewUtils.swapColumns(columnPanelOrder, allColumnsPanel, columnPanel, formerIdx, formerIdx+1, presenter);
				}

			});
			columnPanel.getDelete().addClickHandler(new ClickHandler() {			
				@Override
				public void onClick(ClickEvent event) {
					DisplayUtils.showConfirmDialog("Confirm", DisplayConstants.CONFIRM_DELETE_COLUMN + col.getName(), 
							new Callback() {
								@Override
								public void invoke() {
									columnPanel.addStyleName("fade");
									// allow for fade before removal
									Timer t = new Timer() {								
										@Override
										public void run() {
											allColumnsPanel.remove(columnPanel);
											columnPanelOrder.remove(columnPanel);
											
											// update table entity
											presenter.updateColumnOrder(TableViewUtils.extractColumns(columnPanelOrder));
											
											// update ends, if needed
											int size = columnPanelOrder.size();
											if(size > 0) {
												TableViewUtils.setArrowVisibility(0, size, columnPanelOrder.get(0).getMoveUp(), columnPanelOrder.get(0).getMoveDown());
												TableViewUtils.setArrowVisibility(size-1, size, columnPanelOrder.get(size-1).getMoveUp(), columnPanelOrder.get(size-1).getMoveDown());
											}
										}
									};
									t.schedule(250);
								}
							});
				}
			});
			if(i==0) columnPanel.getMoveUp().setVisible(false);
			if(i==columns.size()-1) columnPanel.getMoveDown().setVisible(false); 
			
			columnPanelOrder.add(columnPanel);
			allColumnsPanel.add(columnPanel);
		}
		parent.add(allColumnsPanel);

		// Add Column
		addColumnPanel = new FlowPanel();
		addColumnPanel.addStyleName("well margin-top-15");		
		refreshAddColumnPanel();

		Button addColumnBtn = DisplayUtils.createIconButton(DisplayConstants.ADD_COLUMN, ButtonType.DEFAULT, "glyphicon-plus");
		addColumnBtn.addStyleName("margin-top-15");	
		addColumnBtn.addClickHandler(new ClickHandler() {			
			@Override
			public void onClick(ClickEvent event) {
				if(addColumnPanel.isVisible()) addColumnPanel.setVisible(false);
				else addColumnPanel.setVisible(true);
			}
		});
		parent.add(addColumnBtn);
		parent.add(addColumnPanel);
		
		return parent;
	}

	/**
	 * Create an editor widget for a column
	 * @param col
	 * @return
	 */
	private Widget createColumnEditor(final ColumnModel col) {
		final FlowPanel form = new FlowPanel();
		form.addStyleName("margin-top-15");		
		
		// Column Name	
		FlowPanel formGroup = new FlowPanel();		
		formGroup.addStyleName("form-group");
		HTML inputLabel = new InlineHTML(DisplayConstants.COLUMN_NAME + ": ");
		inputLabel.addStyleName("boldText");
		final TextBox name = new TextBox();
		if(col.getName() != null) name.setValue(SafeHtmlUtils.fromString(col.getName()).asString());
		name.addStyleName("form-control");
		DisplayUtils.setPlaceholder(name, DisplayConstants.COLUMN_NAME);
		final InlineHTML columnNameError = DisplayUtils.createFormHelpText(DisplayConstants.COLUMN_NAME + " " + DisplayConstants.REQUIRED);
		columnNameError.addStyleName("text-danger-imp");
		columnNameError.setVisible(false);
		formGroup.add(inputLabel);
		formGroup.add(name);
		formGroup.add(columnNameError);
		form.add(formGroup);
		
		// Containers
		final FlowPanel stringLengthContainer = new FlowPanel();
		final FlowPanel defaultValueContainer = new FlowPanel();
		final FlowPanel enumContainer = new FlowPanel();
		// itital column view states
		if(col.getColumnType() == ColumnType.STRING) stringLengthContainer.setVisible(true);
		if(col.getColumnType() == ColumnType.FILEHANDLEID || col.getColumnType() == ColumnType.DATE || col.getColumnType() == ColumnType.BOOLEAN) enumContainer.setVisible(false);
		ColumnTypeChangeHandler typeChangeHandler = new ColumnTypeChangeHandler() {			
			@Override
			public void onChange(ColumnType selectedType) {
				col.setColumnType(selectedType);
				stringLengthContainer.setVisible(selectedType == ColumnType.STRING);
				enumContainer.setVisible(selectedType != ColumnType.FILEHANDLEID && selectedType != ColumnType.DATE && selectedType != ColumnType.BOOLEAN);			
				defaultValueContainer.clear();
				defaultValueContainer.add(TableViewUtils.createDefaultValueRadio(col));
			}
		};

		// Column Type
		inputLabel = new HTML(DisplayConstants.COLUMN_TYPE + ": ");
		inputLabel.addStyleName("margin-top-15 boldText");
		final InlineHTML columnTypeError = DisplayUtils.createFormHelpText(DisplayConstants.COLUMN_TYPE + " " + DisplayConstants.REQUIRED);
		columnTypeError.addStyleName("text-danger-imp");
		columnTypeError.setVisible(false);
		form.add(inputLabel);		
		form.add(createColumnTypeRadio(col, typeChangeHandler));		
		form.add(columnTypeError);
						
		// String length
		stringLengthContainer.setVisible(false);
		inputLabel = new HTML(DisplayConstants.MAX_STRING_LENGTH + " (" + DisplayConstants.OPTIONAL + "): ");
		inputLabel.addStyleName("margin-top-15 boldText control-label");
		stringLengthContainer.add(inputLabel);
		stringLengthContainer.add(TableViewUtils.createStringLengthField(col, stringLengthContainer));
		inputLabel = new HTML(DisplayConstants.MAX_LENGTH_REASON);
		inputLabel.addStyleName("control-label");	
		stringLengthContainer.add(inputLabel);
		form.add(stringLengthContainer);
		
		
		// Default Value	
		defaultValueContainer.add(TableViewUtils.createDefaultValueRadio(col));
		form.add(defaultValueContainer);

		// Enum Values		
		inputLabel = new HTML(DisplayConstants.RESTRICT_VALUES + " (" + DisplayConstants.OPTIONAL + "): ");
		inputLabel.addStyleName("margin-top-15 boldText");
		enumContainer.add(inputLabel);	
		final ListCreatorViewWidget list = new ListCreatorViewWidget(DisplayConstants.ADD_VALUE, true);
		enumContainer.add(createRestrictedValues(col, list));
		form.add(enumContainer);

		final InlineHTML generalError = DisplayUtils.createFormHelpText("");
		generalError.addStyleName("text-danger-imp"); 
		generalError.setVisible(false);
		
		// Create column
		final Button save = DisplayUtils.createButton(DisplayConstants.CREATE_COLUMN, ButtonType.PRIMARY);
		save.addStyleName("margin-top-15");
		save.addClickHandler(new ClickHandler() {			
			@Override
			public void onClick(ClickEvent event) {
				generalError.setVisible(false);
				save.setEnabled(false);
				if(name.getValue() == null || name.getValue().length() == 0) {
					columnNameError.setVisible(true);
					save.setEnabled(true);
					return;
				} else {
					columnNameError.setVisible(false);
				}
				if(col.getColumnType() == null) {
					columnTypeError.setVisible(true);
					save.setEnabled(true);
					return;
				} else {
					columnTypeError.setVisible(false);
				}
				// import values into col. Type and default are set automatically
				col.setName(name.getValue());				
				List<String> restrictedValues = list.getValues();
				if(restrictedValues.size() > 0) col.setEnumValues(restrictedValues);
				
				// create
				presenter.createColumn(col, new AsyncCallback<String> () {
					@Override
					public void onSuccess(String result) { 
						columnEditorPanel.setVisible(false); // hide panel as it will now be rebuilt
						refreshAddColumnPanel(); // clear aout add column view	
					}					
					@Override
					public void onFailure(Throwable caught) {
						save.setEnabled(true);
						generalError.setHTML(DisplayConstants.ERROR_CREATING_COLUMN + ": " + caught.getMessage());
						generalError.setVisible(true);						
					}
				});				
			}
		});
		form.add(save);
		form.add(generalError);
		
		return form;		
	}

	/**
	 * Create a radio input widget for column type. Initializes to the given col, and modifies the given col.
	 * @param col
	 * @return
	 */
	private Widget createColumnTypeRadio(final ColumnModel col, final ColumnTypeChangeHandler handler) {
		FlowPanel columnTypeRadio = new FlowPanel();
		columnTypeRadio.addStyleName("btn-group");
		final List<Button> groupBtns = new ArrayList<Button>(); 
		for(final ColumnType type : new ColumnType[] { ColumnType.STRING, ColumnType.LONG, ColumnType.DOUBLE, ColumnType.BOOLEAN, ColumnType.DATE , ColumnType.FILEHANDLEID }) {			
			String radioLabel = TableViewUtils.getColumnDisplayName(type);
			final Button btn = DisplayUtils.createButton(radioLabel);
			btn.addClickHandler(new ClickHandler() {			
				@Override
				public void onClick(ClickEvent event) {
					for(Button gBtn : groupBtns) {
						gBtn.removeStyleName("active");
					}
					btn.addStyleName("active");
					col.setColumnType(type);
					handler.onChange(type);
				}
			});
			if(col.getColumnType() != null && col.getColumnType() == type) btn.addStyleName("active");
			groupBtns.add(btn);
			columnTypeRadio.add(btn);
		}
		return columnTypeRadio;
	}
	


	/**
	 * Create the restricted values list
	 * @param col
	 * @param list
	 * @return
	 */
	private Widget createRestrictedValues(ColumnModel col, ListCreatorViewWidget list) {
		FlowPanel row = new FlowPanel();
		row.addStyleName("row");
		FlowPanel left = new FlowPanel();
		left.addStyleName("col-sm-6");
		FlowPanel right = new FlowPanel();
		right.addStyleName("col-sm-6");
		row.add(left);
		row.add(right);		
		left.add(list);		
		return row;
	}

	/**
	 * Clears, hides and rebuilds the add column panel.
	 */
	private void refreshAddColumnPanel() {
		addColumnPanel.clear();
		addColumnPanel.setVisible(false);
		ColumnModel newColumn = new ColumnModel();
		addColumnPanel.add(new HTML("<h4>" + DisplayConstants.ADD_COLUMN + "</h4>"));
		addColumnPanel.add(createColumnEditor(newColumn));		
	}

}
