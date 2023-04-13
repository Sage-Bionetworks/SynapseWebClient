package org.sagebionetworks.web.client.presenter;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.inject.Inject;
import org.sagebionetworks.web.client.context.SynapseReactClientFullContextPropsProvider;
import org.sagebionetworks.web.client.place.Trash;
import org.sagebionetworks.web.client.view.TrashView;

public class TrashPresenter
  extends AbstractActivity
  implements Presenter<Trash> {

  private Trash place;
  private TrashView view;
  private SynapseReactClientFullContextPropsProvider propsProvider;

  @Inject
  public TrashPresenter(
    TrashView view,
    SynapseReactClientFullContextPropsProvider propsProvider
  ) {
    this.view = view;
    this.propsProvider = propsProvider;
  }

  @Override
  public void start(AcceptsOneWidget panel, EventBus eventBus) {
    // Install the view
    panel.setWidget(view);
    view.createReactComponentWidget(propsProvider);
  }

  @Override
  public void setPlace(Trash place) {
    this.place = place;
  }
}
