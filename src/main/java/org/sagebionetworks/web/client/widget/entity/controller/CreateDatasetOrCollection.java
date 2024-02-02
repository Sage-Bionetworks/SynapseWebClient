package org.sagebionetworks.web.client.widget.entity.controller;

import static com.google.common.util.concurrent.Futures.whenAllComplete;
import static com.google.common.util.concurrent.MoreExecutors.directExecutor;

import com.google.common.util.concurrent.FluentFuture;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.repo.model.table.ColumnModel;
import org.sagebionetworks.repo.model.table.Dataset;
import org.sagebionetworks.repo.model.table.DatasetCollection;
import org.sagebionetworks.repo.model.table.EntityRefCollectionView;
import org.sagebionetworks.repo.model.table.Table;
import org.sagebionetworks.repo.model.table.ViewEntityType;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.SynapseJavascriptClient;
import org.sagebionetworks.web.client.place.Synapse;
import org.sagebionetworks.web.client.widget.entity.PromptForValuesModalView;
import org.sagebionetworks.web.client.widget.entity.tabs.DatasetsTab;
import org.sagebionetworks.web.client.widget.table.modal.fileview.TableType;

public class CreateDatasetOrCollection implements IsWidget {

  PromptForValuesModalView view;
  SynapseJavascriptClient jsClient;
  GlobalApplicationState globalAppState;
  PortalGinInjector ginInjector;

  @Inject
  public CreateDatasetOrCollection(
    PromptForValuesModalView view,
    SynapseJavascriptClient jsClient,
    GlobalApplicationState globalAppState,
    PortalGinInjector ginInjector
  ) {
    this.view = view;
    this.jsClient = jsClient;
    this.globalAppState = globalAppState;
    this.ginInjector = ginInjector;
  }

  @Override
  public Widget asWidget() {
    return view.asWidget();
  }

  public void configure(String parentId, TableType type) {
    boolean isDataset = TableType.dataset.equals(type);
    boolean isDatasetCollection = TableType.dataset_collection.equals(type);
    if (!(isDataset || isDatasetCollection)) {
      DisplayUtils.showErrorMessage(
        "Invalid type used to configure CreateDatasetOrCollection"
      );
      return;
    }
    String title = "", helpMarkdown = "";

    if (TableType.dataset.equals(type)) {
      title = "Create Dataset";
      helpMarkdown = DatasetsTab.DATASETS_HELP;
    } else {
      title = "Create Dataset Collection";
      helpMarkdown = DatasetsTab.DATASET_COLLECTIONS_HELP;
    }
    PromptForValuesModalView.Configuration.Builder builder =
      ginInjector.getPromptForValuesModalConfigurationBuilder();
    builder
      .setTitle(title)
      .addPrompt("Name", "")
      //          .addPrompt("Description", "")
      .setCallback(values -> {
        view.setLoading(true);
        String name = values.get(0);
        createEntity(parentId, name, "", type);
      })
      .addHelpWidget(helpMarkdown, DatasetsTab.DATASETS_HELP_URL);
    view.configureAndShow(builder.buildConfiguration());
  }

  /**
   * Create the Dataset or Dataset Collection
   *
   * @param name
   */
  private void createEntity(
    String parentId,
    final String name,
    final String description,
    TableType tableType
  ) {
    Table table;
    List<ListenableFuture<?>> futures = new ArrayList<>();
    ViewEntityType viewEntityType;
    if (TableType.dataset.equals(tableType)) {
      table = new Dataset();
      viewEntityType = ViewEntityType.dataset;
    } else {
      table = new DatasetCollection();
      viewEntityType = ViewEntityType.datasetcollection;
    }
    ((EntityRefCollectionView) table).setItems(Collections.EMPTY_LIST); // Workaround for PLFM-7076
    // For Datasets, automatically add the default columns (SWC-5917)
    FluentFuture<List<ColumnModel>> defaultColumnsFuture =
      jsClient.getDefaultColumnsForView(viewEntityType);
    defaultColumnsFuture.addCallback(
      new FutureCallback<List<ColumnModel>>() {
        @Override
        public void onSuccess(@Nullable List<ColumnModel> results) {
          List<String> columnIds = new ArrayList<>();
          for (ColumnModel col : results) {
            columnIds.add(col.getId());
          }
          table.setColumnIds(columnIds);
        }

        @Override
        public void onFailure(Throwable t) {
          view.showError(
            "Error fetching columns for the table: " + t.getMessage()
          );
        }
      },
      directExecutor()
    );
    futures.add(defaultColumnsFuture);

    table.setName(name);
    table.setParentId(parentId);
    table.setDescription(description);
    // Wait for all possible requests to complete before creating the entity.
    FluentFuture.from(
      whenAllComplete(futures)
        .call(
          () -> {
            createEntity(table);
            return null;
          },
          directExecutor()
        )
    );
  }

  private void createEntity(final Entity entity) {
    jsClient
      .createEntity(entity)
      .addCallback(
        new FutureCallback<Entity>() {
          @Override
          public void onSuccess(Entity table) {
            // Go to the dataset page
            view.setLoading(false);
            view.hide();
            globalAppState.getPlaceChanger().goTo(new Synapse(table.getId()));
          }

          @Override
          public void onFailure(Throwable caught) {
            view.showError(caught.getMessage());
          }
        },
        directExecutor()
      );
  }
}
