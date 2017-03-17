package org.sagebionetworks.web.client.widget.accessrequirements.createaccessrequirement;

import com.google.gwt.user.client.ui.IsWidget;

/**
 * View shows the second step of the wizard for Terms Of Use AR
 * 
 * @author Jay
 *
 */
public interface CreateTermsOfUseAccessRequirementStep2View extends IsWidget {
	void setOldTermsVisible(boolean visible);
	void setOldTerms(String terms);
	
	void setWikiPageRenderer(IsWidget w);
	
	public void setPresenter(Presenter p);
	/*
	 * Presenter interface
	 */
	public interface Presenter {
		void onEditWiki();
	}
}
