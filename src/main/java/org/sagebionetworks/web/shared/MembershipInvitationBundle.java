package org.sagebionetworks.web.shared;

import org.sagebionetworks.repo.model.MembershipInvitation;
import org.sagebionetworks.repo.model.MembershipInvtnSubmission;
import org.sagebionetworks.repo.model.Team;
import org.sagebionetworks.repo.model.UserProfile;

import com.google.gwt.user.client.rpc.IsSerializable;

public class MembershipInvitationBundle implements IsSerializable {

	private Team team;
	private MembershipInvtnSubmission membershipInvtnSubmission;
	private MembershipInvitation membershipInvitation;
	private UserProfile userProfile;
	
	/**
	 * Default constructor
	 */
	public MembershipInvitationBundle() {
		
	}

	public MembershipInvitationBundle(MembershipInvtnSubmission membershipInvtnSubmission,
			UserProfile userProfile) {
		super();
		this.membershipInvtnSubmission = membershipInvtnSubmission;
		this.userProfile = userProfile;
	}

	public MembershipInvitationBundle(Team team, MembershipInvitation invite) {
		this.team = team;
		this.membershipInvitation = invite;
	}

	public MembershipInvitation getMembershipInvitation() {
		return membershipInvitation;
	}

	public void setMembershipInvitation(MembershipInvitation membershipInvitation) {
		this.membershipInvitation = membershipInvitation;
	}

	public Team getTeam() {
		return team;
	}

	public void setTeam(Team team) {
		this.team = team;
	}
	public UserProfile getUserProfile() {
		return userProfile;
	}

	public void setUserProfile(UserProfile userProfile) {
		this.userProfile = userProfile;
	}

	public MembershipInvtnSubmission getMembershipInvtnSubmission() {
		return membershipInvtnSubmission;
	}

	public void setMembershipInvtnSubmission(
			MembershipInvtnSubmission membershipInvtnSubmission) {
		this.membershipInvtnSubmission = membershipInvtnSubmission;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime
				* result
				+ ((membershipInvtnSubmission == null) ? 0
						: membershipInvtnSubmission.hashCode());
		result = prime * result + ((team == null) ? 0 : team.hashCode());
		result = prime * result
				+ ((userProfile == null) ? 0 : userProfile.hashCode());
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
		MembershipInvitationBundle other = (MembershipInvitationBundle) obj;
		if (membershipInvtnSubmission == null) {
			if (other.membershipInvtnSubmission != null)
				return false;
		} else if (!membershipInvtnSubmission
				.equals(other.membershipInvtnSubmission))
			return false;
		if (team == null) {
			if (other.team != null)
				return false;
		} else if (!team.equals(other.team))
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
		return "MembershipInvitationBundle [team=" + team
				+ ", membershipInvtnSubmission=" + membershipInvtnSubmission
				+ ", userProfile=" + userProfile + "]";
	}

}
