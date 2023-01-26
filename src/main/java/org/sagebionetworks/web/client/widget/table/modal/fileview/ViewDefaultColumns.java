package org.sagebionetworks.web.client.widget.table.modal.fileview;

import static com.google.common.util.concurrent.Futures.whenAllComplete;
import static com.google.common.util.concurrent.MoreExecutors.directExecutor;

import com.google.common.util.concurrent.FluentFuture;
import com.google.inject.Inject;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.sagebionetworks.repo.model.table.ColumnModel;
import org.sagebionetworks.repo.model.table.ViewEntityType;
import org.sagebionetworks.schema.adapter.AdapterFactory;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.client.PopupUtilsView;
import org.sagebionetworks.web.client.SynapseJavascriptClient;

public class ViewDefaultColumns {

  private SynapseJavascriptClient jsClient;
  private List<ColumnModel> defaultFileViewColumns, defaultProjectViewColumns, defaultSubmissionViewColumns, defaultDatasetColumns, defaultDatasetCollectionColumns;

  private AdapterFactory adapterFactory;
  PopupUtilsView popupUtils;

  @Inject
  public ViewDefaultColumns(
    SynapseJavascriptClient jsClient,
    AdapterFactory adapterFactory,
    PopupUtilsView popupUtils
  ) {
    this.jsClient = jsClient;
    this.adapterFactory = adapterFactory;
    this.popupUtils = popupUtils;
    init();
  }

  public void init() {
    FluentFuture<List<ColumnModel>> fileViewColumnsFuture = jsClient.getDefaultColumnsForView(
      TableType.file_view.getViewTypeMask()
    );
    FluentFuture<List<ColumnModel>> projectViewColumnsFuture = jsClient.getDefaultColumnsForView(
      TableType.project_view.getViewTypeMask()
    );
    FluentFuture<List<ColumnModel>> submissionViewColumnsFuture = jsClient.getDefaultColumnsForView(
      ViewEntityType.submissionview
    );
    FluentFuture<List<ColumnModel>> datasetColumnsFuture = jsClient.getDefaultColumnsForView(
      ViewEntityType.dataset
    );
    FluentFuture<List<ColumnModel>> datasetCollectionColumnsFuture = jsClient.getDefaultColumnsForView(
      ViewEntityType.datasetcollection
    );
    FluentFuture
      .from(
        whenAllComplete(
          fileViewColumnsFuture,
          projectViewColumnsFuture,
          submissionViewColumnsFuture,
          datasetColumnsFuture,
          datasetCollectionColumnsFuture
        )
          .call(
            () -> {
              defaultFileViewColumns = clearIds(fileViewColumnsFuture.get());
              defaultProjectViewColumns =
                clearIds(projectViewColumnsFuture.get());
              defaultSubmissionViewColumns =
                clearIds(submissionViewColumnsFuture.get());
              defaultDatasetColumns = clearIds(datasetColumnsFuture.get());
              defaultDatasetCollectionColumns =
                clearIds(datasetCollectionColumnsFuture.get());
              return null;
            },
            directExecutor()
          )
      )
      .catching(
        Throwable.class,
        e -> {
          popupUtils.showErrorMessage(e.getMessage());
          return null;
        },
        directExecutor()
      );
  }

  private Set<String> getColumnNames(List<ColumnModel> columns) {
    Set<String> defaultColumnNames = new HashSet<String>(columns.size());
    for (ColumnModel cm : columns) {
      defaultColumnNames.add(cm.getName());
    }
    return defaultColumnNames;
  }

  public Set<String> getDefaultViewColumnNames(TableType tableType) {
    return getColumnNames(getDefaultViewColumns(tableType));
  }

  public List<ColumnModel> getDefaultViewColumns(TableType tableType) {
    /*
     * TODO: We should instead asynchronously fetch the default columns using the viewEntityType and viewTypeMask of the specific table.
     * The widgets that call this would need to be refactored to use Future callbacks and handle loading and error states.
     * See https://rest-docs.synapse.org/rest/GET/column/tableview/defaults.html
     */
    if (TableType.submission_view.equals(tableType)) {
      return defaultSubmissionViewColumns;
    } else if (TableType.dataset.equals(tableType)) {
      return defaultDatasetColumns;
    } else if (TableType.dataset_collection.equals(tableType)) {
      return defaultDatasetCollectionColumns;
    } else if (tableType.isIncludeFiles()) {
      return defaultFileViewColumns;
    } else {
      return defaultProjectViewColumns;
    }
  }

  private List<ColumnModel> clearIds(List<ColumnModel> columns) {
    List<ColumnModel> newColumns = new ArrayList<ColumnModel>(columns.size());
    try {
      for (ColumnModel cm : columns) {
        ColumnModel cmCopy = new ColumnModel(
          cm.writeToJSONObject(adapterFactory.createNew())
        );
        cmCopy.setId(null);
        newColumns.add(cmCopy);
      }
    } catch (JSONObjectAdapterException e) {
      popupUtils.showErrorMessage(e.getMessage());
    }
    return newColumns;
  }

  public ColumnModel deepColumnModel(ColumnModel cm) {
    try {
      ColumnModel cmCopy = new ColumnModel(
        cm.writeToJSONObject(adapterFactory.createNew())
      );
      return cmCopy;
    } catch (JSONObjectAdapterException e) {
      popupUtils.showErrorMessage(e.getMessage());
    }
    return null;
  }
}
