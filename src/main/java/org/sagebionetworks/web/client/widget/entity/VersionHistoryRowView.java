package org.sagebionetworks.web.client.widget.entity;

import org.sagebionetworks.web.client.utils.Callback;
import com.google.gwt.user.client.ui.IsWidget;

public interface VersionHistoryRowView extends IsWidget {

	void configure(Long versionNumber, String versionLinkHref, String versionName, String modifiedByUserId, String modifiedOn, String size, String md5, String versionComment, Callback deleteCallback, IsWidget doiWidget);

	void setCanDelete(boolean canEdit);

	void setIsVersionSelected(boolean isVersionSelected);

	void setSizeTableDataVisible(boolean isVisible);

	void setMd5TableDataVisible(boolean isVisible);

	void setIsUnlinked(boolean isUnlinked);
}
