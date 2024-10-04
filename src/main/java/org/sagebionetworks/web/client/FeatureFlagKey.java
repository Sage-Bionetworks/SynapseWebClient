package org.sagebionetworks.web.client;

public enum FeatureFlagKey {
  // If enabled, use the new React-based provenance visualization
  PROVENANCE_V2_VISUALIZATION("PROVENANCE_V2_VISUALIZATION"),

  // If enabled, a Google Map can be used to view the locations of team members on the team page
  GOOGLE_MAP("GOOGLE_MAP"),

  // If enabled, allows team members to view projects shared with the team on the team page
  VIEW_ASSOCIATED_PROJECTS("VIEW_ASSOCIATED_PROJECTS"),

  // If enabled, show provenance on Docker repository entity pages
  PROVENANCE_DOCKER_IMAGES("PROVENANCE_DOCKER_IMAGES"),

  // If enabled, allow viewing & editing the 'description' string field on entities
  DESCRIPTION_FIELD("DESCRIPTION_FIELD"),

  // Shows options to create various experimental wiki widgets
  ADD_WIKI_WIDGETS("ADD_WIKI_WIDGETS"),

  // Allow viewing diffs between wiki page versions
  WIKI_DIFF_TOOL("WIKI_DIFF_TOOL"),

  // Allow using STS and ExternalObjectStore options as custom Storage Locations
  CUSTOM_STORAGE_LOCATION_SETTINGS("CUSTOM_STORAGE_LOCATION_SETTINGS"),

  // Allow creating a challenge submission button and form with a JSON Schema
  CHALLENGE_SUBMISSION_SETTINGS("CHALLENGE_SUBMISSION_SETTINGS"),

  // If enabled, use the new ACL editor for Access Requirements
  SRC_BASED_AR_MODAL_WIZARD("SRC_BASED_AR_MODAL_WIZARD"),

  // If enabled, show the new homepage
  HOMEPAGE_V2("HOMEPAGE_V2"),

  // If enabled, use the re-implemented ACL Editor for entities
  REACT_ENTITY_ACL_EDITOR("REACT_ENTITY_ACL_EDITOR"),

  // If enabled, sharing settings will appear in a dialog immediately after uploading one or more files.
  SHOW_SHARING_SETTINGS_AFTER_UPLOAD("SHOW_SHARING_SETTINGS_AFTER_UPLOAD"),

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
