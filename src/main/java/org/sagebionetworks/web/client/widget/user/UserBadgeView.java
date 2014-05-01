package org.sagebionetworks.web.client.widget.user;

import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.web.client.SynapseView;

import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.IsWidget;

public interface UserBadgeView extends IsWidget, SynapseView {

	/**
	 * Set the presenter.
	 * @param presenter
	 */
	public void setPresenter(Presenter presenter);
	
	public void setProfile(UserProfile profile, Integer maxNameLength);

	public void showLoadError(String principalId);
	
	public void setCustomClickHandler(ClickHandler clickHandler);
	
	/**
	 * Presenter interface
	 */
	public interface Presenter {
	}

}
