package org.sagebionetworks.web.client.widget;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.Modal;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.place.Profile;
import org.sagebionetworks.web.client.place.Synapse.ProfileArea;
import org.sagebionetworks.web.client.security.AuthenticationController;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class QuarantinedEmailModal implements IsWidget {

	public interface Binder extends UiBinder<Widget, QuarantinedEmailModal> {
	}

	@UiField
	Button accountSettingsLink;
	Modal widget;

	@Inject
	public QuarantinedEmailModal(Binder binder, AuthenticationController authController, GlobalApplicationState globalAppState) {
		widget = (Modal) binder.createAndBindUi(this);
		accountSettingsLink.addClickHandler(event -> {
			globalAppState.getPlaceChanger().goTo(new Profile(authController.getCurrentUserPrincipalId(), ProfileArea.SETTINGS));
			widget.hide();
		});
	}

	public void show(String detailedReason) {
		// could show detailed reason in a synAlert if it was informative :
		// synAlert.showError(detailedReason);
		widget.show();
	}

	@Override
	public Widget asWidget() {
		return widget;
	}
}
