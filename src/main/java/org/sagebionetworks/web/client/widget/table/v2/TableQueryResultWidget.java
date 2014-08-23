package org.sagebionetworks.web.client.widget.table.v2;

import org.sagebionetworks.repo.model.table.QueryResultBundle;
import org.sagebionetworks.schema.adapter.AdapterFactory;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.view.bootstrap.table.TBody;
import org.sagebionetworks.web.client.view.bootstrap.table.THead;
import org.sagebionetworks.web.client.view.bootstrap.table.TableHeader;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;

/**
 * This widget will execute a table query and show the resulting table.
 * 
 * @author jmhill
 *
 */
public class TableQueryResultWidget implements TableQueryResultView.Presenter {
	
	SynapseClientAsync synapseClient;
	AdapterFactory adapterFactory;
	TableQueryResultView view;
	
	@Inject
	public TableQueryResultWidget(TableQueryResultView view, SynapseClientAsync synapseClient){
		this.synapseClient = synapseClient;
		this.view = view;
		this.view.setPresenter(this);
	}
	
	public void configure(String queryString){
		// Run the query
		this.synapseClient.queryTable(queryString, new AsyncCallback<String>() {
			@Override
			public void onSuccess(String json) {
				try {
					QueryResultBundle bundle = new QueryResultBundle(adapterFactory.createNew(json));
					renderQueryResult(bundle);
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
	
	private void renderQueryResult(QueryResultBundle bundle){
		THead header = new THead();
		TBody body = new TBody();
		this.view.configureTableData(header, body);
		this.view.setTableVisible(true);
	}
	
	private void showError(Throwable caught){
		
	}
}
