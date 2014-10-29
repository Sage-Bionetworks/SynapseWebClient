package org.sagebionetworks.web.client.widget.table.v2.results;

import org.sagebionetworks.repo.model.asynch.AsynchronousResponseBody;
import org.sagebionetworks.repo.model.table.PartialRowSet;
import org.sagebionetworks.repo.model.table.Query;
import org.sagebionetworks.repo.model.table.QueryBundleRequest;
import org.sagebionetworks.repo.model.table.QueryResultBundle;
import org.sagebionetworks.schema.adapter.AdapterFactory;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.widget.asynch.AsynchronousProgressHandler;
import org.sagebionetworks.web.client.widget.asynch.JobTrackingWidget;
import org.sagebionetworks.web.client.widget.pagination.PageChangeListener;
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
public class TableQueryResultWidget implements TableQueryResultView.Presenter, IsWidget, PageChangeListener {
	public static final String QUERY_CANCELED = "Query canceled";
	// Mask to get all parts of a query.
	private static final Long ALL_PARTS_MASK = new Long(255);
	SynapseClientAsync synapseClient;
	AdapterFactory adapterFactory;
	TableQueryResultView view;
	PortalGinInjector ginInjector;
	QueryResultBundle bundle;
	TablePageWidget pageViewerWidget;
	QueryResultEditorWidget queryResultEditor;
	Query startingQuery;
	boolean isEditable;
	QueryResultsListner queryListener;
	JobTrackingWidget progressWidget;
	
	@Inject
	public TableQueryResultWidget(TableQueryResultView view, SynapseClientAsync synapseClient, PortalGinInjector ginInjector, AdapterFactory adapterFactory){
		this.synapseClient = synapseClient;
		this.view = view;
		this.ginInjector = ginInjector;
		this.pageViewerWidget = ginInjector.createNewTablePageWidget();
		this.progressWidget = ginInjector.creatNewAsynchronousProgressWidget();
		this.adapterFactory = adapterFactory;
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
	public void configure(Query query, boolean isEditable, QueryResultsListner listener){
		this.isEditable = isEditable;
		this.startingQuery = query;
		this.queryListener = listener;
		runQuery();
	}

	private void runQuery() {
		this.view.hideEditor();
		this.view.setErrorVisible(false);
		fireStartEvent();
		this.view.setTableVisible(false);
		this.view.setProgressWidgetVisible(true);
		// run the job
		QueryBundleRequest qbr = new QueryBundleRequest();
		qbr.setPartMask(ALL_PARTS_MASK);
		qbr.setQuery(this.startingQuery);
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
	private void setQueryResults(QueryResultBundle bundle){
		this.bundle = bundle;
		this.view.setErrorVisible(false);
		this.view.setProgressWidgetVisible(false);
		// configure the page widget
		this.pageViewerWidget.configure(bundle, this.startingQuery, false, null, this);
		this.view.setTableVisible(true);
		fireFinishEvent(true);
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
	private void fireFinishEvent(boolean wasSuccessful) {
		if(this.queryListener != null){
			this.queryListener.queryExecutionFinished(wasSuccessful);
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
		fireFinishEvent(false);
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
		this.view.setSaveButtonLoading(false);
		this.queryResultEditor.configure(this.bundle);
		view.showEditor();
	}

	@Override
	public void onSave() {
		view.setSaveButtonLoading(true);
		// Extract the delta
		PartialRowSet prs = this.queryResultEditor.extractDelta();
		synapseClient.applyTableDelta(prs, new AsyncCallback<Void>() {
			
			@Override
			public void onSuccess(Void result) {
				// If the save was success full then re-run the query.
				runQuery();
			}
			
			@Override
			public void onFailure(Throwable caught) {
				showEditError(caught.getMessage());
			}
		});
	}
	
	private void showEditError(String message){
		view.setSaveButtonLoading(false);
		queryResultEditor.showError(message);
	}

	@Override
	public void onPageChange(Long newOffset) {
		if(this.queryListener != null){
			this.queryListener.onPageChange(newOffset);
		}
	}
	
	public Query getStartingQuery(){
		return this.startingQuery;
	}
}
