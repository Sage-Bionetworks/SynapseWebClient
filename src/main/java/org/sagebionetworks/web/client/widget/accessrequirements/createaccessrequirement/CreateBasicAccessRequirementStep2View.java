package org.sagebionetworks.web.client.widget.accessrequirements.createaccessrequirement;

import com.google.gwt.user.client.ui.IsWidget;

/**
 * View shows the second step of the wizard for Terms Of Use AR
 * 
 * @author Jay
 *
 */
public interface CreateBasicAccessRequirementStep2View extends IsWidget {
	void setOldTermsVisible(boolean visible);

	void setOldTerms(String terms);

	void setWikiPageRenderer(IsWidget w);

	void setSynAlert(IsWidget w);

	void setPresenter(Presenter p);

	boolean isCertifiedUserRequired();

	void setIsCertifiedUserRequired(boolean value);

	boolean isValidatedProfileRequired();

	void setIsValidatedProfileRequired(boolean value);

	void setHasAccessorRequirementUIVisible(boolean visible);

	/*
	 * Presenter interface
	 */
	public interface Presenter {
		void onClearOldInstructions();

		void onEditWiki();
	}
}
