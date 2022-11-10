package org.sagebionetworks.web.client.widget.table.v2.schema;

public interface ColumnModelTableRowViewer extends ColumnModelTableRow {
  /**
   * Should this row be selectable.
   *
   * @param isSelectable
   */
  public void setSelectable(boolean isSelectable);
}
