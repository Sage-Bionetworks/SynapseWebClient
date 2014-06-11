package org.sagebionetworks.web.client.widget.team;

import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.utils.Callback;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class WizardProgressWidget implements WizardProgressWidgetView.Presenter {
	private WizardProgressWidgetView view;
	
	@Inject
	public WizardProgressWidget(WizardProgressWidgetView view) {
		this.view = view;
		view.setPresenter(this);
	}

	/**
	 * @param current 0-based index
	 * @param total count of total
	 */
	public void configure(int current, int total) {
		view.configure(current, total);
	};
	
	
	public void clear() {
		view.clear();
	}
	
	public Widget asWidget() {
		view.setPresenter(this);
		return view.asWidget();
	}
}
