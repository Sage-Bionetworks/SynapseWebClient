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
		
}
