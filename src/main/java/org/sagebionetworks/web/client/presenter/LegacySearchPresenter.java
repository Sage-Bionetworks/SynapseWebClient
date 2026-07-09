package org.sagebionetworks.web.client.presenter;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.place.shared.Place;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import javax.inject.Inject;
import org.sagebionetworks.web.client.GWTWrapper;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.place.LegacySearchPlace;
import org.sagebionetworks.web.client.place.SearchV2Place;
import org.sagebionetworks.web.client.place.Synapse;
import org.sagebionetworks.web.shared.WebConstants;

public class LegacySearchPresenter
  extends AbstractActivity
  implements Presenter<LegacySearchPlace> {

  private final GlobalApplicationState globalApplicationState;
  private final GWTWrapper gwt;
  private LegacySearchPlace place;

  @Inject
  public LegacySearchPresenter(
    GlobalApplicationState globalApplicationState,
    GWTWrapper gwt
  ) {
    this.globalApplicationState = globalApplicationState;
    this.gwt = gwt;
  }

  @Override
  public void setPlace(LegacySearchPlace place) {
    this.place = place;
  }

  @Override
  public void start(AcceptsOneWidget panel, EventBus eventBus) {
    String token = place.toToken();
    String decodedToken = (token != null) ? gwt.decodeQueryString(token) : "";
    String normalizedToken = decodedToken.toLowerCase().trim();
    Place redirect;
    if (normalizedToken.matches(WebConstants.SYNAPSE_ENTITY_ID_REGEX)) {
      redirect = new Synapse(normalizedToken);
    } else {
      String queryJson =
        "{\"queryTerm\":[" + gwt.escapeJsonString(decodedToken) + "]}";
      String searchToken =
        "default?" +
        SearchV2Place.QUERY +
        "=" +
        gwt.encodeQueryString(queryJson);
      redirect = new SearchV2Place(searchToken);
    }
    globalApplicationState.getPlaceChanger().goTo(redirect);
  }
}
