package org.sagebionetworks.web.client.widget.table.v2.results;

import static org.sagebionetworks.web.client.ServiceEntryPointUtils.fixServiceEntryPoint;
import static org.sagebionetworks.web.client.widget.table.v2.TableEntityWidget.DEFAULT_LIMIT;
import static org.sagebionetworks.web.client.widget.table.v2.TableEntityWidget.DEFAULT_OFFSET;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.sagebionetworks.repo.model.asynch.AsynchronousResponseBody;
import org.sagebionetworks.repo.model.table.FacetColumnRequest;
import org.sagebionetworks.repo.model.table.Query;
import org.sagebionetworks.repo.model.table.QueryBundleRequest;
import org.sagebionetworks.repo.model.table.QueryResultBundle;
import org.sagebionetworks.repo.model.table.SelectColumn;
import org.sagebionetworks.repo.model.table.SortItem;
import org.sagebionetworks.web.client.GWTWrapper;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.cache.ClientCache;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.asynch.AsynchronousProgressHandler;
import org.sagebionetworks.web.client.widget.asynch.AsynchronousProgressWidget;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.client.widget.table.modal.fileview.TableType;
import org.sagebionetworks.web.shared.asynch.AsynchType;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

/**
 * This widget will execute a table query and show the resulting query results in a paginated view..
 * 
 * @author jmhill
 *
 */
public class TableQueryResultWidget implements TableQueryResultView.Presenter, IsWidget, PagingAndSortingListener {
	public static final int ETAG_CHECK_DELAY_MS = 5000;
	public static final String VERIFYING_ETAG_MESSAGE = "Verifying that the recent changes have propagated through the system...";
	public static final String RUNNING_QUERY_MESSAGE = "Running query...";
	public static final String QUERY_CANCELED = "Query canceled";
	/**
	 * Masks for requesting what should be included in the query bundle.
	 */
	public static final long BUNDLE_MASK_QUERY_RESULTS = 0x1;
	public static final long BUNDLE_MASK_QUERY_COUNT = 0x2;
	public static final long BUNDLE_MASK_QUERY_SELECT_COLUMNS = 0x4;
	public static final long BUNDLE_MASK_QUERY_MAX_ROWS_PER_PAGE = 0x8;
	public static final long BUNDLE_MASK_QUERY_COLUMN_MODELS = 0x10;
	public static final long BUNDLE_MASK_QUERY_FACETS = 0x20;

	private static final Long ALL_PARTS_MASK = new Long(255);
	SynapseClientAsync synapseClient;
	TableQueryResultView view;
	PortalGinInjector ginInjector;
	QueryResultBundle bundle;
	TablePageWidget pageViewerWidget;
	QueryResultEditorWidget queryResultEditor;
	Query startingQuery;
	boolean isEditable;
	TableType tableType;
	QueryResultsListener queryListener;
	SynapseAlert synapseAlert;
	CallbackP<FacetColumnRequest> facetChangedHandler;
	Callback resetFacetsHandler;
	ClientCache clientCache;
	GWTWrapper gwt;
	int currentJobIndex = 0;
	QueryResultBundle cachedFullQueryResultBundle = null;
	boolean facetsVisible = true;
	public static final Map<String, List<SortItem>> SQL_2_SORT_ITEMS_CACHE = new HashMap<>();
	
	@Inject
	public TableQueryResultWidget(TableQueryResultView view, 
			SynapseClientAsync synapseClient, 
			PortalGinInjector ginInjector, 
			SynapseAlert synapseAlert,
			ClientCache clientCache,
			GWTWrapper gwt) {
		this.synapseClient = synapseClient;
		fixServiceEntryPoint(synapseClient);
		this.view = view;
		this.ginInjector = ginInjector;
		this.pageViewerWidget = ginInjector.createNewTablePageWidget();
		this.synapseAlert = synapseAlert;
		this.clientCache = clientCache;
		this.gwt = gwt;
		this.view.setPageWidget(this.pageViewerWidget);
		this.view.setPresenter(this);
		this.view.setSynapseAlertWidget(synapseAlert.asWidget());
		resetFacetsHandler = new Callback() {
			@Override
			public void invoke() {
				startingQuery.setSelectedFacets(null);
				cachedFullQueryResultBundle = null;
				startingQuery.setOffset(0L);
				runQuery();
			}
		};
		facetChangedHandler = new CallbackP<FacetColumnRequest>() {
			@Override
			public void invoke(FacetColumnRequest request) {
				List<FacetColumnRequest> selectedFacets = startingQuery.getSelectedFacets();
				if (selectedFacets == null) {
					selectedFacets = new ArrayList<FacetColumnRequest>();
					startingQuery.setSelectedFacets(selectedFacets);
				}
				for (FacetColumnRequest facetColumnRequest : selectedFacets) {
					if (facetColumnRequest.getColumnName().equals(request.getColumnName())) {
						selectedFacets.remove(facetColumnRequest);
						break;
					}
				}
				selectedFacets.add(request);
				cachedFullQueryResultBundle = null;
				startingQuery.setOffset(0L);
				runQuery();
			}
		};
	}
	
