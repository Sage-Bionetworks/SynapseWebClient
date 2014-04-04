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
	void setPresenter(Presenter presenter);
	
	void updateView(UserProfile profile);

	/**
	 * Show the user that the user's information has been updated.
	 */
	void showUserUpdateSuccess();
	
	void hideCancelButton();
	
	/**
	 * Alerts the view that updating the user's information failed.
	 */
	void userUpdateFailed();
	
	void setUpdateButtonText(String text);
	
	void showInvalidUrlUi();
	void showInvalidUsernameUi();
	
	public interface Presenter extends SynapsePresenter {

		void updateProfile(String firstName, String lastName,String summary, String position, String location, String industry, String company, String email, AttachmentData pic, String teamName, String url, String userName);

		void cancelClicked();
	}
}
