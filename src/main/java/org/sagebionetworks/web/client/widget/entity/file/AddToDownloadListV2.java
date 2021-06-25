package org.sagebionetworks.web.client.widget.entity.file;

import org.sagebionetworks.repo.model.table.Query;

import com.google.gwt.user.client.ui.IsWidget;

public interface AddToDownloadListV2 extends IsWidget {
	void configure(String entityId, Query query);
	void configure(String folderId);
}
