package org.sagebionetworks.web.client;

import static org.junit.Assert.assertEquals;
import static org.sagebionetworks.web.client.StackEndpoints.AUTH_ENDPOINT_KEY;
import static org.sagebionetworks.web.client.StackEndpoints.AUTH_SUFFIX;
import static org.sagebionetworks.web.client.StackEndpoints.FILE_ENDPOINT_KEY;
import static org.sagebionetworks.web.client.StackEndpoints.FILE_SUFFIX;
import static org.sagebionetworks.web.client.StackEndpoints.PARAM3;
import static org.sagebionetworks.web.client.StackEndpoints.PARAM4;
import static org.sagebionetworks.web.client.StackEndpoints.PARAM5;
import static org.sagebionetworks.web.client.StackEndpoints.REPO_ENDPOINT_KEY;
import static org.sagebionetworks.web.client.StackEndpoints.REPO_SUFFIX;
import static org.sagebionetworks.web.client.StackEndpoints.STACK_BEANSTALK_NUMBER_PROPERTY_NAME;
import static org.sagebionetworks.web.client.StackEndpoints.STACK_INSTANCE_PROPERTY_NAME;
import static org.sagebionetworks.web.client.StackEndpoints.STACK_PROPERTY_NAME;
import org.junit.Before;
import org.junit.Test;

public class StackEndpointsTest {
	@Before
	public void setup() {
		clearProperties();
		StackEndpoints.skipLoadingSettingsFile();
	}

	private void clearProperties() {
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
	public void testEndpointConstructionFromRepoEndpoint() {
		String endpointPrefix = "https://repo-staging.prod.sagebase.org";
		System.setProperty(REPO_ENDPOINT_KEY, endpointPrefix + REPO_SUFFIX);
		assertEquals(endpointPrefix + FILE_SUFFIX, StackEndpoints.getFileServiceEndpoint());
		assertEquals(endpointPrefix + AUTH_SUFFIX, StackEndpoints.getAuthenticationServicePublicEndpoint());
		assertEquals(endpointPrefix + REPO_SUFFIX, StackEndpoints.getRepositoryServiceEndpoint());
	}

	@Test
	public void testEndpointConstructionFromParams() {
		String param3 = "dev";
		String param4 = "800";
		String param5 = "1";
		System.setProperty(PARAM3, param3);
		System.setProperty(PARAM4, param4);
		System.setProperty(PARAM5, param5);

		String endpointPrefix = "https://repo-dev-800-1.dev.sagebase.org";
		assertEquals(endpointPrefix + FILE_SUFFIX, StackEndpoints.getFileServiceEndpoint());
		assertEquals(endpointPrefix + AUTH_SUFFIX, StackEndpoints.getAuthenticationServicePublicEndpoint());
		assertEquals(endpointPrefix + REPO_SUFFIX, StackEndpoints.getRepositoryServiceEndpoint());
	}

	@Test
	public void testEndpointConstructionFromNamedParams() {
		String stackName = "prod";
		String instance = "hill";
		String beanstalkNumber = "2";
		System.setProperty(STACK_PROPERTY_NAME, stackName);
		System.setProperty(STACK_INSTANCE_PROPERTY_NAME, instance);
		System.setProperty(STACK_BEANSTALK_NUMBER_PROPERTY_NAME, beanstalkNumber);

		String endpointPrefix = "https://repo-prod-hill-2.prod.sagebase.org";
		assertEquals(endpointPrefix + FILE_SUFFIX, StackEndpoints.getFileServiceEndpoint());
		assertEquals(endpointPrefix + AUTH_SUFFIX, StackEndpoints.getAuthenticationServicePublicEndpoint());
		assertEquals(endpointPrefix + REPO_SUFFIX, StackEndpoints.getRepositoryServiceEndpoint());
	}
}
