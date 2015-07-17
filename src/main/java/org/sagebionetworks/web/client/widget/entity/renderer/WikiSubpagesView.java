package org.sagebionetworks.web.client.widget.entity.renderer;

import java.util.List;

import org.sagebionetworks.repo.model.v2.wiki.V2WikiHeader;
import org.sagebionetworks.web.client.SynapseView;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.entity.renderer.WikiSubpagesWidget.UpdateOrderHintCallback;
import org.sagebionetworks.web.shared.WikiPageKey;

import com.google.gwt.place.shared.Place;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.IsWidget;

public interface WikiSubpagesView extends IsWidget, SynapseView {

	/**
	 * Set the presenter.
	 * @param presenter
	 */
	public void setPresenter(Presenter presenter);

	/**
	 * Configure the view with the parent id
	 * @param entityId
	 * @param title
	 */
	public void configure(List<V2WikiHeader> wikiHeaders, FlowPanel wikiSubpagesContainer, FlowPanel wikiPageContainer,
							String ownerObjectName, Place ownerObjectLink,
							WikiPageKey curWikiKey, boolean isEmbeddedOwnerPage,
							UpdateOrderHintCallback updateOrderHintCallback);
	
	void hideSubpages();
	void showSubpages();
	void setEditOrderButtonVisible(boolean visible);
	
	// List<String> getCurrentOrderHintIdList();
	
	/**
	 * Presenter interface
	 */
	public interface Presenter {

		public CallbackP<WikiPageKey> getReloadWikiPageCallback();

		void configure(WikiPageKey wikiKey, Callback widgetRefreshRequired,
				boolean embeddedInOwnerPage,
				CallbackP<WikiPageKey> reloadWikiPageCallback);

		void setContainers(FlowPanel wikiSubpagesContainer,
				FlowPanel wikiPageContainer);
	}
}
