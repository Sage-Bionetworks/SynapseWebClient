package org.sagebionetworks.web.client.widget.footer;

import org.sagebionetworks.web.client.GlobalApplicationState;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class Footer implements FooterView.Presenter {

	private FooterView view;
	GlobalApplicationState globalAppState;
	private boolean isInitialized = false;
	
	@Inject
	public Footer(FooterView view, GlobalApplicationState globalAppState) {
		this.view = view;
		this.globalAppState = globalAppState;
		view.setPresenter(this);
	}

	public Widget asWidget() {
		view.setPresenter(this);
		if (!isInitialized) {
			isInitialized = true; 
			globalAppState.checkVersionCompatibility(new AsyncCallback<VersionState>() {
				@Override
				public void onSuccess(VersionState state) {
					String versions = state.getVersion();
					String[] vals = versions.split(",");
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
		}
		return view.asWidget();
	}
}