	/**
	 * Configure this widget with a query string.
	 * @param queryString
	 * @param isEditable Is the user allowed to edit the query results?
	 * @param is table a file view?
	 * @param listener Listener for query start and finish events.
	 */
	public void configure(Query query, boolean isEditable, TableType tableType, QueryResultsListener listener){
		this.isEditable = isEditable;
		this.tableType = tableType;
		this.startingQuery = query;
		this.queryListener = listener;
		cachedFullQueryResultBundle = null;
		runQuery();
	}
	
	private void runQuery() {
		currentJobIndex++;
		runQuery(currentJobIndex);
	}
	
	private void runQuery(final int jobIndex) {
		this.view.setErrorVisible(false);
		fireStartEvent();
		pageViewerWidget.setTableVisible(false);
		this.view.setProgressWidgetVisible(true);
		String entityId = QueryBundleUtils.getTableId(this.startingQuery);
		String viewEtag = clientCache.get(entityId + QueryResultEditorWidget.VIEW_RECENTLY_CHANGED_KEY);
		if (viewEtag == null) {
			// run the job
			QueryBundleRequest qbr = new QueryBundleRequest();
			long partMask = BUNDLE_MASK_QUERY_RESULTS;
			// do not ask for query count
			if (cachedFullQueryResultBundle == null) {
				partMask = partMask | BUNDLE_MASK_QUERY_COLUMN_MODELS | BUNDLE_MASK_QUERY_SELECT_COLUMNS;
				if (facetsVisible) {
					partMask = partMask | BUNDLE_MASK_QUERY_FACETS;
				}
			} else {
				// we can release the old query result
				cachedFullQueryResultBundle.setQueryResult(null);
			}
			qbr.setPartMask(partMask);
			qbr.setQuery(this.startingQuery);
			qbr.setEntityId(entityId);
			AsynchronousProgressWidget progressWidget = ginInjector.creatNewAsynchronousProgressWidget();
			this.view.setProgressWidget(progressWidget);
			progressWidget.startAndTrackJob(RUNNING_QUERY_MESSAGE, false, AsynchType.TableQuery, qbr, new AsynchronousProgressHandler() {
				
				@Override
				public void onFailure(Throwable failure) {
					if (currentJobIndex == jobIndex) {
						showError(failure);	
					}
				}
				
				@Override
				public void onComplete(AsynchronousResponseBody response) {
					if (currentJobIndex == jobIndex) {
						setQueryResults((QueryResultBundle) response);
					}
				}
				
				@Override
				public void onCancel() {
					if (currentJobIndex == jobIndex) {
						showError(QUERY_CANCELED);
					}
				}
			});
		} else {
			verifyOldEtagIsNotInView(entityId, viewEtag);
		}
	}
	/**
	 * Look for the given etag in the given file view.  If it is still there, wait a few seconds and try again.  
	 * If the etag is not in the view, then remove the clientCache key and run the query (since this indicates that the user change was propagated to the replicated layer)
	 * @param fileViewEntityId
	 * @param oldEtag
	 */
	public void verifyOldEtagIsNotInView(final String fileViewEntityId, String oldEtag) {
		//check to see if etag exists in view
		QueryBundleRequest qbr = new QueryBundleRequest();
		qbr.setPartMask(ALL_PARTS_MASK);
		Query query = new Query();
		query.setSql("select * from " + fileViewEntityId + " where ROW_ETAG='"+oldEtag+"'");
		query.setOffset(DEFAULT_OFFSET);
		query.setLimit(DEFAULT_LIMIT);
		query.setIsConsistent(true);
		qbr.setQuery(query);
		qbr.setEntityId(fileViewEntityId);
		AsynchronousProgressWidget progressWidget = ginInjector.creatNewAsynchronousProgressWidget();
		this.view.setProgressWidget(progressWidget);
		progressWidget.startAndTrackJob(VERIFYING_ETAG_MESSAGE, false, AsynchType.TableQuery, qbr, new AsynchronousProgressHandler() {
			@Override
			public void onFailure(Throwable failure) {
				showError(failure);
			}
			
			@Override
			public void onComplete(AsynchronousResponseBody response) {
				QueryResultBundle resultBundle = (QueryResultBundle) response;
				if (resultBundle.getQueryCount() > 0) {
					// retry after waiting a few seconds
					gwt.scheduleExecution(new Callback() {
						@Override
						public void invoke() {
							runQuery();
						}
					}, ETAG_CHECK_DELAY_MS);
				} else {
					// clear cache value and run the actual query
					clientCache.remove(fileViewEntityId + QueryResultEditorWidget.VIEW_RECENTLY_CHANGED_KEY);
					runQuery();
				}
			}
			
			@Override
			public void onCancel() {
				showError(QUERY_CANCELED);
			}
		});
	}
	/**
	 * Called after a successful query.
	 * @param bundle
	 */
	private void setQueryResults(final QueryResultBundle bundle){
		if (cachedFullQueryResultBundle != null) {
			bundle.setColumnModels(cachedFullQueryResultBundle.getColumnModels());
			bundle.setFacets(cachedFullQueryResultBundle.getFacets());
			bundle.setSelectColumns(cachedFullQueryResultBundle.getSelectColumns());
		} else {
			cachedFullQueryResultBundle = bundle;
		}
		
		if (SQL_2_SORT_ITEMS_CACHE.containsKey(this.startingQuery.getSql())) {
			setQueryResultsAndSort(bundle, SQL_2_SORT_ITEMS_CACHE.get(this.startingQuery.getSql()));
		} else  {
			// Get the sort info
			this.synapseClient.getSortFromTableQuery(this.startingQuery.getSql(), new AsyncCallback<List<SortItem>>() {
				
				@Override
				public void onSuccess(List<SortItem> sortItems) {
					SQL_2_SORT_ITEMS_CACHE.put(startingQuery.getSql(), sortItems);
					setQueryResultsAndSort(bundle, sortItems);
				}
				
				@Override
				public void onFailure(Throwable caught) {
					showError(caught);
				}
			});
		}
	}
	
