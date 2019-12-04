package org.sagebionetworks.web.shared;

import org.sagebionetworks.repo.model.MembershipInvitation;
import org.sagebionetworks.repo.model.Team;
import com.google.gwt.user.client.rpc.IsSerializable;

public class OpenUserInvitationBundle implements IsSerializable {

	private Team team;
	private MembershipInvitation membershipInvitation;


	public OpenUserInvitationBundle(Team team, MembershipInvitation membershipInvitation) {
		super();
		this.team = team;
		this.membershipInvitation = membershipInvitation;
	}

	public OpenUserInvitationBundle() {}

	public Team getTeam() {
		return team;
	}

	public void setTeam(Team team) {
		this.team = team;
	}

	public MembershipInvitation getMembershipInvitation() {
		return membershipInvitation;
	}

	public void setMembershipInvitation(MembershipInvitation membershipInvitation) {
		this.membershipInvitation = membershipInvitation;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((membershipInvitation == null) ? 0 : membershipInvitation.hashCode());
		result = prime * result + ((team == null) ? 0 : team.hashCode());
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
		OpenUserInvitationBundle other = (OpenUserInvitationBundle) obj;
		if (membershipInvitation == null) {
			if (other.membershipInvitation != null)
				return false;
		} else if (!membershipInvitation.equals(other.membershipInvitation))
			return false;
		if (team == null) {
			if (other.team != null)
				return false;
		} else if (!team.equals(other.team))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "OpenUserInvitationBundle [team=" + team + ", membershipInvitation=" + membershipInvitation + "]";
	}

}
