package org.sagebionetworks.web.client.widget.entity.renderer;

import java.util.List;
import org.sagebionetworks.repo.model.v2.wiki.V2WikiHeader;
import org.sagebionetworks.repo.model.v2.wiki.V2WikiOrderHint;
import org.sagebionetworks.web.client.SynapseJavascriptClient;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.shared.WikiPageKey;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class WikiSubpagesOrderEditor {

	private WikiSubpagesOrderEditorView view;
	private WikiSubpageOrderEditorTree editorTree;
	private String ownerObjectName;
	private SynapseAlert synAlert;
	private SynapseJavascriptClient jsClient;
	private WikiPageKey wikiKey;
	private CallbackP<String> refreshCallback;

	@Inject
	public WikiSubpagesOrderEditor(WikiSubpagesOrderEditorView view, WikiSubpageOrderEditorTree editorTree, SynapseAlert synAlert, SynapseJavascriptClient jsClient) {
		this.view = view;
		this.editorTree = editorTree;
		this.synAlert = synAlert;
		this.jsClient = jsClient;
		refreshCallback = new CallbackP<String>() {
			@Override
			public void invoke(String selectWikiPageId) {
				refresh(selectWikiPageId);
			}
		};
		view.configure(editorTree);
	}

	public void configure(WikiPageKey wikiKey, final String ownerObjectName) {
		// get wiki headers, and order
		this.wikiKey = wikiKey;
		this.ownerObjectName = ownerObjectName;
		view.initializeState();
		refresh(null);
	}

	public void refresh(final String selectWikiPageId) {
		synAlert.clear();
		view.setLoadingVisible(true);
		jsClient.getV2WikiHeaderTree(wikiKey.getOwnerObjectId(), wikiKey.getOwnerObjectType(), new AsyncCallback<List<V2WikiHeader>>() {
			@Override
			public void onSuccess(final List<V2WikiHeader> wikiHeaders) {
				jsClient.getV2WikiOrderHint(wikiKey, new AsyncCallback<V2WikiOrderHint>() {
					@Override
					public void onSuccess(V2WikiOrderHint hint) {
						// "Sort" stuff'
						WikiOrderHintUtils.sortHeadersByOrderHint(wikiHeaders, hint);
						editorTree.configure(selectWikiPageId, wikiKey, wikiHeaders, ownerObjectName, hint, refreshCallback);
						view.setLoadingVisible(false);
					}

					@Override
					public void onFailure(Throwable caught) {
						// Failed to get order hint. Just ignore it.
						synAlert.handleException(caught);
						view.setLoadingVisible(false);
					}
				});
			}

			@Override
			public void onFailure(Throwable caught) {
				synAlert.handleException(caught);
				view.setLoadingVisible(false);
			}
		});
	}

	/**
	 * Generate the WikiSubpagesOrderEditor Widget
	 */
	public Widget asWidget() {
		return view.asWidget();
	}

	public WikiSubpageOrderEditorTree getTree() {
		return editorTree;
	}
}
