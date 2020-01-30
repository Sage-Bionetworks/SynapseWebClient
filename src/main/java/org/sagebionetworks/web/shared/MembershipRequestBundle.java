package org.sagebionetworks.web.shared;

import org.sagebionetworks.repo.model.MembershipRequest;
import org.sagebionetworks.repo.model.UserProfile;
import com.google.gwt.user.client.rpc.IsSerializable;

public class MembershipRequestBundle implements IsSerializable {

	private UserProfile userProfile;
	private MembershipRequest membershipRequest;

	/**
	 * Default constructor
	 */
	public MembershipRequestBundle() {

	}

	public MembershipRequestBundle(UserProfile userProfile, MembershipRequest membershipRequest) {
		super();
		this.userProfile = userProfile;
		this.membershipRequest = membershipRequest;
	}

	public UserProfile getUserProfile() {
		return userProfile;
	}

	public void setUserProfile(UserProfile userProfile) {
		this.userProfile = userProfile;
	}

	public MembershipRequest getMembershipRequest() {
		return membershipRequest;
	}

	public void setMembershipRequest(MembershipRequest membershipRequest) {
		this.membershipRequest = membershipRequest;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((membershipRequest == null) ? 0 : membershipRequest.hashCode());
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
		MembershipRequestBundle other = (MembershipRequestBundle) obj;
		if (membershipRequest == null) {
			if (other.membershipRequest != null)
				return false;
		} else if (!membershipRequest.equals(other.membershipRequest))
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
		return "MembershipRequestBundle [userProfile=" + userProfile + ", membershipRequest=" + membershipRequest + "]";
	}

}
