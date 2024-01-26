package org.sagebionetworks.web.client.widget.table.v2.schema;

import com.google.gwt.user.client.ui.IsWidget;
import java.util.List;

/**
 * An editable widget of a list of ColumnModels
 *
 * @author jmhill
 *
 */
public interface ColumnModelsView extends IsWidget {
  public interface EditHandler {
    /**
     * Called when the edit button is pressed
     */
    public void onEditColumns();
  }

  public void setEditHandler(EditHandler handler);

  /**
   * bulk add column model table rows to the view (single attach)
   * @param rows
   */
  void addColumns(List<ColumnModelTableRow> rows);

  /**
   * Set the view editable
   *
   * @param isEditable
   */
  void configure(boolean isEditable);

  void showErrorMessage(String message);

  void setEditor(IsWidget w);
}
