package org.sagebionetworks.web.client.widget.entity;

import org.sagebionetworks.web.client.SynapseView;

import com.google.gwt.user.client.ui.IsWidget;

public interface AccessRequirementDialogView extends IsWidget, SynapseView {

	/**
	 * Set the presenter.
	 * @param presenter
	 */
	void setPresenter(Presenter presenter);
	void showModal();
	
	void open(String url);
	
	void showControlledUseUI();
	void showNoRestrictionsUI();
	void showOpenUI();
	void showApprovedHeading();
	void showTouHeading();
	void showActHeading();
	void showAnonymousAccessNote();
	void showTermsUI();
	void setTerms(String arText);
	void showImposeRestrictionsAllowedNote();
	void showImposeRestrictionsNotAllowedNote();
	void showAnonymousFlagNote();
	void showImposeRestrictionsNotAllowedFlagNote();
	void showCancelButton();
	void showCloseButton();
	void showLoginButton();
	void showImposeRestrictionsButton();
	void showSignTermsButton();
	void showRequestAccessFromACTButton();
	
	/**
	 * Presenter interface
	 */
	public interface Presenter {
		void requestACTClicked();
		void signTermsOfUseClicked();
		void imposeRestrictionClicked();
		void flagClicked();
	}

}
