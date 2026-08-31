package org.sagebionetworks.web.client;

public enum FeatureFlagKey {
  // Shows options to create various experimental wiki widgets
  ADD_WIKI_WIDGETS("ADD_WIKI_WIDGETS"),

  // If enabled, search bar uses OpenSearch
  OPENSEARCH_ENABLED("OPENSEARCH_ENABLED"),

  // If enabled, show the 'Start Grid Session' button in the action menu for table entities
  SYNAPSE_GRID("SYNAPSE_GRID"),

  // If enabled, show the visibility chip on the project title bar
  PROJECT_VISIBILITY_CHIP("PROJECT_VISIBILITY_CHIP"),

  // Last flag is used only for tests
  TEST_FLAG_ONLY("TEST_FLAG_ONLY");

  private final String key;

  FeatureFlagKey(String key) {
    this.key = key;
  }

  public String getKey() {
    return key;
  }
}
