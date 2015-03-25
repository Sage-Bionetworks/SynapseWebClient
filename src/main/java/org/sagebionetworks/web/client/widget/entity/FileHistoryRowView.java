package org.sagebionetworks.web.client.widget.entity;

import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.utils.CallbackP;

import com.google.gwt.user.client.ui.IsWidget;

public interface FileHistoryRowView extends IsWidget {

	void configure(
			Long versionNumber,
			String versionLinkHref,
			String versionName,
			String modifiedByUserId,
			String modifiedOn,
			String size,
			String md5,
			String versionComment,
			Callback deleteCallback, 
			CallbackP<String> editNameCallback,
			CallbackP<String> editCommentCallback);
	
	void setCanEdit(boolean canEdit);
	void setIsVersionLink(boolean isLink);
}
