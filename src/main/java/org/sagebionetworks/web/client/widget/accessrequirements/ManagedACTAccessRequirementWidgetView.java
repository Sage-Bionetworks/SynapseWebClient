package org.sagebionetworks.web.client.widget.accessrequirements;

import org.sagebionetworks.web.client.widget.lazyload.SupportsLazyLoadInterface;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;

public interface ManagedACTAccessRequirementWidgetView extends IsWidget, SupportsLazyLoadInterface {

	/**
	 * Set the presenter.
	 * @param presenter
	 */
	void setPresenter(Presenter presenter);
	void addStyleNames(String styleNames);
	void setWikiTermsWidget(Widget wikiWidget);
	void setWikiTermsWidgetVisible(boolean visible);
	void showRequestSubmittedByOtherUser();
	void showApprovedHeading();
	void showUnapprovedHeading();
	void showRequestSubmittedMessage();
	void showRequestApprovedMessage();
	void showRequestRejectedMessage(String reason);
	void showCancelRequestButton();
	void showUpdateRequestButton();
	void showRequestAccessButton();
	void resetState();
	void setDataAccessRequestWizard(IsWidget w);
	void setEditAccessRequirementWidget(IsWidget w);
	void setDeleteAccessRequirementWidget(IsWidget w);
	void setSubmitterUserBadge(IsWidget w);
	void setManageAccessWidget(IsWidget w);
	void setReviewAccessRequestsWidget(IsWidget w);
	void setSubjectsWidget(IsWidget w);
	void setVisible(boolean visible);
	void setSynAlert(IsWidget w);
	void hideButtonContainers();
	void setReviewAccessRequestsWidgetContainerVisible(boolean visible);
	void showExpirationDate(String dateString);
	void showLoginButton();
	/**
	 * Presenter interface
	 */
	public interface Presenter {
		void onCancelRequest();
		void onRequestAccess();
	}
}
