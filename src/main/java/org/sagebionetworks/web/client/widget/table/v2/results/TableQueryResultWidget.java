package org.sagebionetworks.web.client.widget.table.v2.results;

import org.sagebionetworks.repo.model.table.PartialRowSet;
import org.sagebionetworks.repo.model.table.QueryResultBundle;
import org.sagebionetworks.repo.model.table.TableStatus;
import org.sagebionetworks.schema.adapter.AdapterFactory;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.SynapseClientAsync;
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
	QueryResultListener queryListner;
	
	@Inject
	public TableQueryResultWidget(TableQueryResultView view, SynapseClientAsync synapseClient, PortalGinInjector ginInjector, AdapterFactory adapterFactory){
		this.synapseClient = synapseClient;
		this.view = view;
		this.ginInjector = ginInjector;
		this.pageViewerWidget = ginInjector.createNewTablePageWidget();
		this.adapterFactory = adapterFactory;
		this.view.setPageWidget(this.pageViewerWidget);
		this.view.setPresenter(this);
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
		this.queryListner = listener;
		runQuery();
	}

	private void runQuery() {
		this.view.hideEditor();
		this.view.setErrorVisible(false);
		this.view.setToolbarVisible(false);
		fireStartEvent();
		// Run the query
		this.synapseClient.queryTable(this.startingQueryString, new AsyncCallback<String>() {
			@Override
			public void onSuccess(String json) {
				try {
					QueryResultBundle bundle = new QueryResultBundle(adapterFactory.createNew(json));
					setQueryResults(bundle);
				} catch (JSONObjectAdapterException e) {
					showError(e);
				}
			}
			@Override
			public void onFailure(Throwable caught) {
				showError(caught);
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
		if(this.queryListner != null){
			this.queryListner.queryExecutionStarted();
		}
	}
	
	/**
	 * Finished a query.
	 */
	private void fireFinishEvent() {
		if(this.queryListner != null){
			this.queryListner.queryExecutionFinished();
		}
	}
	
	/**
	 * Show an error.
	 * @param caught
	 */
	private void showError(Throwable caught){
		String message = caught.getMessage();
		if(caught instanceof TableUnavilableException){
			try {
				TableStatus status = getTableStatus((TableUnavilableException) caught);
				message = "Table status: "+status.getState().name();
			} catch (JSONObjectAdapterException e) {
				message = e.getMessage();
			}
		}
		this.view.setTableVisible(false);
		this.view.showError(message);
		this.view.setErrorVisible(true);
		fireFinishEvent();
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
			PartialRowSet prs = this.queryResultEditor.extractDeleta();
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
