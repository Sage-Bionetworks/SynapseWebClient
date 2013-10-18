package org.sagebionetworks.web.shared;

import com.google.gwt.user.client.rpc.IsSerializable;

public class MembershipRequestBundle implements IsSerializable {

	private String userProfileJson, membershipRequestJson;
	
	/**
	 * Default constructor
	 */
	public MembershipRequestBundle() {
		
	}

	public MembershipRequestBundle(String userProfileJson, String membershipRequestJson) {
		super();
		this.userProfileJson = userProfileJson;
		this.membershipRequestJson = membershipRequestJson;
	}

	public String getMembershipRequestJson() {
		return membershipRequestJson;
	}
	public void setMembershipRequestJson(String membershipRequestJson) {
		this.membershipRequestJson = membershipRequestJson;
	}
	public String getUserProfileJson() {
		return userProfileJson;
	}
	public void setUserProfileJson(String userProfileJson) {
		this.userProfileJson = userProfileJson;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime
				* result
				+ ((membershipRequestJson == null) ? 0 : membershipRequestJson
						.hashCode());
		result = prime * result
				+ ((userProfileJson == null) ? 0 : userProfileJson.hashCode());
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
		MembershipRequestBundle other = (MembershipRequestBundle) obj;
		if (membershipRequestJson == null) {
			if (other.membershipRequestJson != null)
				return false;
		} else if (!membershipRequestJson.equals(other.membershipRequestJson))
			return false;
		if (userProfileJson == null) {
			if (other.userProfileJson != null)
				return false;
		} else if (!userProfileJson.equals(other.userProfileJson))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "MembershipRequestBundle [userProfileJson=" + userProfileJson
				+ ", membershipRequestJson=" + membershipRequestJson + "]";
	}
	
	
}
