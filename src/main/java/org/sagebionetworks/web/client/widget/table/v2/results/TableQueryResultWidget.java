package org.sagebionetworks.web.client.widget.table.v2.results;

import org.sagebionetworks.repo.model.table.QueryResultBundle;
import org.sagebionetworks.schema.adapter.AdapterFactory;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.widget.table.v2.schema.ColumnModelUtils;

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
		this.bundle = ColumnModelUtils.preProcessResutls(bundle);
		this.view.setErrorVisible(false);
		// configure the page widget
		this.pageViewerWidget.configure(bundle, false);
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
}
