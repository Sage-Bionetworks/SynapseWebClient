package org.sagebionetworks.web.client.widget.table;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.sagebionetworks.repo.model.file.FileHandle;
import org.sagebionetworks.repo.model.table.ColumnModel;
import org.sagebionetworks.repo.model.table.ColumnType;
import org.sagebionetworks.repo.model.table.Row;
import org.sagebionetworks.repo.model.table.RowReference;
import org.sagebionetworks.repo.model.table.RowReferenceSet;
import org.sagebionetworks.repo.model.table.RowSelection;
import org.sagebionetworks.repo.model.table.RowSet;
import org.sagebionetworks.repo.model.table.TableEntity;
import org.sagebionetworks.repo.model.table.TableFileHandleResults;
import org.sagebionetworks.repo.model.table.TableStatus;
import org.sagebionetworks.schema.adapter.AdapterFactory;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.place.Synapse;
import org.sagebionetworks.web.client.place.Synapse.EntityArea;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.WidgetRendererPresenter;
import org.sagebionetworks.web.shared.EntityWrapper;
import org.sagebionetworks.web.shared.WikiPageKey;
import org.sagebionetworks.web.shared.exceptions.BadRequestException;
import org.sagebionetworks.web.shared.exceptions.TableUnavilableException;
import org.sagebionetworks.web.shared.table.QueryDetails;
import org.sagebionetworks.web.shared.table.QueryResult;

