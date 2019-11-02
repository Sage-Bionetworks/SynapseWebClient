package org.sagebionetworks.web.client.widget.entity.file;

import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.web.client.SynapseView;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;

public interface FileTitleBarView extends IsWidget, SynapseView {
	void createTitlebar(Entity entity);

	void setFileLocation(String location);

	void setFileDownloadMenuItem(Widget w);

	void setFilenameContainerVisible(boolean visible);

	void setEntityName(String name);

	void setFilename(String fileName);

	void setExternalUrlUIVisible(boolean visible);

	void setExternalUrl(String url);

	void setFileSize(String fileSize);

	void setMd5(String md5);

	void setExternalObjectStoreUIVisible(boolean visible);

	void setExternalObjectStoreInfo(String endpoint, String bucket, String fileKey);

	void setVersion(Long version);

	void setVersionUIVisible(boolean visible);

	void setPresenter(Presenter p);

	void showAddedToDownloadListAlert(String message);

	void setCanDownload(boolean canDownload);

	/**
	 * Presenter interface
	 */
	public interface Presenter {
		void onProgrammaticDownloadOptions();

		void onAddToDownloadList();
	}
}
