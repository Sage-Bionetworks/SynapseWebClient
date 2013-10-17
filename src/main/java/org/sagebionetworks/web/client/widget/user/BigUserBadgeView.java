package org.sagebionetworks.web.client.widget.user;

import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.web.client.SynapseView;

import com.google.gwt.user.client.ui.IsWidget;

public interface BigUserBadgeView extends IsWidget, SynapseView {

	/**
	 * Set the presenter.
	 * @param presenter
	 */
	public void setPresenter(Presenter presenter);
	
	public void setProfile(UserProfile profile, String description);

	public void showLoadError(String principalId);
	
	/**
	 * Presenter interface
	 */
	public interface Presenter {
	}

}
