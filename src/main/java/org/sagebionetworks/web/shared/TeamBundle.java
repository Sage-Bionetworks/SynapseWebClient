package org.sagebionetworks.web.shared;

import com.google.gwt.user.client.rpc.IsSerializable;

public class TeamBundle implements IsSerializable {

	private String teamJson;
	private String userMembershipState;
	private Long totalMemberCount;
	private boolean isUserAdmin;
	/**
	 * Default constructor
	 */
	public TeamBundle() {
		
	}

	public TeamBundle(String teamJson, Long totalMemberCount,
			String userMembershipState, boolean isUserAdmin) {
		super();
		this.teamJson = teamJson;
		this.userMembershipState = userMembershipState;
		this.totalMemberCount = totalMemberCount;
		this.isUserAdmin = isUserAdmin;
	}

	public String getTeamJson() {
		return teamJson;
	}

	public void setTeamJson(String teamJson) {
		this.teamJson = teamJson;
	}

	public String getUserMembershipState() {
		return userMembershipState;
	}
	public void setUserMembershipState(String userMembershipState) {
		this.userMembershipState = userMembershipState;
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
				+ ((totalMemberCount == null) ? 0 : totalMemberCount.hashCode());
		result = prime
				* result
				+ ((userMembershipState == null) ? 0 : userMembershipState
						.hashCode());
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
		if (totalMemberCount == null) {
			if (other.totalMemberCount != null)
				return false;
		} else if (!totalMemberCount.equals(other.totalMemberCount))
			return false;
		if (userMembershipState == null) {
			if (other.userMembershipState != null)
				return false;
		} else if (!userMembershipState.equals(other.userMembershipState))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "TeamBundle [teamJson=" + teamJson + ", userMembershipState="
				+ userMembershipState + ", totalMemberCount="
				+ totalMemberCount + ", isUserAdmin=" + isUserAdmin + "]";
	}
}
