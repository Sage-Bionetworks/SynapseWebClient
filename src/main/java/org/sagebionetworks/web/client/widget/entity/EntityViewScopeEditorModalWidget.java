package org.sagebionetworks.web.client.widget.entity;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.jsinterop.EntityViewScopeEditorModalProps;

public class EntityViewScopeEditorModalWidget implements IsWidget {

  private final GlobalApplicationState globalApplicationState;
  private final EntityViewScopeEditorModalWidgetView view;

  private String entityId;
  private EntityViewScopeEditorModalProps.Callback onCancel;
  private EntityViewScopeEditorModalProps.Callback onUpdate;

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
    EntityViewScopeEditorModalProps.Callback onCancel
  ) {
    this.entityId = entityId;
    this.onUpdate = onUpdate;
    this.onCancel = onCancel;
    EntityViewScopeEditorModalProps props =
      EntityViewScopeEditorModalProps.create(
        entityId,
        onUpdate,
        onCancel,
        false
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
