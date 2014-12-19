package org.sagebionetworks.web.client.widget.entity.renderer;

import org.sagebionetworks.web.client.SynapseView;
import org.sagebionetworks.web.client.widget.entity.renderer.WikiSubpagesOrderEditor.HasChangesHandler;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Tree;

public interface WikiSubpagesOrderEditorView extends IsWidget, SynapseView {
	
	/**
	 * Sets the presenter for this view.
	 * @param presenter The presenter to be set.
	 */
	void setPresenter(Presenter presenter);
	
	void configure(WikiSubpageOrderEditorTree subpageTree, HasChangesHandler hasChangesHandler);
	
	interface Presenter {}
}
