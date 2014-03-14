package org.sagebionetworks.web.client.widget.table;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.sagebionetworks.repo.model.table.ColumnModel;
import org.sagebionetworks.repo.model.table.ColumnType;
import org.sagebionetworks.repo.model.table.RowSet;
import org.sagebionetworks.schema.adapter.AdapterFactory;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.view.RowData;
import org.sagebionetworks.web.client.widget.WidgetRendererPresenter;
import org.sagebionetworks.web.shared.WikiPageKey;
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
	private Integer currentTotalRowCount;
	private boolean canEdit;
	
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
	
	public void configure(String tableEntityId, final List<ColumnModel> tableColumns, final String queryString, final boolean canEdit) {
		this.tableEntityId = tableEntityId;
		this.tableColumns = tableColumns;
		this.currentQuery = queryString == null ? getDefaultQuery(tableEntityId) : queryString;
		this.canEdit = canEdit;
	
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
					final RowSet rowset = new RowSet(adapterFactory.createNew(queryResult.getRowSetJson()));
					currentQuery = queryResult.getExecutedQuery();

					if(updateCallback != null) {
						// update query
						updateCallback.onSuccess(rowset);
						view.setQuery(currentQuery);
					} else {
						// new query
						currentTotalRowCount = queryResult.getTotalRowCount();
																
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
				if(updateCallback != null) updateCallback.onFailure(caught);
				view.showErrorMessage(DisplayConstants.ERROR_LOADING_QUERY_PLEASE_RETRY);
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

}
