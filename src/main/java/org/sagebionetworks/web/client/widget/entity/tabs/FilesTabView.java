package org.sagebionetworks.web.client.widget.entity.tabs;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;

public interface FilesTabView extends IsWidget {
	void setFileTitlebar(Widget w);

	void setFileTitlebarVisible(boolean visible);

	void setFolderTitlebar(Widget w);

	void setFolderTitlebarVisible(boolean visible);

	void setBreadcrumb(Widget w);

	void setFileBrowser(Widget w);

	void setPreview(Widget w);

	void setProvenance(Widget w);

	void setMetadata(Widget w);

	void setFileFolderUIVisible(boolean visible);

	void setActionMenu(Widget w);

	void setWikiPage(Widget w);

	void setSynapseAlert(Widget w);

	void setProvenanceVisible(boolean visible);

	void setWikiPageWidgetVisible(boolean visible);

	void setFileBrowserVisible(boolean visible);

	void setPreviewVisible(boolean visible);

	void clearActionMenuContainer();

	void setModifiedCreatedBy(IsWidget modifiedCreatedBy);

	void setRefreshAlert(Widget w);

	void clearRefreshAlert();

	void setDiscussionThreadListWidget(Widget widget);

	void setDiscussionThreadListWidgetVisible(Boolean visible);

	void setDiscussionText(String entityName);

	void showLoading(boolean value);
}
