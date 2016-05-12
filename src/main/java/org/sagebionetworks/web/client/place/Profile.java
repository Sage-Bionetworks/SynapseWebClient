package org.sagebionetworks.web.client.place;

import org.sagebionetworks.web.client.place.Synapse.ProfileArea;
import org.sagebionetworks.web.client.presenter.ProjectFilterEnum;

import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceTokenizer;
import com.google.gwt.place.shared.Prefix;

public class Profile extends Place implements RestartActivityOptional{
	public static final String EDIT_PROFILE_TOKEN="edit";
	public static final String DELIMITER = "/"; 
	public static final String SETTINGS_DELIMITER = getDelimiter(Synapse.ProfileArea.SETTINGS);
	public static final String PROJECTS_DELIMITER = getDelimiter(Synapse.ProfileArea.PROJECTS);
	public static final String CHALLENGES_DELIMITER = getDelimiter(Synapse.ProfileArea.CHALLENGES);
	public static final String TEAMS_DELIMITER = getDelimiter(Synapse.ProfileArea.TEAMS);
	
	public static final String ALL_PROJECTS_DELIMITER = getDelimiter(ProjectFilterEnum.ALL);
	public static final String FAV_PROJECTS_DELIMITER = getDelimiter(ProjectFilterEnum.FAVORITES);
	public static final String CREATED_BY_ME_DELIMITER = getDelimiter(ProjectFilterEnum.CREATED_BY_ME);
	public static final String ALL_MY_TEAM_PROJECTS_DELIMITER = getDelimiter(ProjectFilterEnum.ALL_MY_TEAM_PROJECTS);
	public static final String SHARED_DIRECTLY_WITH_ME_PROJECTS_DELIMITER = getDelimiter(ProjectFilterEnum.SHARED_DIRECTLY_WITH_ME);
	public static final String TEAM_PROJECTS_DELIMITER = getDelimiter(ProjectFilterEnum.TEAM);
	
	private String token;
	private String userId;
	private String teamId;
	private ProfileArea area;
	private ProjectFilterEnum projectFilter;
	private boolean noRestartActivity;
	
	public Profile(String token) {
		this.token = token;
		teamId = null;
		area = Synapse.ProfileArea.PROJECTS;
		projectFilter = ProjectFilterEnum.ALL;
		
		int slashIndex = token.indexOf(DELIMITER);
		if (slashIndex > -1) {
			userId = token.substring(0, slashIndex);
			//there's more
			String toProcess = token.substring(slashIndex);
			if (toProcess.contains(SETTINGS_DELIMITER)) {
				area = Synapse.ProfileArea.SETTINGS;
				return;
			} else if(toProcess.contains(PROJECTS_DELIMITER)) {
				area = Synapse.ProfileArea.PROJECTS;
				projectFilter = ProjectFilterEnum.ALL; 
				toProcess = toProcess.substring(PROJECTS_DELIMITER.length());
				if (toProcess.length() > 0) {
					if (toProcess.contains(FAV_PROJECTS_DELIMITER)){
						projectFilter = ProjectFilterEnum.FAVORITES;
					} else if (toProcess.contains(CREATED_BY_ME_DELIMITER)){
						projectFilter = ProjectFilterEnum.CREATED_BY_ME;
					} else if (toProcess.contains(SHARED_DIRECTLY_WITH_ME_PROJECTS_DELIMITER)){
						projectFilter = ProjectFilterEnum.SHARED_DIRECTLY_WITH_ME;
					} else if (toProcess.contains(ALL_MY_TEAM_PROJECTS_DELIMITER)){
						projectFilter = ProjectFilterEnum.ALL_MY_TEAM_PROJECTS;
					} else if (toProcess.contains(TEAM_PROJECTS_DELIMITER)){
						projectFilter = ProjectFilterEnum.TEAM;
						toProcess = toProcess.substring(TEAM_PROJECTS_DELIMITER.length());
						slashIndex = toProcess.indexOf(DELIMITER);
						if (slashIndex > -1) {
							teamId = toProcess.substring(slashIndex + 1);
						}
					}
				}
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
			//and by default go to the projects tab
			area = Synapse.ProfileArea.PROJECTS;
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
	
	public static String getDelimiter(Enum tab) {
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

	private void calculateToken(String userId, Synapse.ProfileArea area, ProjectFilterEnum projectFilter, String teamId) {
		this.token = userId;
		if(area != null) {
			this.token += getDelimiter(area);
			if (ProfileArea.PROJECTS.equals(area) && projectFilter != null) {
				token += getDelimiter(projectFilter);
				if (projectFilter.equals(ProjectFilterEnum.TEAM) && teamId != null) {
					token += DELIMITER + teamId;
				}
			}
		}
		
	}

}
