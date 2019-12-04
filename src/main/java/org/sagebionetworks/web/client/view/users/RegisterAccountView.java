package org.sagebionetworks.web.client.view.users;

import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;

public interface RegisterAccountView extends IsWidget {

	void setRegisterWidget(Widget w);

	void setPresenter(Presenter p);

	void setGoogleSynAlert(SynapseAlert synAlert);

	void setGoogleRegisterButtonEnabled(boolean enabled);

	public interface Presenter {
		void checkUsernameAvailable(String username);
	}
}
