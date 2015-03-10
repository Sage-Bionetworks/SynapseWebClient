package org.sagebionetworks.web.client.widget.profile;

import org.sagebionetworks.web.client.SynapseJSNIUtils;
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
	public Widget asWidget() {
		return view.asWidget();
	}

	@Override
	public void configure(String userId, String fileHandleId) {
		if(fileHandleId != null){
			String url = buildUrl(userId, fileHandleId, false);
			view.setImageUrl(url);
		}else{
			view.showDefault();
		}
	}
	
	@Override
	public void configure(String fileHandleId) {
		if(fileHandleId != null){
			String url = buildUrl(null, fileHandleId, false);
			view.setImageUrl(url);
		}else{
			view.showDefault();
		}
	}
	
	private String buildUrl(String userId, String fileHandleId, boolean preview){
		StringBuilder builder = new StringBuilder();
		builder.append(baseUrl);
		builder.append("?"+WebConstants.USER_PROFILE_IMIAGE_ID+"=");
		builder.append(fileHandleId);
		if(userId != null){
			builder.append("&"+WebConstants.USER_PROFILE_PARAM_KEY+"=");
			builder.append(userId);
		}
		builder.append("&"+WebConstants.USER_PROFILE_PREVIEW+"=");
		builder.append(preview);
		return builder.toString();
	}

}
