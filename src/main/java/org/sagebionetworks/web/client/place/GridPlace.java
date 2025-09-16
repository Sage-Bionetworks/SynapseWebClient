package org.sagebionetworks.web.client.place;

import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceTokenizer;
import com.google.gwt.place.shared.Prefix;

public class GridPlace extends Place {

  private String gridSessionId;

  public GridPlace(String token) {
    this.gridSessionId = token;
  }

  public String toToken() {
    return gridSessionId;
  }

  public String getGridSessionId() {
    return gridSessionId;
  }

  @Prefix("Grid")
  public static class Tokenizer implements PlaceTokenizer<GridPlace> {

    @Override
    public String getToken(GridPlace place) {
      return place.toToken();
    }

    @Override
    public GridPlace getPlace(String token) {
      return new GridPlace(token);
    }
  }
}
