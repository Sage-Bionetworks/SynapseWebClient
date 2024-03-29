package org.sagebionetworks.web.client.widget.table.v2.results;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.sagebionetworks.repo.model.table.ColumnModel;
import org.sagebionetworks.repo.model.table.FacetColumnRequest;
import org.sagebionetworks.repo.model.table.Query;
import org.sagebionetworks.repo.model.table.QueryResultBundle;
import org.sagebionetworks.repo.model.table.Row;
import org.sagebionetworks.repo.model.table.SortItem;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.pagination.BasicPaginationWidget;
import org.sagebionetworks.web.client.widget.table.modal.fileview.TableType;
import org.sagebionetworks.web.client.widget.table.v2.schema.ColumnModelUtils;

/**
 * A widget for displaying a single page of a query result.
 *
 * @author John
 *
 */
public class TablePageWidget implements IsWidget, RowSelectionListener {

  public static final String LAST_UPDATED_ON = "Last updated on ";
  TablePageView view;
  PortalGinInjector ginInjector;
  List<ColumnModel> types;
  RowSelectionListener rowSelectionListener;
  BasicPaginationWidget paginationWidget;
  List<RowWidget> rows;
  String tableId;
  TableType tableType;

  /*
   * This flag is used to ignore selection event while this widget is causing selection changes.
   */
  boolean isSelectionChanging;

  @Inject
  public TablePageWidget(
    TablePageView view,
    PortalGinInjector ginInjector,
    BasicPaginationWidget paginationWidget
  ) {
    this.ginInjector = ginInjector;
    this.paginationWidget = paginationWidget;
    this.view = view;
    this.view.setPaginationWidget(paginationWidget);
    paginationWidget.hideClearFix();
  }

  /**
   * Configure this page with query results.
   *
   * @param bundle The query results.
   * @param query The query used to generate this page.
   * @param rowSelectionListener If null then selection will be disabled.
   * @param pageChangeListener If null then pagination will be disabled.
   */
  public void configure(
    QueryResultBundle bundle,
    Query query,
    List<SortItem> sortList,
    TableType tableType,
    RowSelectionListener rowSelectionListener,
    final PagingAndSortingListener pageChangeListener,
    CallbackP<FacetColumnRequest> facetChangedHandler
  ) {
    this.tableType = tableType;
    this.rowSelectionListener = rowSelectionListener;
    view.showLoading();
    Integer rowCount = bundle
      .getQueryResult()
      .getQueryResults()
      .getRows()
      .size();
    String lastUpdatedOn = "";
    if (bundle.getLastUpdatedOn() != null) {
      lastUpdatedOn =
        LAST_UPDATED_ON +
        ginInjector
          .getDateTimeUtils()
          .getDateTimeString(bundle.getLastUpdatedOn());
    }
    view.setLastUpdatedOn(lastUpdatedOn);
    // The pagination widget is only visible if a listener was provider
    if (pageChangeListener != null) {
      this.paginationWidget.configure(
          query.getLimit(),
          query.getOffset(),
          rowCount.longValue(),
          pageChangeListener
        );
      view.setPaginationWidgetVisible(true);
    } else {
      view.setPaginationWidgetVisible(false);
    }
    tableId = QueryBundleUtils.getTableId(bundle);
    // Map the columns to types
    types =
      ColumnModelUtils.buildTypesForQueryResults(
        QueryBundleUtils.getSelectFromBundle(bundle),
        bundle.getColumnModels()
      );
    // setup the headers from the types
    List<IsWidget> headers = new ArrayList<IsWidget>();
    Map<String, SortItem> sortedHeaders = new HashMap<String, SortItem>();
    if (sortList != null) {
      for (SortItem sort : sortList) {
        sortedHeaders.put(sort.getColumn(), sort);
      }
    }
    for (ColumnModel type : types) {
      // Create each header
      String headerName = type.getName();
      // For the static case we just set the header name.
      StaticTableHeader sth = ginInjector.createStaticTableHeader();
      sth.setHeader(headerName);
      sth.setIsResizable(false);
      headers.add(sth);
    }

    view.setTableHeaders(headers);
    rows = new ArrayList<RowWidget>(rowCount);
    // Build the rows for this table
    for (Row row : bundle.getQueryResult().getQueryResults().getRows()) {
      // Create the row
      addRow(row);
    }
    // add rows in bulk to the view (single attach event)
    view.addRows(rows);
    view.hideLoading();
  }

  /**
   * @param types
   * @param isSelectable
   * @param row
   */
  private RowWidget addRow(Row row) {
    // Create a new row and configure it with the data.
    RowWidget rowWidget = ginInjector.createRowWidget();
    // We only listen to selection changes on the row if one was provided.
    RowSelectionListener listner = null;
    if (rowSelectionListener != null) {
      listner = this;
    }
    rowWidget.configure(tableId, types, tableType, row, listner);
    rows.add(rowWidget);
    return rowWidget;
  }

  @Override
  public Widget asWidget() {
    return view.asWidget();
  }

  /**
   * Add a new row to the table.
   */
  public void onAddNewRow() {
    view.addRow(addRow(new Row()));
  }

  /**
   * Toggle selection.
   */
  public void onToggleSelect() {
    if (isOneRowOrMoreRowsSelected()) {
      onSelectNone();
    } else {
      onSelectAll();
    }
  }

  /**
   * Delete the selected rows
   */
  public void onDeleteSelected() {
    Iterator<RowWidget> it = this.rows.iterator();
    while (it.hasNext()) {
      RowWidget row = it.next();
      if (row.isSelected()) {
        view.removeRow(row);
        it.remove();
      }
    }
    onSelectionChanged();
  }

  /**
   * Select no rows.
   */
  public void onSelectNone() {
    setAllSelect(false);
  }

  /**
   * Select all rows.
   */
  public void onSelectAll() {
    setAllSelect(true);
  }

  /**
   * Change all sections.
   *
   * @param isSelected
   */
  private void setAllSelect(boolean isSelected) {
    try {
      this.isSelectionChanging = true;
      for (RowWidget row : rows) {
        row.setSelected(isSelected);
      }
    } finally {
      this.isSelectionChanging = false;
    }
    onSelectionChanged();
  }

  /**
   * Returns true if one or more rows are selected. False if no rows are selected.
   *
   * @return
   */
  public boolean isOneRowOrMoreRowsSelected() {
    for (RowWidget row : rows) {
      if (row.isSelected()) {
        return true;
      }
    }
    return false;
  }

  /**
   * Extract a copy of the rows in this widget according to the current state.
   *
   * @return
   */
  public List<Row> extractRowSet() {
    List<Row> copy = new ArrayList<Row>(rows.size());
    for (RowWidget rowWidget : rows) {
      Row row = rowWidget.getRow();
      copy.add(row);
    }
    return copy;
  }

  /**
   * Headers for this page. If a ColumnModle has an ID then it is a real column. If the ID is null
   * then it is a derived column.
   *
   * @return
   */
  public List<ColumnModel> extractHeaders() {
    return types;
  }

  /**
   * Called when a row changes its selection.
   */
  public void onSelectionChanged() {
    // Only send out the message if selection is not in the process of changing.
    if (!this.isSelectionChanging && this.rowSelectionListener != null) {
      this.rowSelectionListener.onSelectionChanged();
    }
  }

  /**
   * Is this page valid?
   *
   * @return
   */
  public boolean isValid() {
    boolean isValid = true;
    for (RowWidget row : rows) {
      if (!row.isValid()) {
        isValid = false;
      }
    }
    return isValid;
  }

  public void setTableVisible(boolean visible) {
    view.setTableVisible(visible);
  }
}
