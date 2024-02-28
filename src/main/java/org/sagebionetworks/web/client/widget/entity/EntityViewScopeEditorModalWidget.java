package org.sagebionetworks.web.client.widget.entity;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import org.sagebionetworks.repo.model.Reference;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.jsinterop.EntityViewScopeEditorModalProps;
import org.sagebionetworks.web.client.jsinterop.SqlDefinedTableEditorModalProps;
import org.sagebionetworks.web.client.widget.table.modal.fileview.TableType;

public class EntityViewScopeEditorModalWidget implements IsWidget {

  private final GlobalApplicationState globalApplicationState;
  private final EntityViewScopeEditorModalWidgetView view;

  private String entityId;
  private EntityViewScopeEditorModalProps.Callback onCancel;
  private EntityViewScopeEditorModalProps.Callback onUpdate;
  boolean open;

  @Inject
  public EntityViewScopeEditorModalWidget(
    EntityViewScopeEditorModalWidgetView view,
    GlobalApplicationState globalApplicationState
  ) {
    super();
    this.view = view;
    this.globalApplicationState = globalApplicationState;
  }

  public void configure(
    String entityId,
    EntityViewScopeEditorModalProps.Callback onUpdate,
    EntityViewScopeEditorModalProps.Callback onCancel,
    boolean open
  ) {
    this.entityId = entityId;
    this.onUpdate = onUpdate;
    this.onCancel = onCancel;
    this.open = open;
    EntityViewScopeEditorModalProps props =
      EntityViewScopeEditorModalProps.create(
        entityId,
        onUpdate,
        onCancel,
        open
      );
    view.renderComponent(props);
  }

  public void setOpen(boolean open) {
    globalApplicationState.setIsEditing(open);
    EntityViewScopeEditorModalProps props =
      EntityViewScopeEditorModalProps.create(
        entityId,
        onUpdate,
        onCancel,
        open
      );
    view.renderComponent(props);
  }

  @Override
  public Widget asWidget() {
    return view.asWidget();
  }
}
