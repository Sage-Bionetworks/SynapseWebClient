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
import org.sagebionetworks.web.client.widget.WidgetRendererPresenter;
import org.sagebionetworks.web.shared.WikiPageKey;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class SimpleTableWidget implements SimpleTableWidgetView.Presenter, WidgetRendererPresenter {
	
	private SimpleTableWidgetView view;
	private SynapseClientAsync synapseClient;
	private AdapterFactory adapterFactory;
	
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
		// Execute Query String		
		synapseClient.executeTableQuery(queryString, new AsyncCallback<String>() {
			@Override
			public void onSuccess(String rowSetJson) {
				try {
					RowSet rowset = new RowSet(adapterFactory.createNew(rowSetJson));
					// make column lookup
					final Map<String,ColumnModel> idToCol = new HashMap<String, ColumnModel>();
					for(ColumnModel col : tableColumns) idToCol.put(col.getId(), col);

					// Determine column type and which columns to send to view from query results
					List<ColumnModel> displayColumns = new ArrayList<ColumnModel>();		
					for(String resultColumnId : rowset.getHeaders()) {
						ColumnModel col = wrapDerivedColumnIfNeeded(idToCol, resultColumnId);
						if(col != null) displayColumns.add(col);
					}

					// TODO : extract pagination information fomr QuerySpecification, if any
					final Integer offset = null;
					final Integer limit = null;										
					
					// send to view
					view.configure(displayColumns, rowset, canEdit, offset, limit);
				} catch (JSONObjectAdapterException e1) {
					view.showErrorMessage(DisplayConstants.ERROR_INCOMPATIBLE_CLIENT_VERSION);
				}																		
			}

			@Override
			public void onFailure(Throwable caught) {
				view.showErrorMessage(DisplayConstants.ERROR_LOADING_QUERY_PLEASE_RETRY);
			}
		});	

		
		
//		// Convert Query to QuerySpecification 
//		synapseClient.getTableQuerySpecification(queryString, new AsyncCallback<String>() {			
//			@Override
//			public void onSuccess(String querySpecificationJson) {				
//				QuerySpecification querySpec = getFakeQuerySpecification(); // TODO : create querySpec with querySpecificationJson
//				// TODO : extract pagination information fomr QuerySpecification, if any
//				final Integer offset = null;
//				final Integer limit = null;										
//				
//			}
//		
//			@Override
//			public void onFailure(Throwable caught) {
//				view.showErrorMessage(DisplayConstants.ERROR_LOADING_QUERY_PLEASE_RETRY);
//			}
//		}); 		
	}


	@Override
	public void configure(WikiPageKey wikiKey,
			Map<String, String> widgetDescriptor,
			Callback widgetRefreshRequired, Long wikiVersionInView) {
		throw new RuntimeException("NYI");
	}

	/*
	 * Private Methods
	 */
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

	
	/*
	 * Temp
	 */
//	private QuerySpecification getFakeQuerySpecification() {
//		SetQuantifier setQuantifier = SetQuantifier.ALL;
//		SelectList selectList = new SelectList("*", null);
//		FromClause fromClause = new FromClause(new TableReference("syn123"));
//		TableExpression tableExpression = new TableExpression(fromClause);
//		QuerySpecification qs = new QuerySpecification(setQuantifier, selectList, tableExpression);
//		return qs;
//	}

}
