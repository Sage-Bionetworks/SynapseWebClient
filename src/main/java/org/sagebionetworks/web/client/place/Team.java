package org.sagebionetworks.web.client.place;

import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceTokenizer;
import com.google.gwt.place.shared.Prefix;

public class Team extends Place{
	
	private String teamId;

	public Team(String token) {
		this.teamId = token;
	}

	public String toToken() {
		return teamId;
	}
	
	public String getTeamId() {
		return teamId;
	}
	
	@Prefix("!Team")
	public static class Tokenizer implements PlaceTokenizer<Team> {
        @Override
        public String getToken(Team place) {
            return place.toToken();
        }

        @Override
        public Team getPlace(String token) {
            return new Team(token);
        }
    }

}
