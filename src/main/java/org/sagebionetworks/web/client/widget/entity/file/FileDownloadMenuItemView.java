package org.sagebionetworks.web.client.widget.entity.file;

import com.google.gwt.user.client.ui.IsWidget;

public interface FileDownloadMenuItemView extends IsWidget {

	void setPresenter(Presenter presenter);

	void clear();

	void setIsDirectDownloadLink(String href);

	void setIsUnauthenticatedS3DirectDownload();

	void setIsAuthorizedDirectDownloadLink();

	void showLoginS3DirectDownloadDialog(String endpoint);

	void showS3DirectDownloadDialog();

	/**
	 * Presenter interface
	 */
	public interface Presenter extends S3DirectLoginDialog.Presenter {
		void queryForSftpLoginInstructions(String directDownloadUrl);

		void onUnauthenticatedS3DirectDownloadClicked();

		void onAuthorizedDirectDownloadClicked();

		void onDirectDownloadClicked();
	}
}
