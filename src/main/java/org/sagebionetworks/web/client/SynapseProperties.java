package org.sagebionetworks.web.client;

import org.sagebionetworks.web.shared.PublicPrincipalIds;

public interface SynapseProperties {
	void initSynapseProperties();
	String getSynapseProperty(String key);
	PublicPrincipalIds getPublicPrincipalIds();
}
