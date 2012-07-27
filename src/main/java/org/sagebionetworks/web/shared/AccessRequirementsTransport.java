package org.sagebionetworks.web.shared;

import com.google.gwt.user.client.rpc.IsSerializable;

public class AccessRequirementsTransport implements IsSerializable {
	private String entityString;
	private String entityClassAsString;
	private String accessRequirementsString;
	private String userProfileString;
	
	public String getEntityString() {
		return entityString;
	}
	public void setEntityString(String entityString) {
		this.entityString = entityString;
	}
	public String getEntityClassAsString() {
		return entityClassAsString;
	}
	public void setEntityClassAsString(String entityClassAsString) {
		this.entityClassAsString = entityClassAsString;
	}
	public String getAccessRequirementsString() {
		return accessRequirementsString;
	}
	public void setAccessRequirementsString(String accessRequirementsString) {
		this.accessRequirementsString = accessRequirementsString;
	}
	public String getUserProfileString() {
		return userProfileString;
	}
	public void setUserProfileString(String userProfileString) {
		this.userProfileString = userProfileString;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime
				* result
				+ ((accessRequirementsString == null) ? 0
						: accessRequirementsString.hashCode());
		result = prime
				* result
				+ ((entityClassAsString == null) ? 0 : entityClassAsString
						.hashCode());
		result = prime * result
				+ ((entityString == null) ? 0 : entityString.hashCode());
		result = prime
				* result
				+ ((userProfileString == null) ? 0 : userProfileString
						.hashCode());
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
		AccessRequirementsTransport other = (AccessRequirementsTransport) obj;
		if (accessRequirementsString == null) {
			if (other.accessRequirementsString != null)
				return false;
		} else if (!accessRequirementsString
				.equals(other.accessRequirementsString))
			return false;
		if (entityClassAsString == null) {
			if (other.entityClassAsString != null)
				return false;
		} else if (!entityClassAsString.equals(other.entityClassAsString))
			return false;
		if (entityString == null) {
			if (other.entityString != null)
				return false;
		} else if (!entityString.equals(other.entityString))
			return false;
		if (userProfileString == null) {
			if (other.userProfileString != null)
				return false;
		} else if (!userProfileString.equals(other.userProfileString))
			return false;
		return true;
	}
	@Override
	public String toString() {
		return "AccessRequirementsTransport [entityString=" + entityString
				+ ", entityClassAsString=" + entityClassAsString
				+ ", accessRequirementsString=" + accessRequirementsString
				+ ", userProfileString=" + userProfileString + "]";
	}

	
	
	
}
