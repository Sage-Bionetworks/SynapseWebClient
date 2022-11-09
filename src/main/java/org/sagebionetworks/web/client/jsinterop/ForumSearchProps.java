package org.sagebionetworks.web.client.jsinterop;

import jsinterop.annotations.JsFunction;
import jsinterop.annotations.JsNullable;
import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;

@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
public class ForumSearchProps extends ReactComponentProps {

  String forumId;

  @JsNullable
  String projectId;

  @FunctionalInterface
  @JsFunction
  public interface OnSearchResultsVisibleHandler {
    void onUpdate(boolean visible);
  }

  @JsNullable
  OnSearchResultsVisibleHandler onSearchResultsVisible;

  @JsOverlay
  public static ForumSearchProps create(
    String forumId,
    String projectId,
    OnSearchResultsVisibleHandler onSearchResultsVisible
  ) {
    ForumSearchProps props = new ForumSearchProps();
    props.forumId = forumId;
    props.projectId = projectId;
    props.onSearchResultsVisible = onSearchResultsVisible;
    return props;
  }
}
