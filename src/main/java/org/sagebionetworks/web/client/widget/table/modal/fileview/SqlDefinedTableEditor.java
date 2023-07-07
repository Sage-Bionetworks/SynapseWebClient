package org.sagebionetworks.web.client.widget.table.modal.fileview;

import static com.google.common.util.concurrent.MoreExecutors.directExecutor;

import com.google.common.util.concurrent.FutureCallback;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.repo.model.EntityType;
import org.sagebionetworks.repo.model.EntityTypeUtils;
import org.sagebionetworks.repo.model.table.HasDefiningSql;
import org.sagebionetworks.repo.model.table.MaterializedView;
import org.sagebionetworks.repo.model.table.Table;
import org.sagebionetworks.repo.model.table.VirtualTable;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.PopupUtilsView;
import org.sagebionetworks.web.client.SynapseJavascriptClient;
import org.sagebionetworks.web.client.place.Synapse;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;

public class SqlDefinedTableEditor
  implements SqlDefinedTableEditorView.Presenter, IsWidget {

  public static final String MATERIALIZED_VIEW_HELP_MARKDOWN =
    "A Synapse Materialized View is a type of table that is automatically built from a Synapse SQL query. " +
    "The defining SQL of a Materialized View can contain JOIN clauses to combine data from multiple tables.";

  public static final String VIRTUAL_TABLE_HELP_MARKDOWN =
    "A Synapse Virtual Table is a type of table that is defined by a Synapse SQL query. " +
    "Any query on a Virtual Table will execute the defining SQL statement on the referenced table. " +
    "The defining SQL of a Virtual Table cannot include JOIN clauses on multiple tables.";
  SynapseAlert synAlert;
  SqlDefinedTableEditorView view;
  SynapseJavascriptClient jsClient;
  GlobalApplicationState globalAppState;
  PopupUtilsView popupUtils;
  String parentEntityId;
  EntityType entityType;

  @Inject
  public SqlDefinedTableEditor(
    SqlDefinedTableEditorView view,
    SynapseJavascriptClient jsClient,
    SynapseAlert synAlert,
    GlobalApplicationState globalAppState
  ) {
    this.view = view;
    this.jsClient = jsClient;
    this.globalAppState = globalAppState;
    this.synAlert = synAlert;
    this.view.setPresenter(this);
    view.setSynAlert(synAlert);
  }

  public SqlDefinedTableEditor configure(
    String parentEntityId,
    EntityType entityType
  ) {
    if (EntityType.materializedview.equals(entityType)) {
      view.setHelp(
        MATERIALIZED_VIEW_HELP_MARKDOWN,
        CreateTableViewWizard.VIEW_URL
      );
    } else if (EntityType.virtualtable.equals(entityType)) {
      view.setHelp(VIRTUAL_TABLE_HELP_MARKDOWN, CreateTableViewWizard.VIEW_URL);
    } else {
      throw new IllegalArgumentException(
        "Expected MaterializedView or VirtualTable but got " +
        entityType.toString()
      );
    }

    view.setModalTitle("Create " + EntityTypeUtils.getDisplayName(entityType));

    this.parentEntityId = parentEntityId;
    this.entityType = entityType;
    synAlert.clear();
    view.reset();
    return this;
  }

  public void show() {
    view.show();
  }

  @Override
  public Widget asWidget() {
    return view.asWidget();
  }

  @Override
  public void onSave() {
    // create the table
    Table newEntity;
    if (EntityType.materializedview.equals(this.entityType)) {
      newEntity = new MaterializedView();
    } else if (EntityType.virtualtable.equals(this.entityType)) {
      newEntity = new VirtualTable();
    } else {
      throw new IllegalArgumentException(
        "Expected MaterializedView or VirtualTable but got " +
        entityType.toString()
      );
    }
    newEntity.setName(view.getName());
    newEntity.setDescription(view.getDescription());
    ((HasDefiningSql) newEntity).setDefiningSQL(view.getDefiningSql());
    newEntity.setParentId(parentEntityId);
    synAlert.clear();
    jsClient
      .createEntity(newEntity)
      .addCallback(
        new FutureCallback<Entity>() {
          @Override
          public void onSuccess(Entity entity) {
            view.hide();
            globalAppState.getPlaceChanger().goTo(new Synapse(entity.getId()));
          }

          @Override
          public void onFailure(Throwable caught) {
            synAlert.handleException(caught);
          }
        },
        directExecutor()
      );
  }
}
