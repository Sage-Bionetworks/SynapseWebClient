package org.sagebionetworks.web.client.widget.table.v2.results;

import static org.sagebionetworks.web.client.ServiceEntryPointUtils.fixServiceEntryPoint;
import static org.sagebionetworks.web.client.widget.table.v2.TableEntityWidget.DEFAULT_LIMIT;
import static org.sagebionetworks.web.client.widget.table.v2.TableEntityWidget.DEFAULT_OFFSET;
import java.util.ArrayList;
import java.util.List;
import org.sagebionetworks.repo.model.ErrorResponseCode;
import org.sagebionetworks.repo.model.asynch.AsynchronousResponseBody;
import org.sagebionetworks.repo.model.table.FacetColumnRequest;
import org.sagebionetworks.repo.model.table.Query;
import org.sagebionetworks.repo.model.table.QueryBundleRequest;
import org.sagebionetworks.repo.model.table.QueryResult;
import org.sagebionetworks.repo.model.table.QueryResultBundle;
import org.sagebionetworks.repo.model.table.Row;
import org.sagebionetworks.repo.model.table.RowSet;
import org.sagebionetworks.repo.model.table.SelectColumn;
import org.sagebionetworks.repo.model.table.SortDirection;
import org.sagebionetworks.repo.model.table.SortItem;
import org.sagebionetworks.web.client.GWTWrapper;
import org.sagebionetworks.web.client.PopupUtilsView;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.cache.ClientCache;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.asynch.AsynchronousProgressHandler;
import org.sagebionetworks.web.client.widget.asynch.AsynchronousProgressWidget;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.client.widget.table.modal.fileview.TableType;
import org.sagebionetworks.web.client.widget.table.v2.results.facets.FacetsWidget;
import org.sagebionetworks.web.shared.asynch.AsynchType;
import org.sagebionetworks.web.shared.exceptions.BadRequestException;
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
	public static final String SCHEMA_CHANGED_MESSAGE = "The underlying Table/View schema has been changed so this query must be reset.";
	public static final String FACET_COLUMNS_CHANGED_MESSAGE = "requested facet column names must all be in the set";
	public static final int ETAG_CHECK_DELAY_MS = 5000;
	public static final String VERIFYING_ETAG_MESSAGE = "Verifying that the recent changes have propagated through the system...";
	public static final String RUNNING_QUERY_MESSAGE = ""; // while running, just show loading spinner (and cancel)
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
	public static final long BUNDLE_MASK_QUERY_SUM_FILE_SIZES = 0x40;


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
	FacetsWidget facetsWidget;
	boolean facetsRequireRefresh;
	PopupUtilsView popupUtils;

	@Inject
	public TableQueryResultWidget(TableQueryResultView view, SynapseClientAsync synapseClient, PortalGinInjector ginInjector, SynapseAlert synapseAlert, ClientCache clientCache, GWTWrapper gwt, FacetsWidget facetsWidget, PopupUtilsView popupUtils) {
		this.synapseClient = synapseClient;
		fixServiceEntryPoint(synapseClient);
		this.view = view;
		this.ginInjector = ginInjector;
		this.pageViewerWidget = ginInjector.createNewTablePageWidget();
		this.synapseAlert = synapseAlert;
		this.clientCache = clientCache;
		this.gwt = gwt;
		this.facetsWidget = facetsWidget;
		this.popupUtils = popupUtils;
		view.setFacetsWidget(facetsWidget);
		this.view.setPageWidget(this.pageViewerWidget);
		this.view.setPresenter(this);
		this.view.setSynapseAlertWidget(synapseAlert.asWidget());
		resetFacetsHandler = new Callback() {
			@Override
			public void invoke() {
				startingQuery.setSelectedFacets(null);
				cachedFullQueryResultBundle = null;
				startingQuery.setOffset(0L);
				queryChanging();
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
				facetsRequireRefresh = true;
				queryChanging();
			}
		};
	}

	/**
	 * Configure this widget with a query string.
	 * 
	 * @param queryString
	 * @param isEditable Is the user allowed to edit the query results?
	 * @param is table a file view?
	 * @param listener Listener for query start and finish events.
	 */
	public void configure(Query query, boolean isEditable, TableType tableType, QueryResultsListener listener) {
		facetsRequireRefresh = true;
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
			if (facetsRequireRefresh) {
				// no need to update facets if it's just a page change or sort
				facetsWidget.configure(startingQuery, facetChangedHandler, resetFacetsHandler);
			} else {
				// facet refresh unnecessary for this query execution, but reset to true for next time.
				facetsRequireRefresh = true;
			}

			// run the job
			QueryBundleRequest qbr = new QueryBundleRequest();
			long partMask = BUNDLE_MASK_QUERY_RESULTS;
			// do not ask for query count
			if (cachedFullQueryResultBundle == null) {
				partMask = partMask | BUNDLE_MASK_QUERY_COLUMN_MODELS | BUNDLE_MASK_QUERY_SELECT_COLUMNS;
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
						if (!startingQuery.getIsConsistent()) {
							retryConsistentQuery(failure.getMessage());
						} else {
							showError(failure);
						}
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

	public void retryConsistentQuery(String message) {
		if (!startingQuery.getIsConsistent()) {
			// log, but try again with isConsistent = true.
			synapseAlert.consoleError("Unexpected results when isConsistent=false, retrying with isConsistent=true.  " + message);
			startingQuery.setIsConsistent(true);
			runQuery(currentJobIndex);
		}
	}

	/**
	 * Look for the given etag in the given file view. If it is still there, wait a few seconds and try
	 * again. If the etag is not in the view, then remove the clientCache key and run the query (since
	 * this indicates that the user change was propagated to the replicated layer)
	 * 
	 * @param fileViewEntityId
	 * @param oldEtag
	 */
	public void verifyOldEtagIsNotInView(final String fileViewEntityId, String oldEtag) {
		// check to see if etag exists in view
		QueryBundleRequest qbr = new QueryBundleRequest();
		qbr.setPartMask(ALL_PARTS_MASK);
		Query query = new Query();
		query.setSql("select * from " + fileViewEntityId + " where ROW_ETAG='" + oldEtag + "'");
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
	 * 
	 * @param bundle
	 */
	private void setQueryResults(final QueryResultBundle bundle) {
		QueryResult result = bundle.getQueryResult();
		RowSet rowSet = result.getQueryResults();
		List<Row> rows = rowSet.getRows();

		if (!startingQuery.getIsConsistent() && rows.isEmpty()) {
			retryConsistentQuery("No rows returned.");
			return;
		}
		if (cachedFullQueryResultBundle != null) {
			bundle.setColumnModels(cachedFullQueryResultBundle.getColumnModels());
			bundle.setFacets(cachedFullQueryResultBundle.getFacets());
			bundle.setSelectColumns(cachedFullQueryResultBundle.getSelectColumns());
		} else {
			cachedFullQueryResultBundle = bundle;
		}

		setQueryResultsAndSort(bundle, startingQuery.getSort());
	}

	private void setQueryResultsAndSort(QueryResultBundle bundle, List<SortItem> sortItems) {
		this.bundle = bundle;
		this.view.setErrorVisible(false);
		this.view.setProgressWidgetVisible(false);
		// configure the page widget
		this.pageViewerWidget.configure(bundle, this.startingQuery, sortItems, false, tableType, null, this, facetChangedHandler);
		pageViewerWidget.setTableVisible(true);
		fireFinishEvent(true, isQueryResultEditable());
	}

	/**
	 * The results are editable if all of the select columns have ID
	 * 
	 * @return
	 */
	public boolean isQueryResultEditable() {
		List<SelectColumn> selectColums = QueryBundleUtils.getSelectFromBundle(this.bundle);
		if (selectColums == null) {
			return false;
		}
		// Do all columns have IDs?
		for (SelectColumn col : selectColums) {
			if (col.getId() == null) {
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
		if (this.queryListener != null) {
			this.queryListener.queryExecutionStarted();
		}
	}

	/**
	 * Finished a query.
	 */
	private void fireFinishEvent(boolean wasSuccessful, boolean resultsEditable) {
		if (this.queryListener != null) {
			this.queryListener.queryExecutionFinished(wasSuccessful, resultsEditable);
		}
	}

	/**
	 * Show an error.
	 * 
	 * @param caught
	 */
	private void showError(Throwable caught) {
		setupErrorState();
		// due to invalid column set? (see PLFM-5491)
		if (caught instanceof BadRequestException && ErrorResponseCode.INVALID_TABLE_QUERY_FACET_COLUMN_REQUEST.equals(((BadRequestException) caught).getErrorResponseCode())) {
			popupUtils.showErrorMessage(SCHEMA_CHANGED_MESSAGE);
			resetFacetsHandler.invoke();
		} else {
			synapseAlert.handleException(caught);
		}
	}

	/**
	 * Show an error message.
	 * 
	 * @param message
	 */
	private void showError(String message) {
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
		if (this.queryResultEditor == null) {
			this.queryResultEditor = ginInjector.createNewQueryResultEditorWidget();
			view.setEditorWidget(this.queryResultEditor);
		}
		this.queryResultEditor.showEditor(bundle, tableType);
	}

	@Override
	public void onPageChange(Long newOffset) {
		facetsRequireRefresh = false;
		this.startingQuery.setOffset(newOffset);
		queryChanging();
	}

	private void queryChanging() {
		if (this.queryListener != null) {
			this.queryListener.onStartingNewQuery(this.startingQuery);
		}
		view.scrollTableIntoView();
		runQuery();
	}

	public Query getStartingQuery() {
		return this.startingQuery;
	}

	@Override
	public void onToggleSort(String header) {
		facetsRequireRefresh = false;
		SortItem targetSortItem = null;
		List<SortItem> sortItems = startingQuery.getSort();
		if (sortItems == null) {
			sortItems = new ArrayList<>();
			startingQuery.setSort(sortItems);
		}
		for (SortItem sortItem : sortItems) {
			if (header.equals(sortItem.getColumn())) {
				targetSortItem = sortItem;
				break;
			}
		}
		// transition through UNSORTED (not in sort list) -> DESC -> ASC -> UNSORTED (remove from sort list)
		if (targetSortItem == null) {
			// new sort, set to default
			targetSortItem = new SortItem();
			targetSortItem.setColumn(header);
			targetSortItem.setDirection(SortDirection.DESC);
			sortItems.add(targetSortItem);
		} else if (SortDirection.DESC.equals(targetSortItem.getDirection())) {
			targetSortItem.setDirection(SortDirection.ASC);
		} else {
			sortItems.remove(targetSortItem);
		}

		// reset offset and run the new query
		startingQuery.setOffset(0L);
		queryChanging();
	}

	public void setFacetsVisible(boolean visible) {
		view.setFacetsVisible(visible);
	}

}
