package org.sagebionetworks.web.client.widget.entity.tabs;

import org.sagebionetworks.repo.model.ObjectType;
import org.sagebionetworks.web.client.place.Synapse;
import org.sagebionetworks.web.client.place.Synapse.EntityArea;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.entity.WikiPageWidget;
import org.sagebionetworks.web.client.widget.entity.WikiPageWidget.Callback;
import org.sagebionetworks.web.shared.WikiPageKey;

import com.google.inject.Inject;

public class TablesTab implements TablesTabView.Presenter{
	Tab tab;
	
	@Inject
	public TablesTab(Tab tab) {
		this.tab = tab;
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
	
	public void configure() {
		
		tab.setPlace(new Synapse(entityId, versionNumber, EntityArea.TABLES, wikiPageId));
	}
	
	public Tab asTab(){
		return tab;
	}
}
