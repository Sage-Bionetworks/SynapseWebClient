package org.sagebionetworks.web.shared;

import org.sagebionetworks.repo.model.MembershipInvitation;
import org.sagebionetworks.repo.model.UserProfile;

import com.google.gwt.user.client.rpc.IsSerializable;

public class OpenTeamInvitationBundle implements IsSerializable {
	
	private MembershipInvitation MembershipInvitation;
	private UserProfile userProfile;
	
	public OpenTeamInvitationBundle(
			MembershipInvitation MembershipInvitation,
			UserProfile userProfile) {
		super();
		this.MembershipInvitation = MembershipInvitation;
		this.userProfile = userProfile;
	}
	public OpenTeamInvitationBundle() {
	}
	public MembershipInvitation getMembershipInvitation() {
		return MembershipInvitation;
	}
	public void setMembershipInvitation(
			MembershipInvitation MembershipInvitation) {
		this.MembershipInvitation = MembershipInvitation;
	}
	public UserProfile getUserProfile() {
		return userProfile;
	}
	public void setUserProfile(UserProfile userProfile) {
		this.userProfile = userProfile;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime
				* result
				+ ((MembershipInvitation == null) ? 0
						: MembershipInvitation.hashCode());
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
		OpenTeamInvitationBundle other = (OpenTeamInvitationBundle) obj;
		if (MembershipInvitation == null) {
			if (other.MembershipInvitation != null)
				return false;
		} else if (!MembershipInvitation
				.equals(other.MembershipInvitation))
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
		return "OpenTeamInvitationBundle [MembershipInvitation="
				+ MembershipInvitation + ", userProfile=" + userProfile
				+ "]";
	}

}
