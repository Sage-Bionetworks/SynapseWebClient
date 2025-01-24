package org.sagebionetworks.web.client.place;

import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceTokenizer;
import com.google.gwt.place.shared.Prefix;

public class DataCatalogPagePlace extends Place {

  private String token;

  public DataCatalogPagePlace(String token) {
    this.token = token;
  }

  public String toToken() {
    return token;
  }

  @Prefix("DataCatalog")
  public static class Tokenizer
    implements PlaceTokenizer<DataCatalogPagePlace> {

    @Override
    public String getToken(DataCatalogPagePlace place) {
      return place.toToken();
    }

    @Override
    public DataCatalogPagePlace getPlace(String token) {
      return new DataCatalogPagePlace(token);
    }
  }
}
