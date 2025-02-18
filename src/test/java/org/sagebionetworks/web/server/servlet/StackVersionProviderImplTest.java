package org.sagebionetworks.web.server.servlet;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.sagebionetworks.client.SynapseClient;
import org.sagebionetworks.client.exceptions.SynapseException;
import org.sagebionetworks.repo.model.versionInfo.SynapseVersionInfo;
import org.sagebionetworks.web.shared.exceptions.RestServiceException;

@RunWith(MockitoJUnitRunner.class)
public class StackVersionProviderImplTest {

  @Mock
  SynapseProvider mockSynapseProvider;

  @Mock
  SynapseClient mockProdSynapseClient;

  @Mock
  SynapseClient mockStagingSynapseClient;

  @Mock
  SynapseVersionInfo mockSynapseVersionInfo;

  StackVersionProviderImpl stackVersionProviderImpl;

  @Before
  public void setUp() throws SynapseException {
    when(mockSynapseProvider.createNewClient("www.synapse.org"))
      .thenReturn(mockProdSynapseClient);
    when(mockSynapseProvider.createNewClient("staging.synapse.org"))
      .thenReturn(mockStagingSynapseClient);
    when(mockProdSynapseClient.getVersionInfo())
      .thenReturn(mockSynapseVersionInfo);
    when(mockStagingSynapseClient.getVersionInfo())
      .thenReturn(mockSynapseVersionInfo);
    when(mockSynapseVersionInfo.getVersion()).thenReturn("mockSynapseVersion");

    stackVersionProviderImpl =
      new StackVersionProviderImpl(mockSynapseProvider);
  }

  @Test
  public void testGetVersion() throws RestServiceException, SynapseException {
    String result = stackVersionProviderImpl.get("www.synapse.org");
    assertNotNull(result);

    verify(mockSynapseProvider).createNewClient("www.synapse.org");
    verify(mockProdSynapseClient).getVersionInfo();

    // Call again, make sure cache is used
    stackVersionProviderImpl.get("www.synapse.org");

    verify(mockSynapseProvider, times(1)).createNewClient("www.synapse.org");
    verify(mockProdSynapseClient, times(1)).getVersionInfo();

    // Call again with a different origin, cache should NOT be used
    stackVersionProviderImpl.get("staging.synapse.org");

    verify(mockSynapseProvider, times(1))
      .createNewClient("staging.synapse.org");
    verify(mockStagingSynapseClient, times(1)).getVersionInfo();
  }
}
