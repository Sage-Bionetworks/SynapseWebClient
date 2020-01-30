package org.sagebionetworks.web.server.servlet;

import org.sagebionetworks.client.SynapseClient;
import org.sagebionetworks.client.SynapseClientImpl;
import org.sagebionetworks.client.SynapseProfileProxy;

/**
 * Very simple implementation.
 * 
 * @author John
 *
 */
public class SynapseProviderImpl implements SynapseProvider {
	@Override
	public SynapseClient createNewClient() {
		return SynapseRetryProxy.createProxy(SynapseProfileProxy.createProfileProxy(new SynapseClientImpl()));
	}


}
