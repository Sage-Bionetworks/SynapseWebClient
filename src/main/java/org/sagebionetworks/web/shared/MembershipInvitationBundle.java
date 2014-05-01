package org.sagebionetworks.web.shared;

import com.google.gwt.user.client.rpc.IsSerializable;

public class MembershipInvitationBundle implements IsSerializable {

	private String teamJson, membershipInvitationJson, userProfileJson;
	
	/**
	 * Default constructor
	 */
	public MembershipInvitationBundle() {
		
	}

	public MembershipInvitationBundle(String teamJson, String userProfileJson, String membershipInvitationJson) {
		super();
		this.teamJson = teamJson;
		this.membershipInvitationJson = membershipInvitationJson;
		this.userProfileJson = userProfileJson;
	}

	public String getTeamJson() {
		return teamJson;
	}

	public void setTeamJson(String teamJson) {
		this.teamJson = teamJson;
	}
	
	public String getUserProfileJson() {
		return userProfileJson;
	}
	
	public void setUserProfileJson(String userProfileJson) {
		this.userProfileJson = userProfileJson;
	}
	
	public String getMembershipInvitationJson() {
		return membershipInvitationJson;
	}
	public void setMembershipInvitationJson(String membershipInvitationJson) {
		this.membershipInvitationJson = membershipInvitationJson;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime
				* result
				+ ((membershipInvitationJson == null) ? 0
						: membershipInvitationJson.hashCode());
		result = prime * result
				+ ((teamJson == null) ? 0 : teamJson.hashCode());
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
		if (membershipInvitationJson == null) {
			if (other.membershipInvitationJson != null)
				return false;
		} else if (!membershipInvitationJson
				.equals(other.membershipInvitationJson))
			return false;
		if (teamJson == null) {
			if (other.teamJson != null)
				return false;
		} else if (!teamJson.equals(other.teamJson))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "MembershipInvitationBundle [teamJson=" + teamJson
				+ ", membershipInvitationJson=" + membershipInvitationJson
				+ "]";
	}
}
