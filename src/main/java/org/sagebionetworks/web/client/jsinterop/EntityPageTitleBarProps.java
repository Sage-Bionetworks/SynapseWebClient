package org.sagebionetworks.web.client.jsinterop;

import jsinterop.annotations.JsFunction;
import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;
import org.sagebionetworks.web.client.jsinterop.entity.actionmenu.EntityActionMenuPropsJsInterop;
import org.sagebionetworks.web.client.widget.entity.menu.v3.EntityActionMenuProps;

@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
public class EntityPageTitleBarProps extends ReactComponentProps {

  @JsFunction
  public interface Callback {
    void run();
  }

  String entityId;
  long versionNumber;
  Callback onActMemberClickAddConditionsForUse;
  EntityActionMenuPropsJsInterop entityActionMenuProps;

  @JsOverlay
  public static EntityPageTitleBarProps create(String targetId) {
    EntityPageTitleBarProps props = new EntityPageTitleBarProps();
    props.entityId = targetId;
    return props;
  }

  @JsOverlay
  public static EntityPageTitleBarProps create(
    String targetId,
    long targetVersionNumber
  ) {
    EntityPageTitleBarProps props = create(targetId);
    props.versionNumber = targetVersionNumber;
    return props;
  }

  @JsOverlay
  public final void setEntityActionMenuProps(
    EntityActionMenuProps entityActionMenuProps
  ) {
    this.entityActionMenuProps = entityActionMenuProps.toJsInterop();
  }

  @JsOverlay
  public final void setOnActMemberClickAddConditionsForUse(Callback callback) {
    this.onActMemberClickAddConditionsForUse = callback;
  }

  @JsOverlay
  public final EntityActionMenuPropsJsInterop getEntityActionMenuProps() {
    return entityActionMenuProps;
  }

  @JsOverlay
  public final String getEntityId() {
    return entityId;
  }

  @JsOverlay
  public final long getVersionNumber() {
    return versionNumber;
  }

  @JsOverlay
  public final Callback getOnActMemberClickAddConditionsForUse() {
    return onActMemberClickAddConditionsForUse;
  }
}
