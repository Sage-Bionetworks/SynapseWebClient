package org.sagebionetworks.web.client.widget.team;

import org.sagebionetworks.web.client.SynapsePresenter;
import org.sagebionetworks.web.client.SynapseView;

import com.google.gwt.user.client.ui.IsWidget;

public interface InviteWidgetView extends IsWidget, SynapseView {
	
	/**
	 * Set this view's presenter
	 * @param presenter
	 */
	public void setPresenter(Presenter presenter);
	public void configure();
	public interface Presenter extends SynapsePresenter {
		public void sendInvitation(String principalId, String message, String userDisplayName);
	}
}
