package org.sagebionetworks.web.client.widget.entity.renderer;

import org.sagebionetworks.web.client.utils.Callback;
import com.google.gwt.user.client.ui.Tree;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class WikiSubpagesOrderEditor implements WikiSubpagesOrderEditorView.Presenter {
	
	private WikiSubpagesOrderEditorView view;
	private Tree subpageTree;
	private HasChangesHandler hasChangesHandler;
	
	@Inject
	public WikiSubpagesOrderEditor(WikiSubpagesOrderEditorView view) {
		this.view = view;
	}
	
	public void configure(Tree subpageTree, HasChangesHandler hasChangesHandler) {
		this.subpageTree = subpageTree;
		this.hasChangesHandler = hasChangesHandler;
		view.configure(subpageTree, hasChangesHandler);
	}
	
	/**
	 * Generate the WikiSubpagesOrderEditor Widget
	 */
	public Widget asWidget() {
		return view.asWidget();
	}
	
	public void pushChangesToSynapse(Callback changesPushedCallback) {
		changesPushedCallback.invoke();
	}
	
	public Tree getTree() {
		return subpageTree;
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
