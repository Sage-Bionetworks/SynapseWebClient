package org.sagebionetworks.web.client.widget.entity.file;

public interface S3DirectLoginDialog {
	public interface Presenter {
		void onUnauthenticatedS3DirectDownloadClicked();

		void onAuthorizedDirectDownloadClicked();

		void onLoginS3DirectDownloadClicked(String accessKey, String secretKey);

		void onAuthenticatedS3DirectDownloadClicked();
	}

	void setPresenter(Presenter presenter);

	void showLoginS3DirectDownloadDialog(String endpoint);

	void showS3DirectDownloadDialog();
}
