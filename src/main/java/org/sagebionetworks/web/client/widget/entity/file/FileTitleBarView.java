package org.sagebionetworks.web.client.widget.entity.file;

import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.repo.model.EntityBundle;
import org.sagebionetworks.repo.model.EntityType;
import org.sagebionetworks.web.client.SynapseView;
import org.sagebionetworks.web.client.events.EntityUpdatedEvent;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.utils.CallbackP;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;

public interface FileTitleBarView extends IsWidget, SynapseView {
	void createTitlebar(Entity entity);
	void setFileLocation(String location);
	void setFileDownloadButton(Widget w);
	void setFilenameContainerVisible(boolean visible);
	void setEntityName(String name);
	void setFilename(String fileName);
	void setExternalUrlUIVisible(boolean visible);
	void setExternalUrl(String url);
	void setFileSize(String fileSize);
	void setMd5(String md5);
	void setExternalObjectStoreUIVisible(boolean visible);
	void setExternalObjectStoreInfo(String endpoint, String bucket, String fileKey);
}
