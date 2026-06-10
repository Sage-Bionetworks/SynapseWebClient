package org.sagebionetworks.web.client.widget;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.Modal;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.OneSageUtils;
import org.sagebionetworks.web.client.security.AuthenticationController;

@Singleton
public class QuarantinedEmailModal implements IsWidget {

  public interface Binder extends UiBinder<Widget, QuarantinedEmailModal> {}

  @UiField
  Button accountSettingsLink;

  Modal widget;

  private final Binder binder = GWT.create(Binder.class);

  @Inject
  public QuarantinedEmailModal(
    AuthenticationController authController,
    GlobalApplicationState globalAppState,
    OneSageUtils oneSageUtils
  ) {
    widget = (Modal) binder.createAndBindUi(this);
    accountSettingsLink.addClickHandler(event -> {
      Window.open(oneSageUtils.getAccountSettingsURL(), "_blank", "");
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
