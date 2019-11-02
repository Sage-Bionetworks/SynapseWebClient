package org.sagebionetworks.web.client.place;

import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceTokenizer;
import com.google.gwt.place.shared.Prefix;

public class MapPlace extends Place {

	private String teamId;

	public MapPlace(String token) {
		this.teamId = token;
	}

	public String toToken() {
		return teamId;
	}

	public String getTeamId() {
		return teamId;
	}

	@Prefix("!Map")
	public static class Tokenizer implements PlaceTokenizer<MapPlace> {
		@Override
		public String getToken(MapPlace place) {
			return place.toToken();
		}

		@Override
		public MapPlace getPlace(String token) {
			return new MapPlace(token);
		}
	}

}
