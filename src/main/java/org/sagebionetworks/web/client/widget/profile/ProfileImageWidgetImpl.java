package org.sagebionetworks.web.client.widget.profile;

import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.shared.WebConstants;

import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class ProfileImageWidgetImpl implements ProfileImageWidget {
	
	ProfileImageView view;
	String baseUrl;
	
	@Inject
	public ProfileImageWidgetImpl(ProfileImageView view, SynapseJSNIUtils jniUtils){
		this.view = view;
		baseUrl = jniUtils.getBaseProfileAttachmentUrl();
	}
	
	@Override
	public void setRemovePictureCallback(Callback callback) {
		view.setRemovePictureCallback(callback);
	}

	@Override
	public Widget asWidget() {
		return view.asWidget();
	}

	@Override
	public void configure(String userId, String fileHandleId) {
		if(fileHandleId != null){
			String url = buildUrl(userId, fileHandleId, true, true);
			view.setImageUrl(url);
		}else{
			view.showDefault();
		}
	}
	
	@Override
	public void configure(String fileHandleId) {
		if(fileHandleId != null){
			String url = buildUrl(null, fileHandleId, false, false);
			view.setImageUrl(url);
		}else{
			view.showDefault();
		}
	}
	
	private String buildUrl(String userId, String fileHandleId, boolean preview, boolean applied){
		StringBuilder builder = new StringBuilder();
		builder.append(baseUrl);
		builder.append("?"+WebConstants.USER_PROFILE_IMAGE_ID+"=");
		builder.append(fileHandleId);
		builder.append("&"+WebConstants.USER_PROFILE_USER_ID+"=");
		builder.append(userId);
		builder.append("&"+WebConstants.USER_PROFILE_PREVIEW+"=");
		builder.append(preview);
		builder.append("&"+WebConstants.USER_PROFILE_APPLIED+"=");
		builder.append(applied);
		return builder.toString();
	}

}
