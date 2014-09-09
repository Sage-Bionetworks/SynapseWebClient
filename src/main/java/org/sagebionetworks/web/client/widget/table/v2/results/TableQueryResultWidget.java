package org.sagebionetworks.web.client.widget.table.v2.results;

import org.sagebionetworks.repo.model.asynch.AsynchronousResponseBody;
import org.sagebionetworks.repo.model.table.PartialRowSet;
import org.sagebionetworks.repo.model.table.Query;
import org.sagebionetworks.repo.model.table.QueryBundleRequest;
import org.sagebionetworks.repo.model.table.QueryResultBundle;
import org.sagebionetworks.repo.model.table.TableStatus;
import org.sagebionetworks.schema.adapter.AdapterFactory;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.widget.asynch.AsynchronousProgressHandler;
import org.sagebionetworks.web.client.widget.asynch.AsynchronousProgressWidget;
import org.sagebionetworks.web.shared.asynch.AsynchType;
import org.sagebionetworks.web.shared.exceptions.TableUnavilableException;

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
public class TableQueryResultWidget implements TableQueryResultView.Presenter, IsWidget {
	
	SynapseClientAsync synapseClient;
	AdapterFactory adapterFactory;
	TableQueryResultView view;
	PortalGinInjector ginInjector;
	QueryResultBundle bundle;
	TablePageWidget pageViewerWidget;
	QueryResultEditorWidget queryResultEditor;
	String startingQueryString;
	boolean isEditable;
	QueryResultListener queryListener;
	AsynchronousProgressWidget progressWidget;
	
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
	public void configure(String queryString, boolean isEditable, QueryResultListener listener){
		this.isEditable = isEditable;
		this.startingQueryString = queryString;
		this.queryListener = listener;
		runQuery();
	}

	private void runQuery() {
		this.view.hideEditor();
		this.view.setErrorVisible(false);
		this.view.setToolbarVisible(false);
		fireStartEvent();
		this.view.setTableVisible(false);
		this.view.setProgressWidgetVisible(true);
		// run the job
		QueryBundleRequest qbr = new QueryBundleRequest();
		qbr.setPartMask(new Long(0x15));
		Query query = new Query();
		qbr.setQuery(query);
		query.setIsConsistent(true);
		query.setSql(this.startingQueryString);
		query.setLimit(15L);
		query.setOffset(0L);
		this.progressWidget.configure("Running query...", false, AsynchType.TableQuery, qbr, new AsynchronousProgressHandler() {
			
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
				showError("Query canceled");
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
		this.pageViewerWidget.configure(bundle, false, null);
		this.view.setTableVisible(true);
		this.view.setToolbarVisible(true);
		this.view.setEditEnabled(this.isEditable);
		fireFinishEvent();
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
	private void fireFinishEvent() {
		if(this.queryListener != null){
			this.queryListener.queryExecutionFinished();
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
		this.view.setToolbarVisible(false);
		this.view.setProgressWidgetVisible(false);
		fireFinishEvent();
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
		try {
			// Extract the delta
			PartialRowSet prs = this.queryResultEditor.extractDelta();
			String json = prs.writeToJSONObject(adapterFactory.createNew()).toJSONString();
			synapseClient.applyTableDelta(json, new AsyncCallback<Void>() {
				
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
		} catch (JSONObjectAdapterException e) {
			showEditError(e.getMessage());
		}
	}
	
	private void showEditError(String message){
		view.setSaveButtonLoading(false);
		queryResultEditor.showError(message);
	}
	
	
	private TableStatus getTableStatus(TableUnavilableException e) throws JSONObjectAdapterException{
		return new TableStatus(adapterFactory.createNew(e.getMessage()));
	}
}
