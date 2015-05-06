package org.sagebionetworks.web.client.widget.table.v2.results;

import java.util.List;

import org.sagebionetworks.repo.model.asynch.AsynchronousResponseBody;
import org.sagebionetworks.repo.model.table.Query;
import org.sagebionetworks.repo.model.table.QueryBundleRequest;
import org.sagebionetworks.repo.model.table.QueryResultBundle;
import org.sagebionetworks.repo.model.table.SelectColumn;
import org.sagebionetworks.repo.model.table.SortItem;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.asynch.AsynchronousProgressHandler;
import org.sagebionetworks.web.client.widget.asynch.JobTrackingWidget;
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
	
	public static final String QUERY_CANCELED = "Query canceled";
	// Mask to get all parts of a query.
	private static final Long ALL_PARTS_MASK = new Long(255);
	SynapseClientAsync synapseClient;
	TableQueryResultView view;
	PortalGinInjector ginInjector;
	QueryResultBundle bundle;
	TablePageWidget pageViewerWidget;
	QueryResultEditorWidget queryResultEditor;
	Query startingQuery;
	boolean isEditable;
	QueryResultsListener queryListener;
	JobTrackingWidget progressWidget;
	
	@Inject
	public TableQueryResultWidget(TableQueryResultView view, SynapseClientAsync synapseClient, PortalGinInjector ginInjector){
		this.synapseClient = synapseClient;
		this.view = view;
		this.ginInjector = ginInjector;
		this.pageViewerWidget = ginInjector.createNewTablePageWidget();
		this.progressWidget = ginInjector.creatNewAsynchronousProgressWidget();
		this.view.setPageWidget(this.pageViewerWidget);
		this.view.setPresenter(this);
		this.view.setProgressWidget(this.progressWidget);
	}
	
	/**
	 * Configure this widget with a query string.
	 * @param queryString
	 * @param isEditable Is the user allowed to edit the query results?
	 * @param listener Listener for query start and finish events.
	 */
	public void configure(Query query, boolean isEditable, QueryResultsListener listener){
		this.isEditable = isEditable;
		this.startingQuery = query;
		this.queryListener = listener;
		runQuery();
	}

	private void runQuery() {
		this.view.setErrorVisible(false);
		fireStartEvent();
		this.view.setTableVisible(false);
		this.view.setProgressWidgetVisible(true);
		// run the job
		QueryBundleRequest qbr = new QueryBundleRequest();
		qbr.setPartMask(ALL_PARTS_MASK);
		qbr.setQuery(this.startingQuery);
		qbr.setEntityId(QueryBundleUtils.getTableId(this.startingQuery));
		this.progressWidget.startAndTrackJob("Running query...", false, AsynchType.TableQuery, qbr, new AsynchronousProgressHandler() {
			
			@Override
			public void onFailure(Throwable failure) {
				showError(failure);
			}
			
			@Override
			public void onComplete(AsynchronousResponseBody response) {
				setQueryResults((QueryResultBundle) response);
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
		// Get the sort info
		this.synapseClient.getSortFromTableQuery(this.startingQuery.getSql(), new AsyncCallback<List<SortItem>>() {
			
			@Override
			public void onSuccess(List<SortItem> sortItems) {
				setQueryResultsAndSort(bundle, sortItems);
			}
			
			@Override
			public void onFailure(Throwable caught) {
				showError(caught);
			}
		});

	}
	
	private void setQueryResultsAndSort(QueryResultBundle bundle, List<SortItem> sortItems){
		this.bundle = bundle;
		this.view.setErrorVisible(false);
		this.view.setProgressWidgetVisible(false);
		SortItem sort = null;
		if(sortItems != null && !sortItems.isEmpty()){
			sort = sortItems.get(0);
		}
		// configure the page widget
		this.pageViewerWidget.configure(bundle, this.startingQuery,sort, false, null, this);
		this.view.setTableVisible(true);
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
		String message = caught.getMessage();
		showError(message);
	}
	
	/**
	 * Show an error message.
	 * @param message
	 */
	private void showError(String message){
		this.view.setTableVisible(false);
		this.view.showError(message);
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
		this.queryResultEditor.showEditor(bundle, new Callback() {
			@Override
			public void invoke() {
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
	
}
