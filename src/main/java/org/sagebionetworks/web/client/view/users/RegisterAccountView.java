package org.sagebionetworks.web.client.view.users;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;

public interface RegisterAccountView extends IsWidget {
  void setRegisterWidget(Widget w);

  void setPresenter(Presenter p);

  void setGoogleSynAlert(SynapseAlert synAlert);

  void setGoogleRegisterButtonEnabled(boolean enabled);

  void showEmailSentUI(boolean visible);

  public interface Presenter {
    void checkUsernameAvailable(String username);
  }
}
