package org.sagebionetworks.web.client.widget.evaluation;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.jsinterop.SubmissionViewScopeEditorModalProps;

public class SubmissionViewScopeEditorModalWidget implements IsWidget {

  private final GlobalApplicationState globalApplicationState;
  private final SubmissionViewScopeEditorModalWidgetView view;

  private String entityId;
  private SubmissionViewScopeEditorModalProps.Callback onCancel;
  private SubmissionViewScopeEditorModalProps.Callback onUpdate;

  @Inject
  public SubmissionViewScopeEditorModalWidget(
    SubmissionViewScopeEditorModalWidgetView view,
    GlobalApplicationState globalApplicationState
  ) {
    super();
    this.view = view;
    this.globalApplicationState = globalApplicationState;
  }

  public void configure(
    String entityId,
    SubmissionViewScopeEditorModalProps.Callback onUpdate,
    SubmissionViewScopeEditorModalProps.Callback onCancel
  ) {
    this.entityId = entityId;
    this.onUpdate = onUpdate;
    this.onCancel = onCancel;
    SubmissionViewScopeEditorModalProps props =
      SubmissionViewScopeEditorModalProps.create(
        entityId,
        onUpdate,
        onCancel,
        false
      );
    view.renderComponent(props);
  }

  public void setOpen(boolean open) {
    globalApplicationState.setIsEditing(open);
    SubmissionViewScopeEditorModalProps props =
      SubmissionViewScopeEditorModalProps.create(
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
