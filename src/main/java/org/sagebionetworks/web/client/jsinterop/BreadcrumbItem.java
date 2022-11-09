package org.sagebionetworks.web.client.jsinterop;

import jsinterop.annotations.JsFunction;
import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;

@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
public class BreadcrumbItem extends ReactComponentProps {

  @JsFunction
  public interface OnClick {
    void onClick(ReactPointerEvent event);
  }

  /* The text to show in the breadcrumb item. Strings > 25 characters will be truncated */
  String text;
  /* Whether this item represents the current page. If true, this item will not have a link. Default false */
  boolean current;
  /* Link for the item */
  String href;
  /* Event handler fired when the link is clicked */
  BreadcrumbItem.OnClick onClick;

  @JsOverlay
  public static BreadcrumbItem create(
    String text,
    boolean current,
    String href,
    BreadcrumbItem.OnClick onClick
  ) {
    BreadcrumbItem props = new BreadcrumbItem();
    props.text = text;
    props.current = current;
    props.href = href;
    props.onClick = onClick;
    return props;
  }
}
