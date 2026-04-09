package org.sagebionetworks.web.client.place;

import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceTokenizer;
import com.google.gwt.place.shared.Prefix;

public class GridPlace extends Place {

  public static final String PARAM_SESSION_ID = "sessionId";

  private final String sessionId;

  public GridPlace(String token) {
    // If it's 'default', the ID is already in the URL parameters for React to find and use.
    if (token != null && !token.startsWith("default")) {
      this.sessionId = token;
    } else {
      // token is "default", GWT only needs the path to route to GridPage.
      this.sessionId = null;
    }
  }

  public String getSessionId() {
    return sessionId;
  }

  @Prefix("Grid")
  public static class Tokenizer implements PlaceTokenizer<GridPlace> {

    @Override
    public String getToken(GridPlace place) {
      if (place.getSessionId() != null) {
        return "default?" + PARAM_SESSION_ID + "=" + place.sessionId;
      }

      return "default";
    }

    @Override
    public GridPlace getPlace(String token) {
      return new GridPlace(token);
    }
  }
}
