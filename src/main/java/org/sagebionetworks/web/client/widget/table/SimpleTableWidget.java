package org.sagebionetworks.web.client.widget.table;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.sagebionetworks.repo.model.table.ColumnModel;
import org.sagebionetworks.repo.model.table.ColumnType;
import org.sagebionetworks.repo.model.table.Row;
import org.sagebionetworks.repo.model.table.RowReferenceSet;
import org.sagebionetworks.repo.model.table.RowSet;
import org.sagebionetworks.repo.model.table.TableStatus;
import org.sagebionetworks.schema.adapter.AdapterFactory;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.place.Synapse.EntityArea;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.WidgetRendererPresenter;
import org.sagebionetworks.web.client.widget.handlers.AreaChangeHandler;
import org.sagebionetworks.web.shared.WikiPageKey;
import org.sagebionetworks.web.shared.exceptions.BadRequestException;
import org.sagebionetworks.web.shared.exceptions.TableUnavilableException;
import org.sagebionetworks.web.shared.table.QueryDetails;
import org.sagebionetworks.web.shared.table.QueryResult;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class SimpleTableWidget implements SimpleTableWidgetView.Presenter, WidgetRendererPresenter {
	
	private final static Integer DEFAULT_PAGE_SIZE = 50; 
	private final static Integer DEFAULT_OFFSET = 0; 
	
	private SimpleTableWidgetView view;
	private SynapseClientAsync synapseClient;
	private AdapterFactory adapterFactory;
	private String tableEntityId;
	private List<ColumnModel> tableColumns;
	private String currentQuery;
	private List<String> currentHeaders;
	private Integer currentTotalRowCount;
	private String currentEtag;
	private boolean canEdit;
	private Long startProgress;
	private QueryChangeHandler queryChangeHandler;
	private boolean isFirstDefault = false;
	
	@Inject
	public SimpleTableWidget(SimpleTableWidgetView view, SynapseClientAsync synapseClient, AdapterFactory adapterFactory) {
		this.view = view;
		this.synapseClient = synapseClient;
		this.adapterFactory = adapterFactory;
		view.setPresenter(this);
	}	
	    
	@Override
	public Widget asWidget() {
		view.setPresenter(this);
		return view.asWidget();		
	}
	
	public void configure(String tableEntityId, List<ColumnModel> tableColumns, boolean canEdit, TableRowHeader rowHeader, QueryChangeHandler queryChangeHandler) {
		configure(tableEntityId, tableColumns, canEdit, null, rowHeader, queryChangeHandler);
	}
	
	public void configure(String tableEntityId, List<ColumnModel> tableColumns, boolean canEdit, String queryString, QueryChangeHandler queryChangeHandler) {
		configure(tableEntityId, tableColumns, canEdit, queryString, null, queryChangeHandler);
	}
	
	private void configure(String tableEntityId, List<ColumnModel> tableColumns, boolean canEdit, String queryString, TableRowHeader rowHeader, QueryChangeHandler queryChangeHandler) {
		if(rowHeader != null) view.showInfo("RowHeader", "row: "+ rowHeader.getRowId() + ", version: "+ rowHeader.getVersion()); // TODO : Remove this
		
		this.tableEntityId = tableEntityId;
		this.tableColumns = tableColumns;
		if(queryString == null) {
			isFirstDefault = true;
			this.currentQuery = getDefaultQuery(tableEntityId);	
		} else {
			isFirstDefault = false;
			this.currentQuery = queryString;
		}
		
		this.canEdit = canEdit;
		this.queryChangeHandler = queryChangeHandler;
		this.startProgress = null;
	
		view.showLoading();
		executeQuery(currentQuery, null, null);
		
	}

	private String getDefaultQuery(String tableEntityId) {
		return "SELECT * FROM " + tableEntityId + " LIMIT " + DEFAULT_PAGE_SIZE + " OFFSET " + DEFAULT_OFFSET;
	}

	@Override
	public void configure(WikiPageKey wikiKey,
			Map<String, String> widgetDescriptor,
			Callback widgetRefreshRequired, Long wikiVersionInView) {
		throw new RuntimeException("NYI");
	}

	@Override
	public void alterCurrentQuery(QueryDetails alterDetails, AsyncCallback<RowSet> callback) {
		executeQuery(currentQuery, alterDetails, callback);
	}

	@Override
	public void query(String query) {
		view.showLoading();
		this.currentQuery = query;
		executeQuery(query, null, null);
	}
	
	@Override
	public void retryCurrentQuery() {
		view.showLoading();
		executeQuery(currentQuery, null, null);
	}

	@Override
	public void updateRow(TableModel rowModel, final AsyncCallback<RowReferenceSet> callback) {		
		Row row = TableUtils.convertModelToRow(currentHeaders, rowModel);		
		// rows with temporary ids are new rows
		if(row != null && row.getRowId() != null && row.getRowId().toString().startsWith(TableModel.TEMP_ID_PREFIX)) 
			row.setRowId(null); 
		sendRowToTable(row, currentEtag, currentHeaders, callback);
	}

	@Override
	public void addRow() {
    	// fill default values
		TableModel model = new TableModel(); // with temp id
    	for(ColumnModel columnModel : tableColumns) {	
    		String value = null;
    		if(columnModel.getDefaultValue() != null) {
    			if(columnModel.getColumnType() == ColumnType.LONG) {
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
	
	/*
	 * Private Methods
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
						currentTotalRowCount = queryResult.getTotalRowCount();
						currentHeaders = rowset.getHeaders();
						
						final Map<String,ColumnModel> idToCol = new HashMap<String, ColumnModel>();
						for(ColumnModel col : tableColumns) idToCol.put(col.getId(), col);

						// Determine column type and which columns to send to view from query result headers
						final List<ColumnModel> displayColumns = new ArrayList<ColumnModel>();		
						for(String resultColumnId : rowset.getHeaders()) {
							ColumnModel col = wrapDerivedColumnIfNeeded(idToCol, resultColumnId);
							if(col != null) displayColumns.add(col);
						}
						
						// send to view
						view.createNewTable(displayColumns, rowset, currentTotalRowCount, canEdit, queryString, queryResult.getQueryDetails());						
					}
				} catch (JSONObjectAdapterException e1) {
					view.showErrorMessage(DisplayConstants.ERROR_INCOMPATIBLE_CLIENT_VERSION);
				}																		
			}

			@Override
			public void onFailure(Throwable caught) {
				if(caught instanceof TableUnavilableException) {
					Integer progress = null;
					TableStatus status = null;
					try {
						status = new TableStatus(adapterFactory.createNew(((TableUnavilableException) caught).getStatusJson()));
						if(startProgress == null) startProgress = status.getProgresssCurrent();					
						if(startProgress != null) progress = new Long(100*(status.getProgresssCurrent().longValue() - startProgress.longValue()) / status.getProgresssTotal().longValue()).intValue();
						if(progress > 100) progress = 100;
					} catch (JSONObjectAdapterException e) {
					}
					view.showTableUnavailable(status, progress);
				} else if(caught instanceof BadRequestException) {
						view.showQueryProblem(caught.getMessage());
				} else {
					if(updateCallback != null) updateCallback.onFailure(caught);
					view.showErrorMessage(DisplayConstants.ERROR_LOADING_QUERY_PLEASE_RETRY);
				}
			}
		});
	}
	
	private ColumnModel wrapDerivedColumnIfNeeded(final Map<String, ColumnModel> idToCol, String resultColumnId) {
		// test for strait column id, otherwise it is a derived column
		try {				
			Long.parseLong(resultColumnId);
			if(idToCol.containsKey(resultColumnId)) {
				return idToCol.get(resultColumnId);
			} // ignore unknown non-derived columns
		} catch (NumberFormatException e) {									
			return createDerivedColumn(resultColumnId);				
		}
		return null;
	}

	private DerivedColumnModel createDerivedColumn(String resultColumnId) {
		DerivedColumnModel derivedCol = new DerivedColumnModel();
		derivedCol.setId(resultColumnId);
		derivedCol.setName(resultColumnId);
		derivedCol.setColumnType(ColumnType.STRING);
		return derivedCol;
	}

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
					view.showErrorMessage(DisplayConstants.ROW_UPDATE_FAILED);
					callback.onFailure(caught);
				}
			});
		} catch (JSONObjectAdapterException e) {
			view.showErrorMessage(DisplayConstants.ERROR_INCOMPATIBLE_CLIENT_VERSION);
			callback.onFailure(e);
		}
	}

}