import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class SimpleTableWidget implements SimpleTableWidgetView.Presenter, WidgetRendererPresenter {
	
	private final static Integer DEFAULT_PAGE_SIZE = 50; 
	private final static Integer DEFAULT_OFFSET = 0; 
	
	private SimpleTableWidgetView view;
	private TableEntity table;
	private SynapseClientAsync synapseClient;
	private AdapterFactory adapterFactory;
	private String tableEntityId;
	private List<ColumnModel> tableColumns;
	private String currentQuery;
	private List<String> currentHeaders;
	private Integer currentTotalRowCount;
	private String currentEtag;
	private boolean canEdit = false;
	private Long startProgress;
	private QueryChangeHandler queryChangeHandler;
	private boolean isFirstDefault = false;
	private AuthenticationController authenticationController;
	private GlobalApplicationState globalApplicationState;
	
	@Inject
	public SimpleTableWidget(SimpleTableWidgetView view,
			SynapseClientAsync synapseClient, AdapterFactory adapterFactory,
			AuthenticationController authenticationController,
			GlobalApplicationState globalApplicationState) {
		this.view = view;
		this.synapseClient = synapseClient;
		this.adapterFactory = adapterFactory;
		this.authenticationController = authenticationController;
		this.globalApplicationState = globalApplicationState;
		view.setPresenter(this);
	}	
	    
	@Override
	public Widget asWidget() {
		view.setPresenter(this);
		return view.asWidget();		
	}

	public void configure(TableEntity table, boolean canEdit) {
		configure(table, canEdit, null, null, null);
	}
	
	public void configure(TableEntity table, boolean canEdit, String query, QueryChangeHandler queryChangeHandler) {
		configure(table, canEdit, query, null, queryChangeHandler);
	}
	
	public void configure(TableEntity table, final boolean canEdit, TableRowHeader rowHeader, QueryChangeHandler queryChangeHandler) {
		configure(table, canEdit, null, rowHeader, queryChangeHandler);
	}

	@Override
	public void configure(WikiPageKey wikiKey, Map<String, String> widgetDescriptor, Callback widgetRefreshRequired, Long wikiVersionInView) {
		throw new RuntimeException("NYI");
		// configure(...)
	}
	
	/**
	 * Consolidated configure method
	 * @param table
	 * @param canEdit
	 * @param query
	 * @param rowHeader
	 * @param queryChangeHandler
	 */
	private void configure(final TableEntity table, final boolean canEdit, final String query, final TableRowHeader rowHeader, final QueryChangeHandler queryChangeHandler) {
		this.table = table;		
		this.canEdit = canEdit;
		synapseClient.getColumnModelsForTableEntity(table.getId(), new AsyncCallback<List<String>>() {
			@Override
			public void onSuccess(List<String> result) {
				try {
					tableColumns = new ArrayList<ColumnModel>();
					for(String colStr : result) {
						tableColumns.add(new ColumnModel(adapterFactory.createNew(colStr)));
					}
					
					buildTable(table.getId(), tableColumns, canEdit, query, rowHeader, queryChangeHandler);
				} catch (JSONObjectAdapterException e) {
					onFailure(e);
				}
			}
			@Override
			public void onFailure(Throwable caught) {
				if(!DisplayUtils.handleServiceException(caught, globalApplicationState, authenticationController.isLoggedIn(), view))
					view.showErrorMessage(DisplayConstants.ERROR_GENERIC_RELOAD + ": " + caught.getMessage());
			}
		});			
	}
	
	/**
	 * Execute the proper query to build the table/row view
	 * @param tableEntityId
	 * @param tableColumns
	 * @param canEdit
	 * @param queryString
	 * @param rowHeader
	 * @param queryChangeHandler
	 */
	private void buildTable(String tableEntityId, List<ColumnModel> tableColumns, boolean canEdit, String queryString, TableRowHeader rowHeader, QueryChangeHandler queryChangeHandler) {
		this.tableEntityId = tableEntityId;
		this.tableColumns = tableColumns;
		this.canEdit = canEdit;
		this.queryChangeHandler = queryChangeHandler;
		this.startProgress = null;

		if(rowHeader == null) {
			// execute regular query
			if(queryString == null) {
				isFirstDefault = true;
				this.currentQuery = getDefaultQuery(tableEntityId);	
			} else {
				isFirstDefault = false;
				this.currentQuery = queryString;
			}
		
			view.showLoading();
			executeQuery(currentQuery, null, null);			
		} else {
			// execute a query for the TableRow specified			
			view.showLoading();
			executeRowQuery(rowHeader);
		}
	}

	@Override
	public void alterCurrentQuery(QueryDetails alterDetails, AsyncCallback<RowSet> callback) {
		executeQuery(currentQuery, alterDetails, callback);
	}

	@Override
	public void query(final String query) {
		view.showLoading();		
		this.currentQuery = query;
		executeQuery(query, null, null);				
	}
	
	@Override
	public void rerunCurrentQuery() {
		view.showLoading();
		if(currentQuery == null) currentQuery = getDefaultQuery(tableEntityId);
		executeQuery(currentQuery, null, null);
	}

	@Override
	public void updateRow(TableModel rowModel, final AsyncCallback<RowReferenceSet> callback) {		
		Row row = TableUtils.convertModelToRow(currentHeaders, rowModel);		
		sendRowToTable(row, currentEtag, currentHeaders, callback);
	}

	@Override
	public void addRow() {
    	// fill default values
		TableModel model = new TableModel(); // with temp id
    	for(ColumnModel columnModel : tableColumns) {	
    		String value = null;
    		if(columnModel.getDefaultValue() != null) {
    			if(columnModel.getColumnType() == ColumnType.INTEGER) {
    				value = columnModel.getDefaultValue();
    			} else if(columnModel.getColumnType() == ColumnType.DOUBLE) {
    				value = columnModel.getDefaultValue(); 
    			} else if(columnModel.getColumnType() == ColumnType.BOOLEAN) {
    				value = columnModel.getDefaultValue().toLowerCase(); 
    			} else {
    				value = columnModel.getDefaultValue();
    			}
    		}
    		model.put(columnModel.getId(), value);
    	}    	
    	
    	// add row to view
		view.insertNewRow(model);
	}
	
	/**
	 * Add a new column to the table
	 */
	@Override
	public void createColumn(ColumnModel col, final AsyncCallback<String> callback) {
		try {									
			String columnJson = col.writeToJSONObject(adapterFactory.createNew()).toJSONString();
			synapseClient.createColumnModel(columnJson, new AsyncCallback<String>() {
				@Override
				public void onSuccess(String result) {
					try {
						ColumnModel newCol = new ColumnModel(adapterFactory.createNew(result));
						if(newCol.getId() != null) {
							if(table.getColumnIds() == null) table.setColumnIds(new ArrayList<String>()); 
							table.getColumnIds().add(newCol.getId());
							updateTableEntity();
							callback.onSuccess(null);
						}
						else {
							onFailure(null);
						}
					} catch (JSONObjectAdapterException e) {
						onFailure(e);
					}
				}
				
				@Override
				public void onFailure(Throwable caught) {
					callback.onFailure(caught);
				}
			});
		} catch (JSONObjectAdapterException e) {
			view.showErrorMessage(DisplayConstants.ERROR_INCOMPATIBLE_CLIENT_VERSION);
		}
	}

	/**
	 * Update the entire list of columns in a table to provide reordering and column removal
	 */
	@Override
	public void updateColumnOrder(List<String> columnIds) {
		table.setColumnIds(columnIds);
		updateTableEntity();
	}

	/**
	 * Delete rows from the table
	 */
	@Override
	public void deleteRows(List<TableModel> selectedRows) {
		RowSelection toDeleteSelection = new RowSelection();
		toDeleteSelection.setTableId(tableEntityId);
		toDeleteSelection.setEtag(currentEtag);
		final List<Long> rowIds = new ArrayList<Long>();
		for(TableModel model : selectedRows) {
			if(model != null && model.getId() != null) {				
				rowIds.add(Long.parseLong(model.getId()));
			}
		} 
		toDeleteSelection.setRowIds(rowIds);
		
		try {
			synapseClient.deleteRowsFromTable(toDeleteSelection.writeToJSONObject(adapterFactory.createNew()).toJSONString(), new AsyncCallback<String>() {
				@Override
				public void onSuccess(String result) {
					RowReferenceSet rrs = null;
					try {
						rrs = new RowReferenceSet(adapterFactory.createNew(result));
						currentEtag = rrs.getEtag();
						view.showInfo(rowIds.size() + " " + DisplayConstants.ROWS_DELETED, "");
						rerunCurrentQuery();
					} catch (JSONObjectAdapterException e) {
						view.showErrorMessage(DisplayConstants.ERROR_INCOMPATIBLE_CLIENT_VERSION);			
					}

				}
				@Override
				public void onFailure(Throwable caught) {
					view.showErrorMessage(DisplayConstants.ERROR_DELETE_ROWS + ": " + caught.getMessage());
				}
			});
		} catch (JSONObjectAdapterException e) {
			view.showErrorMessage(DisplayConstants.ERROR_INCOMPATIBLE_CLIENT_VERSION);
		}
	}

	@Override
	public void viewRow(List<TableModel> selectedRows) {
		if(selectedRows != null && selectedRows.size() > 0 && selectedRows.get(0).getId() != null) {		
			// only view the first row
			globalApplicationState.getPlaceChanger().goTo(new Synapse(tableEntityId, null, EntityArea.TABLES, DisplayUtils.getTableRowViewAreaToken(selectedRows.get(0).getId())));
		}
	}

	@Override
	public void getFileHandle(String rowId, String versionNumber, String colId,
			final AsyncCallback<FileHandle> callback) {
		try {
			RowReferenceSet fileHandlesToFind = new RowReferenceSet();		
			fileHandlesToFind.setTableId(tableEntityId);		
			fileHandlesToFind.setEtag(currentEtag);
			fileHandlesToFind.setHeaders(Arrays.asList(new String [] { colId }));
			List<RowReference> rows = new ArrayList<RowReference>();
			RowReference row = new RowReference();
			row.setRowId(Long.parseLong(rowId));
			row.setVersionNumber(Long.parseLong(versionNumber));
			rows.add(row);
			fileHandlesToFind.setRows(rows);
		
			synapseClient.getTableFileHandle(fileHandlesToFind.writeToJSONObject(adapterFactory.createNew()).toJSONString(), new AsyncCallback<String>() {
				@Override
				public void onSuccess(String result) {
					try {
						if(result == null) onFailure(null);
						
						TableFileHandleResults results;
							results = new TableFileHandleResults(adapterFactory.createNew(result));
						if (results != null
								&& results.getRows() != null
								&& results.getRows().size() > 0
								&& results.getRows().get(0).getList() != null
								&& results.getRows().get(0).getList().size() > 0) {
							// send file handle
							callback.onSuccess(results.getRows().get(0).getList().get(0));
						}
					} catch (Exception e) {
						onFailure(null);
					}
				}
				@Override
				public void onFailure(Throwable caught) {
					callback.onFailure(caught);
				}
			});
		} catch (Exception e) {
			callback.onFailure(null);
		}
	}

	
	/*
	 * Private Methods
	 */
	
	/**
	 * Run a query and update the view
	 * @param queryString
	 * @param modifyingQueryDetails
	 * @param updateCallback
	 */
	private void executeQuery(final String queryString, QueryDetails modifyingQueryDetails, final AsyncCallback<RowSet> updateCallback) {
		final boolean getTotalRowCount = updateCallback == null;
		// Execute Query String		
		synapseClient.executeTableQuery(queryString, modifyingQueryDetails, getTotalRowCount, new AsyncCallback<QueryResult>() {
			@Override
			public void onSuccess(QueryResult queryResult) {
				try {
					// for both new and update queries
					final RowSet rowset = new RowSet(adapterFactory.createNew(queryResult.getRowSetJson()));
					currentQuery = queryResult.getExecutedQuery();
					currentEtag = rowset.getEtag();
					if(!isFirstDefault && queryChangeHandler != null) queryChangeHandler.onQueryChange(currentQuery);
					else isFirstDefault = false;

					if(updateCallback != null) {
						// update query
						updateCallback.onSuccess(rowset);
						view.setQuery(currentQuery);
					} else {
						// new query
						currentTotalRowCount = queryResult.getTotalRowCount() == null ? 0 : queryResult.getTotalRowCount();						
						QueryDetails queryDetails = queryResult.getQueryDetails();
						
						final Map<String,ColumnModel> idToCol = new HashMap<String, ColumnModel>();
						for(ColumnModel col : tableColumns) idToCol.put(col.getId(), col);

						final List<ColumnModel> displayColumns = new ArrayList<ColumnModel>();
						List<String> tableColIds = TableUtils.extractHeaders(tableColumns);
						if(rowset.getHeaders() == null || rowset.getHeaders().size() == 0) {
							// if headers are empty (no results) add table columns						
							if(tableColumns == null) tableColumns = new ArrayList<ColumnModel>();
							currentHeaders = tableColIds;
							displayColumns.addAll(tableColumns); 
						} else {
							currentHeaders = rowset.getHeaders();
							// first add table columns from rowset *in order*
							for(ColumnModel col : tableColumns) {
								if(rowset.getHeaders().contains(col.getId())) displayColumns.add(col); 
							}
							// then grab any remaining derived columns
							for(String resultColumnId : rowset.getHeaders()) {
								if(idToCol.containsKey(resultColumnId)) continue; // skip tableColumns
								ColumnModel col = TableUtils.wrapDerivedColumnIfNeeded(idToCol, resultColumnId);
								if(col != null) displayColumns.add(col);
							}							
						}
						
						// send to view
						view.createNewTable(table.getId(), displayColumns, rowset, currentTotalRowCount, canEdit, currentQuery, queryDetails);						
					}
				} catch (JSONObjectAdapterException e1) {
					view.showErrorMessage(DisplayConstants.ERROR_INCOMPATIBLE_CLIENT_VERSION);
				}																		
			}

			@Override
			public void onFailure(Throwable caught) {
				if(caught instanceof TableUnavilableException) {
					handleTableUnavailableException(caught);
				} else if(caught instanceof BadRequestException) {
						view.showQueryProblem(caught.getMessage());
				} else {
					if(updateCallback != null) updateCallback.onFailure(caught);
					view.showErrorMessage(DisplayConstants.ERROR_LOADING_QUERY_PLEASE_RETRY + ": " + caught.getMessage());
				}
			}
		});
	}
	
	/**
	 * Not fundamentally different from executeQuery, just different process logic
	 */
	private void executeRowQuery(TableRowHeader rowHeader) {
		String query = getDefaultRowQuery(rowHeader);
		synapseClient.executeTableQuery(query, null, false, new AsyncCallback<QueryResult>() {
			@Override
			public void onSuccess(QueryResult result) {
				RowSet rowset;
				try {
					rowset = new RowSet(adapterFactory.createNew(result.getRowSetJson()));
					// send to view
					view.createRowView(tableColumns, rowset);						
				} catch (JSONObjectAdapterException e) {
					view.showErrorMessage(DisplayConstants.ERROR_INCOMPATIBLE_CLIENT_VERSION);
				}
			}
			@Override
			public void onFailure(Throwable caught) {
				if(caught instanceof TableUnavilableException) {
					handleTableUnavailableException(caught);
				} else if(caught instanceof BadRequestException) {
						view.showQueryProblem(caught.getMessage());
				} else {					
					view.showErrorMessage(DisplayConstants.ERROR_LOADING_QUERY_PLEASE_RETRY);
				}
				
			}
		});
	}
	
	private void handleTableUnavailableException(Throwable caught) {
		Integer progress = null;
		TableStatus status = null;
		try {
			status = new TableStatus(adapterFactory.createNew(((TableUnavilableException) caught).getStatusJson()));
			if(startProgress == null) startProgress = status.getProgressCurrent();					
			if(startProgress != null) progress = new Long(100*(status.getProgressCurrent().longValue() - startProgress.longValue()) / status.getProgressTotal().longValue()).intValue();
			if(progress != null && progress > 100) progress = 100;
		} catch (JSONObjectAdapterException e) {
		}
		view.showTableUnavailable(status, progress);
	}


	/**
	 * Add or update row. Row objects without a rowId are considered an add
	 * @param row
	 * @param etag
	 * @param headers
	 * @param callback
	 */
	private void sendRowToTable(Row row, String etag, List<String> headers, final AsyncCallback<RowReferenceSet> callback) {
		RowSet rowSet = new RowSet();
		rowSet.setTableId(tableEntityId);
		rowSet.setEtag(etag);
		rowSet.setHeaders(headers);
		rowSet.setRows(Arrays.asList(new Row[] { row }));
		try {
			synapseClient.sendRowsToTable(rowSet.writeToJSONObject(adapterFactory.createNew()).toJSONString(), new AsyncCallback<String>() {
				@Override
				public void onSuccess(String result) {
					RowReferenceSet rrs = null;
					try {
						rrs = new RowReferenceSet(adapterFactory.createNew(result));
						currentEtag = rrs.getEtag();
					} catch (JSONObjectAdapterException e) {
						view.showErrorMessage(DisplayConstants.ERROR_INCOMPATIBLE_CLIENT_VERSION);
						callback.onFailure(e);
					}
					callback.onSuccess(rrs);
				}
				@Override
				public void onFailure(Throwable caught) {
					view.showErrorMessage(DisplayConstants.ROW_UPDATE_FAILED + " " + DisplayConstants.REASON + ": " + caught.getMessage());
					callback.onFailure(caught);
				}
			});
		} catch (JSONObjectAdapterException e) {
			view.showErrorMessage(DisplayConstants.ERROR_INCOMPATIBLE_CLIENT_VERSION);
			callback.onFailure(e);
		}
	}

	
	/**
	 * Update the current TableEntity in repo
	 */
	private void updateTableEntity() {
		try {
			String entityJson = table.writeToJSONObject(adapterFactory.createNew()).toJSONString();
			synapseClient.createOrUpdateEntity(entityJson, null, false, new AsyncCallback<String>() {
				@Override
				public void onSuccess(String result) {
					synapseClient.getEntity(table.getId(), new AsyncCallback<EntityWrapper>() {
						@Override
						public void onSuccess(EntityWrapper result) {
							try {						
								table = new TableEntity(adapterFactory.createNew(result.getEntityJson()));
								// delay reconfiguring a bit to allow table processing to catch up. Avoids excessive TableUnavailableExceptions
								Timer t = new Timer() {			
									@Override
									public void run() {
										configure(table, canEdit);
									}
								};
								t.schedule(500);
								view.showLoading();
							} catch (JSONObjectAdapterException e) {
								onFailure(e);
							}							
						}
						@Override
						public void onFailure(Throwable caught) {
							if(!DisplayUtils.handleServiceException(caught, globalApplicationState, authenticationController.isLoggedIn(), view))
								view.showErrorMessage(DisplayConstants.TABLE_UPDATE_FAILED);							
						}
					});
				}
				@Override
				public void onFailure(Throwable caught) {
					if(!DisplayUtils.handleServiceException(caught, globalApplicationState, authenticationController.isLoggedIn(), view))
						view.showErrorMessage(DisplayConstants.TABLE_UPDATE_FAILED);
				}
			});
		} catch (JSONObjectAdapterException e) {
			view.showErrorMessage(DisplayConstants.TABLE_UPDATE_FAILED);
		}
	}

	/**
	 * Default Query for the table
	 * @param tableEntityId
	 * @return
	 */
	private String getDefaultQuery(String tableEntityId) {
		return "SELECT * FROM " + tableEntityId + " LIMIT " + DEFAULT_PAGE_SIZE + " OFFSET " + DEFAULT_OFFSET;
	}

	/**
	 * Default Query for a row
	 * @param rowHeader
	 * @return
	 */
	private String getDefaultRowQuery(TableRowHeader rowHeader) {
		if(rowHeader != null && rowHeader.getRowId() != null && !rowHeader.getRowId().equals("")) {			
			String query = "SELECT * FROM " + tableEntityId + " WHERE ROW_ID=" + rowHeader.getRowId();
			if(rowHeader.getVersion() != null && !rowHeader.getVersion().equals("")) query += " AND ROW_VERSION="+ rowHeader.getVersion();  
			query += " LIMIT 1";
			return query;
		}
		return null;
	}

}
