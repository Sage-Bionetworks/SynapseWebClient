package org.sagebionetworks.web.client.place;

import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceTokenizer;
import com.google.gwt.place.shared.Prefix;

public class UserAccessRequestHistoryPlace extends Place {

  String token;

  public UserAccessRequestHistoryPlace(String token) {
    this.token = token;
  }

  public String toToken() {
    return token;
  }

  @Prefix("RequestHistory")
  public static class Tokenizer
    implements PlaceTokenizer<UserAccessRequestHistoryPlace> {

    @Override
    public String getToken(UserAccessRequestHistoryPlace place) {
      return place.toToken();
    }

    @Override
    public UserAccessRequestHistoryPlace getPlace(String token) {
      return new UserAccessRequestHistoryPlace(token);
    }
  }
}
