package org.sagebionetworks.web.client.view;

import org.sagebionetworks.web.client.SynapsePresenter;
import org.sagebionetworks.web.client.SynapseView;
import org.sagebionetworks.web.shared.BCCSignupProfile;

import com.google.gwt.user.client.ui.IsWidget;

public interface BCCOverviewView extends IsWidget, SynapseView {
	public void showOverView();
	
	public void showSubmissionAcknowledgement();
	
	public void showSubmissionError();
	
	/**
	 * Set this view's presenter
	 * @param presenter
	 */
	public void setPresenter(Presenter presenter);	
	
	public interface Presenter extends SynapsePresenter {
		public BCCSignupProfile getBCCSignupProfile();
		public BCCCallback getBCCSignupCallback();
	}

}
