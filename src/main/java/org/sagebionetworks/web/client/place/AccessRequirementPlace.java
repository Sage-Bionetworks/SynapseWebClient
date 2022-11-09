package org.sagebionetworks.web.client.place;

import com.google.gwt.place.shared.PlaceTokenizer;
import com.google.gwt.place.shared.Prefix;

public class AccessRequirementPlace extends ParameterizedPlace {

  public static final String AR_ID_PARAM = "AR_ID";

  public AccessRequirementPlace(String token) {
    super(token);
  }

  @Prefix("!AccessRequirement")
  public static class Tokenizer
    implements PlaceTokenizer<AccessRequirementPlace> {

    @Override
    public String getToken(AccessRequirementPlace place) {
      return place.toToken();
    }

    @Override
    public AccessRequirementPlace getPlace(String token) {
      return new AccessRequirementPlace(token);
    }
  }
}
