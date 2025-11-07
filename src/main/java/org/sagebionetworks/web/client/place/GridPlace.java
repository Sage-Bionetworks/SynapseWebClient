package org.sagebionetworks.web.client.place;

import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceTokenizer;
import com.google.gwt.place.shared.Prefix;
import com.google.gwt.user.client.Window;

public class GridPlace extends Place {

  public static final String SESSION_ID = "sessionId";

  public GridPlace(String token) {
    if (token != null && !token.startsWith("default")) {
      // Redirect /Grid:{sessionId} to /Grid:default?sessionId={sessionId}
      Window.Location.assign("/Grid:default?" + SESSION_ID + "=" + token);
    }
  }

  @Prefix("Grid")
  public static class Tokenizer implements PlaceTokenizer<GridPlace> {

    @Override
    public String getToken(GridPlace place) {
      return "default";
    }

    @Override
    public GridPlace getPlace(String token) {
      return new GridPlace(token);
    }
  }
}
