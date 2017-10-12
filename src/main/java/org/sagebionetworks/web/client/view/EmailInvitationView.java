package org.sagebionetworks.web.client.view;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;

public interface EmailInvitationView extends IsWidget {
	void setRegisterWidget(Widget w);

	void setSynapseAlertContainer(Widget w);

	interface Presenter {
	}
}
