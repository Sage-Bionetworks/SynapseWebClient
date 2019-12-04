package org.sagebionetworks.web.client;

import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.shared.PublicPrincipalIds;

public interface SynapseProperties {
	void initSynapseProperties(Callback c);

	String getSynapseProperty(String key);

	PublicPrincipalIds getPublicPrincipalIds();
}
