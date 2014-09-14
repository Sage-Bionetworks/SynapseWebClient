package org.sagebionetworks.web.client.place;

import org.sagebionetworks.web.client.place.Synapse.ProfileArea;

import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceTokenizer;
import com.google.gwt.place.shared.Prefix;

public class Profile extends Place implements RestartActivityOptional{
	public static final String EDIT_PROFILE_TOKEN="edit";
	public static final String DELIMITER = "/"; 
	public static final String SETTINGS_DELIMITER = getDelimiter(Synapse.ProfileArea.SETTINGS);
	public static final String PROJECTS_DELIMITER = getDelimiter(Synapse.ProfileArea.PROJECTS);
	public static final String FAVORITES_DELIMITER = getDelimiter(Synapse.ProfileArea.FAVORITES);
	public static final String CHALLENGES_DELIMITER = getDelimiter(Synapse.ProfileArea.CHALLENGES);
	public static final String TEAMS_DELIMITER = getDelimiter(Synapse.ProfileArea.TEAMS);
	
	private String token;
	private String userId;
	private ProfileArea area;
	private boolean noRestartActivity;
	
	public Profile(String token) {
		this.token = token;
		int firstSlash = token.indexOf(DELIMITER);
		if (firstSlash > -1) {
			userId = token.substring(0, firstSlash);
			//there's more
			String toProcess = token.substring(firstSlash);
			
			if(toProcess.contains(SETTINGS_DELIMITER)) {
				area = Synapse.ProfileArea.SETTINGS;
				return;
			} else if(toProcess.contains(PROJECTS_DELIMITER)) {
				area = Synapse.ProfileArea.PROJECTS;
				return;
			} else if(toProcess.contains(FAVORITES_DELIMITER)) {
				area = Synapse.ProfileArea.FAVORITES;
				return;
			} else if(toProcess.contains(CHALLENGES_DELIMITER)) {
				area = Synapse.ProfileArea.CHALLENGES;
				return;
			} else if(toProcess.contains(TEAMS_DELIMITER)) {
				area = Synapse.ProfileArea.TEAMS;
				return;
			}
		} else {
			userId = token;
		}
	}
	
	public Profile(String userId, ProfileArea area) {	
		String areaToken = area == null ? "" : DELIMITER + area.toString().toLowerCase();
		this.token = userId + areaToken;
			
		this.userId = userId;
		this.area = area;
	}
	
	public String toToken() {
		return token;
	}
	
	public ProfileArea getArea() {
		return area;
	}
	
	public void setArea(ProfileArea area) {
		this.area = area;
		calculateToken(userId, area);
	}
	
	public String getUserId() {
		return userId;
	}
	
	public void setUserId(String userId) {
		this.userId = userId;
	}
	
	public static String getDelimiter(Synapse.ProfileArea tab) {
		return DELIMITER+tab.toString().toLowerCase();
	}
	
	@Prefix("!Profile")
	public static class Tokenizer implements PlaceTokenizer<Profile> {
        @Override
        public String getToken(Profile place) {
            return place.toToken();
        }

        @Override
        public Profile getPlace(String token) {
            return new Profile(token);
        }
    }
	
	@Override
	public void setNoRestartActivity(boolean noRestart) {
		this.noRestartActivity = noRestart;
	}

	@Override
	public boolean isNoRestartActivity() {
		return noRestartActivity;
	}

	private void calculateToken(String userId, Synapse.ProfileArea area) {
		this.token = userId;
		if(area != null) {
			this.token += getDelimiter(area);
		}
	}

}
