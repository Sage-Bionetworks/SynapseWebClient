package org.sagebionetworks.web.client.view;

import com.google.gwt.user.client.ui.IsWidget;
import org.sagebionetworks.web.client.SynapseView;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;

public interface NewAccountView extends IsWidget, SynapseView {
  /**
   * Set this view's presenter
   *
   * @param presenter
   */
  void setPresenter(Presenter presenter);

  void markUsernameUnavailable();

  void setEmail(String email);

  void setLoading(boolean loading);

  void setSynAlert(SynapseAlert synAlert);

  public interface Presenter {
    void checkUsernameAvailable(String username);

    void completeRegistration(
      String userName,
      String fName,
      String lName,
      String password
    );
  }
}
