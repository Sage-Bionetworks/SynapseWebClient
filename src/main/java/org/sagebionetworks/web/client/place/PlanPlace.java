package org.sagebionetworks.web.client.place;

import com.google.gwt.place.shared.PlaceTokenizer;
import com.google.gwt.place.shared.Prefix;

public class PlanPlace extends ParameterizedPlace {

  public PlanPlace(String token) {
    super(token);
  }

  @Prefix("Plan")
  public static class Tokenizer implements PlaceTokenizer<PlanPlace> {

    @Override
    public String getToken(PlanPlace place) {
      return place.toToken();
    }

    @Override
    public PlanPlace getPlace(String token) {
      return new PlanPlace(token);
    }
  }
}
