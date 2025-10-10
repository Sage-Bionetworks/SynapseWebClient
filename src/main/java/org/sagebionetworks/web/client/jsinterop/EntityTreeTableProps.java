package org.sagebionetworks.web.client.jsinterop;

import jsinterop.annotations.JsFunction;
import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;

@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
public class EntityTreeTableProps extends ReactComponentProps {

  public String rootId;
  public boolean expandRootByDefault;
  public boolean showRootNode;
  public boolean enableSorting;

  @JsFunction
  public interface Callback {
    void run(String entityId);
  }

  public Callback onEntityIdClicked;

  @JsOverlay
  private static EntityTreeTableProps create(
    String rootEntityId,
    Callback onEntityIdClicked,
    boolean expandRootByDefault,
    boolean showRootNode,
    boolean enableSorting
  ) {
    EntityTreeTableProps props = new EntityTreeTableProps();
    props.rootId = rootEntityId;
    props.onEntityIdClicked = onEntityIdClicked;
    props.expandRootByDefault = expandRootByDefault;
    props.showRootNode = showRootNode;
    props.enableSorting = enableSorting;
    return props;
  }

  @JsOverlay
  public static EntityTreeTableProps create(
    String rootEntityId,
    Callback onEntityIdClicked
  ) {
    return create(rootEntityId, onEntityIdClicked, true, false, true);
  }
}
