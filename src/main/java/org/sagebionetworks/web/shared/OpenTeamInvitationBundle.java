package org.sagebionetworks.web.shared;

import org.sagebionetworks.repo.model.MembershipInvtnSubmission;
import org.sagebionetworks.repo.model.UserProfile;

import com.google.gwt.user.client.rpc.IsSerializable;

public class OpenTeamInvitationBundle implements IsSerializable {
	
	private MembershipInvtnSubmission membershipInvtnSubmission;
	private UserProfile userProfile;
	
	public OpenTeamInvitationBundle(
			MembershipInvtnSubmission membershipInvtnSubmission,
			UserProfile userProfile) {
		super();
		this.membershipInvtnSubmission = membershipInvtnSubmission;
		this.userProfile = userProfile;
	}
	public OpenTeamInvitationBundle() {
	}
	public MembershipInvtnSubmission getMembershipInvtnSubmission() {
		return membershipInvtnSubmission;
	}
	public void setMembershipInvtnSubmission(
			MembershipInvtnSubmission membershipInvtnSubmission) {
		this.membershipInvtnSubmission = membershipInvtnSubmission;
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
				+ ((membershipInvtnSubmission == null) ? 0
						: membershipInvtnSubmission.hashCode());
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
		if (membershipInvtnSubmission == null) {
			if (other.membershipInvtnSubmission != null)
				return false;
		} else if (!membershipInvtnSubmission
				.equals(other.membershipInvtnSubmission))
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
		return "OpenTeamInvitationBundle [membershipInvtnSubmission="
				+ membershipInvtnSubmission + ", userProfile=" + userProfile
				+ "]";
	}

}
