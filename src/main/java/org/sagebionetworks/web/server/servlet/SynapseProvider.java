package org.sagebionetworks.web.server.servlet;

import org.sagebionetworks.client.SynapseClient;

/**
 * Abstraction for creating a Synapse client;
 * 
 * @author John
 *
 */
public interface SynapseProvider {

	/**
	 * Create a new Synapse client.
	 * 
	 * @return
	 */
	public SynapseClient createNewClient();
}
