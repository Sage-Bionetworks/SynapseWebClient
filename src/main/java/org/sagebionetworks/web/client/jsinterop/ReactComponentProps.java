package org.sagebionetworks.web.client.jsinterop;

import com.google.gwt.dom.client.Element;
import jsinterop.annotations.JsConstructor;
import jsinterop.annotations.JsFunction;
import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;

@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
public class ReactComponentProps {

  @JsConstructor
  public ReactComponentProps() {}

  @JsFunction
  public interface CallbackRef {
    void run(Element element);
  }

  public JsArray<ReactElement<?>> children;

  // Either a ComponentRef or CallbackRef may be passed. A CallbackRef will be invoked when the ref is set.
  public Object ref;

  @JsOverlay
  public final void addChild(ReactElement<?> child) {
    if (children == null) {
      children = new JsArray<>();
    }
    children.push(child);
  }

  @JsOverlay
  public final void clearChildren() {
    children = new JsArray<>();
  }

  @JsOverlay
  public final JsArray<ReactElement<?>> getChildren() {
    if (children == null) {
      children = new JsArray<>();
    }
    return children;
  }
}
