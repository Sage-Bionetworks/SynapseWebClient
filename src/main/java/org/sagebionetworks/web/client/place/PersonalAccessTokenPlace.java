package org.sagebionetworks.web.client.place;

import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceTokenizer;
import com.google.gwt.place.shared.Prefix;

public class PersonalAccessTokenPlace extends Place {

  private String token;

  public PersonalAccessTokenPlace(String token) {
    this.token = token;
  }

  public String toToken() {
    return token;
  }

  @Prefix("!PersonalAccessTokens")
  public static class Tokenizer
    implements PlaceTokenizer<PersonalAccessTokenPlace> {

    @Override
    public String getToken(PersonalAccessTokenPlace place) {
      return place.toToken();
    }

    @Override
    public PersonalAccessTokenPlace getPlace(String token) {
      return new PersonalAccessTokenPlace(token);
    }
  }
}
