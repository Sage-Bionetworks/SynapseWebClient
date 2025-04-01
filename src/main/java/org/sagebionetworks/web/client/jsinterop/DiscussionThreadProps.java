package org.sagebionetworks.web.client.jsinterop;

import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;

@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
public class DiscussionThreadProps extends ReactComponentProps {

  public String threadId;
  public int limit;

  @JsOverlay
  public static DiscussionThreadProps create(String threadId, int limit) {
    DiscussionThreadProps props = new DiscussionThreadProps();
    props.threadId = threadId;
    props.limit = limit;
    return props;
  }
}
