package org.sagebionetworks.web.shared;

import com.google.gwt.user.client.rpc.IsSerializable;


public class PublicPrincipalIds implements IsSerializable {
	private Long publicAclPrincipalId, authenticatedAclPrincipalId, anonymousUserId;

	public PublicPrincipalIds() {}

	public PublicPrincipalIds(Long publicAclPrincipalId, Long authenticatedAclPrincipalId, Long anonymousUserId) {
		super();
		this.publicAclPrincipalId = publicAclPrincipalId;
		this.authenticatedAclPrincipalId = authenticatedAclPrincipalId;
		this.anonymousUserId = anonymousUserId;
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

	public Long getAnonymousUserPrincipalId() {
		return anonymousUserId;
	}

	public void setAnonymousUserId(Long anonymousUserId) {
		this.anonymousUserId = anonymousUserId;
	}

	public boolean isPublic(Long principalId) {
		if (principalId == null)
			return false;
		return principalId.equals(getPublicAclPrincipalId()) || principalId.equals(getAuthenticatedAclPrincipalId()) || principalId.equals(getAnonymousUserPrincipalId());
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((anonymousUserId == null) ? 0 : anonymousUserId.hashCode());
		result = prime * result + ((authenticatedAclPrincipalId == null) ? 0 : authenticatedAclPrincipalId.hashCode());
		result = prime * result + ((publicAclPrincipalId == null) ? 0 : publicAclPrincipalId.hashCode());
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
		if (anonymousUserId == null) {
			if (other.anonymousUserId != null)
				return false;
		} else if (!anonymousUserId.equals(other.anonymousUserId))
			return false;
		if (authenticatedAclPrincipalId == null) {
			if (other.authenticatedAclPrincipalId != null)
				return false;
		} else if (!authenticatedAclPrincipalId.equals(other.authenticatedAclPrincipalId))
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
		return "PublicPrincipalIds [publicAclPrincipalId=" + publicAclPrincipalId + ", authenticatedAclPrincipalId=" + authenticatedAclPrincipalId + ", anonymousUserId=" + anonymousUserId + "]";
	}

}
