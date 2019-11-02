package org.sagebionetworks.web.client.view;

import org.sagebionetworks.web.client.SynapseView;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;

public interface ChangeUsernameView extends IsWidget, SynapseView {

	/**
	 * Set this view's presenter
	 * 
	 * @param presenter
	 */
	void setPresenter(Presenter presenter);

	public interface Presenter {
		void setUsername(String newUsername);
	}

	void setSynapseAlertWidget(Widget synAlert);

}
