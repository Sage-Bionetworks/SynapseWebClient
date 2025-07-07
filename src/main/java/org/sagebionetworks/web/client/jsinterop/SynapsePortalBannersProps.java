package org.sagebionetworks.web.client.jsinterop;

import jsinterop.annotations.JsNullable;
import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;

@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
public class SynapsePortalBannersProps extends ReactComponentProps {

  String entityId;

  @JsNullable
  String dataCatalogEntityId;

  @JsNullable
  String sourceAppConfigTableID;

  @JsOverlay
  public static SynapsePortalBannersProps create(
    String entityId,
    String dataCatalogEntityId,
    String sourceAppConfigTableID
  ) {
    SynapsePortalBannersProps props = new SynapsePortalBannersProps();
    props.entityId = entityId;
    props.dataCatalogEntityId = dataCatalogEntityId;
    props.sourceAppConfigTableID = sourceAppConfigTableID;
    return props;
  }

  @JsOverlay
  public static SynapsePortalBannersProps create(String entityId) {
    SynapsePortalBannersProps props = new SynapsePortalBannersProps();
    props.entityId = entityId;
    return props;
  }
}
