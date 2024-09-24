package org.sagebionetworks.web.client.presenter;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.inject.Inject;
import org.sagebionetworks.web.client.place.PlansPlace;
import org.sagebionetworks.web.client.view.PlansView;

public class PlansPresenter
  extends AbstractActivity
  implements Presenter<PlansPlace> {

  private PlansPlace place;
  private PlansView view;

  @Inject
  public PlansPresenter(PlansView view) {
    this.view = view;
  }

  @Override
  public void start(AcceptsOneWidget panel, EventBus eventBus) {
    // Install the view
    panel.setWidget(view);
  }

  @Override
  public void setPlace(PlansPlace place) {
    this.place = place;
    view.render();
  }
}
