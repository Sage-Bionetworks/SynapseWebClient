package org.sagebionetworks.web.client.widget.entity.renderer;

import java.util.List;
import org.sagebionetworks.repo.model.ObjectType;
import org.sagebionetworks.repo.model.entitybundle.v2.EntityBundle;
import org.sagebionetworks.repo.model.entitybundle.v2.EntityBundleRequest;
import org.sagebionetworks.repo.model.v2.wiki.V2WikiHeader;
import org.sagebionetworks.repo.model.v2.wiki.V2WikiOrderHint;
import org.sagebionetworks.web.client.SynapseJavascriptClient;
import org.sagebionetworks.web.client.place.Synapse;
import org.sagebionetworks.web.client.place.Wiki;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.entity.menu.v2.ActionMenuWidget;
import org.sagebionetworks.web.shared.WikiPageKey;
import org.sagebionetworks.web.shared.exceptions.NotFoundException;
import com.google.gwt.place.shared.Place;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class WikiSubpagesWidget implements IsWidget, WikiSubpagesView.Presenter {

	private WikiSubpagesView view;
	private WikiPageKey wikiKey;
	private String ownerObjectName;
	private Place ownerObjectLink;

	private boolean canEdit;
	private ActionMenuWidget actionMenu;

	// true if wiki is embedded in it's owner page. false if it should be shown as a stand-alone wiki
	private boolean isEmbeddedInOwnerPage;
	private CallbackP<WikiPageKey> reloadWikiPageCallback;
	private SynapseJavascriptClient jsClient;
	List<V2WikiHeader> currentWikiHeaders, newWikiHeaders;
	V2WikiOrderHint newWikiOrderHint;

	@Inject
	public WikiSubpagesWidget(WikiSubpagesView view, AuthenticationController authenticationController, SynapseJavascriptClient jsClient) {
		this.view = view;
		this.jsClient = jsClient;
		view.setPresenter(this);
	}

	public void configure(final WikiPageKey wikiKey, boolean embeddedInOwnerPage, CallbackP<WikiPageKey> reloadWikiPageCallback, ActionMenuWidget actionMenu) {

		// SWC-4177: if loading a page within the currently shown nav tree, don't reload the tree.
		if (this.wikiKey != null && this.wikiKey.getOwnerObjectId().equals(wikiKey.getOwnerObjectId()) && view.contains(wikiKey.getWikiPageId())) {
			this.wikiKey = wikiKey;
			view.setPage(wikiKey.getWikiPageId());
			view.showSubpages();
			// SWC-4256: in the background, check to see if the wiki header tree is the same...
			refreshRootObject();
			return;
		}

		canEdit = false;
		this.reloadWikiPageCallback = reloadWikiPageCallback;
		this.wikiKey = wikiKey;
		this.isEmbeddedInOwnerPage = embeddedInOwnerPage;
		this.actionMenu = actionMenu;

		view.clear();
		view.hideSubpages();
		refreshRootObject();
	}

	@Override
	public void clearCachedHeaderTree() {
		currentWikiHeaders = null;
	}

	public void refreshRootObject() {
		// figure out owner object name/link
		if (wikiKey.getOwnerObjectType().equalsIgnoreCase(ObjectType.ENTITY.toString())) {
			// lookup the entity name based on the id
			EntityBundleRequest bundleRequest = new EntityBundleRequest();
			bundleRequest.setIncludeEntity(true);
			bundleRequest.setIncludePermissions(true);
			jsClient.getEntityBundle(wikiKey.getOwnerObjectId(), bundleRequest, new AsyncCallback<EntityBundle>() {
				@Override
				public void onSuccess(EntityBundle bundle) {
					// if the owner object name is different, clear old wiki headers (force reconfigure)
					if (!bundle.getEntity().getName().equals(ownerObjectName)) {
						currentWikiHeaders = null;
					}
					ownerObjectName = bundle.getEntity().getName();
					ownerObjectLink = getLinkPlace(bundle.getEntity().getId(), wikiKey.getVersion(), null, isEmbeddedInOwnerPage);
					canEdit = bundle.getPermissions().getCanEdit();
					refreshWikiHeaderTree();
				}

				@Override
				public void onFailure(Throwable caught) {
					view.showErrorMessage(caught.getMessage());
				}
			});
		}
	}

	public static Place getLinkPlace(String entityId, Long entityVersion, String wikiId, boolean isEmbeddedInOwnerPage) {
		if (isEmbeddedInOwnerPage)
			return new Synapse(entityId, entityVersion, Synapse.EntityArea.WIKI, wikiId);
		else
			return new Wiki(entityId, ObjectType.ENTITY.toString(), wikiId);
	}

	public Widget asWidget() {
		return view.asWidget();
	}

	@Override
	public void refreshWikiHeaderTree() {
		newWikiHeaders = null;
		newWikiOrderHint = null;
		// ask for the wiki header tree and order hint in parallel.
		jsClient.getV2WikiHeaderTree(wikiKey.getOwnerObjectId(), wikiKey.getOwnerObjectType(), new AsyncCallback<List<V2WikiHeader>>() {
			@Override
			public void onSuccess(final List<V2WikiHeader> wikiHeaders) {
				newWikiHeaders = wikiHeaders;
				afterGetNewHeaderTreeAndOrderHint();
			}

			@Override
			public void onFailure(Throwable caught) {
				// if this is because the wiki header root was not found, and the parent wiki id is null,
				if (caught instanceof NotFoundException) {
					// do nothing, show nothing
					view.clear();
				} else {
					view.showErrorMessage(caught.getMessage());
				}
			}
		});

		jsClient.getV2WikiOrderHint(wikiKey, new AsyncCallback<V2WikiOrderHint>() {
			@Override
			public void onSuccess(V2WikiOrderHint result) {
				newWikiOrderHint = result;
				afterGetNewHeaderTreeAndOrderHint();
			}

			@Override
			public void onFailure(Throwable caught) {
				// Failed to get order hint. Just ignore it.
				newWikiOrderHint = new V2WikiOrderHint();
				afterGetNewHeaderTreeAndOrderHint();
			}
		});
	}

	private void afterGetNewHeaderTreeAndOrderHint() {
		if (newWikiHeaders != null && newWikiOrderHint != null) {
			WikiOrderHintUtils.sortHeadersByOrderHint(newWikiHeaders, newWikiOrderHint);
			if (currentWikiHeaders == null || !currentWikiHeaders.equals(newWikiHeaders)) {
				view.clear();
				view.configure(newWikiHeaders, ownerObjectName, ownerObjectLink, wikiKey, isEmbeddedInOwnerPage, reloadWikiPageCallback, actionMenu);
				currentWikiHeaders = newWikiHeaders;
				if (newWikiHeaders.size() <= 1) {
					view.hideSubpages();
				}
			}

			view.setEditOrderButtonVisible(canEdit);
		}
	}
}
