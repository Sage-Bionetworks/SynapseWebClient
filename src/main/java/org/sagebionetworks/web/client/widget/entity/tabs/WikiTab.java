package org.sagebionetworks.web.client.widget.entity.tabs;

import org.sagebionetworks.repo.model.ObjectType;
import org.sagebionetworks.repo.model.entitybundle.v2.EntityBundle;
import org.sagebionetworks.web.client.PortalGinInjector;
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
	PortalGinInjector ginInjector;
	String entityId, entityName;
	EntityBundle projectBundle;

	@Inject
	public WikiTab(Tab tab, PortalGinInjector ginInjector) {
		this.ginInjector = ginInjector;
		this.tab = tab;
		tab.configure("Wiki", "Build narrative content to describe your project in the Wiki.", WebConstants.DOCS_URL + "Wikis.1975746682.html", EntityArea.WIKI);
	}

	public void lazyInject() {
		if (wikiPageWidget == null) {
			this.wikiPageWidget = ginInjector.getWikiPageWidget();
			wikiPageWidget.addStyleName("panel panel-default panel-body margin-bottom-0-imp");
			wikiPageWidget.setWikiReloadHandler(wikiPageId -> {
				tab.configureEntityActionController(projectBundle, true, wikiPageId);
				setEntityNameAndPlace(entityId, entityName, wikiPageId);
			});
			tab.setContent(wikiPageWidget.asWidget());
		}
	}

	public void setTabClickedCallback(CallbackP<Tab> onClickCallback) {
		tab.addTabClickedCallback(onClickCallback);
	}

	public void configure(String entityId, String entityName, EntityBundle projectBundle, String wikiPageId, Boolean canEdit,
			Callback callback) {
		lazyInject();
		this.entityId = entityId;
		this.entityName = entityName;
		this.projectBundle = projectBundle;
		tab.configureEntityActionController(projectBundle, true, wikiPageId);
		WikiPageKey wikiPageKey = new WikiPageKey(entityId, ObjectType.ENTITY.name(), wikiPageId);
		wikiPageWidget.configure(wikiPageKey, canEdit, callback);
		wikiPageWidget.setActionMenu(tab.getEntityActionMenu());
		wikiPageWidget.showSubpages(tab.getEntityActionMenu());
		setEntityNameAndPlace(entityId, entityName, wikiPageId);
	}

	public void setEntityNameAndPlace(String entityId, String entityName, String wikiPageId) {
		Long versionNumber = null; // version is always null for project
		tab.setEntityNameAndPlace(entityName, new Synapse(entityId, versionNumber, EntityArea.WIKI, wikiPageId));
	}

	public void clear() {
		if (wikiPageWidget != null) {
			wikiPageWidget.clear();
		}
	}

	public Tab asTab() {
		return tab;
	}
}
