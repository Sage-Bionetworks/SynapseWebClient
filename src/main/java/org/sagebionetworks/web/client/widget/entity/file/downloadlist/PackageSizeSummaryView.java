package org.sagebionetworks.web.client.widget.entity.file.downloadlist;

import com.google.gwt.user.client.ui.IsWidget;

public interface PackageSizeSummaryView extends IsWidget {
	void setFileCount(int fileCount);
	void setSize(String friendlyFileSize);
	void setEstimatedDownloadTime(String time);
}
