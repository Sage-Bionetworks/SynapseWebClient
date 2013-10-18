package org.sagebionetworks.web.shared;

import com.google.gwt.user.client.rpc.IsSerializable;

public class TeamBundle implements IsSerializable {

	private String teamJson;
	private String teamMembershipStatusJson;
	private Long totalMemberCount;
	private boolean isUserAdmin;
	/**
	 * Default constructor
	 */
	public TeamBundle() {
		
	}

	public TeamBundle(String teamJson, Long totalMemberCount,
			String teamMembershipStatusJson, boolean isUserAdmin) {
		super();
		this.teamJson = teamJson;
		this.teamMembershipStatusJson = teamMembershipStatusJson;
		this.totalMemberCount = totalMemberCount;
		this.isUserAdmin = isUserAdmin;
	}

	public String getTeamJson() {
		return teamJson;
	}

	public void setTeamJson(String teamJson) {
		this.teamJson = teamJson;
	}
	public String getTeamMembershipStatusJson() {
		return teamMembershipStatusJson;
	}
	public void setTeamMembershipStatusJson(String teamMembershipStatusJson) {
		this.teamMembershipStatusJson = teamMembershipStatusJson;
	}
	public boolean isUserAdmin() {
		return isUserAdmin;
	}
	public void setIsUserAdmin(boolean isUserAdmin) {
		this.isUserAdmin = isUserAdmin;
	}
	public Long getTotalMemberCount() {
		return totalMemberCount;
	}
	public void setTotalMemberCount(Long totalMemberCount) {
		this.totalMemberCount = totalMemberCount;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (isUserAdmin ? 1231 : 1237);
		result = prime * result
				+ ((teamJson == null) ? 0 : teamJson.hashCode());
		result = prime
				* result
				+ ((teamMembershipStatusJson == null) ? 0
						: teamMembershipStatusJson.hashCode());
		result = prime
				* result
				+ ((totalMemberCount == null) ? 0 : totalMemberCount.hashCode());
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
		TeamBundle other = (TeamBundle) obj;
		if (isUserAdmin != other.isUserAdmin)
			return false;
		if (teamJson == null) {
			if (other.teamJson != null)
				return false;
		} else if (!teamJson.equals(other.teamJson))
			return false;
		if (teamMembershipStatusJson == null) {
			if (other.teamMembershipStatusJson != null)
				return false;
		} else if (!teamMembershipStatusJson
				.equals(other.teamMembershipStatusJson))
			return false;
		if (totalMemberCount == null) {
			if (other.totalMemberCount != null)
				return false;
		} else if (!totalMemberCount.equals(other.totalMemberCount))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "TeamBundle [teamJson=" + teamJson
				+ ", teamMembershipStateJson=" + teamMembershipStatusJson
				+ ", totalMemberCount=" + totalMemberCount + ", isUserAdmin="
				+ isUserAdmin + "]";
	}
	
}
