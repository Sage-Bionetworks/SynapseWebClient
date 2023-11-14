package org.sagebionetworks.web.client.jsinterop;

import java.util.List;
import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;
import org.sagebionetworks.repo.model.EntityType;
import org.sagebionetworks.repo.model.table.ColumnModel;
import org.sagebionetworks.repo.model.table.ViewScope;

@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
public class TableColumnSchemaFormProps extends ReactComponentProps {

  String entityType;
  Object viewScope;
  Object[] initialData;
  ReactRef<TableColumnSchemaFormRef> ref;

  @JsOverlay
  public static TableColumnSchemaFormProps create(
    EntityType entityType,
    ViewScope viewScope,
    List<ColumnModel> initialData,
    ReactRef<TableColumnSchemaFormRef> ref
  ) {
    TableColumnSchemaFormProps props = new TableColumnSchemaFormProps();
    props.entityType = entityType.name();
    props.viewScope = JSONEntityUtils.toJsInteropCompatibleObject(viewScope);
    props.initialData =
      initialData
        .stream()
        .map(JSONEntityUtils::toJsInteropCompatibleObject)
        .toArray();
    props.ref = ref;
    return props;
  }
}
