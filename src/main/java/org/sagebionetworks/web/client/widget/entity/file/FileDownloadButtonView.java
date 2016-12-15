package org.sagebionetworks.web.client.widget.entity.file;

import org.gwtbootstrap3.client.ui.constants.ButtonSize;
import org.sagebionetworks.web.client.events.EntityUpdatedEvent;

import com.google.gwt.user.client.ui.IsWidget;

public interface FileDownloadButtonView extends IsWidget {

	void setPresenter(Presenter presenter);
	void clear();
	void setClientsHelpVisible(boolean visible);
	void setAuthorizedDirectDownloadLinkVisible(boolean visible);
	void setLicensedDownloadLinkVisible(boolean visible);
	void setDirectDownloadLink(String href);
	void setDirectDownloadLinkVisible(boolean visible);
	void setSynAlert(IsWidget w);
	void setFileClientsHelp(IsWidget w);
	void setButtonSize(ButtonSize size);
	/**
	 * Presenter interface
	 */
	public interface Presenter {
		void fireEntityUpdatedEvent(EntityUpdatedEvent event);
		void queryForSftpLoginInstructions(String directDownloadUrl);
		void onLicensedDownloadClick();
		void onAuthorizedDirectDownloadClicked();
	}
}
