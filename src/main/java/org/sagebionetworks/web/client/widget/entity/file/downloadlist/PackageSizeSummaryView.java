package org.sagebionetworks.web.client.widget.entity.file.downloadlist;

import com.google.gwt.user.client.ui.IsWidget;

public interface PackageSizeSummaryView extends IsWidget {
	void setFileCount(String fileCount);

	void setSize(String friendlyFileSize);

	void setEstimatedDownloadTimeLoadingVisible(boolean visible);

	void setEstimatedDownloadTime(String time);

	void showWhiteSpinner();

	void addTextStyle(String style);
}
