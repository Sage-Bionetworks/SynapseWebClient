package org.sagebionetworks.web.client.place;

import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceTokenizer;
import com.google.gwt.place.shared.Prefix;

public class CuratorDashboardPlace extends Place {

  private String token;

  public CuratorDashboardPlace(String token) {
    this.token = token;
  }

  public String toToken() {
    return token;
  }

  @Prefix("CuratorDashboard")
  public static class Tokenizer
    implements PlaceTokenizer<CuratorDashboardPlace> {

    @Override
    public String getToken(CuratorDashboardPlace place) {
      return place.toToken();
    }

    @Override
    public CuratorDashboardPlace getPlace(String token) {
      return new CuratorDashboardPlace(token);
    }
  }
}
