package org.sagebionetworks.web.client.widget.table.v2.schema;

import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import org.sagebionetworks.repo.model.entitybundle.v2.EntityBundle;
import org.sagebionetworks.repo.model.table.ColumnModel;
import org.sagebionetworks.repo.model.table.HasDefiningSql;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.PopupUtilsView;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.events.EntityUpdatedEvent;
import org.sagebionetworks.web.client.widget.SynapseWidgetPresenter;
import org.sagebionetworks.web.client.widget.table.modal.fileview.TableType;

/**
 * Widget for viewing and editing a table's column models
 */
public class ColumnModelsWidget
  implements ColumnModelsView.EditHandler, SynapseWidgetPresenter {

  private final PortalGinInjector ginInjector;
  private final ColumnModelsView view;
  private final PopupUtilsView popupUtilsView;
  private final ColumnModelsEditorWidget editor;

  boolean isEditable;
  EntityBundle bundle;
  TableType tableType;

  @Inject
  public ColumnModelsWidget(
    PortalGinInjector ginInjector,
    PopupUtilsView popupUtilsView,
    ColumnModelsEditorWidget editor
  ) {
    this.ginInjector = ginInjector;
    this.popupUtilsView = popupUtilsView;
    this.view = ginInjector.createNewColumnModelsView();
    this.view.setEditHandler(this);
    this.editor = editor;
    this.view.setEditor(editor);
  }

  public void configure(EntityBundle bundle, boolean isEditable) {
    boolean hasDefiningSql = bundle.getEntity() instanceof HasDefiningSql;
    this.isEditable = isEditable && !hasDefiningSql;
    this.bundle = bundle;
    List<ColumnModel> startingModels = bundle
      .getTableBundle()
      .getColumnModels();
    view.configure(this.isEditable);
    // We can get the default columns/annotations for views and datasets
    tableType = TableType.getTableType(bundle.getEntity());
    List<ColumnModelTableRow> rowViewers = new ArrayList<>();
    for (ColumnModel cm : startingModels) {
      // Create a viewer
      ColumnModelTableRowViewer rowViewer =
        ginInjector.createNewColumnModelTableRowViewer();
      ColumnModelUtils.applyColumnModelToRow(cm, rowViewer);
      rowViewer.setSelectable(false);
      rowViewers.add(rowViewer);
    }
    view.addColumns(rowViewers);
    editor.configure(
      bundle.getEntity().getId(),
      this::onEditFinished,
      this::onCancelEdit
    );
  }

  @Override
  public Widget asWidget() {
    return view.asWidget();
  }

  @Override
  public void onEditColumns() {
    if (!this.isEditable) {
      throw new IllegalStateException(
        "Cannot call onEditColumns() for a read-only widget"
      );
    }
    editor.setOpen(true);
  }

  private void onEditFinished() {
    popupUtilsView.notify(
      "Schema Updated",
      "You made changes to the columns in this " + tableType.getDisplayName(),
      DisplayUtils.NotificationVariant.INFO
    );
    // Hide the dialog
    editor.setOpen(false);
    ginInjector
      .getEventBus()
      .fireEvent(new EntityUpdatedEvent(bundle.getEntity().getId()));
  }

  private void onCancelEdit() {
    popupUtilsView.showConfirmDialog(
      "Discard Changes?",
      "Any unsaved changes will be lost. Are you sure you want to close the column editor?",
      () -> editor.setOpen(false)
    );
  }
}
