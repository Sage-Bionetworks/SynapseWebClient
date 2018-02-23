package org.sagebionetworks.web.client.widget.entity;

import static org.sagebionetworks.web.client.ServiceEntryPointUtils.fixServiceEntryPoint;
import static org.sagebionetworks.web.client.widget.entity.controller.EntityActionControllerImpl.DELETED;
import static org.sagebionetworks.web.client.widget.entity.controller.EntityActionControllerImpl.THE;
import static org.sagebionetworks.web.client.widget.entity.controller.EntityActionControllerImpl.WAS_SUCCESSFULLY_DELETED;
import static org.sagebionetworks.web.client.widget.entity.controller.EntityActionControllerImpl.WIKI;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.sagebionetworks.repo.model.v2.wiki.V2WikiHeader;
import org.sagebionetworks.repo.model.v2.wiki.V2WikiPage;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.SynapseJavascriptClient;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.shared.WikiPageKey;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class WikiPageDeleteConfirmationDialog implements WikiPageDeleteConfirmationDialogView.Presenter, IsWidget {
	private WikiPageDeleteConfirmationDialogView view;
	private SynapseJavascriptClient jsClient;
	private SynapseClientAsync synapseClient;
	String parentWikiPageId;
	WikiPageKey key;
	CallbackP<String> callback;
	@Inject
	public WikiPageDeleteConfirmationDialog(WikiPageDeleteConfirmationDialogView view,
			SynapseJavascriptClient jsClient,
			SynapseClientAsync synapseClient) {
		this.view = view;
		this.jsClient = jsClient;
		this.synapseClient = synapseClient;
		fixServiceEntryPoint(synapseClient);
		view.setPresenter(this);
	}		
	
	public Widget asWidget() {
		return view.asWidget();
	}
	
	/**
	 * Show the wiki confirmation dialog.
	 * @param key WikiPageKey of page that user may want to delete.
	 * @param callbackAfterDelete This dialog will call you back with the parent wiki page id if the user confirms deletion (and the page is successfully deleted).
	 */
	public void show(WikiPageKey key, CallbackP<String> callbackAfterDelete) {
		this.key = key;
		this.callback = callbackAfterDelete;
		// Get the wiki page title and parent wiki id.  Go to the parent wiki if this delete is successful.
		jsClient.getV2WikiPage(key, new AsyncCallback<V2WikiPage>() {
			@Override
			public void onSuccess(V2WikiPage page) {
				// Confirm the delete with the user.
				parentWikiPageId = page.getParentWikiId();
				onDeleteWikiGetHeaderTree(key, page.getTitle());
			}
			
			@Override
			public void onFailure(Throwable caught) {
				view.showErrorMessage(caught.getMessage());
			}
		});
	}
	
	public void onDeleteWikiGetHeaderTree(WikiPageKey key, final String currentPageTitle) {
		synapseClient.getV2WikiHeaderTree(key.getOwnerObjectId(), key.getOwnerObjectType(), new AsyncCallback<List<V2WikiHeader>>() {
			@Override
			public void onFailure(Throwable caught) {
				view.showErrorMessage(caught.getMessage());
			}
			public void onSuccess(List<V2WikiHeader> wikiHeaders) {
				view.showModal(key.getWikiPageId(), getWikiHeaderMap(wikiHeaders), getWikiChildrenMap(wikiHeaders));		
			};
		});
	}
	public Map<String, V2WikiHeader> getWikiHeaderMap(List<V2WikiHeader> wikiHeaders) {
		Map<String, V2WikiHeader> id2Header = new HashMap<>();
		for (V2WikiHeader v2WikiHeader : wikiHeaders) {
			if (v2WikiHeader.getParentId() == null) {
				//update the root wiki title to be the entity name
				v2WikiHeader.setTitle("(the root wiki)");
			}
			id2Header.put(v2WikiHeader.getId(), v2WikiHeader);
		}
		return id2Header;
	}
	
	public Map<String, List<V2WikiHeader>> getWikiChildrenMap(List<V2WikiHeader> wikiHeaders) {
		Map<String, List<V2WikiHeader>> id2Children = new HashMap<>();
		for (V2WikiHeader v2WikiHeader : wikiHeaders) {
			if (v2WikiHeader.getParentId() != null) {
				List<V2WikiHeader> children = id2Children.get(v2WikiHeader.getParentId());
				if (children == null) {
					children = new ArrayList<>();
					id2Children.put(v2WikiHeader.getParentId(), children);
				}
				children.add(v2WikiHeader);
			}
		}
		return id2Children;
	}
	
	@Override
	public void onDeleteWiki() {
		//confirmed, delete the wiki!
		synapseClient.deleteV2WikiPage(key, new AsyncCallback<Void>() {
			@Override
			public void onSuccess(Void result) {
				view.showInfo(DELETED, THE + WIKI + WAS_SUCCESSFULLY_DELETED);
				if (callback != null) {
					callback.invoke(parentWikiPageId);
				}
			}
			
			@Override
			public void onFailure(Throwable caught) {
				view.showErrorMessage(caught.getMessage());
			}
		});
	}
}
