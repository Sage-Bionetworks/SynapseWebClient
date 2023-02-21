package org.sagebionetworks.web.server;

import java.util.Properties;
import java.util.logging.Logger;
import org.sagebionetworks.SettingsLoader;

public class StackEndpoints {

  public static final String STAGING_SYNAPSE_ORG = "staging.synapse.org";
  public static final String TST_SYNAPSE_ORG = "tst.synapse.org";
  public static final String PORTAL_DEV_HOST = "portal-dev.dev.sagebase.org";
  private static Logger logger = Logger.getLogger(
    StackEndpoints.class.getName()
  );

  public static final String REPO_ENDPOINT_KEY =
    "org.sagebionetworks.repositoryservice.endpoint";
  public static final String FILE_ENDPOINT_KEY =
    "org.sagebionetworks.fileservice.endpoint";
  public static final String AUTH_ENDPOINT_KEY =
    "org.sagebionetworks.authenticationservice.publicendpoint";

  public static final String STACK_INSTANCE_PROPERTY_NAME =
    "org.sagebionetworks.stack.instance";
  public static final String STACK_PROPERTY_NAME = "org.sagebionetworks.stack";
  public static final String STACK_BEANSTALK_NUMBER_PROPERTY_NAME =
    "org.sagebionetworks.stack.repo.beanstalk.number";

  public static final String PARAM3 = "PARAM3";
  public static final String PARAM4 = "PARAM4";
  public static final String PARAM5 = "PARAM5";

  public static final String REPO_SUFFIX = "/repo/v1";
  public static final String FILE_SUFFIX = "/file/v1";
  public static final String AUTH_SUFFIX = "/auth/v1";

  private static boolean hasLoadedConfiguration = false;
  private static String endpointPrefixFromConfiguration = null;
  private static boolean loadSettingsFile = true;

  public static String getRepositoryServiceEndpoint(String host) {
    return getEndpoint(host, REPO_SUFFIX);
  }

  public static String getFileServiceEndpoint(String host) {
    return getEndpoint(host, FILE_SUFFIX);
  }

  public static String getAuthenticationServicePublicEndpoint(String host) {
    return getEndpoint(host, AUTH_SUFFIX);
  }

  public static String getEndpoint(String host, String suffix) {
    return getEndpointPrefix(host) + suffix;
  }

  private static String getEndpointPrefix(String host) {
    if (!hasLoadedConfiguration) {
      // init endpointPrefix
      if (loadSettingsFile) {
        // fallback to loading from settings
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
        } catch (Exception e) {
          e.printStackTrace();
        }
      }

      String repoEndpoint = System.getProperty(REPO_ENDPOINT_KEY);
      if (repoEndpoint != null) {
        // done, overwriting using old params
        endpointPrefixFromConfiguration =
          repoEndpoint.substring(0, repoEndpoint.indexOf("/repo/"));
      }
      hasLoadedConfiguration = true;
    }

    String endpointPrefix = endpointPrefixFromConfiguration;

    if (endpointPrefix == null) {
      // No configuration was specified, so look at the hostname of the incoming request
      if (STAGING_SYNAPSE_ORG.equals(host)) { // Staging
        endpointPrefix = "https://repo-staging.prod.sagebase.org";
      } else if (TST_SYNAPSE_ORG.equals(host)) { // tst
        endpointPrefix = "https://repo-tst.prod.sagebase.org";
      } else if (PORTAL_DEV_HOST.equals(host)) { // Dev instance
        endpointPrefix = "https://repo-dev.dev.sagebase.org";
      } else { // www.synapse.org, or some other host
        // None of these hosts match and no configuration was loaded from settings, fall back to prod
        endpointPrefix = "https://repo-prod.prod.sagebase.org";
      }
    }

    return endpointPrefix;
  }

  /**
   * For testing only
   */
  public static void clear() {
    hasLoadedConfiguration = false;
    endpointPrefixFromConfiguration = null;
  }

  public static void skipLoadingSettingsFile() {
    loadSettingsFile = false;
  }
}
