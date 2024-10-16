package org.sagebionetworks.web.client.view;

import com.google.gwt.place.shared.Place;
import com.google.gwt.user.client.ui.IsWidget;
import org.sagebionetworks.web.client.SynapseView;

public interface LoginView extends IsWidget, SynapseView {
  void setPresenter(Presenter loginPresenter);

  void showLoggingInLoader();

  void hideLoggingInLoader();
  void setSynAlert(IsWidget w);

  public interface Presenter {
    void goTo(Place place);
    void goToLastPlace();
  }
}
