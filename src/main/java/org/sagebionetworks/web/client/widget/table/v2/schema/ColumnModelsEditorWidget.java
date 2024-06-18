package org.sagebionetworks.web.client.widget.table.v2.schema;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.jsinterop.TableColumnSchemaEditorProps;

public class ColumnModelsEditorWidget implements IsWidget {

  private final ColumnModelsEditorWidgetView view;

  private final GlobalApplicationState globalApplicationState;

  private String entityId;
  private TableColumnSchemaEditorProps.OnColumnsUpdated onColumnsUpdated;
  private TableColumnSchemaEditorProps.OnCancel onCancel;

  @Inject
  public ColumnModelsEditorWidget(
    ColumnModelsEditorWidgetView view,
    GlobalApplicationState globalApplicationState
  ) {
    super();
    this.view = view;
    this.globalApplicationState = globalApplicationState;
  }

  public void configure(
    String entityId,
    TableColumnSchemaEditorProps.OnColumnsUpdated onColumnsUpdated,
    TableColumnSchemaEditorProps.OnCancel onCancel
  ) {
    this.entityId = entityId;
    this.onColumnsUpdated = onColumnsUpdated;
    this.onCancel = onCancel;
    TableColumnSchemaEditorProps props = TableColumnSchemaEditorProps.create(
      entityId,
      false,
      onColumnsUpdated,
      onCancel
    );

    view.renderComponent(props);
  }

  public void setOpen(boolean open) {
    globalApplicationState.setIsEditing(open);
    TableColumnSchemaEditorProps props = TableColumnSchemaEditorProps.create(
      entityId,
      open,
      onColumnsUpdated,
      onCancel
    );
    view.renderComponent(props);
  }

  @Override
  public Widget asWidget() {
    return view.asWidget();
  }
}
