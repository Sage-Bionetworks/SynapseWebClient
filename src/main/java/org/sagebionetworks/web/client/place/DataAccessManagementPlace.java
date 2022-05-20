package org.sagebionetworks.web.client.place;

import com.google.gwt.place.shared.PlaceTokenizer;
import com.google.gwt.place.shared.Prefix;

public class DataAccessManagementPlace extends ParameterizedPlace {

    public DataAccessManagementPlace(String token) {
        super(token);
    }

    @Prefix("!DataAccessManagement")
    public static class Tokenizer implements PlaceTokenizer<DataAccessManagementPlace> {
        @Override
        public String getToken(DataAccessManagementPlace place) {
            return place.toToken();
        }

        @Override
        public DataAccessManagementPlace getPlace(String token) {
            return new DataAccessManagementPlace(token);
        }
    }
}
