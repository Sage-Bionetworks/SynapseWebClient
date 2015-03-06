package org.sagebionetworks.web.client.view;

import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.web.client.SynapsePresenter;
import org.sagebionetworks.web.client.SynapseView;
import org.sagebionetworks.web.client.widget.upload.FileInputWidget;

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
	
	/**
	 * Alerts the view that updating the user's information failed.
	 */
	void userUpdateFailed();
	
	void setUpdateButtonText(String text);
	
	void showInvalidUrlUi();
	void showInvalidUsernameUi();
	
	void setIsDataModified(boolean isEditing);
	
	public interface Presenter extends SynapsePresenter {

		void updateProfile(String firstName, String lastName,String summary, String position, String location, String industry, String company, String email, String imageFileHandleId, String teamName, String url, String userName);
		void redirectToLinkedIn();
		void rollback();
		void startEditing();
		void stopEditing();
		void onUploadImage();
	}

	void addFileInputWidget(IsWidget widget);

	void updateProfilePicture(UserProfile profile);
}
