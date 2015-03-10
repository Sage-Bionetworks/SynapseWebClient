package org.sagebionetworks.web.client.widget.profile;

import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.web.client.presenter.LoginPresenter;
import org.sagebionetworks.web.client.widget.upload.FileInputWidget;
import org.sagebionetworks.web.client.widget.upload.FileMetadata;
import org.sagebionetworks.web.client.widget.upload.FileUploadHandler;

import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class UserProfileEditorWidgetImpl implements UserProfileEditorWidget, UserProfileEditorWidgetView.Presenter {
	
	private static final String PLEASE_SELECT_A_FILE = "Please select a file";
	private static final String CAN_ONLY_INCLUDE = "Can only include letters, numbers, dot (.), dash (-), and underscore (_)";
	private static final String MUST_BE_AT_LEAST_3_CHARACTERS = "Must be at least 3 characters";
	UserProfileEditorWidgetView view;
	ProfileImageWidget imageWidget;
	FileInputWidget fileInputWidget;
	String fileHandleId;
	
	@Inject
	public UserProfileEditorWidgetImpl(UserProfileEditorWidgetView view, ProfileImageWidget imageWidget, FileInputWidget fileInputWidget) {
		super();
		this.view = view;
		this.imageWidget = imageWidget;
		this.fileInputWidget = fileInputWidget;
		this.view.addFileInputWidget(fileInputWidget);
		this.view.addImageWidget(imageWidget);
		this.view.setPresenter(this);
	}

	@Override
	public Widget asWidget() {
		return this.view.asWidget();
	}

	@Override
	public void configure(UserProfile profile) {
		view.hideUsernameError();
		view.setUsername(profile.getUserName());
		view.setFirstName(profile.getFirstName());
		view.setLastName(profile.getLastName());
		view.setBio(profile.getSummary());
		this.fileHandleId = profile.getProfilePicureFileHandleId();
		imageWidget.configure(this.fileHandleId);
	}

	@Override
	public boolean isValid() {
		view.hideUploadError();
		view.hideUsernameError();
		boolean valid = true;
		String username = view.getUsername();
		if (username != null && !LoginPresenter.isValidUsername(username)) {
			valid = false;
			if(username.length() < 3){
				view.showUsernameError(MUST_BE_AT_LEAST_3_CHARACTERS);
			}else{
				view.showUsernameError(CAN_ONLY_INCLUDE);
			}
		}
		return valid;
	}

	@Override
	public String getFirstName() {
		return view.getFirstName();
	}

	@Override
	public String getImageId() {
		return fileHandleId;
	}

	@Override
	public String getLastName() {
		return view.getLastName();
	}

	@Override
	public String getUsername() {
		return view.getUsername();
	}

	@Override
	public String getPosition() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getCompany() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getIndustry() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getLocation() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getUrl() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getSummary() {
		return view.getBio();
	}

	@Override
	public void onUploadFile() {
		view.hideUploadError();
		FileMetadata[] selectedFiles = this.fileInputWidget.getSelectedFileMetadata();
		if(selectedFiles == null || selectedFiles.length < 1){
			view.showUploadError(PLEASE_SELECT_A_FILE);
			return;
		}
		this.view.setUploading(true);
		// upload the file
		this.fileInputWidget.uploadSelectedFile(new FileUploadHandler() {
			
			@Override
			public void uploadSuccess(String fileHandleId) {
				view.setUploading(false);
				fileInputWidget.reset();
				setNewFileHandle(fileHandleId);
			}
			
			@Override
			public void uploadFailed(String error) {
				view.setUploading(false);
				view.showUploadError(error);
			}
		});
	}
	
	private void setNewFileHandle(String fileHandleId) {
		this.fileHandleId = fileHandleId;
		this.imageWidget.configure(this.fileHandleId);
	}

}
