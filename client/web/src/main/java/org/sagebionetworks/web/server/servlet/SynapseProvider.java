package org.sagebionetworks.web.server.servlet;

import org.sagebionetworks.client.Synapse;

/**
 * Abstraction for creating a Synapse client;
 * @author John
 *
 */
public interface SynapseProvider {

	/**
	 * Create a new Synapse client.
	 * @return
	 */
	public Synapse createNewClient();
}
