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
import org.sagebionetworks.web.client.TableQueryUtilServiceAsync;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.WidgetRendererPresenter;
import org.sagebionetworks.web.shared.WikiPageKey;
import org.sagebionetworks.web.shared.table.QueryDetails;
import org.sagebionetworks.web.shared.table.QueryResult;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class SimpleTableWidget implements SimpleTableWidgetView.Presenter, WidgetRendererPresenter {
	
	private SimpleTableWidgetView view;
	private SynapseClientAsync synapseClient;
	private AdapterFactory adapterFactory;
	private TableQueryUtilServiceAsync tableQueryUtilService;
	private String tableEntityId;
	private List<ColumnModel> tableColumns;
	private String currentQuery;
	private boolean canEdit;
	
	@Inject
	public SimpleTableWidget(SimpleTableWidgetView view, SynapseClientAsync synapseClient, AdapterFactory adapterFactory, TableQueryUtilServiceAsync tableQueryUtilService) {
		this.view = view;
		this.synapseClient = synapseClient;
		this.adapterFactory = adapterFactory;
		this.tableQueryUtilService = tableQueryUtilService;
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
		this.currentQuery = queryString;
		this.canEdit = canEdit;
		
		executeQuery(queryString, null);	
	}

	@Override
	public void configure(WikiPageKey wikiKey,
			Map<String, String> widgetDescriptor,
			Callback widgetRefreshRequired, Long wikiVersionInView) {
		throw new RuntimeException("NYI");
	}

	@Override
	public void alterCurrentQuery(QueryDetails alterDetails) {
		executeQuery(currentQuery, alterDetails);
	}

	@Override
	public void query(String query) {
		executeQuery(query, null);
	}
	
	
	/*
	 * Private Methods
	 */
	private void executeQuery(final String queryString, QueryDetails modifyingQueryDetails) {
		// Execute Query String		
		synapseClient.executeTableQuery(queryString, modifyingQueryDetails, new AsyncCallback<QueryResult>() {
			@Override
			public void onSuccess(QueryResult queryResult) {
				try {
					final RowSet rowset = new RowSet(adapterFactory.createNew(queryResult.getRowSetJson()));
					// make column lookup
					final Map<String,ColumnModel> idToCol = new HashMap<String, ColumnModel>();
					for(ColumnModel col : tableColumns) idToCol.put(col.getId(), col);

					// Determine column type and which columns to send to view from query results
					final List<ColumnModel> displayColumns = new ArrayList<ColumnModel>();		
					for(String resultColumnId : rowset.getHeaders()) {
						ColumnModel col = wrapDerivedColumnIfNeeded(idToCol, resultColumnId);
						if(col != null) displayColumns.add(col);
					}

					// TODO : get total row count for query
					int totalRowCount = 100;
					
					// send to view
					view.configure(displayColumns, rowset, totalRowCount, canEdit, queryString, queryResult.getQueryDetails());
				} catch (JSONObjectAdapterException e1) {
					view.showErrorMessage(DisplayConstants.ERROR_INCOMPATIBLE_CLIENT_VERSION);
				}																		
			}

			@Override
			public void onFailure(Throwable caught) {
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
