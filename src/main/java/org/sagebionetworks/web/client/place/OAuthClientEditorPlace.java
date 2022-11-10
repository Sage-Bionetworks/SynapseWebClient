package org.sagebionetworks.web.client.place;

import com.google.gwt.place.shared.PlaceTokenizer;
import com.google.gwt.place.shared.Prefix;

public class OAuthClientEditorPlace extends ParameterizedPlace {

  public OAuthClientEditorPlace(String token) {
    super(token);
  }

  @Prefix("!OAuthClientEditor")
  public static class Tokenizer
    implements PlaceTokenizer<OAuthClientEditorPlace> {

    @Override
    public String getToken(OAuthClientEditorPlace place) {
      return place.toToken();
    }

    @Override
    public OAuthClientEditorPlace getPlace(String token) {
      return new OAuthClientEditorPlace(token);
    }
  }
}
