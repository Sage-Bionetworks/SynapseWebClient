package org.sagebionetworks.web.shared.users;

public class AclPrincipal {
	private Long principalId;
	private boolean isIndividual;
	private String displayName;
	private boolean isOwner;
	public Long getPrincipalId() {
		return principalId;
	}
	public void setPrincipalId(Long principalId) {
		this.principalId = principalId;
	}
	public boolean isIndividual() {
		return isIndividual;
	}
	public void setIndividual(boolean isIndividual) {
		this.isIndividual = isIndividual;
	}
	public String getDisplayName() {
		return displayName;
	}
	public void setDisplayName(String displayName) {
		this.displayName = displayName;
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
				+ ((principalId == null) ? 0 : principalId.hashCode());
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
		AclPrincipal other = (AclPrincipal) obj;
		if (principalId == null) {
			if (other.principalId != null)
				return false;
		} else if (!principalId.equals(other.principalId))
			return false;
		return true;
	}
	@Override
	public String toString() {
		return displayName;
	}
	
	
}
