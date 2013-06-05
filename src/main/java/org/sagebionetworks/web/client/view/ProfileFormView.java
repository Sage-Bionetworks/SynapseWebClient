package org.sagebionetworks.web.client.view;

import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.repo.model.attachment.AttachmentData;
import org.sagebionetworks.web.client.SynapsePresenter;
import org.sagebionetworks.web.client.SynapseView;

import com.google.gwt.user.client.ui.IsWidget;

public interface ProfileFormView extends IsWidget, SynapseView {
	
	/**
	 * Set this view's presenter
	 * @param presenter
	 */
	public void setPresenter(Presenter presenter);
	
	public void updateView(UserProfile profile);

	/**
	 * Show the user that the user's information has been updated.
	 */
	public void showUserUpdateSuccess();
	
	/**
	 * Alerts the view that updating the user's information failed.
	 */
	public void userUpdateFailed();
	
	public interface Presenter extends SynapsePresenter {

		void updateProfile(String firstName, String lastName,String summary, String position, String location, String industry, String company, String email, AttachmentData pic, String teamName, String url);

		void cancelClicked();
	}
}
