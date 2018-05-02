package org.sagebionetworks.web.client;

import java.io.IOException;

import org.jdom.JDOMException;

public class StackEndpoints {
	public static final String REPO_ENDPOINT_KEY = "org.sagebionetworks.repositoryservice.endpoint";
	public static final String FILE_ENDPOINT_KEY = "org.sagebionetworks.fileservice.endpoint";
	public static final String AUTH_ENDPOINT_KEY = "org.sagebionetworks.authenticationservice.publicendpoint";

	public static final String STACK_INSTANCE_PROPERTY_NAME = "org.sagebionetworks.stack.instance";
	public static final String STACK_PROPERTY_NAME = "org.sagebionetworks.stack";

	public static final String PARAM3 = "PARAM3";
	public static final String PARAM4 = "PARAM4";
	public static final String PARAM5 = "PARAM5";

	public static final String REPO_SUFFIX = "/repo/v1";
	public static final String FILE_SUFFIX = "/file/v1";
	public static final String AUTH_SUFFIX = "/auth/v1";

	private static String endpointPrefix = null;

	public static String getRepositoryServiceEndpoint() {
		return getEndpoint(REPO_ENDPOINT_KEY, REPO_SUFFIX);
	}

	public static String getFileServiceEndpoint() {
		return getEndpoint(FILE_ENDPOINT_KEY, FILE_SUFFIX);
	}

	public static String getAuthenticationServicePublicEndpoint() {
		return getEndpoint(AUTH_ENDPOINT_KEY, AUTH_SUFFIX);
	}

	public static String getEndpoint(String propertyName, String suffix) {
		// is this defined in the application properties (can be overwritten in the
		// maven settings file)
		String value = System.getProperty(propertyName);
		if (value == null) {
			// not overwritten, build it up using other properties
			value = getEndpointPrefix() + suffix;
		}
		return value;
	}

	private static String getEndpointPrefix() {
		if (endpointPrefix == null) {
			// init endpoint prefix
			try {
				//override any properties with the m2 settings property values (if available)
				System.setProperties(SettingsLoader.loadSettingsFile());
			} catch (IOException e) {
				e.printStackTrace();
			} catch (JDOMException e) {
				e.printStackTrace();
			}
			// get stack name, like "prod" or "dev"
			String stackName = System.getProperty(PARAM3);
			if (stackName == null) {
				stackName = System.getProperty(STACK_PROPERTY_NAME);
			}
			// get stack instance, like "225"
			String stackInstance = System.getProperty(PARAM4);
			if (stackInstance == null) {
				stackInstance = System.getProperty(STACK_INSTANCE_PROPERTY_NAME);
			}

			// get stack number, like "0" or "1"
			String stack = System.getProperty(PARAM5);
			if (stack == null) {
				stack = "0";
			}

			// put it all together.  like https://repo-prod-225-0.prod.sagebase.org
			endpointPrefix = "https://repo-" + stackName + "-" + stackInstance + "-" + stack + ".prod.sagebase.org";
		}
		return endpointPrefix;
	}
}
