package org.sagebionetworks.web.client.presenter;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.place.shared.Place;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.inject.Inject;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.OneSageUtils;
import org.sagebionetworks.web.client.place.PersonalAccessTokenPlace;

public class PersonalAccessTokensPresenter
  extends AbstractActivity
  implements Presenter<PersonalAccessTokenPlace> {

  private final GlobalApplicationState globalApplicationState;
  private final OneSageUtils oneSageUtils;

  @Inject
  public PersonalAccessTokensPresenter(
    GlobalApplicationState globalApplicationState,
    OneSageUtils oneSageUtils
  ) {
    this.globalApplicationState = globalApplicationState;
    this.oneSageUtils = oneSageUtils;
  }

  @Override
  public void setPlace(PersonalAccessTokenPlace place) {}

  @Override
  public void start(AcceptsOneWidget panel, EventBus eventBus) {
    Window.Location.replace(
      oneSageUtils.getOneSageURL("/authenticated/personalaccesstokens")
    );
  }

  public void goTo(Place place) {
    globalApplicationState.getPlaceChanger().goTo(place);
  }
}
