package org.sagebionetworks.web.client.presenter;

import org.sagebionetworks.repo.model.wiki.WikiHeader;
import org.sagebionetworks.repo.model.wiki.WikiPage;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.place.WikiByTitle;
import org.sagebionetworks.web.client.view.SynapseStandaloneWikiView;
import org.sagebionetworks.web.shared.PaginatedResults;
import org.sagebionetworks.web.shared.WikiPageKey;
import org.sagebionetworks.web.shared.WikiPaginatedResults;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.inject.Inject;

public class SynapseStandaloneWikiPresenter extends AbstractActivity implements SynapseStandaloneWikiView.Presenter, Presenter<WikiByTitle> {
		
	private SynapseStandaloneWikiView view;
	private SynapseClientAsync synapseClient;
	
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
	public void setPlace(WikiByTitle place) {
		view.showLoading();
		final String title = place.toToken();
		if (!DisplayUtils.isDefined(title)) {
			view.showErrorMessage("No wiki title given");
			return;
		}
		synapseClient.getStandaloneWikis(new AsyncCallback<WikiPaginatedResults>() {
			public void onSuccess(WikiPaginatedResults result) {
				//find target wiki
				String wikiPageId = getTargetPage(title, result.getPageHeaders());
				if (wikiPageId == null) {
					//not found
					view.showErrorMessage("Wiki title not found: " + title);		
				} else {
					configure(new WikiPageKey(result.getOwnerId(), result.getOwnerType().name(), wikiPageId));	
				}
			};
			@Override
			public void onFailure(Throwable caught) {
				view.showErrorMessage(caught.getMessage());
			}
		});
	}
	
	/**
	 * Given a title, find the wiki page id whose page title matches (if all whitespace is removed).
	 * @param title
	 * @param wikis
	 * @return
	 */
	public String getTargetPage(String title, PaginatedResults<WikiHeader> wikis) {
		for (WikiHeader header : wikis.getResults()) {
			//we do not expect a large number of consecutive spaces, so \\s is preferred over \\s+ in this case
			String wikiTitle = header.getTitle().replaceAll("\\s","");
			if (title.equalsIgnoreCase(wikiTitle)) {
				return header.getId();
			}
		}
		return null;
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
