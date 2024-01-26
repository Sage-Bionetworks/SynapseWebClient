package org.sagebionetworks.web.client.widget.table.v2.schema;

import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import java.util.List;
import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.html.Div;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.cookie.CookieProvider;
import org.sagebionetworks.web.client.view.bootstrap.table.TBody;
import org.sagebionetworks.web.client.view.bootstrap.table.Table;
import org.sagebionetworks.web.client.view.bootstrap.table.TableHeader;

/**
 * A table view of a list of ColumnModels
 *
 * @author jmhill
 *
 */
public class ColumnModelsViewImpl
  extends Composite
  implements ColumnModelsView {

  public interface Binder extends UiBinder<Widget, ColumnModelsViewImpl> {}

  @UiField
  Table table;

  @UiField
  TBody tableBody;

  @UiField
  Button editColumnsButton;

  @UiField
  TableHeader columnIdTableHeader;

  @UiField
  Div editorContainer;

  EditHandler editHandler;

  @Inject
  public ColumnModelsViewImpl(final Binder uiBinder, CookieProvider cookies) {
    initWidget(uiBinder.createAndBindUi(this));
  }

  @Override
  public void setEditHandler(EditHandler handler) {
    editHandler = handler;
    editColumnsButton.addClickHandler(event -> editHandler.onEditColumns());
  }

  @Override
  public void addColumns(List<ColumnModelTableRow> rows) {
    tableBody.removeFromParent();

    for (ColumnModelTableRow row : rows) {
      tableBody.add(row);
    }
    table.add(tableBody);
  }

  @Override
  public void configure(boolean isEditable) {
    // Clear any rows
    tableBody.clear();
    editColumnsButton.setVisible(isEditable);
    columnIdTableHeader.setVisible(true);
  }

  @Override
  public void showErrorMessage(String message) {
    DisplayUtils.showErrorMessage(message);
  }

  @Override
  public void setEditor(IsWidget w) {
    editorContainer.add(w);
  }
}
