package org.sagebionetworks.web.client.place;

import org.sagebionetworks.web.client.place.Synapse.ProfileArea;
import org.sagebionetworks.web.client.presenter.ProjectFilterEnum;
import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceTokenizer;
import com.google.gwt.place.shared.Prefix;

public class Profile extends Place {
	public static final String EDIT_PROFILE_TOKEN = "edit";
	public static final String VIEW_PROFILE_TOKEN = "v";
	public static final String DELIMITER = "/";

	private String token;
	private String userId;
	private String teamId;
	private ProfileArea area;
	private ProjectFilterEnum projectFilter;

	public Profile(String token) {
		this.token = token.toLowerCase();
		teamId = null;
		area = Synapse.ProfileArea.PROFILE;
		projectFilter = ProjectFilterEnum.ALL;
		String[] tokens = token.split(DELIMITER);
		if (tokens.length > 1) {
			// at least 2 tokens
			userId = tokens[0];
			try {
				area = ProfileArea.valueOf(tokens[1].toUpperCase());
				if (Synapse.ProfileArea.PROJECTS.equals(area)) {
					projectFilter = ProjectFilterEnum.ALL;
					if (tokens.length > 2) {
						projectFilter = ProjectFilterEnum.valueOf(tokens[2].toUpperCase());
						if (ProjectFilterEnum.TEAM.equals(projectFilter) && tokens.length > 3) {
							teamId = tokens[3];
						}
					}
				}
			} catch (Exception e) {
				// parsing error. will reroute to default values (All Projects)
			}
		} else {
			userId = token;
			// and by default go to the profile tab
			area = Synapse.ProfileArea.PROFILE;
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

	public ProjectFilterEnum getProjectFilter() {
		return projectFilter;
	}

	public void setArea(ProfileArea area, ProjectFilterEnum projectFilter, String teamId) {
		this.area = area;
		this.projectFilter = projectFilter;
		this.teamId = teamId;
		calculateToken(userId, area, projectFilter, teamId);
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getTeamId() {
		return teamId;
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

	private void calculateToken(String userId, Synapse.ProfileArea area, ProjectFilterEnum projectFilter, String teamId) {
		this.token = userId;
		if (area != null) {
			this.token += getDelimiter(area);
			if (ProfileArea.PROJECTS.equals(area) && projectFilter != null) {
				token += getDelimiter(projectFilter);
				if (projectFilter.equals(ProjectFilterEnum.TEAM) && teamId != null) {
					token += DELIMITER + teamId;
				}
			}
		}
	}

	public static String getDelimiter(Enum tab) {
		return DELIMITER + tab.toString().toLowerCase();
	}

}
