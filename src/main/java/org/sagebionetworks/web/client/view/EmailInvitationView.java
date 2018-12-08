package org.sagebionetworks.web.client.view;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;

public interface EmailInvitationView extends IsWidget {
	void setSynapseAlertContainer(Widget w);
	void showLoading();
	void hideLoading();
	void showInfo(String message);
	void refreshHeader();
}
