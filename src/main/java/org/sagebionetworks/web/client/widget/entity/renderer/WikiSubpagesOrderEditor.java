package org.sagebionetworks.web.client.widget.entity.renderer;

import java.util.List;

import org.sagebionetworks.repo.model.v2.wiki.V2WikiHeader;
import org.sagebionetworks.schema.adapter.JSONEntity;
import org.sagebionetworks.web.client.utils.Callback;

import com.google.gwt.user.client.ui.Tree;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class WikiSubpagesOrderEditor implements WikiSubpagesOrderEditorView.Presenter {
	
	private WikiSubpagesOrderEditorView view;
	private WikiSubpageOrderEditorTree editorTree;
	private HasChangesHandler hasChangesHandler;
	
	@Inject
	public WikiSubpagesOrderEditor(WikiSubpagesOrderEditorView view, WikiSubpageOrderEditorTree editorTree) {
		this.view = view;
		this.editorTree = editorTree;
	}
	
	public void configure(List<V2WikiHeader> wikiHeaders, String ownerObjectName, HasChangesHandler hasChangesHandler) {
		editorTree.configure(wikiHeaders, ownerObjectName);
		this.hasChangesHandler = hasChangesHandler;
		view.configure(editorTree, hasChangesHandler);
	}
	
	public void initializeState() {
		view.initializeState();
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
	
	/**
	 * This handler is notified when there are changes made to the editor.
	 */
	public interface HasChangesHandler{
		/**
		 * Called with true then the user has changes in the editor.  Called with false when there are no changes in this editor.
		 * @param hasChanges True when there are changes.  False when there are no changes.
		 */
		void hasChanges(boolean hasChanges);
		
	}
}
