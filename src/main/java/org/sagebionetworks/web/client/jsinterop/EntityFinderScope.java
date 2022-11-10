package org.sagebionetworks.web.client.jsinterop;

import jsinterop.annotations.JsEnum;
import jsinterop.annotations.JsPackage;

@JsEnum(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
public enum EntityFinderScope {
  ALL_PROJECTS("All Projects"),
  CURRENT_PROJECT("Current Project"),
  CREATED_BY_ME("Projects Created By Me"),
  FAVORITES("My Favorites");

  private final String value;

  EntityFinderScope(String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }
}
