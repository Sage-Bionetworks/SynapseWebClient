package org.sagebionetworks.web.client.widget.verification;

import java.util.List;

import org.sagebionetworks.repo.model.wiki.WikiHeader;
import org.sagebionetworks.web.client.SynapseView;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;

public interface VerificationSubmissionWidgetView extends SynapseView, IsWidget {
	void clear();
	void show();
	void hide();
	void setSynAlert(Widget w);
	void setFileHandleList(Widget w);
	void setWikiPage(Widget w);
	void setWikiPageVisible(boolean visible);
	void setPresenter(Presenter presenter);
	void setTitle(String title);
	void setEmails(List<String> emails);
	void setFirstName(String fname);
	void setLastName(String lname);
	void setOrganization(String organization);
	void setLocation(String location);
	void setOrcID(String href);
	void setProfileLink(String profileId, String href);
	void setSubmitButtonVisible(boolean visible);
	void setCancelButtonVisible(boolean visible);
	void setOKButtonVisible(boolean visible);
	void setDeleteButtonVisible(boolean visible);
	void setApproveButtonVisible(boolean visible);
	void setRejectButtonVisible(boolean visible);
	void setSuspendButtonVisible(boolean visible);
	void setSuspendedReason(String reason);
	void setSuspendedAlertVisible(boolean visible);
	void popupError(String message);
	void openWindow(String url);
	void setPromptModal(Widget w);
	/**
	 * Presenter interface
	 */
	public interface Presenter {
		void submitVerification();
		
		void approveVerification();
		void rejectVerification();
		
		void suspendVerification();
		void deleteVerification();
	}
}
