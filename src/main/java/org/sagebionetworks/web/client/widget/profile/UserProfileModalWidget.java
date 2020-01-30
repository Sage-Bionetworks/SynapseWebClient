package org.sagebionetworks.web.client.widget.profile;

import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.web.client.utils.Callback;
import com.google.gwt.user.client.ui.IsWidget;

/**
 * Abstraction for a modal to edit a user profile.
 * 
 * @author jhill
 *
 */
public interface UserProfileModalWidget extends IsWidget, UserProfileModalView.Presenter {

	/**
	 * Show the modal profile editor.
	 * 
	 * @param userId The ID of the user to edit.
	 * @param callback This callback is invoked if the profile changes.
	 */
	public void showEditProfile(String userId, Callback callback);

	/**
	 * Show the modal profile editor.
	 * 
	 * @param importedProfile
	 * @param callback
	 */
	public void showEditProfile(String userId, UserProfile importedProfile, Callback callback);
}