	private void setQueryResultsAndSort(QueryResultBundle bundle, List<SortItem> sortItems){
		this.bundle = bundle;
		this.view.setErrorVisible(false);
		this.view.setProgressWidgetVisible(false);
		// configure the page widget
		this.pageViewerWidget.configure(bundle, this.startingQuery, sortItems, false, tableType, null, this, facetChangedHandler, resetFacetsHandler);
		pageViewerWidget.setTableVisible(true);
		fireFinishEvent(true, isQueryResultEditable());
	}

	/**
	 * The results are editable if all of the select columns have ID
	 * @return
	 */
	public boolean isQueryResultEditable(){
		List<SelectColumn> selectColums = QueryBundleUtils.getSelectFromBundle(this.bundle);
		if(selectColums == null){
			return false;
		}
		// Do all columns have IDs?
		for(SelectColumn col: selectColums){
			if(col.getId() == null){
				return false;
			}
		}
		// All of the columns have ID so we can edit
		return true;
	}
	
	/**
	 * Starting a query.
	 */
	private void fireStartEvent() {
		if(this.queryListener != null){
			this.queryListener.queryExecutionStarted();
		}
	}
	
	/**
	 * Finished a query.
	 */
	private void fireFinishEvent(boolean wasSuccessful, boolean resultsEditable) {
		if(this.queryListener != null){
			this.queryListener.queryExecutionFinished(wasSuccessful, resultsEditable);
		}
	}
	
	/**
	 * Show an error.
	 * @param caught
	 */
	private void showError(Throwable caught){
		setupErrorState();
		synapseAlert.handleException(caught);
	}
	
	/**
	 * Show an error message.
	 * @param message
	 */
	private void showError(String message){
		setupErrorState();
		synapseAlert.showError(message);
	}
	
	private void setupErrorState() {
		pageViewerWidget.setTableVisible(false);
		this.view.setProgressWidgetVisible(false);
		fireFinishEvent(false, false);
		this.view.setErrorVisible(true);
	}

	@Override
	public Widget asWidget() {
		return view.asWidget();
	}

	@Override
	public void onEditRows() {
		if(this.queryResultEditor == null){
			this.queryResultEditor = ginInjector.createNewQueryResultEditorWidget();
			view.setEditorWidget(this.queryResultEditor);
		}
		this.queryResultEditor.showEditor(bundle, tableType, new Callback() {
			@Override
			public void invoke() {
				cachedFullQueryResultBundle = null;
				runQuery();
			}
		});
	}

	@Override
	public void onPageChange(Long newOffset) {
		this.startingQuery.setOffset(newOffset);
		queryChanging();
	}
	
	private void runSql(String sql){
		startingQuery.setSql(sql);
		startingQuery.setOffset(0L);
		queryChanging();
	}

	private void queryChanging() {
		if(this.queryListener != null){
			this.queryListener.onStartingNewQuery(this.startingQuery);
		}
		view.scrollTableIntoView();
		runQuery();
	}
	
	public Query getStartingQuery(){
		return this.startingQuery;
	}

	@Override
	public void onToggleSort(String header) {
		// This call will generate a new SQL string with the requested column toggled.
		synapseClient.toggleSortOnTableQuery(this.startingQuery.getSql(), header, new AsyncCallback<String>(){
			@Override
			public void onFailure(Throwable caught) {
				showError(caught);
			}

			@Override
			public void onSuccess(String sql) {
				runSql(sql);
			}});
	}
	
	public void setFacetsVisible(boolean visible) {
		facetsVisible = visible;
		pageViewerWidget.setFacetsVisible(visible);
	}
	
}
