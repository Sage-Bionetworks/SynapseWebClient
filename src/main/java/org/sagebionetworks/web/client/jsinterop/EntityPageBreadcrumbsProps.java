package org.sagebionetworks.web.client.jsinterop;

import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;

@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
public class EntityPageBreadcrumbsProps extends ReactComponentProps {

  BreadcrumbItem[] items;

  @JsOverlay
  public static EntityPageBreadcrumbsProps create(BreadcrumbItem[] items) {
    EntityPageBreadcrumbsProps props = new EntityPageBreadcrumbsProps();
    props.items = items;
    return props;
  }
}
