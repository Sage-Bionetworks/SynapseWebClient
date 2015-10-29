package org.sagebionetworks.web.client.widget.entity.tabs;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;

public interface FilesTabView extends IsWidget {
	public interface Presenter {
	}
	void setPresenter(Presenter p);
	void setFileTitlebar(Widget w);
	void setFileTitlebarVisible(boolean visible);
	void setFolderTitlebar(Widget w);
	void setFolderTitlebarVisible(boolean visible);
	void setBreadcrumb(Widget w);
	void setFileBrowser(Widget w);
	void setPreview(Widget w);
	void setProvenance(Widget w);
	void setMetadata(Widget w);
	void setMetadataVisible(boolean visible);
	void setActionMenu(Widget w);
	void setWikiPage(Widget w);
	void setSynapseAlert(Widget w);
	void configureProgrammaticClients(String entityId, Long versionNumber);
	void setProvenanceVisible(boolean visible);
	void setProgrammaticClientsVisible(boolean visible);
	void setWikiPageWidgetVisible(boolean visible);
	void setFileBrowserVisible(boolean visible);
	void setPreviewVisible(boolean visible);
	void clearActionMenuContainer();
	void setModifiedCreatedBy(IsWidget modifiedCreatedBy);
}
