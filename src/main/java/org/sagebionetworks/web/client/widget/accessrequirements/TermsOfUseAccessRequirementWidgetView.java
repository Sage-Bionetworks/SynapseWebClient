package org.sagebionetworks.web.client.widget.accessrequirements;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;

public interface TermsOfUseAccessRequirementWidgetView extends IsWidget {

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
	void showSignTermsButton();
	/**
	 * Presenter interface
	 */
	public interface Presenter {
		void onSignTerms();
	}

}
