package org.sagebionetworks.web.client.presenter;

import static org.sagebionetworks.web.client.ServiceEntryPointUtils.fixServiceEntryPoint;
import java.util.HashMap;
import java.util.Map;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.place.Help;
import org.sagebionetworks.web.client.view.HelpView;
import org.sagebionetworks.web.shared.WikiPageKey;
import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.inject.Inject;

public class HelpPresenter extends AbstractActivity implements HelpView.Presenter, Presenter<Help> {

	private Help place;
	private HelpView view;
	private Map<String, WikiPageKey> pageName2WikiKeyMap;
	private SynapseClientAsync synapseClient;

	@Inject
	public HelpPresenter(HelpView view, SynapseClientAsync synapseClient) {
		this.view = view;
		this.synapseClient = synapseClient;
		fixServiceEntryPoint(synapseClient);
		view.setPresenter(this);
	}

	@Override
	public void start(AcceptsOneWidget panel, EventBus eventBus) {
		// Install the view
		panel.setWidget(view);
	}

	@Override
	public void setPlace(Help place) {
		this.place = place;
		this.view.setPresenter(this);
		final String pageName = place.toToken();
		if (pageName2WikiKeyMap == null) {
			// initialize pageName2WikiKeyMap
			synapseClient.getPageNameToWikiKeyMap(new AsyncCallback<HashMap<String, WikiPageKey>>() {

				@Override
				public void onSuccess(HashMap<String, WikiPageKey> result) {
					pageName2WikiKeyMap = result;
					showHelpPage(pageName);
				}

				@Override
				public void onFailure(Throwable caught) {
					view.showErrorMessage(caught.getMessage());
				}
			});
		} else {
			showHelpPage(pageName);
		}
	}

	public void showHelpPage(String pageName) {
		WikiPageKey key = pageName2WikiKeyMap.get(pageName);
		if (key != null)
			view.showHelpPage(key);
	}

	@Override
	public String mayStop() {
		view.clear();
		return null;
	}
}
