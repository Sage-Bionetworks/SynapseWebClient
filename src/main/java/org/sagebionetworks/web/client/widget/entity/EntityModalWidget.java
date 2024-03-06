package org.sagebionetworks.web.client.widget.entity;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.jsinterop.EntityModalProps;
import org.sagebionetworks.web.client.jsinterop.SqlDefinedTableEditorModalProps;

public class EntityModalWidget implements IsWidget {

  private final GlobalApplicationState globalApplicationState;
  private final EntityModalWidgetView view;

  private String entityId;
  private Long versionNumber;
  private EntityModalProps.Callback onClose;
  private String initialTab;
  private boolean showTabs;

  @Inject
  public EntityModalWidget(
    EntityModalWidgetView view,
    GlobalApplicationState globalApplicationState
  ) {
    super();
    this.view = view;
    this.globalApplicationState = globalApplicationState;
  }

  private EntityModalProps getProps(boolean open) {
    return EntityModalProps.create(
      entityId,
      versionNumber,
      open,
      onClose,
      initialTab,
      showTabs,
      globalApplicationState::setIsEditing
    );
  }

  public void configure(
    String entityId,
    Long versionNumber,
    EntityModalProps.Callback onClose,
    String initialTab,
    boolean showTabs
  ) {
    this.entityId = entityId;
    this.versionNumber = versionNumber;
    this.onClose = onClose;
    this.initialTab = initialTab;
    this.showTabs = showTabs;

    view.renderComponent(getProps(false));
  }

  public void setOpen(boolean open) {
    view.renderComponent(getProps(open));

    // The dialog can be opened without editing data, so we shouldn't toggle `setIsEditing` on open.
    // However, if the dialog is closed, we are definitely no longer editing
    if (!open) {
      globalApplicationState.setIsEditing(false);
    }
  }

  @Override
  public Widget asWidget() {
    return view.asWidget();
  }
}
