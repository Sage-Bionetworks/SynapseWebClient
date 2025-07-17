package org.sagebionetworks.web.client.presenter;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.inject.Inject;
import org.sagebionetworks.web.client.place.Trash;
import org.sagebionetworks.web.client.view.TrashView;

public class TrashPresenter
  extends AbstractActivity
  implements Presenter<Trash> {

  private Trash place;
  private TrashView view;

  @Inject
  public TrashPresenter(TrashView view) {
    this.view = view;
  }

  @Override
  public void start(AcceptsOneWidget panel, EventBus eventBus) {
    // Install the view
    panel.setWidget(view);
    view.createReactComponentWidget();
  }

  @Override
  public void setPlace(Trash place) {
    this.place = place;
  }
}
