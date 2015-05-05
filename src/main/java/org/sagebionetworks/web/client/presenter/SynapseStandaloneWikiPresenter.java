package org.sagebionetworks.web.client.presenter;

import java.util.HashMap;
import java.util.Map;

import org.sagebionetworks.repo.model.wiki.WikiPage;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.place.StandaloneWiki;
import org.sagebionetworks.web.client.view.SynapseStandaloneWikiView;
import org.sagebionetworks.web.shared.WikiPageKey;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.inject.Inject;

public class SynapseStandaloneWikiPresenter extends AbstractActivity implements SynapseStandaloneWikiView.Presenter, Presenter<StandaloneWiki> {

	private SynapseStandaloneWikiView view;
	private SynapseClientAsync synapseClient;
	private Map<String, WikiPageKey> pageName2WikiKeyMap;
	
	@Inject
	public SynapseStandaloneWikiPresenter(SynapseStandaloneWikiView view, SynapseClientAsync synapseClient){
		this.view = view;
		this.synapseClient = synapseClient;
		view.setPresenter(this);
	}

	@Override
	public void start(AcceptsOneWidget panel, EventBus eventBus) {
		// Install the view
		panel.setWidget(view);
	}
	
	@Override
	public void setPlace(StandaloneWiki place) {
		view.showLoading();
		final String token = place.toToken();
		if (!DisplayUtils.isDefined(token)) {
			view.showErrorMessage("No wiki alias or key given.");
			return;
		}
		//Is this a wiki page key?  If not, treat it like an wiki page alias
		if (place.getWikiId() != null && place.getOwnerId() != null && place.getOwnerType() != null) {
			WikiPageKey key = new WikiPageKey(place.getOwnerId(), place.getOwnerType(), place.getWikiId());
			configure(key);
		} else if (pageName2WikiKeyMap == null) {
			//initialize pageName2WikiKeyMap
			synapseClient.getPageNameToWikiKeyMap(new AsyncCallback<HashMap<String,WikiPageKey>>() {
				
				@Override
				public void onSuccess(HashMap<String, WikiPageKey> result) {
					pageName2WikiKeyMap = result;
					showWikiPage(token);
				}
				
				@Override
				public void onFailure(Throwable caught) {
					view.showErrorMessage(caught.getMessage());
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
			view.showErrorMessage("Wiki alias not found: " + alias);
		}
	}
	
	public void configure(final WikiPageKey wikiKey) {
		synapseClient.getV2WikiPageAsV1(wikiKey, new AsyncCallback<WikiPage>() {
			@Override
			public void onSuccess(WikiPage result) {
				view.configure(result.getMarkdown(), wikiKey);
			}
			@Override
			public void onFailure(Throwable caught) {
				view.showErrorMessage(DisplayConstants.ERROR_LOADING_WIKI_FAILED+caught.getMessage());
			}
		});
	}
	
	@Override
    public String mayStop() {
        view.clear();
        return null;
    }
}
