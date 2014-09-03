package org.sagebionetworks.web.client.widget.table.v2.results;

import org.sagebionetworks.repo.model.table.PartialRowSet;
import org.sagebionetworks.repo.model.table.QueryResultBundle;
import org.sagebionetworks.schema.adapter.AdapterFactory;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.SynapseClientAsync;

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
	
	public void configure(String queryString){
		this.startingQueryString = queryString;
		// Run the query
		this.synapseClient.queryTable(queryString, new AsyncCallback<String>() {
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
	
	private void setQueryResults(QueryResultBundle bundle){
		this.bundle = bundle;
		this.view.setErrorVisible(false);
		// configure the page widget
		this.pageViewerWidget.configure(bundle, false, null);
		this.view.setTableVisible(true);
	}
	
	/**
	 * Show an error.
	 * @param caught
	 */
	private void showError(Throwable caught){
		this.view.setTableVisible(false);
		this.view.showError(caught.getMessage());
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
					configure(startingQueryString);
				}
				
				@Override
				public void onFailure(Throwable caught) {
					queryResultEditor.showError(caught.getMessage());
				}
			});
		} catch (JSONObjectAdapterException e) {
			queryResultEditor.showError(e.getMessage());
		}
	}
	
}
