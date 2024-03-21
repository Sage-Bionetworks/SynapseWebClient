package org.sagebionetworks.web.client.widget.table.modal.fileview;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import org.sagebionetworks.repo.model.Reference;
import org.sagebionetworks.repo.model.entitybundle.v2.EntityBundle;
import org.sagebionetworks.repo.model.table.EntityView;
import org.sagebionetworks.web.client.SynapseJavascriptClient;
import org.sagebionetworks.web.client.events.EntityUpdatedEvent;
import org.sagebionetworks.web.client.widget.SynapseWidgetPresenter;
import org.sagebionetworks.web.client.widget.entity.EntityViewScopeEditorModalWidget;

/**
 * All business logic for viewing and editing the EntityView scope.
 * <p>
 * <p>
 * <p>
 * Scope Widget - these are the UI output elements in this widget:
 * <p>
 * +-------------------------------------------+ |Scope | | | | (EntityContainerListWidget, not
 * editable)| | | | +----+ | | |Edit| (shown if widget set to editable) | | +----+ |
 * +------------------------------------+------+ | ^ | onEdit (show modal) | onSave (update view
 * scope) v | +--+---------------------------------+------+ |Edit Scope (modal) | | | | (Editable
 * EntityContainerListWidget) | | | | +------+ +----+ | | |Cancel| |Save| | | +------+ +----+ |
 * +-------------------------------------------+
 *
 * @author Jay
 */
public class EntityViewScopeWidget
  implements SynapseWidgetPresenter, EntityViewScopeWidgetView.Presenter {

  boolean isEditable;
  EntityViewScopeWidgetView view;
  SynapseJavascriptClient jsClient;
  EntityBundle bundle;
  EntityContainerListWidget viewScopeWidget;
  EntityViewScopeEditorModalWidget editEntityViewScopeModalWidget;
  EntityView currentView;
  TableType tableType;
  EventBus eventBus;

  /**
   * New presenter with its view.
   *
   * @param view
   */
  @Inject
  public EntityViewScopeWidget(
    EntityViewScopeWidgetView view,
    SynapseJavascriptClient jsClient,
    EntityContainerListWidget viewScopeWidget,
    EntityViewScopeEditorModalWidget editEntityViewScopeModalWidget,
    EventBus eventBus
  ) {
    this.jsClient = jsClient;
    this.view = view;
    this.viewScopeWidget = viewScopeWidget;
    this.editEntityViewScopeModalWidget = editEntityViewScopeModalWidget;
    this.eventBus = eventBus;
    view.setPresenter(this);
    view.setEditableEntityViewModalWidget(
      editEntityViewScopeModalWidget.asWidget()
    );
    view.setEntityListWidget(viewScopeWidget.asWidget());
  }

  private List<Reference> getReferencesFromIdList(List<String> ids) {
    if (ids == null) {
      ids = new ArrayList<>();
    }
    List<Reference> references = new ArrayList<>(ids.size());
    for (String entityId : ids) {
      Reference reference = new Reference();
      reference.setTargetId(entityId);
      references.add(reference);
    }
    return references;
  }

  public void configure(EntityBundle bundle, boolean isEditable) {
    this.isEditable = isEditable;
    this.bundle = bundle;
    boolean isVisible = false;
    if (bundle.getEntity() instanceof EntityView) {
      isVisible = true;

      currentView = (EntityView) bundle.getEntity();
      tableType = TableType.getTableType(currentView);
      List<Reference> references = getReferencesFromIdList(
        currentView.getScopeIds()
      );

      view.setEditMaskAndScopeButtonVisible(isEditable);
      viewScopeWidget.configure(references, false, tableType);
    }

    view.setVisible(isVisible);
  }

  @Override
  public Widget asWidget() {
    return view.asWidget();
  }

  @Override
  public void updateViewTypeMask() {
    tableType = TableType.getEntityViewTableType(
      view.isFileSelected(),
      view.isFolderSelected(),
      view.isTableSelected(),
      view.isDatasetSelected()
    );
  }

  @Override
  public void onEditScopeAndMask() {
    String entityId = currentView.getId();
    editEntityViewScopeModalWidget.configure(
      entityId,
      () -> {
        editEntityViewScopeModalWidget.setOpen(false);
        eventBus.fireEvent(new EntityUpdatedEvent(currentView.getId()));
      },
      () -> {
        editEntityViewScopeModalWidget.setOpen(false);
      }
    );
    editEntityViewScopeModalWidget.setOpen(true);
  }
}
