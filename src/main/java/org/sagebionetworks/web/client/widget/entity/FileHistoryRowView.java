package org.sagebionetworks.web.client.widget.entity;

import org.sagebionetworks.web.client.utils.Callback;

import com.google.gwt.user.client.ui.IsWidget;

public interface FileHistoryRowView extends IsWidget {

	void configure(
			String versionName,
			String modifiedByUserId,
			String modifiedOn,
			String size,
			String md5,
			Callback deleteCallback, 
			Callback editCallback);
	
}
