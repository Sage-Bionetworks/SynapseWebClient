package org.sagebionetworks.web.shared.users;

import java.util.List;

import org.sagebionetworks.repo.model.ACCESS_TYPE;

import com.google.gwt.user.client.rpc.IsSerializable;

public class AclEntry implements IsSerializable {

	private AclPrincipal principal;
	private List<ACCESS_TYPE> accessTypes;
	
	public AclEntry() {
		
	}

	public AclEntry(AclPrincipal principal, List<ACCESS_TYPE> accessTypes) {
		super();
		this.principal = principal;
		this.accessTypes = accessTypes;
	}

	public AclPrincipal getPrincipal() {
		return principal;
	}

	public void setPrincipal(AclPrincipal principal) {
		this.principal = principal;
	}

	public List<ACCESS_TYPE> getAccessTypes() {
		return accessTypes;
	}

	public void setAccessTypes(List<ACCESS_TYPE> accessTypes) {
		this.accessTypes = accessTypes;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((accessTypes == null) ? 0 : accessTypes.hashCode());
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
