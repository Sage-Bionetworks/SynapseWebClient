package org.sagebionetworks.web.client.widget.entity;

import org.sagebionetworks.repo.model.ObjectType;
import org.sagebionetworks.web.client.place.Synapse;
import org.sagebionetworks.web.client.place.Synapse.EntityArea;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.entity.WikiPageWidget.Callback;
import org.sagebionetworks.web.shared.WikiPageKey;

import com.google.inject.Inject;

public class WikiTab {
	Tab tab;
	private WikiPageWidget wikiPageWidget;
	
	@Inject
	public WikiTab(Tab tab, WikiPageWidget wikiPageWidget) {
		this.tab = tab;
		this.wikiPageWidget = wikiPageWidget;
		tab.configure("Wiki", wikiPageWidget.asWidget());
	}
	
	public void showTab() {
		tab.showTab();
	}
	
	public void hideTab() {
		tab.hideTab();
	}
	
	public void setTabClickedCallback(CallbackP<Tab> onClickCallback) {
		tab.setTabClickedCallback(onClickCallback);
	}
	
	public void setWikiReloadHandler(CallbackP<String> wikiReloadHandler) {
		wikiPageWidget.setWikiReloadHandler(wikiReloadHandler);
	}
	
	public void configure(String entityId, String wikiPageId, Boolean canEdit,
			Callback callback, boolean isEmbeddedInOwnerPage) {
		Long versionNumber = null; //version is always null for project
		WikiPageKey wikiPageKey = new WikiPageKey(entityId, ObjectType.ENTITY.name(), wikiPageId);
		
		wikiPageWidget.configure(wikiPageKey, canEdit, callback, isEmbeddedInOwnerPage);
		tab.setPlace(new Synapse(entityId, versionNumber, EntityArea.WIKI, wikiPageId));
	}
	
	public Tab asTab(){
		return tab;
	}
}
