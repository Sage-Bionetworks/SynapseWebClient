package org.sagebionetworks.web.shared.users;

import java.util.Set;
import org.sagebionetworks.repo.model.ACCESS_TYPE;
import com.google.gwt.user.client.rpc.IsSerializable;

public class AclEntry implements IsSerializable {
	private Set<ACCESS_TYPE> accessTypes;
	private boolean isIndividual;
	private String ownerId, title, subtitle;

	public AclEntry() {}


	public AclEntry(String ownerId, Set<ACCESS_TYPE> accessTypes, String title, String subtitle, boolean isIndividual) {
		super();
		this.ownerId = ownerId;
		this.accessTypes = accessTypes;
		this.title = title;
		this.isIndividual = isIndividual;
		this.subtitle = subtitle;
	}

	public Set<ACCESS_TYPE> getAccessTypes() {
		return accessTypes;
	}

	public boolean isIndividual() {
		return isIndividual;
	}

	public void setAccessTypes(Set<ACCESS_TYPE> accessTypes) {
		this.accessTypes = accessTypes;
	}

	public String getOwnerId() {
		return ownerId;
	}

	public void setOwnerId(String ownerId) {
		this.ownerId = ownerId;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getSubtitle() {
		return subtitle;
	}

	public void setSubtitle(String subtitle) {
		this.subtitle = subtitle;
	}


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((accessTypes == null) ? 0 : accessTypes.hashCode());
		result = prime * result + (isIndividual ? 1231 : 1237);
		result = prime * result + ((ownerId == null) ? 0 : ownerId.hashCode());
		result = prime * result + ((subtitle == null) ? 0 : subtitle.hashCode());
		result = prime * result + ((title == null) ? 0 : title.hashCode());
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
		AclEntry other = (AclEntry) obj;
		if (accessTypes == null) {
			if (other.accessTypes != null)
				return false;
		} else if (!accessTypes.equals(other.accessTypes))
			return false;
		if (isIndividual != other.isIndividual)
			return false;
		if (ownerId == null) {
			if (other.ownerId != null)
				return false;
		} else if (!ownerId.equals(other.ownerId))
			return false;
		if (subtitle == null) {
			if (other.subtitle != null)
				return false;
		} else if (!subtitle.equals(other.subtitle))
			return false;
		if (title == null) {
			if (other.title != null)
				return false;
		} else if (!title.equals(other.title))
			return false;
		return true;
	}


	@Override
	public String toString() {
		return "AclEntry [accessTypes=" + accessTypes + ", isIndividual=" + isIndividual + ", ownerId=" + ownerId + ", title=" + title + ", subtitle=" + subtitle + "]";
	}


}
