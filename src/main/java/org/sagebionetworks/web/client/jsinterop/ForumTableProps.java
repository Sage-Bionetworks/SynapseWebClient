package org.sagebionetworks.web.client.jsinterop;

import jsinterop.annotations.JsFunction;
import jsinterop.annotations.JsNullable;
import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;

@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
public class ForumTableProps extends ReactComponentProps {

  @FunctionalInterface
  @JsFunction
  public interface OnClickLinkHandler {
    void onClickLink(String threadId, String projectId);
  }

  @FunctionalInterface
  @JsFunction
  public interface OnViewForumClickedHandler {
    void onViewForumClicked();
  }

  @FunctionalInterface
  @JsFunction
  public interface OnIsEmptyChangeHandler {
    void onIsEmptyChange(boolean isEmpty);
  }

  @JsNullable
  String forumId;

  @JsNullable
  String entityId;

  @JsNullable
  String filter;

  int limit;

  OnClickLinkHandler onClickLink;

  @JsNullable
  OnViewForumClickedHandler onViewForumClicked;

  boolean noBackgroundColor;

  @JsNullable
  OnIsEmptyChangeHandler onIsEmptyChange;

  @JsNullable
  String maxHeight;

  /**
   * Provide either entityId, or forumId and filter.
   * onViewForumClicked is only used with entityId.
   */
  @JsOverlay
  public static ForumTableProps create(
    String forumId,
    String entityId,
    String filter,
    int limit,
    OnClickLinkHandler onClickLink,
    OnViewForumClickedHandler onViewForumClicked,
    boolean noBackgroundColor,
    OnIsEmptyChangeHandler onIsEmptyChange,
    String maxHeight
  ) {
    ForumTableProps props = new ForumTableProps();
    if (forumId != null) {
      props.forumId = forumId;
    }
    if (entityId != null) {
      props.entityId = entityId;
    }
    if (filter != null) {
      props.filter = filter;
    }
    props.limit = limit;
    props.onClickLink = onClickLink;
    props.onViewForumClicked = onViewForumClicked;
    props.noBackgroundColor = noBackgroundColor;
    props.onIsEmptyChange = onIsEmptyChange;
    if (maxHeight != null) {
      props.maxHeight = maxHeight;
    }
    return props;
  }
}
