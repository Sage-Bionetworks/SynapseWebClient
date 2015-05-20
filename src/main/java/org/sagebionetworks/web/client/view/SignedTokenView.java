package org.sagebionetworks.web.client.view;

import org.sagebionetworks.web.client.SynapsePresenter;
import org.sagebionetworks.web.server.servlet.NotificationTokenType;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;

public interface SignedTokenView extends IsWidget {
	void setSynapseAlert(Widget w);
	void showSuccess(String successMessage);
	void clear();
	
	/**
	 * Set this view's presenter
	 * @param presenter
	 */
	void setPresenter(Presenter presenter);	
	
	public interface Presenter extends SynapsePresenter {
		void okClicked();
	}

}
