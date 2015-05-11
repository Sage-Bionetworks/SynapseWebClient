package org.sagebionetworks.web.shared;

import org.sagebionetworks.repo.model.EntityBundle;
import org.sagebionetworks.repo.model.UserProfile;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * This class is to transfer an entity bundle, plus any additional data from service calls, into a single servlet call from the client.
 * Initially this is being used for the EntityBadge (tooltip)
 */
public class EntityBundlePlus implements IsSerializable {
	EntityBundle entityBundle;
	UserProfile profile;
	public EntityBundle getEntityBundle() {
		return entityBundle;
	}
	public void setEntityBundle(EntityBundle entityBundle) {
		this.entityBundle = entityBundle;
	}
	public UserProfile getProfile() {
		return profile;
	}
	public void setProfile(UserProfile profile) {
		this.profile = profile;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((entityBundle == null) ? 0 : entityBundle.hashCode());
		result = prime * result + ((profile == null) ? 0 : profile.hashCode());
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
		EntityBundlePlus other = (EntityBundlePlus) obj;
		if (entityBundle == null) {
			if (other.entityBundle != null)
				return false;
		} else if (!entityBundle.equals(other.entityBundle))
			return false;
		if (profile == null) {
			if (other.profile != null)
				return false;
		} else if (!profile.equals(other.profile))
			return false;
		return true;
	}
	@Override
	public String toString() {
		return "EntityBundlePlus [entityBundle=" + entityBundle + ", profile="
				+ profile + "]";
	}
	
}
