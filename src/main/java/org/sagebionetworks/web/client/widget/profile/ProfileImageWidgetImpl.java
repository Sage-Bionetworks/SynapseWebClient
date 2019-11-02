package org.sagebionetworks.web.client.widget.profile;

import org.sagebionetworks.repo.model.file.FileHandleAssociateType;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.utils.Callback;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class ProfileImageWidgetImpl implements ProfileImageWidget {

	ProfileImageView view;
	Callback removePictureCallback;
	SynapseJSNIUtils jsniUtils;

	@Inject
	public ProfileImageWidgetImpl(ProfileImageView view, SynapseJSNIUtils jsniUtils) {
		this.view = view;
		this.jsniUtils = jsniUtils;
		view.setPresenter(this);
	}

	@Override
	public void setRemovePictureCallback(Callback removePictureCallback) {
		this.removePictureCallback = removePictureCallback;
	}

	@Override
	public Widget asWidget() {
		return view.asWidget();
	}

	@Override
	public void configure(String userId, String fileHandleId) {
		boolean hasProfilePicture = fileHandleId != null;
		view.setRemovePictureButtonVisible(hasProfilePicture);
		if (hasProfilePicture) {
			String url = jsniUtils.getFileHandleAssociationUrl(userId, FileHandleAssociateType.UserProfileAttachment, fileHandleId);
			view.setImageUrl(url);
		} else {
			view.showDefault();
		}
	}

	@Override
	public void configure(String fileHandleId) {
		boolean hasProfilePicture = fileHandleId != null;
		view.setRemovePictureButtonVisible(hasProfilePicture);
		if (fileHandleId != null) {
			String url = jsniUtils.getRawFileHandleUrl(fileHandleId);
			view.setImageUrl(url);
		} else {
			view.showDefault();
		}
	}

	@Override
	public void onRemovePicture() {
		if (removePictureCallback != null) {
			removePictureCallback.invoke();
		}
	}

}
