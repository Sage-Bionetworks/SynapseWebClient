package org.sagebionetworks.web.client;

public class StackEndpoints {
	public static final String REPO_ENDPOINT_KEY = "org.sagebionetworks.repositoryservice.endpoint";
	public static final String FILE_ENDPOINT_KEY = "org.sagebionetworks.fileservice.endpoint";
	public static final String AUTH_ENDPOINT_KEY = "org.sagebionetworks.authenticationservice.publicendpoint";

	public static String getRepositoryServiceEndpoint() {
		return getProperty(REPO_ENDPOINT_KEY);
	}
	
	public static String getFileServiceEndpoint() {
		return getProperty(FILE_ENDPOINT_KEY);
	}
	
	public static String getAuthenticationServicePublicEndpoint() {
		return getProperty(AUTH_ENDPOINT_KEY);
	}
	
	public static String getProperty(String propertyName) {
		// is this defined in the application properties (can be overwritten in the maven settings file)
		String param1 = System.getProperty("PARAM1");
		//TODO: the default value is based on PARAM1
		String defaultValue = param1;
		return System.getProperty(propertyName, defaultValue);
	}
}
