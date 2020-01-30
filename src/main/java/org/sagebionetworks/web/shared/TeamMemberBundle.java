package org.sagebionetworks.web.shared;

import org.sagebionetworks.repo.model.UserProfile;
import com.google.gwt.user.client.rpc.IsSerializable;

public class TeamMemberBundle implements IsSerializable {

	private UserProfile userProfile;
	private Boolean isAdmin;
	private String teamId;

	public TeamMemberBundle() {}

	public TeamMemberBundle(UserProfile userProfile, Boolean isAdmin, String teamId) {
		super();
		this.userProfile = userProfile;
		this.isAdmin = isAdmin;
		this.teamId = teamId;
	}

	public Boolean getIsTeamAdmin() {
		return isAdmin;
	}

	public void setIsAdmin(Boolean isAdmin) {
		this.isAdmin = isAdmin;
	}

	public UserProfile getUserProfile() {
		return userProfile;
	}

	public void setUserProfile(UserProfile userProfile) {
		this.userProfile = userProfile;
	}

	public String getTeamId() {
		return teamId;
	}

	public void setTeamId(String teamId) {
		this.teamId = teamId;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((isAdmin == null) ? 0 : isAdmin.hashCode());
		result = prime * result + ((teamId == null) ? 0 : teamId.hashCode());
		result = prime * result + ((userProfile == null) ? 0 : userProfile.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		TeamMemberBundle other = (TeamMemberBundle) obj;
		if (isAdmin == null) {
			if (other.isAdmin != null)
				return false;
		} else if (!isAdmin.equals(other.isAdmin))
			return false;
		if (teamId == null) {
			if (other.teamId != null)
				return false;
		} else if (!teamId.equals(other.teamId))
			return false;
		if (userProfile == null) {
			if (other.userProfile != null)
				return false;
		} else if (!userProfile.equals(other.userProfile))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "TeamMemberBundle [userProfile=" + userProfile + ", isAdmin=" + isAdmin + ", teamId=" + teamId + "]";
	}


}
