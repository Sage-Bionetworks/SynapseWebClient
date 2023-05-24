package org.sagebionetworks.web.client.presenter;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.inject.Inject;
import org.sagebionetworks.web.client.place.FollowingPlace;
import org.sagebionetworks.web.client.view.FollowingPageView;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;

public class FollowingPagePresenter
  extends AbstractActivity
  implements Presenter<FollowingPlace> {

  private final FollowingPageView view;

  @Inject
  public FollowingPagePresenter(FollowingPageView view, SynapseAlert synAlert) {
    this.view = view;
    view.setSynAlert(synAlert);
  }

  @Override
  public void start(AcceptsOneWidget panel, EventBus eventBus) {
    panel.setWidget(this.view.asWidget());
  }

  @Override
  public void setPlace(final FollowingPlace place) {
    view.clear();
  }

  @Override
  public String mayStop() {
    view.clear();
    return null;
  }
}
