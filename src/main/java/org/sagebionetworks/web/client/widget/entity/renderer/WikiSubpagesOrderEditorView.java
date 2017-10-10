package org.sagebionetworks.web.client.widget.entity.renderer;

import com.google.gwt.user.client.ui.IsWidget;

public interface WikiSubpagesOrderEditorView extends IsWidget {
	
	/**
	 * Sets the presenter for this view.
	 * @param presenter The presenter to be set.
	 */
	void setPresenter(Presenter presenter);
	
	void configure(WikiSubpageOrderEditorTree subpageTree);
	void setSynAlert(IsWidget w);
	void initializeState();
	
	interface Presenter {}
}
