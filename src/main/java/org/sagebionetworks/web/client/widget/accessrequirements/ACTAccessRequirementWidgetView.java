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
	void showRequestApprovedMessage();
	void resetState();
	void showRequestAccessButton();
	void setEditAccessRequirementWidget(IsWidget w);
	void setDeleteAccessRequirementWidget(IsWidget w);
	void setSubjectsWidget(IsWidget w);
	void setVisible(boolean visible);
	void setSynAlert(IsWidget w);
	void hideButtonContainers();
	void setManageAccessWidget(IsWidget w);
	void setConvertAccessRequirementWidget(IsWidget w);
	void showLoginButton();
	
	/**
	 * Presenter interface
	 */
	public interface Presenter {
		void onRequestAccess();
	}
}
