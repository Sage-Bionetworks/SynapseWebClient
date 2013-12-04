package org.sagebionetworks.web.client.widget.table;

import java.util.Map;

import org.sagebionetworks.schema.adapter.AdapterFactory;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.transform.JsoProvider;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.WidgetRendererPresenter;
import org.sagebionetworks.web.shared.TableObject;
import org.sagebionetworks.web.shared.WikiPageKey;

import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class SynapseTableWidget implements SynapseTableWidgetView.Presenter, WidgetRendererPresenter {
	
	private SynapseTableWidgetView view;
	private SynapseClientAsync synapseClient;
	private AuthenticationController authenticationController;
	private AdapterFactory adapterFactory;
	
	private TableObject table;
	
	@Inject
	public SynapseTableWidget(SynapseTableWidgetView view, 
			SynapseClientAsync synapseClient,
			AuthenticationController authenticationController, 
			AdapterFactory adapterFactory) {
		this.view = view;
		this.synapseClient = synapseClient;
		this.authenticationController = authenticationController;
		this.adapterFactory = adapterFactory;
		view.setPresenter(this);
	}	
	
	public void configure(TableObject table) {
		this.table = table;
		String queryString = "select *";
		view.configure(table, queryString);
	}
	
	@Override
	public void configure(WikiPageKey wikiKey, Map<String, String> widgetDescriptor, Callback widgetRefreshRequired) {
		view.setPresenter(this);
	}

    
	@Override
	public Widget asWidget() {
		view.setPresenter(this);
		return view.asWidget();		
	}

	@Override
	public void query(String query) {
		// TODO Auto-generated method stub
		
	}
	
}
