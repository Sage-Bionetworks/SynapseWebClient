package org.sagebionetworks.web.client.presenter;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.place.shared.Place;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.inject.Inject;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.place.PersonalAccessTokenPlace;
import org.sagebionetworks.web.client.view.PersonalAccessTokensView;

public class PersonalAccessTokensPresenter
  extends AbstractActivity
  implements Presenter<PersonalAccessTokenPlace> {

  private PersonalAccessTokensView view;
  private GlobalApplicationState globalApplicationState;

  @Inject
  public PersonalAccessTokensPresenter(
    PersonalAccessTokensView view,
    GlobalApplicationState globalApplicationState
  ) {
    this.view = view;
    this.globalApplicationState = globalApplicationState;
    this.view.setPresenter(this);
  }

  @Override
  public void setPlace(PersonalAccessTokenPlace place) {
    this.view.setPresenter(this);
  }

  @Override
  public void start(AcceptsOneWidget panel, EventBus eventBus) {
    this.view.render();
    panel.setWidget(view.asWidget());
  }

  public void goTo(Place place) {
    globalApplicationState.getPlaceChanger().goTo(place);
  }
}
