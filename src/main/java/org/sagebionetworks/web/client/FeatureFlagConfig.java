package org.sagebionetworks.web.client;

public interface FeatureFlagConfig {
  boolean isFeatureEnabled(String featureName);
}
