package org.sagebionetworks.web.shared;

import org.sagebionetworks.repo.model.MembershipInvitation;
import org.sagebionetworks.repo.model.UserProfile;
import com.google.gwt.user.client.rpc.IsSerializable;

public class OpenTeamInvitationBundle implements IsSerializable {

	private MembershipInvitation membershipInvitation;
	private UserProfile userProfile;

	public OpenTeamInvitationBundle(MembershipInvitation membershipInvitation, UserProfile userProfile) {
		super();
		this.membershipInvitation = membershipInvitation;
		this.userProfile = userProfile;
	}

	public OpenTeamInvitationBundle() {}

	public MembershipInvitation getMembershipInvitation() {
		return membershipInvitation;
	}

	public void setMembershipInvitation(MembershipInvitation membershipInvitation) {
		this.membershipInvitation = membershipInvitation;
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
		result = prime * result + ((membershipInvitation == null) ? 0 : membershipInvitation.hashCode());
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
		OpenTeamInvitationBundle other = (OpenTeamInvitationBundle) obj;
		if (membershipInvitation == null) {
			if (other.membershipInvitation != null)
				return false;
		} else if (!membershipInvitation.equals(other.membershipInvitation))
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
		return "OpenTeamInvitationBundle [membershipInvitation=" + membershipInvitation + ", userProfile=" + userProfile + "]";
	}

}
