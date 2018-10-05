package org.sagebionetworks.web.client.widget.entity.file;

import org.gwtbootstrap3.client.ui.constants.ButtonSize;
import org.sagebionetworks.web.client.events.EntityUpdatedEvent;

import com.google.gwt.user.client.ui.IsWidget;

public interface FileDownloadButtonView extends IsWidget {

	void setPresenter(Presenter presenter);
	void clear();
	void setIsDirectDownloadLink(String href);
	void setIsUnauthenticatedS3DirectDownload();
	void setIsAuthorizedDirectDownloadLink();
	void addWidget(IsWidget w);
	void setButtonSize(ButtonSize size);
	
	void showLoginS3DirectDownloadDialog(String endpoint);
	void showS3DirectDownloadDialog();
	/**
	 * Presenter interface
	 */
	public interface Presenter extends S3DirectLoginDialog.Presenter{
		void queryForSftpLoginInstructions(String directDownloadUrl);
		void onUnauthenticatedS3DirectDownloadClicked();
		void onAuthorizedDirectDownloadClicked();
	}
}
