package org.sagebionetworks.web.client.widget.accessrequirements.submission;

import com.google.gwt.user.client.ui.IsWidget;

public interface ACTDataAccessSubmissionWidgetView extends IsWidget {

	/**
	 * Set the presenter.
	 * 
	 * @param presenter
	 */
	void setPresenter(Presenter presenter);

	void addStyleNames(String styleNames);

	void setVisible(boolean visible);

	void setSynAlert(IsWidget w);

	void setPromptModal(IsWidget w);

	void setState(String s);

	void setInstitution(String s);

	void setProjectLead(String s);

	void setIntendedDataUse(String s);

	void clearAccessors();

	void addAccessors(IsWidget w, String username);

	void setDucWidget(IsWidget w);

	void setIrbWidget(IsWidget w);

	void setOtherAttachmentWidget(IsWidget w);

	void setIsRenewal(boolean b);

	void setPublications(String s);

	void setSummaryOfUse(String s);

	void setSubmittedOn(String s);

	void showRejectButton();

	void showApproveButton();

	void setSubmittedBy(IsWidget w);

	void setDucColumnVisible(boolean visible);

	void setIrbColumnVisible(boolean visible);

	void setOtherAttachmentsColumnVisible(boolean visible);

	void setRenewalColumnsVisible(boolean visible);

	void hideActions();

	void showMoreInfoDialog();

	void setRejectedReasonVisible(boolean visible);

	void setRejectedReason(String reason);

	/**
	 * Presenter interface
	 */
	public interface Presenter {
		void onApprove();

		void onReject();

		void onMoreInfo();
	}

}
