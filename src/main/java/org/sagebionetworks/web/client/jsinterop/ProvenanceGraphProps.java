package org.sagebionetworks.web.client.jsinterop;

import com.google.gwt.core.client.JavaScriptObject;
import java.util.List;
import jsinterop.annotations.JsFunction;
import jsinterop.annotations.JsNullable;
import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;
import org.sagebionetworks.repo.model.Reference;

@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
public class ProvenanceGraphProps extends ReactComponentProps {

  ReferenceJsObject[] entityRefs;
  String containerHeight;

  @JsNullable
  JavaScriptObject initialNodes;

  @JsNullable
  JavaScriptObject initialEdges;

  @FunctionalInterface
  @JsFunction
  public interface OnUpdateJavaScriptObject {
    void onUpdate(JavaScriptObject jsObject);
  }

  @JsNullable
  OnUpdateJavaScriptObject onNodesChangedListener;

  @JsNullable
  OnUpdateJavaScriptObject onEdgesChangedListener;

  @JsOverlay
  public static ProvenanceGraphProps create(
    List<Reference> refs,
    String containerHeight,
    JavaScriptObject initialNodes,
    JavaScriptObject initialEdges,
    OnUpdateJavaScriptObject nodesListener,
    OnUpdateJavaScriptObject edgesListener
  ) {
    ProvenanceGraphProps props = new ProvenanceGraphProps();
    props.entityRefs = new ReferenceJsObject[refs.size()];
    for (int i = 0; i < refs.size(); i++) {
      ReferenceJsObject newRef = ReferenceJsObject.create(
        refs.get(i).getTargetId(),
        refs.get(i).getTargetVersionNumber()
      );
      props.entityRefs[i] = newRef;
    }

    props.containerHeight = containerHeight;
    props.initialNodes = initialNodes;
    props.initialEdges = initialEdges;
    props.onNodesChangedListener = nodesListener;
    props.onEdgesChangedListener = edgesListener;
    return props;
  }
}
