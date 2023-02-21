package org.sagebionetworks.web.unitserver;

import static org.junit.Assert.assertEquals;
import static org.sagebionetworks.web.server.StackEndpoints.AUTH_ENDPOINT_KEY;
import static org.sagebionetworks.web.server.StackEndpoints.AUTH_SUFFIX;
import static org.sagebionetworks.web.server.StackEndpoints.FILE_ENDPOINT_KEY;
import static org.sagebionetworks.web.server.StackEndpoints.FILE_SUFFIX;
import static org.sagebionetworks.web.server.StackEndpoints.PARAM3;
import static org.sagebionetworks.web.server.StackEndpoints.PARAM4;
import static org.sagebionetworks.web.server.StackEndpoints.PARAM5;
import static org.sagebionetworks.web.server.StackEndpoints.REPO_ENDPOINT_KEY;
import static org.sagebionetworks.web.server.StackEndpoints.REPO_SUFFIX;
import static org.sagebionetworks.web.server.StackEndpoints.STACK_BEANSTALK_NUMBER_PROPERTY_NAME;
import static org.sagebionetworks.web.server.StackEndpoints.STACK_INSTANCE_PROPERTY_NAME;
import static org.sagebionetworks.web.server.StackEndpoints.STACK_PROPERTY_NAME;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import org.sagebionetworks.web.server.StackEndpoints;

public class StackEndpointsTest {

  @Before
  public void setup() {
    clearProperties();
    StackEndpoints.skipLoadingSettingsFile();
  }

  @AfterClass
  public static void afterAll() {
    clearProperties();
  }

  private static void clearProperties() {
    System.clearProperty(REPO_ENDPOINT_KEY);
    System.clearProperty(FILE_ENDPOINT_KEY);
    System.clearProperty(AUTH_ENDPOINT_KEY);
    System.clearProperty(PARAM3);
    System.clearProperty(PARAM4);
    System.clearProperty(PARAM5);
    System.clearProperty(STACK_PROPERTY_NAME);
    System.clearProperty(STACK_INSTANCE_PROPERTY_NAME);
    System.clearProperty(STACK_BEANSTALK_NUMBER_PROPERTY_NAME);
    StackEndpoints.clear();
  }

  @Test
  public void testEndpointConstructionFromConfiguredEndpoint() {
    String requestHostName = "www.synapse.org";
    String endpointPrefix = "https://some-other-stack.prod.sagebase.org";

    System.setProperty(REPO_ENDPOINT_KEY, endpointPrefix + REPO_SUFFIX);

    assertEquals(
      endpointPrefix + FILE_SUFFIX,
      StackEndpoints.getFileServiceEndpoint(requestHostName)
    );
    assertEquals(
      endpointPrefix + AUTH_SUFFIX,
      StackEndpoints.getAuthenticationServicePublicEndpoint(requestHostName)
    );
    assertEquals(
      endpointPrefix + REPO_SUFFIX,
      StackEndpoints.getRepositoryServiceEndpoint(requestHostName)
    );
  }

  @Test
  public void testEndpointConstructionProduction() {
    String requestHostName = "www.synapse.org";
    String expected = "https://repo-prod.prod.sagebase.org";

    assertEquals(
      expected + FILE_SUFFIX,
      StackEndpoints.getFileServiceEndpoint(requestHostName)
    );
    assertEquals(
      expected + AUTH_SUFFIX,
      StackEndpoints.getAuthenticationServicePublicEndpoint(requestHostName)
    );
    assertEquals(
      expected + REPO_SUFFIX,
      StackEndpoints.getRepositoryServiceEndpoint(requestHostName)
    );
  }

  @Test
  public void testEndpointConstructionStaging() {
    String requestHostName = "staging.synapse.org";
    String expected = "https://repo-staging.prod.sagebase.org";

    assertEquals(
      expected + FILE_SUFFIX,
      StackEndpoints.getFileServiceEndpoint(requestHostName)
    );
    assertEquals(
      expected + AUTH_SUFFIX,
      StackEndpoints.getAuthenticationServicePublicEndpoint(requestHostName)
    );
    assertEquals(
      expected + REPO_SUFFIX,
      StackEndpoints.getRepositoryServiceEndpoint(requestHostName)
    );
  }

  @Test
  public void testEndpointConstructionTst() {
    String requestHostName = "tst.synapse.org";
    String expected = "https://repo-tst.prod.sagebase.org";

    assertEquals(
      expected + FILE_SUFFIX,
      StackEndpoints.getFileServiceEndpoint(requestHostName)
    );
    assertEquals(
      expected + AUTH_SUFFIX,
      StackEndpoints.getAuthenticationServicePublicEndpoint(requestHostName)
    );
    assertEquals(
      expected + REPO_SUFFIX,
      StackEndpoints.getRepositoryServiceEndpoint(requestHostName)
    );
  }

  @Test
  public void testEndpointConstructionDev() {
    String requestHostName = "portal-dev.dev.sagebase.org";
    String expected = "https://repo-dev.dev.sagebase.org";

    assertEquals(
      expected + FILE_SUFFIX,
      StackEndpoints.getFileServiceEndpoint(requestHostName)
    );
    assertEquals(
      expected + AUTH_SUFFIX,
      StackEndpoints.getAuthenticationServicePublicEndpoint(requestHostName)
    );
    assertEquals(
      expected + REPO_SUFFIX,
      StackEndpoints.getRepositoryServiceEndpoint(requestHostName)
    );
  }

  @Test
  public void testEndpointConstructionFallbackToProd() {
    String requestHostName = "some-other-host-for-the-portal.net";
    String expected = "https://repo-prod.prod.sagebase.org";

    assertEquals(
      expected + FILE_SUFFIX,
      StackEndpoints.getFileServiceEndpoint(requestHostName)
    );
    assertEquals(
      expected + AUTH_SUFFIX,
      StackEndpoints.getAuthenticationServicePublicEndpoint(requestHostName)
    );
    assertEquals(
      expected + REPO_SUFFIX,
      StackEndpoints.getRepositoryServiceEndpoint(requestHostName)
    );
  }
}
