package org.sagebionetworks.web.client.place;

import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceTokenizer;
import com.google.gwt.place.shared.Prefix;

public class DownloadCartPlace extends Place {

  private String token;

  public DownloadCartPlace(String token) {
    this.token = token;
  }

  public String toToken() {
    return token;
  }

  @Prefix("!DownloadCart")
  public static class Tokenizer implements PlaceTokenizer<DownloadCartPlace> {

    @Override
    public String getToken(DownloadCartPlace place) {
      return place.toToken();
    }

    @Override
    public DownloadCartPlace getPlace(String token) {
      return new DownloadCartPlace(token);
    }
  }
}
