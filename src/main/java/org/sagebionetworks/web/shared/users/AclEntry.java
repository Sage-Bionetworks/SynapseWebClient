package org.sagebionetworks.web.shared.users;

import java.util.Set;

import org.sagebionetworks.repo.model.ACCESS_TYPE;
import org.sagebionetworks.repo.model.UserGroupHeader;

import com.google.gwt.user.client.rpc.IsSerializable;

public class AclEntry implements IsSerializable {

	private UserGroupHeader principal;
	private Set<ACCESS_TYPE> accessTypes;
	private boolean isOwner;
	
	public AclEntry() {}

	public AclEntry(UserGroupHeader principal, Set<ACCESS_TYPE> accessTypes, boolean isOwner) {
		super();
		this.principal = principal;
		this.accessTypes = accessTypes;
		this.isOwner = isOwner;
	}

	public UserGroupHeader getPrincipal() {
		return principal;
	}

	public void setPrincipal(UserGroupHeader principal) {
		this.principal = principal;
	}

	public Set<ACCESS_TYPE> getAccessTypes() {
		return accessTypes;
	}

	public void setAccessTypes(Set<ACCESS_TYPE> accessTypes) {
		this.accessTypes = accessTypes;
	}

	public boolean isOwner() {
		return isOwner;
	}

	public void setOwner(boolean isOwner) {
		this.isOwner = isOwner;
	}


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((accessTypes == null) ? 0 : accessTypes.hashCode());
		result = prime * result + (isOwner ? 1231 : 1237);
		result = prime * result
				+ ((principal == null) ? 0 : principal.hashCode());
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
		if (isOwner != other.isOwner)
			return false;
		if (principal == null) {
			if (other.principal != null)
				return false;
		} else if (!principal.equals(other.principal))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "AclEntry [principal=" + principal + ", accessTypes="
				+ accessTypes + "]";
	}
	

}
