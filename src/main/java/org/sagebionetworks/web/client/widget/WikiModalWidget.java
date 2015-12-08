package org.sagebionetworks.web.client.widget;

import java.util.HashMap;

import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.widget.entity.MarkdownWidget;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.shared.WikiPageKey;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class WikiModalWidget implements WikiModalWidgetView.Presenter, IsWidget {
	private SynapseClientAsync synapseClient;
	private MarkdownWidget helpWikiPage;
	private SynapseAlert synAlert;
	private WikiModalWidgetView view;
	public static HashMap<String,WikiPageKey> pageNameToWikiKeyMap;
	
	@Inject
	public WikiModalWidget(
			WikiModalWidgetView view,
			MarkdownWidget helpWikiPage,
			SynapseClientAsync synapseClient,
			SynapseAlert synAlert
			) {
		this.view = view;
		this.helpWikiPage = helpWikiPage;
		this.synapseClient = synapseClient;
		this.synAlert = synAlert;
		view.setWikiPage(helpWikiPage.asWidget());
		view.setSynAlert(synAlert.asWidget());
		view.setPresenter(this);
	}
	
	public WikiModalWidget show(WikiPageKey key) {
		helpWikiPage.loadMarkdownFromWikiPage(key, false);
		view.show();
		return this;
	}
	
	public WikiModalWidget show(final String pageName) {
		clear();
		if (pageNameToWikiKeyMap == null) {
			synapseClient.getPageNameToWikiKeyMap(new AsyncCallback<HashMap<String,WikiPageKey>>() {
				@Override
				public void onSuccess(HashMap<String,WikiPageKey> result) {
					pageNameToWikiKeyMap = result;
					loadWikiFromWikiKeyMap(pageName);
				};
				@Override
				public void onFailure(Throwable caught) {
					synAlert.showError(caught.getMessage());
				}
			});
		} else {
			loadWikiFromWikiKeyMap(pageName);
		}
		
		return this;
	}
	
	private void clear() {
		synAlert.clear();
		view.clear();
	}
	
	private void loadWikiFromWikiKeyMap(String pageName) {
		WikiPageKey key = pageNameToWikiKeyMap.get(pageName);
		show(key);
	}
	
	@Override
	public Widget asWidget() {
		return view.asWidget();
	}
}
