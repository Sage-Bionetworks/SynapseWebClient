package org.sagebionetworks.web.server.servlet;

import org.sagebionetworks.client.SynapseClient;
import org.sagebionetworks.client.SynapseClientImpl;
import org.sagebionetworks.client.SynapseProfileProxy;
import org.sagebionetworks.web.server.StackEndpoints;

/**
 * Very simple implementation.
 *
 * @author John
 *
 */
public class SynapseProviderImpl implements SynapseProvider {

  @Override
  public SynapseClient createNewClient() {
    return SynapseRetryProxy.createProxy(
      SynapseProfileProxy.createProfileProxy(new SynapseClientImpl())
    );
  }

  @Override
  public SynapseClient createNewClient(String portalRequestHost) {
    SynapseClient client = SynapseRetryProxy.createProxy(
      SynapseProfileProxy.createProfileProxy(new SynapseClientImpl())
    );
    client.setAuthEndpoint(
      StackEndpoints.getAuthenticationServicePublicEndpoint(portalRequestHost)
    );
    client.setRepositoryEndpoint(
      StackEndpoints.getRepositoryServiceEndpoint(portalRequestHost)
    );
    client.setFileEndpoint(
      StackEndpoints.getFileServiceEndpoint(portalRequestHost)
    );
    return client;
  }
}
