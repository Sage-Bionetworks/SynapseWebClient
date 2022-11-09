package org.sagebionetworks.web.client.widget.table.v2.results;

import com.google.gwt.user.client.ui.IsWidget;
import java.util.List;

/**
 *
 * Abstraction for a view of a single page of a table query result.
 *
 * @author John
 *
 */
public interface TablePageView extends IsWidget {
  /**
   * Set the headers for this table. This will be extended to including column sorting data.
   *
   * @param headers
   */
  public void setTableHeaders(List<IsWidget> headers);

  /**
   * Add a row to this table.
   *
   * @param rowWidget
   */
  public void addRow(RowWidget rowWidget);

  /**
   * Add rows in bulk to this table.
   *
   * @param rowWidget
   */
  public void addRows(List<RowWidget> rowWidgets);

  /**
   * Remove this row from the view.
   *
   * @param row
   */
  public void removeRow(RowWidget row);

  /**
   * Set the pagination widget
   *
   * @param paginationWidget
   */
  public void setPaginationWidget(IsWidget paginationWidget);

  /**
   * Show or hide the pagination widgets
   *
   * @param visible
   */
  public void setPaginationWidgetVisible(boolean visible);

  void setTableVisible(boolean visible);

  void showLoading();

  void hideLoading();
  void setLastUpdatedOn(String lastUpdatedOn);
}
