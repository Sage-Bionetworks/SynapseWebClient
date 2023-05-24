package org.sagebionetworks.web.client.place;

import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceTokenizer;
import com.google.gwt.place.shared.Prefix;

public class FollowingPlace extends Place {

  private String token;

  public FollowingPlace(String token) {
    this.token = token;
  }

  public String toToken() {
    return token;
  }

  @Prefix("!Following")
  public static class Tokenizer implements PlaceTokenizer<FollowingPlace> {

    @Override
    public String getToken(FollowingPlace place) {
      return place.toToken();
    }

    @Override
    public FollowingPlace getPlace(String token) {
      return new FollowingPlace(token);
    }
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((token == null) ? 0 : token.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    FollowingPlace other = (FollowingPlace) obj;
    if (token == null) {
      if (other.token != null) return false;
    } else if (!token.equals(other.token)) return false;
    return true;
  }
}
