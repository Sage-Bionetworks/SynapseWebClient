package org.sagebionetworks.web.shared;

import org.sagebionetworks.repo.model.entitybundle.v2.EntityBundle;
import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * This class is to transfer an entity bundle, plus any additional data from service calls, into a
 * single servlet call from the client. Initially this is being used for the EntityBadge (tooltip)
 */
public class EntityBundlePlus implements IsSerializable {
	EntityBundle entityBundle;
	Long latestVersionNumber;

	@Override
	public String toString() {
		return "EntityBundlePlus [entityBundle=" + entityBundle + ", latestVersionNumber=" + latestVersionNumber + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((entityBundle == null) ? 0 : entityBundle.hashCode());
		result = prime * result + ((latestVersionNumber == null) ? 0 : latestVersionNumber.hashCode());
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
		if (latestVersionNumber == null) {
			if (other.latestVersionNumber != null)
				return false;
		} else if (!latestVersionNumber.equals(other.latestVersionNumber))
			return false;
		return true;
	}

	public Long getLatestVersionNumber() {
		return latestVersionNumber;
	}

	public void setLatestVersionNumber(Long latestVersionNumber) {
		this.latestVersionNumber = latestVersionNumber;
	}

	public EntityBundle getEntityBundle() {
		return entityBundle;
	}

	public void setEntityBundle(EntityBundle entityBundle) {
		this.entityBundle = entityBundle;
	}


}
