package org.sagebionetworks.web.client;

public enum FeatureFlagKey {
  PROVENANCE_V2_VISUALIZATION("Provenance v2 visualization");

  private final String key;

  FeatureFlagKey(String key) {
    this.key = key;
  }

  public String getKey() {
    return key;
  }
}
