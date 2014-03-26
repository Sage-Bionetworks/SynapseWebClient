package org.sagebionetworks.web.shared;

import com.google.gwt.user.client.rpc.IsSerializable;

public class GroupMembershipState implements IsSerializable {
	Boolean isMember;
	String joinDate;
	
	public GroupMembershipState() {
	}
	
	public GroupMembershipState(Boolean isMember, String joinDate) {
		this.isMember = isMember;
		this.joinDate = joinDate;
	}


	public Boolean getIsMember() {
		return isMember;
	}
	public void setIsMember(Boolean isMember) {
		this.isMember = isMember;
	}
	
	public String getJoinDate() {
		return joinDate;
	}
	
	public void setJoinDate(String joinDate) {
		this.joinDate = joinDate;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((isMember == null) ? 0 : isMember.hashCode());
		result = prime * result
				+ ((joinDate == null) ? 0 : joinDate.hashCode());
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
		GroupMembershipState other = (GroupMembershipState) obj;
		if (isMember == null) {
			if (other.isMember != null)
				return false;
		} else if (!isMember.equals(other.isMember))
			return false;
		if (joinDate == null) {
			if (other.joinDate != null)
				return false;
		} else if (!joinDate.equals(other.joinDate))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "GroupMembershipState [isMember=" + isMember + ", joinDate="
				+ joinDate + "]";
	}
	
	
	
	
}
