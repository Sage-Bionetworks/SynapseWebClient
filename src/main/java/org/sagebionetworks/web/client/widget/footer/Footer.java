package org.sagebionetworks.web.client.widget.footer;

import org.sagebionetworks.web.client.SynapseClientAsync;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class Footer implements FooterView.Presenter {

	private FooterView view;
	SynapseClientAsync synapseClient;
	
	@Inject
	public Footer(FooterView view, SynapseClientAsync synapseClient) {
		this.view = view;
		this.synapseClient = synapseClient;
		view.setPresenter(this);
	}

	public Widget asWidget() {
		view.setPresenter(this);
		synapseClient.getSynapseVersions(new AsyncCallback<String>() {
			
			@Override
			public void onSuccess(String result) {
				String[] vals = result.split(",");
				if(vals.length == 2)
					view.setVersion(vals[0],vals[1]);
				else 
					onFailure(null);
			}
			
			@Override
			public void onFailure(Throwable caught) {
				view.setVersion("unknown", "unknown");
			}
		});
		return view.asWidget();
	}
}
