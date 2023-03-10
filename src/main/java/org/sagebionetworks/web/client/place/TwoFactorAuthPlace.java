package org.sagebionetworks.web.client.place;

import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceTokenizer;
import com.google.gwt.place.shared.Prefix;

public class TwoFactorAuthPlace extends Place {

  private String token;

  public static final String BEGIN_ENROLLMENT = "Enroll";
  public static final String CREATE_RECOVERY_CODES = "GenerateRecoveryCodes";
  public static final String REPLACE_RECOVERY_CODES = "RegenerateRecoveryCodes";

  public TwoFactorAuthPlace(String token) {
    this.token = token;
  }

  public String toToken() {
    return token;
  }

  @Prefix("!TwoFactorAuth")
  public static class Tokenizer implements PlaceTokenizer<TwoFactorAuthPlace> {

    @Override
    public String getToken(TwoFactorAuthPlace place) {
      return place.toToken();
    }

    @Override
    public TwoFactorAuthPlace getPlace(String token) {
      return new TwoFactorAuthPlace(token);
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
    TwoFactorAuthPlace other = (TwoFactorAuthPlace) obj;
    if (token == null) {
      if (other.token != null) return false;
    } else if (!token.equals(other.token)) return false;
    return true;
  }
}
