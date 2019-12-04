package org.sagebionetworks.web.client.widget.entity.renderer;

import java.util.List;
import org.sagebionetworks.repo.model.v2.wiki.V2WikiHeader;
import org.sagebionetworks.web.client.SynapseView;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.entity.menu.v2.ActionMenuWidget;
import org.sagebionetworks.web.shared.WikiPageKey;
import com.google.gwt.place.shared.Place;
import com.google.gwt.user.client.ui.IsWidget;

public interface WikiSubpagesView extends IsWidget, SynapseView {
	/**
	 * Configure the view with the parent id
	 * 
	 * @param entityId
	 * @param title
	 */
	public void configure(List<V2WikiHeader> wikiHeaders, String ownerObjectName, Place ownerObjectLink, WikiPageKey curWikiKey, boolean isEmbeddedOwnerPage, CallbackP<WikiPageKey> wikiPageCallback, ActionMenuWidget actionMenu);

	void hideSubpages();

	void showSubpages();

	void setEditOrderButtonVisible(boolean visible);

	boolean contains(String wikiPageKey);

	void setPage(String wikiPageKey);

	void setPresenter(Presenter p);

	public interface Presenter {
		void refreshWikiHeaderTree();

		void clearCachedHeaderTree();
	}
}
