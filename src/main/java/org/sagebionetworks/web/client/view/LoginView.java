package org.sagebionetworks.web.client.view;

import com.google.gwt.place.shared.Place;
import com.google.gwt.user.client.ui.IsWidget;
import org.sagebionetworks.web.client.SynapseView;
import org.sagebionetworks.web.client.utils.Callback;

public interface LoginView extends IsWidget, SynapseView {
  void setPresenter(Presenter loginPresenter);

  void showLoggingInLoader();

  void hideLoggingInLoader();

  void showLogin();

  void showTermsOfUse(boolean hasAccepted);

  void setSynAlert(IsWidget w);

  public interface Presenter {
    void goTo(Place place);

    void goToLastPlace();
    void onAcceptTermsOfUse();
    void onCancelAcceptTermsOfUse();
  }
}
