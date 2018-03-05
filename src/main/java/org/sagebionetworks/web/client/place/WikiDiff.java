package org.sagebionetworks.web.client.place;

import com.google.gwt.place.shared.PlaceTokenizer;
import com.google.gwt.place.shared.Prefix;

public class WikiDiff extends ParameterizedPlace{
	public static final String OWNER_ID = "ownerId";
	public static final String OWNER_TYPE = "ownerType";
	public static final String WIKI_ID = "wikiId";
	public static final String WIKI_VERSION_1 = "version1";
	public static final String WIKI_VERSION_2 = "version2";

	public WikiDiff(String token) {
		super(token);
	}

	@Prefix("!WikiDiff")
	public static class Tokenizer implements PlaceTokenizer<WikiDiff> {
        @Override
        public String getToken(WikiDiff place) {
            return place.toToken();
        }

        @Override
        public WikiDiff getPlace(String token) {
            return new WikiDiff(token);
        }
    }
}
