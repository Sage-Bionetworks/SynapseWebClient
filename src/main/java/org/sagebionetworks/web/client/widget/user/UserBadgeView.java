package org.sagebionetworks.web.client.widget.user;

import org.sagebionetworks.web.client.SynapseView;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.user.client.ui.IsWidget;

public interface UserBadgeView extends IsWidget, SynapseView {

	/**
	 * Set the presenter.
	 * @param presenter
	 */
	void setPresenter(Presenter presenter);
	void setDisplayName(String displayName, String shortDisplayName);
	void showLoadError(String error);
	void showAnonymousUserPicture();
	void showCustomUserPicture(String url);
	void showDescription(String description);
	void setSize(BadgeSize size);
	
	/**
	 * Presenter interface
	 */
	public interface Presenter {
		void badgeClicked(ClickEvent event);
	}

}
