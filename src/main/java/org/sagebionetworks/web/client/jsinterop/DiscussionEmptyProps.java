package org.sagebionetworks.web.client.jsinterop;

import jsinterop.annotations.JsFunction;
import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;

@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
public class DiscussionEmptyProps extends ReactComponentProps {

  @JsFunction
  public interface Callback {
    void run();
  }

  public Callback onViewForumClicked;

  @JsOverlay
  public static DiscussionEmptyProps create(Callback onViewForumClicked) {
    DiscussionEmptyProps props = new DiscussionEmptyProps();
    props.onViewForumClicked = onViewForumClicked;
    return props;
  }
}
