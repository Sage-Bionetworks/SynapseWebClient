package org.sagebionetworks.web.client.widget.table.modal.fileview;

// Table, or a Project View, or a combination View composed of Files/Folders/Tables

import static org.sagebionetworks.web.shared.WebConstants.DATASET;
import static org.sagebionetworks.web.shared.WebConstants.DOCKER;
import static org.sagebionetworks.web.shared.WebConstants.FILE;
import static org.sagebionetworks.web.shared.WebConstants.FOLDER;
import static org.sagebionetworks.web.shared.WebConstants.PROJECT;
import static org.sagebionetworks.web.shared.WebConstants.SUBMISSION_VIEW;
import static org.sagebionetworks.web.shared.WebConstants.TABLE;
import static org.sagebionetworks.web.shared.WebConstants.VIEW;

import java.util.Objects;
import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.repo.model.table.Dataset;
import org.sagebionetworks.repo.model.table.DatasetCollection;
import org.sagebionetworks.repo.model.table.EntityView;
import org.sagebionetworks.repo.model.table.MaterializedView;
import org.sagebionetworks.repo.model.table.SubmissionView;
import org.sagebionetworks.repo.model.table.Table;
import org.sagebionetworks.repo.model.table.TableEntity;
import org.sagebionetworks.repo.model.table.ViewTypeMask;
import org.sagebionetworks.repo.model.table.VirtualTable;
import org.sagebionetworks.web.client.DisplayConstants;

public class TableType {

  /**
   * We specifically enumerate some common TableTypes to use elsewhere, but a TableType object does not have to match one of these
   */

  // TableEntity, SubmissionView, MaterializedView, VirtualTable, and Dataset don't use viewTypeMask
  public static final TableType table = new TableType(TableEntity.class, null);
  public static final TableType submission_view = new TableType(
    SubmissionView.class,
    null
  );
  public static final TableType materialized_view = new TableType(
    MaterializedView.class,
    null
  );
  public static final TableType virtual_table = new TableType(
    VirtualTable.class,
    null
  );

  // We specify a viewTypeMask of 'FILE' for Datasets because they work like file views in many ways
  public static final TableType dataset = new TableType(Dataset.class, FILE);
  public static final TableType dataset_collection = new TableType(
    DatasetCollection.class,
    DATASET
  );

  // We define types of EntityViews for convenience/usability, but a user could specify a custom mask.
  public static final TableType project_view = new TableType(
    EntityView.class,
    PROJECT
  );
  public static final TableType file_view = new TableType(
    EntityView.class,
    FILE
  );

  private Class<? extends Table> clazz;
  private Integer viewTypeMask;

  public TableType(Class<? extends Table> clazz, Integer viewTypeMask) {
    this.clazz = clazz;
    this.viewTypeMask = viewTypeMask;
  }

  public Integer getViewTypeMask() {
    return this.viewTypeMask;
  }

  public boolean isIncludeFiles() {
    return viewTypeMask != null && (viewTypeMask & FILE) > 0;
  }

  public boolean isIncludeFolders() {
    return viewTypeMask != null && (viewTypeMask & FOLDER) > 0;
  }

  public boolean isIncludeTables() {
    return viewTypeMask != null && (viewTypeMask & TABLE) > 0;
  }

  public boolean isIncludeDatasets() {
    return viewTypeMask != null && (viewTypeMask & DATASET) > 0;
  }

  public boolean isIncludeDockerRepo() {
    return viewTypeMask != null && (viewTypeMask & DOCKER) > 0;
  }

  public boolean isIncludeEntityView() {
    return viewTypeMask != null && (viewTypeMask & VIEW) > 0;
  }

  public boolean isIncludeSubmissionView() {
    return viewTypeMask != null && (viewTypeMask & SUBMISSION_VIEW) > 0;
  }

  public boolean isIncludeProject() {
    return viewTypeMask != null && (viewTypeMask & PROJECT) > 0;
  }

  public static TableType getEntityViewTableType(
    boolean isFileSelected,
    boolean isFolderSelected,
    boolean isTableSelected,
    boolean isDatasetSelected
  ) {
    int viewTypeMask = 0;
    if (isFileSelected) {
      viewTypeMask = FILE;
    }
    if (isFolderSelected) {
      viewTypeMask = viewTypeMask | FOLDER;
    }
    if (isTableSelected) {
      viewTypeMask = viewTypeMask | TABLE;
    }
    if (isDatasetSelected) {
      viewTypeMask = viewTypeMask | DATASET;
    }
    return new TableType(EntityView.class, viewTypeMask);
  }

  public static TableType getTableType(Entity entity) {
    if (entity instanceof TableEntity) {
      return TableType.table;
    } else if (entity instanceof Dataset) {
      return TableType.dataset;
    } else if (entity instanceof DatasetCollection) {
      return TableType.dataset_collection;
    } else if (entity instanceof SubmissionView) {
      return TableType.submission_view;
    } else if (entity instanceof MaterializedView) {
      return TableType.materialized_view;
    } else if (entity instanceof VirtualTable) {
      return TableType.virtual_table;
    } else if (entity instanceof EntityView) {
      EntityView view = (EntityView) entity;

      Long typeMask = view.getViewTypeMask();
      if (typeMask == null) {
        typeMask = ViewTypeMask.getMaskForDepricatedType(view.getType());
      }
      return new TableType(EntityView.class, Math.toIntExact(typeMask));
    }
    return null;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    TableType tableType = (TableType) o;
    return (
      clazz.equals(tableType.clazz) &&
      this.viewTypeMask == ((TableType) o).viewTypeMask
    );
  }

  @Override
  public int hashCode() {
    return Objects.hash(clazz, this.viewTypeMask);
  }

  public String getDisplayName() {
    if (this.clazz == TableEntity.class) {
      return DisplayConstants.TABLE;
    }
    if (this.clazz == SubmissionView.class) {
      return DisplayConstants.SUBMISSION_VIEW;
    }
    if (this.clazz == MaterializedView.class) {
      return DisplayConstants.MATERIALIZED_VIEW;
    }
    if (this.clazz == VirtualTable.class) {
      return DisplayConstants.VIRTUAL_TABLE;
    }
    if (this.clazz == Dataset.class) {
      return DisplayConstants.DATASET;
    }
    if (this.clazz == DatasetCollection.class) {
      return DisplayConstants.DATASET_COLLECTION;
    }
    if (this.clazz == EntityView.class) {
      // For EntityViews, the display name depends on the mask
      if (this.getViewTypeMask().equals(FILE)) {
        // Files only -> "File View"
        return DisplayConstants.FILE_VIEW;
      } else if (this.getViewTypeMask().equals(PROJECT)) {
        // Projects only -> "Project View"
        return DisplayConstants.PROJECT_VIEW;
      } else {
        // Otherwise, just "View"
        return DisplayConstants.VIEW;
      }
    }
    return DisplayConstants.TABLE;
  }

  public Class<? extends Table> getClazz() {
    return clazz;
  }
}
