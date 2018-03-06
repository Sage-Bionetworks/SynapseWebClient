package org.sagebionetworks.web.client.presenter;

import static org.sagebionetworks.web.client.ClientProperties.DIFF_LIB_JS;
import static org.sagebionetworks.web.client.ClientProperties.DIFF_VIEW_JS;
import static org.sagebionetworks.web.client.ServiceEntryPointUtils.fixServiceEntryPoint;

import java.util.ArrayList;
import java.util.List;

import org.sagebionetworks.repo.model.v2.wiki.V2WikiHistorySnapshot;
import org.sagebionetworks.repo.model.wiki.WikiPage;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.SynapseJavascriptClient;
import org.sagebionetworks.web.client.place.WikiDiff;
import org.sagebionetworks.web.client.resources.ResourceLoader;
import org.sagebionetworks.web.client.resources.WebResource;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.view.WikiDiffView;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.shared.PaginatedResults;
import org.sagebionetworks.web.shared.WikiPageKey;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.inject.Inject;

public class WikiDiffPresenter extends AbstractActivity implements WikiDiffView.Presenter, Presenter<WikiDiff> {
	private WikiDiff place;
	private WikiDiffView view;
	private SynapseAlert synAlert;
	private GlobalApplicationState globalAppState;
	private SynapseClientAsync synapseClient;
	private SynapseJavascriptClient jsClient;
	WikiPageKey key;
	String version1, version2;
	List<V2WikiHistorySnapshot> wikiVersionHistory;
	public static final Long LIMIT = 30L;
	@Inject
	public WikiDiffPresenter(WikiDiffView view,
			SynapseClientAsync synapseClient,
			SynapseJavascriptClient jsClient,
			SynapseAlert synAlert,
			PortalGinInjector ginInjector,
			GlobalApplicationState globalAppState,
			ResourceLoader resourceLoader) {
		this.view = view;
		this.synAlert = synAlert;
		this.globalAppState = globalAppState;
		this.synapseClient = synapseClient;
		fixServiceEntryPoint(synapseClient);
		this.jsClient = jsClient;
		view.setPresenter(this);
		view.setSynAlert(synAlert.asWidget());
		loadDiffLibrary(resourceLoader);
	}

	private void loadDiffLibrary(ResourceLoader resourceLoader) {
		if (!resourceLoader.isLoaded(DIFF_LIB_JS)) {
			List<WebResource> resources = new ArrayList<>();
			resources.add(DIFF_LIB_JS);
			resources.add(DIFF_VIEW_JS);
			resourceLoader.requires(resources, new AsyncCallback<Void>() {
				@Override
				public void onFailure(Throwable caught) {
					synAlert.handleException(caught);
				}
				@Override
				public void onSuccess(Void result) {
				}
			});
		}
	}
	
	@Override
	public void start(AcceptsOneWidget panel, EventBus eventBus) {
		// Install the view
		panel.setWidget(view);
		loadData();
	}
	
	public void loadData() {
		synAlert.clear();
		globalAppState.pushCurrentPlace(place);
		
		view.setVersion1(version1);
		view.setVersion2(version2);
		
		// refresh wiki versions available (kick off recursive call to get all versions, 30 at a time)
		wikiVersionHistory = new ArrayList<>();
		Long offset = 0L;
		getVersions(offset);
		
		// get selected wiki versions (if set)
		if (version1 != null && version2 != null) {
			getWikiMarkdown(version1, markdown1 -> {
				getWikiMarkdown(version2, markdown2 -> {
					view.showDiff(markdown1, markdown2);
				});
			});
		}
	}
	
	public void getVersions(Long offset) {
		synapseClient.getV2WikiHistory(key, LIMIT, offset, new AsyncCallback<PaginatedResults<V2WikiHistorySnapshot>>() {
			@Override
			public void onFailure(Throwable caught) {
				synAlert.handleException(caught);
			}

			@Override
			public void onSuccess(PaginatedResults<V2WikiHistorySnapshot> result) {
				PaginatedResults<V2WikiHistorySnapshot> paginatedHistory = result;
				// paginatedHistory.getTotalNumberOfResults() should return total!
				List<V2WikiHistorySnapshot> historyAsListOfHeaders = paginatedHistory.getResults();
				if (historyAsListOfHeaders == null || historyAsListOfHeaders.isEmpty()) {
					// done
					view.setVersionHistory(wikiVersionHistory);
				} else {
					// add versions, and look for more
					wikiVersionHistory.addAll(historyAsListOfHeaders);
					getVersions(offset+LIMIT);
				}
			}
		});
	}
	public void getWikiMarkdown(String wikiVersion, CallbackP<String> callback) {
		synAlert.clear();
		Long version = Long.parseLong(wikiVersion);
		jsClient.getVersionOfV2WikiPageAsV1(key, version, new AsyncCallback<WikiPage>() {
			@Override
			public void onFailure(Throwable caught) {
				synAlert.handleException(caught);
			}

			@Override
			public void onSuccess(WikiPage result) {
				callback.invoke(result.getMarkdown());
			}
		});
	}
	
	@Override
	public void setPlace(WikiDiff place) {
		this.place = place;
		this.view.setPresenter(this);
		String ownerId = place.getParam(WikiDiff.OWNER_ID);
		String ownerType = place.getParam(WikiDiff.OWNER_TYPE);
		String wikiPageId = place.getParam(WikiDiff.WIKI_ID);
		key = new WikiPageKey(ownerId, ownerType, wikiPageId);
		
		version1 = place.getParam(WikiDiff.WIKI_VERSION_1);
		version2 = place.getParam(WikiDiff.WIKI_VERSION_2);
	}
	
	public WikiDiff getPlace() {
		return place;
	}
	
	@Override
	public void onVersion1Selected(String version) {
		version1 = version;
		place.putParam(WikiDiff.WIKI_VERSION_1, version1);
		loadData();
	}
	
	@Override
	public void onVersion2Selected(String version) {
		version2 = version;
		place.putParam(WikiDiff.WIKI_VERSION_2, version2);
		loadData();
	}
	
	@Override
    public String mayStop() {
        view.clear();
        return null;
    }
}
