package org.sagebionetworks.web.shared;

import com.google.gwt.user.client.rpc.IsSerializable;


public class PublicPrincipalIds implements IsSerializable{
	private Long publicAclPrincipalId, authenticatedAclPrincipalId;

	public PublicPrincipalIds() {
	}
	
	public PublicPrincipalIds(Long publicAclPrincipalId,
			Long authenticatedAclPrincipalId) {
		super();
		this.publicAclPrincipalId = publicAclPrincipalId;
		this.authenticatedAclPrincipalId = authenticatedAclPrincipalId;
	}
	
	public Long getAuthenticatedAclPrincipalId() {
		return authenticatedAclPrincipalId;
	}
	
	public void setAuthenticatedAclPrincipalId(Long authenticatedAclPrincipalId) {
		this.authenticatedAclPrincipalId = authenticatedAclPrincipalId;
	}
	
	public Long getPublicAclPrincipalId() {
		return publicAclPrincipalId;
	}
	
	public void setPublicAclPrincipalId(Long publicAclPrincipalId) {
		this.publicAclPrincipalId = publicAclPrincipalId;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime
				* result
				+ ((authenticatedAclPrincipalId == null) ? 0
						: authenticatedAclPrincipalId.hashCode());
		result = prime
				* result
				+ ((publicAclPrincipalId == null) ? 0 : publicAclPrincipalId
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
		PublicPrincipalIds other = (PublicPrincipalIds) obj;
		if (authenticatedAclPrincipalId == null) {
			if (other.authenticatedAclPrincipalId != null)
				return false;
		} else if (!authenticatedAclPrincipalId
				.equals(other.authenticatedAclPrincipalId))
			return false;
		if (publicAclPrincipalId == null) {
			if (other.publicAclPrincipalId != null)
				return false;
		} else if (!publicAclPrincipalId.equals(other.publicAclPrincipalId))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "PublicPrincipalIds [publicAclPrincipalId="
				+ publicAclPrincipalId + ", authenticatedAclPrincipalId="
				+ authenticatedAclPrincipalId + "]";
	}
	
	
}
