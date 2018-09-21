package org.sagebionetworks.web.client.widget.entity.file.downloadlist;

import com.google.gwt.user.client.ui.IsWidget;

public interface DownloadListWidgetView extends IsWidget {
	void setPresenter(Presenter p);
	void setSynAlert(IsWidget w);
	void setFileHandleAssociationTable(IsWidget w);
	void setPackageSizeSummary(IsWidget w);
	void setProgressTrackingWidgetVisible(boolean visible);
	void setProgressTrackingWidget(IsWidget w);
	void setCreatePackageUIVisible(boolean visible);
	void setDownloadPackageUIVisible(boolean visible);
	void setPackageDownloadURL(String downloadUrl);
	public interface Presenter {
		void onClearDownloadList();
		void onDownloadPackage(String zipFileName);
	}
}
