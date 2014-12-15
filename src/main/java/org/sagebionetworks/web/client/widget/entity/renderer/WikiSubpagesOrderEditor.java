package org.sagebionetworks.web.client.widget.entity.renderer;

import java.util.LinkedList;
import java.util.List;

import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.sharing.AccessControlListEditor.HasChangesHandler;

import com.google.gwt.user.client.ui.Tree;
import com.google.gwt.user.client.ui.TreeItem;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class WikiSubpagesOrderEditor implements WikiSubpagesOrderEditorView.Presenter {
	
	private WikiSubpagesOrderEditorView view;
	private Tree subpageTree;
	private SynapseClientAsync synapseClient;
	private HasChangesHandler hasChangesHandler;
	private boolean unsavedChanges;
	
	@Inject
	public WikiSubpagesOrderEditor(WikiSubpagesOrderEditorView view,
									SynapseClientAsync synapseClient) {
		this.view = view;
		this.synapseClient = synapseClient;
	}
	
	public void configure(Tree subpageTree, HasChangesHandler hasChangesHandler, Callback updateOrderCallback) {
		this.subpageTree = subpageTree;
		this.hasChangesHandler = hasChangesHandler;
		view.configure(subpageTree);
	}
	
	/**
	 * Generate the ACLEditor Widget
	 */
	public Widget asWidget() {
		return view.asWidget();
	}
	
	@Override
	public void setUnsavedChanges(boolean unsavedChanges) {
		this.unsavedChanges = unsavedChanges;
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
