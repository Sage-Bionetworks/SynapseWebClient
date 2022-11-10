package org.sagebionetworks.web.client.place;

import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceTokenizer;
import com.google.gwt.place.shared.Prefix;

public class DataAccessApprovalTokenPlace extends Place {

  private String token;

  public DataAccessApprovalTokenPlace(String token) {
    this.token = token;
  }

  public String toToken() {
    return token;
  }

  @Prefix("!DataAccessApprovalToken")
  public static class Tokenizer
    implements PlaceTokenizer<DataAccessApprovalTokenPlace> {

    @Override
    public String getToken(DataAccessApprovalTokenPlace place) {
      return place.toToken();
    }

    @Override
    public DataAccessApprovalTokenPlace getPlace(String token) {
      return new DataAccessApprovalTokenPlace(token);
    }
  }
}
