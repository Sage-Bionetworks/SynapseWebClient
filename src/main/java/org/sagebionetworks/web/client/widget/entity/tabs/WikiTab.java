package org.sagebionetworks.web.client.widget.entity.tabs;

import org.sagebionetworks.repo.model.ObjectType;
import org.sagebionetworks.web.client.place.Synapse;
import org.sagebionetworks.web.client.place.Synapse.EntityArea;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.entity.WikiPageWidget;
import org.sagebionetworks.web.client.widget.entity.WikiPageWidget.Callback;
import org.sagebionetworks.web.shared.WebConstants;
import org.sagebionetworks.web.shared.WikiPageKey;

import com.google.inject.Inject;

public class WikiTab {
	Tab tab;
	private WikiPageWidget wikiPageWidget;
	
	@Inject
	public WikiTab(Tab tab, WikiPageWidget wikiPageWidget) {
		this.tab = tab;
		this.wikiPageWidget = wikiPageWidget;
		tab.configure("Wiki", wikiPageWidget.asWidget(), "Build narrative content to describe your project in the Wiki.", WebConstants.DOCS_URL + "wikis.html");
		wikiPageWidget.addStyleName("panel panel-default panel-body margin-bottom-0-imp");
	}
	
	public void setTabClickedCallback(CallbackP<Tab> onClickCallback) {
		tab.addTabClickedCallback(onClickCallback);
	}
	
	public void setWikiReloadHandler(CallbackP<String> wikiReloadHandler) {
		wikiPageWidget.setWikiReloadHandler(wikiReloadHandler);
	}
	
	public void configure(String entityId, String entityName, String wikiPageId, Boolean canEdit,
			Callback callback) {
		Long versionNumber = null; //version is always null for project
		WikiPageKey wikiPageKey = new WikiPageKey(entityId, ObjectType.ENTITY.name(), wikiPageId);
		wikiPageWidget.configure(wikiPageKey, canEdit, callback, true);
		tab.setEntityNameAndPlace(entityName, new Synapse(entityId, versionNumber, EntityArea.WIKI, wikiPageId));
	}
	
	public void clear() {
		wikiPageWidget.clear();
	}
	
	public Tab asTab(){
		return tab;
	}
}
