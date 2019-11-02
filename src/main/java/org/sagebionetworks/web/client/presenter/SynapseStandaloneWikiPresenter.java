package org.sagebionetworks.web.client.presenter;

import static org.sagebionetworks.web.client.ServiceEntryPointUtils.fixServiceEntryPoint;
import java.util.HashMap;
import java.util.Map;
import org.sagebionetworks.repo.model.wiki.WikiPage;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.SynapseJavascriptClient;
import org.sagebionetworks.web.client.place.StandaloneWiki;
import org.sagebionetworks.web.client.view.SynapseStandaloneWikiView;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.shared.WikiPageKey;
import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.inject.Inject;

public class SynapseStandaloneWikiPresenter extends AbstractActivity implements Presenter<StandaloneWiki> {

	private SynapseStandaloneWikiView view;
	private SynapseClientAsync synapseClient;
	private Map<String, WikiPageKey> pageName2WikiKeyMap;
	private SynapseJavascriptClient jsClient;
	private SynapseAlert synAlert;

	@Inject
	public SynapseStandaloneWikiPresenter(SynapseStandaloneWikiView view, SynapseClientAsync synapseClient, SynapseJavascriptClient jsClient, SynapseAlert synAlert) {
		this.view = view;
		this.synapseClient = synapseClient;
		fixServiceEntryPoint(synapseClient);
		this.jsClient = jsClient;
		this.synAlert = synAlert;
		view.setSynAlert(synAlert);
	}

	@Override
	public void start(AcceptsOneWidget panel, EventBus eventBus) {
		// Install the view
		panel.setWidget(view);
	}

	@Override
	public void setPlace(StandaloneWiki place) {
		synAlert.clear();
		final String token = place.toToken();
		if (!DisplayUtils.isDefined(token)) {
			synAlert.showError("No wiki alias or key given.");
			return;
		}
		// Is this a wiki page key? If not, treat it like an wiki page alias
		if (place.getWikiId() != null && place.getOwnerId() != null && place.getOwnerType() != null) {
			WikiPageKey key = new WikiPageKey(place.getOwnerId(), place.getOwnerType(), place.getWikiId());
			configure(key);
		} else if (pageName2WikiKeyMap == null) {
			// initialize pageName2WikiKeyMap
			synapseClient.getPageNameToWikiKeyMap(new AsyncCallback<HashMap<String, WikiPageKey>>() {

				@Override
				public void onSuccess(HashMap<String, WikiPageKey> result) {
					pageName2WikiKeyMap = result;
					showWikiPage(token);
				}

				@Override
				public void onFailure(Throwable caught) {
					synAlert.handleException(caught);
				}
			});
		} else {
			showWikiPage(token);
		}
	}

	public void showWikiPage(String alias) {
		WikiPageKey key = pageName2WikiKeyMap.get(alias);
		if (key != null) {
			configure(key);
		} else {
			synAlert.showError("Wiki alias not found: " + alias);
		}
	}

	public void configure(final WikiPageKey wikiKey) {
		synAlert.clear();
		jsClient.getV2WikiPageAsV1(wikiKey, new AsyncCallback<WikiPage>() {
			@Override
			public void onSuccess(WikiPage result) {
				view.configure(result.getMarkdown(), wikiKey);
			}

			@Override
			public void onFailure(Throwable caught) {
				synAlert.handleException(caught);
			}
		});
	}

	@Override
	public String mayStop() {
		return null;
	}
}
