package org.sagebionetworks.web.client.presenter;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.inject.Inject;
import org.sagebionetworks.web.client.place.UserAccessRequestHistoryPlace;
import org.sagebionetworks.web.client.view.UserAccessRequestHistoryView;

public class UserAccessRequestHistoryPresenter
  extends AbstractActivity
  implements Presenter<UserAccessRequestHistoryPlace> {

  private UserAccessRequestHistoryView view;
  private UserAccessRequestHistoryPlace place;

  @Inject
  public UserAccessRequestHistoryPresenter(UserAccessRequestHistoryView view) {
    this.view = view;
  }

  @Override
  public void start(AcceptsOneWidget panel, EventBus eventBus) {
    panel.setWidget(view);
  }

  @Override
  public void setPlace(UserAccessRequestHistoryPlace place) {
    this.place = place;
    this.view.render();
  }

  public UserAccessRequestHistoryPlace getPlace() {
    return place;
  }

  @Override
  public String mayStop() {
    return null;
  }
}
