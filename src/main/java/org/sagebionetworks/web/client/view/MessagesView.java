package org.sagebionetworks.web.client.view;

import org.sagebionetworks.web.client.SynapsePresenter;
import org.sagebionetworks.web.client.SynapseView;

import com.google.gwt.user.client.ui.IsWidget;

public interface MessagesView extends IsWidget, SynapseView {
	/**
	 * Set this view's Presenter
	 * @param presenter
	 */
	void setPresenter(Presenter presenter);
	
	// TODO: Declare methods. Presenter methods like "setUsername".
	
	
	public interface Presenter extends SynapsePresenter {
		// declare some methods and stuff.
	}
}
