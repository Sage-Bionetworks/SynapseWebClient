package org.sagebionetworks.web.client;

import static org.junit.Assert.*;
import static org.sagebionetworks.web.client.StackEndpoints.*;

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
}
