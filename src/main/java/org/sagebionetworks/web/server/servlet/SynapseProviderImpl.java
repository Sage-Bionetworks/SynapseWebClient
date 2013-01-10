package org.sagebionetworks.web.server.servlet;

import org.sagebionetworks.client.Synapse;

/**
 * Very simple implementation.
 * @author John
 *
 */
public class SynapseProviderImpl implements SynapseProvider {
	

	@Override
	public Synapse createNewClient() {
		return new Synapse();
		// ONE LINE CHANGE TO USE STUB SYNAPSE CLIENT:		
		// return SynapseClientStubUtil.createSynapseClient();		
	}

}
