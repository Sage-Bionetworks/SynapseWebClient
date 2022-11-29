package org.sagebionetworks.web.client.jsinterop.entity.actionmenu;

import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;

@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
public class EntityActionMenuDropdownMap {

  private EntityActionMenuDropdownConfiguration DOWNLOAD;
  private EntityActionMenuDropdownConfiguration PRIMARY;

  @JsOverlay
  public static EntityActionMenuDropdownMap create(
    EntityActionMenuDropdownConfiguration download,
    EntityActionMenuDropdownConfiguration primary
  ) {
    EntityActionMenuDropdownMap map = new EntityActionMenuDropdownMap();
    map.DOWNLOAD = download;
    map.PRIMARY = primary;
    return map;
  }

  @JsOverlay
  public final EntityActionMenuDropdownConfiguration getDownloadMenuConfiguration() {
    return DOWNLOAD;
  }

  @JsOverlay
  public final void setDownloadMenuConfiguration(
    EntityActionMenuDropdownConfiguration config
  ) {
    this.DOWNLOAD = config;
  }

  @JsOverlay
  public final EntityActionMenuDropdownConfiguration getPrimaryMenuConfiguration() {
    return PRIMARY;
  }

  @JsOverlay
  public final void setPrimaryMenuConfiguration(
    EntityActionMenuDropdownConfiguration config
  ) {
    this.PRIMARY = config;
  }
}
