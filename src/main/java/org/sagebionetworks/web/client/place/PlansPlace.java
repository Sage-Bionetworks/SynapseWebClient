package org.sagebionetworks.web.client.place;

import com.google.gwt.place.shared.PlaceTokenizer;
import com.google.gwt.place.shared.Prefix;

public class PlansPlace extends ParameterizedPlace {

  public PlansPlace(String token) {
    super(token);
  }

  @Prefix("Plans")
  public static class Tokenizer implements PlaceTokenizer<PlansPlace> {

    @Override
    public String getToken(PlansPlace place) {
      return place.toToken();
    }

    @Override
    public PlansPlace getPlace(String token) {
      return new PlansPlace(token);
    }
  }
}
