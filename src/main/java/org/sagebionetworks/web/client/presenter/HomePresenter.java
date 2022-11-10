package org.sagebionetworks.web.client.presenter;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.inject.Inject;
import org.sagebionetworks.web.client.place.Home;
import org.sagebionetworks.web.client.view.HomeView;

public class HomePresenter extends AbstractActivity implements Presenter<Home> {

  private Home place;
  private HomeView view;

  @Inject
  public HomePresenter(HomeView view) {
    this.view = view;
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
  }
}
