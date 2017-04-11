package org.sagebionetworks.web.client.widget.accessrequirements;

import org.sagebionetworks.web.client.widget.lazyload.SupportsLazyLoadInterface;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;

public interface ACTAccessRequirementWidgetView extends IsWidget, SupportsLazyLoadInterface {

	/**
	 * Set the presenter.
	 * @param presenter
	 */
	void setPresenter(Presenter presenter);
	void addStyleNames(String styleNames);
	void showTermsUI();
	void setTerms(String arText);
	void showWikiTermsUI();
	void setWikiTermsWidget(Widget wikiWidget);
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
	void setManageAccessWidget(IsWidget w);
	void setSubjectsWidget(IsWidget w);
	void setVisible(boolean visible);
	void setSynAlert(IsWidget w);
	/**
	 * Presenter interface
	 */
	public interface Presenter {
		void onCancelRequest();
		void onRequestAccess();
	}

}
