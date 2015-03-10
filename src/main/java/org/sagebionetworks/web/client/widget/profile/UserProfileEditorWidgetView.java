package org.sagebionetworks.web.client.widget.profile;

import com.google.gwt.user.client.ui.IsWidget;

public interface UserProfileEditorWidgetView extends IsWidget {
	
	public interface Presenter{
		/**
		 * Called when the user selects upload.
		 */
		public void onUploadFile();
	}

	void setUsername(String userName);

	void setFirstName(String firstName);

	String getFirstName();

	String getLastName();

	String getUsername();

	void setLastName(String lastName);

	void setBio(String summary);

	String getBio();

	void showUsernameError(String error);

	void hideUsernameError();

	void addImageWidget(IsWidget image);

	void addFileInputWidget(IsWidget fileInputWidget);
	
	public void setPresenter(Presenter presenter);

	void showUploadError(String string);

	void hideUploadError();

	void setUploading(boolean uplaoding);

}
