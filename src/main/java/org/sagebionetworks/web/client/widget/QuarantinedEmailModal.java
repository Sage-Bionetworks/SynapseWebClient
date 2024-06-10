package org.sagebionetworks.web.client.widget;

import static org.sagebionetworks.web.shared.WebConstants.ONESAGE_ACCOUNT_SETTINGS_URL;

import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.Modal;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.security.AuthenticationController;

public class QuarantinedEmailModal implements IsWidget {

  public interface Binder extends UiBinder<Widget, QuarantinedEmailModal> {}

  @UiField
  Button accountSettingsLink;

  Modal widget;

  @Inject
  public QuarantinedEmailModal(
    Binder binder,
    AuthenticationController authController,
    GlobalApplicationState globalAppState
  ) {
    widget = (Modal) binder.createAndBindUi(this);
    accountSettingsLink.addClickHandler(event -> {
      Window.open(ONESAGE_ACCOUNT_SETTINGS_URL, "_blank", "");
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
