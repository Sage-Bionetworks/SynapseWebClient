package org.sagebionetworks.web.client.jsinterop.mui;

import jsinterop.annotations.JsNullable;
import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;
import org.sagebionetworks.web.client.jsinterop.PropsWithStyle;
import org.sagebionetworks.web.client.jsinterop.React;

@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
public class GridProps extends PropsWithStyle {

  @JsNullable
  String id;

  boolean container;

  @JsNullable
  int xs;

  @JsNullable
  int sm;

  @JsNullable
  int md;

  @JsNullable
  int lg;

  @JsNullable
  int xl;

  @JsNullable
  int xsOffset;

  @JsNullable
  int smOffset;

  @JsNullable
  int mdOffset;

  @JsNullable
  int lgOffset;

  @JsNullable
  int xlOffset;

  @JsNullable
  String mt;

  @JsNullable
  String pl;

  @JsNullable
  String rowSpacing;

  @JsNullable
  String columnSpacing;

  @JsOverlay
  public static GridProps create(boolean container) {
    GridProps props = new GridProps();
    props.ref = React.createRef();
    if (container) {
      props.container = true;
    }
    return props;
  }
}
