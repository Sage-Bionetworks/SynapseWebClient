package org.sagebionetworks.web.shared;

import org.sagebionetworks.repo.model.Team;
import org.sagebionetworks.repo.model.TeamMembershipStatus;
import com.google.gwt.user.client.rpc.IsSerializable;

public class TeamBundle implements IsSerializable {

	private Team team;
	private TeamMembershipStatus teamMembershipStatus;
	private Long totalMemberCount;
	private boolean isUserAdmin;

	/**
	 * Default constructor
	 */
	public TeamBundle() {

	}

	public TeamBundle(Team team, Long totalMemberCount, TeamMembershipStatus teamMembershipStatus, boolean isUserAdmin) {
		super();
		this.team = team;
		this.teamMembershipStatus = teamMembershipStatus;
		this.totalMemberCount = totalMemberCount;
		this.isUserAdmin = isUserAdmin;
	}

	public Team getTeam() {
		return team;
	}

	public void setTeam(Team team) {
		this.team = team;
	}

	public TeamMembershipStatus getTeamMembershipStatus() {
		return teamMembershipStatus;
	}

	public void setTeamMembershipStatus(TeamMembershipStatus teamMembershipStatus) {
		this.teamMembershipStatus = teamMembershipStatus;
	}

	public void setUserAdmin(boolean isUserAdmin) {
		this.isUserAdmin = isUserAdmin;
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
		result = prime * result + ((team == null) ? 0 : team.hashCode());
		result = prime * result + ((teamMembershipStatus == null) ? 0 : teamMembershipStatus.hashCode());
		result = prime * result + ((totalMemberCount == null) ? 0 : totalMemberCount.hashCode());
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
		if (team == null) {
			if (other.team != null)
				return false;
		} else if (!team.equals(other.team))
			return false;
		if (teamMembershipStatus == null) {
			if (other.teamMembershipStatus != null)
				return false;
		} else if (!teamMembershipStatus.equals(other.teamMembershipStatus))
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
		return "TeamBundle [team=" + team + ", teamMembershipStatus=" + teamMembershipStatus + ", totalMemberCount=" + totalMemberCount + ", isUserAdmin=" + isUserAdmin + "]";
	}

}
