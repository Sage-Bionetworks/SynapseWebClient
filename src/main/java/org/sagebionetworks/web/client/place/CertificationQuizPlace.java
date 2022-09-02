package org.sagebionetworks.web.client.place;

import com.google.gwt.place.shared.PlaceTokenizer;
import com.google.gwt.place.shared.Prefix;

public class CertificationQuizPlace extends ParameterizedPlace {

    public CertificationQuizPlace(String token) {
        super(token);
    }

    @Prefix("!CertificationQuiz")
    public static class Tokenizer implements PlaceTokenizer<CertificationQuizPlace> {
        @Override
        public String getToken(CertificationQuizPlace place) {
            return place.toToken();
        }

        @Override
        public CertificationQuizPlace getPlace(String token) {
            return new CertificationQuizPlace(token);
        }
    }
}