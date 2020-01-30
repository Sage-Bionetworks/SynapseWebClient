package org.sagebionetworks.web.client;

import java.io.IOException;
import java.util.Properties;
import org.jdom.JDOMException;
import org.sagebionetworks.SettingsLoader;

public class StackEndpoints {
	public static final String REPO_ENDPOINT_KEY = "org.sagebionetworks.repositoryservice.endpoint";
	public static final String FILE_ENDPOINT_KEY = "org.sagebionetworks.fileservice.endpoint";
	public static final String AUTH_ENDPOINT_KEY = "org.sagebionetworks.authenticationservice.publicendpoint";

	public static final String STACK_INSTANCE_PROPERTY_NAME = "org.sagebionetworks.stack.instance";
	public static final String STACK_PROPERTY_NAME = "org.sagebionetworks.stack";
	public static final String STACK_BEANSTALK_NUMBER_PROPERTY_NAME = "org.sagebionetworks.stack.repo.beanstalk.number";

	public static final String PARAM3 = "PARAM3";
	public static final String PARAM4 = "PARAM4";
	public static final String PARAM5 = "PARAM5";

	public static final String REPO_SUFFIX = "/repo/v1";
	public static final String FILE_SUFFIX = "/file/v1";
	public static final String AUTH_SUFFIX = "/auth/v1";

	private static String endpointPrefix = null;
	private static boolean loadSettingsFile = true;

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
		return getEndpointPrefix() + suffix;
	}

	private static String getEndpointPrefix() {
		if (endpointPrefix == null) {
			// init endpoint prefix
			if (loadSettingsFile) {
				try {
					// override any properties with the m2 settings property values (if set)
					Properties props = SettingsLoader.loadSettingsFile();
					if (props != null) {
						for (Object propertyName : props.keySet()) {
							String value = (String) props.get(propertyName);
							if (value != null && value.length() > 0) {
								System.setProperty((String) propertyName, value);
							}
						}
					}
				} catch (IOException e) {
					e.printStackTrace();
				} catch (JDOMException e) {
					e.printStackTrace();
				}
			}

			String repoEndpoint = System.getProperty(REPO_ENDPOINT_KEY);
			if (repoEndpoint != null) {
				// done, overwriting using old params
				endpointPrefix = repoEndpoint.substring(0, repoEndpoint.indexOf("/repo/"));
			} else {
				// get stack name, like "prod" or "dev"
				String stackName = System.getProperty(STACK_PROPERTY_NAME);
				if (stackName == null) {
					stackName = System.getProperty(PARAM3);
				}
				// get stack instance, like "222"
				String stackInstance = System.getProperty(STACK_INSTANCE_PROPERTY_NAME);
				if (stackInstance == null) {
					stackInstance = System.getProperty(PARAM4);
				}

				// get beanstalk number, like "0" or "1"
				String stack = System.getProperty(STACK_BEANSTALK_NUMBER_PROPERTY_NAME);
				if (stack == null) {
					stack = System.getProperty(PARAM5);
					if (stack == null) {
						stack = "0";
					}
				}

				// put it all together. like https://repo-prod-222-0.prod.sagebase.org
				endpointPrefix = "https://repo-" + stackName + "-" + stackInstance + "-" + stack + "." + stackName + ".sagebase.org";
			}
		}
		return endpointPrefix;
	}

	/**
	 * For testing only
	 */
	public static void clear() {
		endpointPrefix = null;
	}

	public static void skipLoadingSettingsFile() {
		loadSettingsFile = false;
	}

}
