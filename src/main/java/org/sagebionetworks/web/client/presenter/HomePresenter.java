package org.sagebionetworks.web.client.presenter;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.inject.Inject;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.place.Home;
import org.sagebionetworks.web.client.place.Profile;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.view.HomeView;

public class HomePresenter extends AbstractActivity implements Presenter<Home> {

  private Home place;
  private HomeView view;
  private AuthenticationController authController;
  private GlobalApplicationState globalAppState;

  @Inject
  public HomePresenter(
    HomeView view,
    AuthenticationController authController,
    GlobalApplicationState globalAppState
  ) {
    this.view = view;
    this.authController = authController;
    this.globalAppState = globalAppState;
    view.scrollToTop();
  }

  @Override
  public void start(AcceptsOneWidget panel, EventBus eventBus) {
    // Install the view
    this.view.render();
    panel.setWidget(view);
  }

  @Override
  public void setPlace(Home place) {
    this.place = place;
    view.refresh();
    if (authController.isLoggedIn()) {
      globalAppState
        .getPlaceChanger()
        .goTo(new Profile(Profile.VIEW_PROFILE_TOKEN + "/projects/all"));
    }
  }
}
