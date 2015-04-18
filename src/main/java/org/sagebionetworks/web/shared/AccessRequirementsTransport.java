package org.sagebionetworks.web.shared;



import org.sagebionetworks.repo.model.AccessRequirement;
import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.repo.model.UserProfile;

import com.google.gwt.user.client.rpc.IsSerializable;

public class AccessRequirementsTransport implements IsSerializable {
	private Entity entity;
	private PaginatedResults<AccessRequirement> accessRequirements;
	private UserProfile userProfile;
	
	
	public Entity getEntity() {
		return entity;
	}
	public void setEntity(Entity entity) {
		this.entity = entity;
	}
	public PaginatedResults<AccessRequirement> getAccessRequirements() {
		return accessRequirements;
	}
	public void setAccessRequirements(
			PaginatedResults<AccessRequirement> accessRequirements) {
		this.accessRequirements = accessRequirements;
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
				+ ((accessRequirements == null) ? 0 : accessRequirements
						.hashCode());
		result = prime * result + ((entity == null) ? 0 : entity.hashCode());
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
		AccessRequirementsTransport other = (AccessRequirementsTransport) obj;
		if (accessRequirements == null) {
			if (other.accessRequirements != null)
				return false;
		} else if (!accessRequirements.equals(other.accessRequirements))
			return false;
		if (entity == null) {
			if (other.entity != null)
				return false;
		} else if (!entity.equals(other.entity))
			return false;
		if (userProfile == null) {
			if (other.userProfile != null)
				return false;
		} else if (!userProfile.equals(other.userProfile))
			return false;
		return true;
	}
	
}
