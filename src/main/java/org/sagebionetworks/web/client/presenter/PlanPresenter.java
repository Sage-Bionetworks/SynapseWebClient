package org.sagebionetworks.web.client.presenter;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.inject.Inject;
import org.sagebionetworks.web.client.place.PlanPlace;
import org.sagebionetworks.web.client.view.PlanView;

public class PlanPresenter
  extends AbstractActivity
  implements Presenter<PlanPlace> {

  private PlanPlace place;
  private PlanView view;

  @Inject
  public PlanPresenter(PlanView view) {
    this.view = view;
  }

  @Override
  public void start(AcceptsOneWidget panel, EventBus eventBus) {
    // Install the view
    panel.setWidget(view);
  }

  @Override
  public void setPlace(PlanPlace place) {
    this.place = place;
    view.render();
  }
}
