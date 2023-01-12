package org.sagebionetworks.web.client.widget.table.v2.schema;

import org.sagebionetworks.repo.model.table.ColumnModel;

public interface ColumnModelTableRowEditorWidget extends ColumnModelTableRow {
  /**
   * Configure this widget before using.
   *
   * @param model The column model to configure this editor.
   * @param selectionPresenter When provided, this editor will show the selection check-box and
   *        register selection changes.
   */
  public void configure(
    ColumnModel model,
    SelectionPresenter selectionPresenter
  );

  /**
   * Validate This column model.
   *
   * @return
   */
  public boolean validate();

  public void setToBeDefaultFileViewColumn();

  public void setIsView();
}
