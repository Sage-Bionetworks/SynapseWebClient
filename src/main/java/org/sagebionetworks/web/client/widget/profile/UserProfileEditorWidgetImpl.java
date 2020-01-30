package org.sagebionetworks.web.client.widget.profile;

import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.ValidationUtils;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.upload.FileUpload;
import org.sagebionetworks.web.client.widget.upload.ImageUploadWidget;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class UserProfileEditorWidgetImpl implements UserProfileEditorWidget, UserProfileEditorWidgetView.Presenter {
	public static final String PLEASE_ENTER_A_VALID_URL = "Please enter a valid URL";
	public static final String PLEASE_SELECT_A_FILE = "Please select a file";
	public static final String CAN_ONLY_INCLUDE = "Can only include letters, numbers, dot (.), dash (-), and underscore (_)";
	public static final String MUST_BE_AT_LEAST_3_CHARACTERS = "Must be at least 3 characters";

	UserProfileEditorWidgetView view;
	ProfileImageWidget imageWidget;
	ImageUploadWidget fileHandleUploadWidget;
	String fileHandleId;
	Callback uploadCompleteCallback;

	@Inject
	public UserProfileEditorWidgetImpl(UserProfileEditorWidgetView view, ProfileImageWidget imageWidget, ImageUploadWidget fileHandleUploadWidget, PortalGinInjector ginInjector) {
		super();
		this.view = view;
		this.imageWidget = imageWidget;
		this.fileHandleUploadWidget = fileHandleUploadWidget;
		fileHandleUploadWidget.setView(ginInjector.getCroppedImageUploadView());
		this.view.addFileInputWidget(fileHandleUploadWidget);
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
		view.hideLinkError();
		view.setUsername(profile.getUserName());
		view.setFirstName(profile.getFirstName());
		view.setLastName(profile.getLastName());
		view.setCurrentPosition(profile.getPosition());
		view.setCurrentAffiliation(profile.getCompany());
		view.setBio(profile.getSummary());
		view.setIndustry(profile.getIndustry());
		view.setLocation(profile.getLocation());
		view.setLink(profile.getUrl());
		this.fileHandleId = profile.getProfilePicureFileHandleId();
		imageWidget.configure(this.fileHandleId);
		imageWidget.setRemovePictureCallback(new Callback() {
			@Override
			public void invoke() {
				setNewFileHandle(null);
			}
		});
		fileHandleUploadWidget.configure(new CallbackP<FileUpload>() {
			@Override
			public void invoke(FileUpload fileUploaded) {
				setNewFileHandle(fileUploaded.getFileHandleId());
			}
		});
	}

	@Override
	public boolean isValid() {
		view.hideUsernameError();
		view.hideLinkError();
		boolean valid = true;
		// username
		String username = view.getUsername();
		if (!ValidationUtils.isValidUsername(username)) {
			valid = false;
			if (username.length() < 3) {
				view.showUsernameError(MUST_BE_AT_LEAST_3_CHARACTERS);
			} else {
				view.showUsernameError(CAN_ONLY_INCLUDE);
			}
		}
		// link
		String link = view.getLink();
		if (link != null && !"".equals(link.trim())) {
			if (!ValidationUtils.isValidUrl(link, true)) {
				valid = false;
				view.showLinkError(PLEASE_ENTER_A_VALID_URL);
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
		return view.getCurrentPosition();
	}

	@Override
	public String getCompany() {
		return view.getCurrentAffiliation();
	}

	@Override
	public String getIndustry() {
		return view.getIndustry();
	}

	@Override
	public String getLocation() {
		return view.getLocation();
	}

	@Override
	public String getUrl() {
		return view.getLink();
	}

	@Override
	public String getSummary() {
		return view.getBio();
	}

	public void setNewFileHandle(String fileHandleId) {
		this.fileHandleId = fileHandleId;
		this.fileHandleUploadWidget.reset();
		this.imageWidget.configure(this.fileHandleId);
		if (uploadCompleteCallback != null) {
			uploadCompleteCallback.invoke();
		}
	}

	@Override
	public void addKeyDownHandler(KeyDownHandler keyDownHandler) {
		view.addKeyDownHandlerToFields(keyDownHandler);
	}

	@Override
	public void setUploadingCallback(Callback startedUploadingCallback) {
		fileHandleUploadWidget.setUploadingCallback(startedUploadingCallback);
	}

	@Override
	public void setUploadingCompleteCallback(Callback uploadCompleteCallback) {
		this.uploadCompleteCallback = uploadCompleteCallback;
	}
}
