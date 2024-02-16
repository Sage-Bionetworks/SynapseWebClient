package org.sagebionetworks.web.client.widget.entity;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.jsinterop.SqlDefinedTableEditorModalProps;

public class SqlDefinedEditorModalWidget implements IsWidget {

  private final GlobalApplicationState globalApplicationState;
  private final SqlDefinedEditorModalWidgetView view;

  private boolean open;

  private String entityId;
  private SqlDefinedTableEditorModalProps.OnCancel onCancel;
  private SqlDefinedTableEditorModalProps.OnUpdate onUpdate;

  @Inject
  public SqlDefinedEditorModalWidget(
    SqlDefinedEditorModalWidgetView view,
    GlobalApplicationState globalApplicationState
  ) {
    super();
    this.view = view;
    this.globalApplicationState = globalApplicationState;
  }

  public void configure(
    String entityId,
    SqlDefinedTableEditorModalProps.OnUpdate onUpdate,
    SqlDefinedTableEditorModalProps.OnCancel onCancel
  ) {
    this.entityId = entityId;
    this.onUpdate = onUpdate;
    this.onCancel = onCancel;
    SqlDefinedTableEditorModalProps props =
      SqlDefinedTableEditorModalProps.create(
        entityId,
        false,
        onUpdate,
        onCancel
      );
    view.renderComponent(props);
  }

  public void setOpen(boolean open) {
    globalApplicationState.setIsEditing(open);
    SqlDefinedTableEditorModalProps props =
      SqlDefinedTableEditorModalProps.create(
        entityId,
        open,
        onUpdate,
        onCancel
      );
    view.renderComponent(props);
  }

  @Override
  public Widget asWidget() {
    return view.asWidget();
  }
}
