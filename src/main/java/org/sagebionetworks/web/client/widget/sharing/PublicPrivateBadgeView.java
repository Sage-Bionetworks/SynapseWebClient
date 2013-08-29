package org.sagebionetworks.web.client.widget.sharing;

import org.sagebionetworks.web.client.SynapseView;

import com.google.gwt.user.client.ui.IsWidget;

public interface PublicPrivateBadgeView extends IsWidget, SynapseView {

	/**
	 * Set the presenter.
	 * @param presenter
	 */
	public void setPresenter(Presenter presenter);
	
	public void configure(boolean isPublic);

	/**
	 * Presenter interface
	 */
	public interface Presenter {
		
	}

}
