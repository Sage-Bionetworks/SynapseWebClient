package org.sagebionetworks.web.client.presenter;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.inject.Inject;
import org.sagebionetworks.web.client.place.DataAccessManagementPlace;
import org.sagebionetworks.web.client.view.DataAccessManagementView;

public class DataAccessManagementPresenter
  extends AbstractActivity
  implements Presenter<DataAccessManagementPlace> {

  private DataAccessManagementView view;
  private DataAccessManagementPlace place;

  @Inject
  public DataAccessManagementPresenter(DataAccessManagementView view) {
    this.view = view;
  }

  @Override
  public void start(AcceptsOneWidget panel, EventBus eventBus) {
    panel.setWidget(view);
  }

  @Override
  public void setPlace(DataAccessManagementPlace place) {
    this.place = place;
    this.view.render();
  }

  public DataAccessManagementPlace getPlace() {
    return place;
  }

  @Override
  public String mayStop() {
    return null;
  }
}
