package org.sagebionetworks.web.client.widget.table.v2.schema;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.gwtbootstrap3.client.ui.FormControlStatic;
import org.gwtbootstrap3.client.ui.FormGroup;
import org.gwtbootstrap3.client.ui.TextBox;
import org.gwtbootstrap3.client.ui.html.Paragraph;
import org.sagebionetworks.web.client.cookie.CookieProvider;
import org.sagebionetworks.web.client.view.bootstrap.table.TableRow;
import org.sagebionetworks.web.client.widget.table.v2.results.cell.CellEditor;

/**
 * Editor implementation of ColumnModelTableRow
 *
 * @author John
 *
 */
public class ColumnModelTableRowEditorViewImpl
  extends AbstractColumnModelTableRow
  implements ColumnModelTableRowEditorView {

  public interface Binder
    extends UiBinder<TableRow, ColumnModelTableRowEditorViewImpl> {}

  @UiField
  FormGroup nameGroup;

  @UiField
  TextBox name;

  @UiField
  Paragraph nameHelp;

  @UiField
  ListBox type;

  @UiField
  FormGroup sizeGroup;

  @UiField
  TextBox maxSize;

  @UiField
  Paragraph sizeHelp;

  @UiField
  FormGroup maxListLengthGroup;

  @UiField
  TextBox maxListLength;

  @UiField
  Paragraph maxListLengthHelp;

  @UiField
  SimplePanel defaultPanel;

  CellEditor defaultWidget;

  @UiField
  TextBox restrictValues;

  @UiField
  ListBox facet;

  @UiField
  FormControlStatic nameStatic;

  @UiField
  FormControlStatic typeStatic;

  @UiField
  FormControlStatic maxSizeStatic;

  String id;
  TypePresenter presenter;

  @Inject
  public ColumnModelTableRowEditorViewImpl(
    Binder uiBinder,
    CookieProvider cookies
  ) {
    row = uiBinder.createAndBindUi(this);
    ChangeHandler typeChangeHandler = new ChangeHandler() {
      @Override
      public void onChange(ChangeEvent event) {
        presenter.onTypeChanged();
      }
    };
    type.addChangeHandler(typeChangeHandler);
  }

  @Override
  public String getId() {
    return id;
  }

  @Override
  public String getColumnName() {
    return name.getText();
  }

  @Override
  public ColumnTypeViewEnum getColumnType() {
    return ColumnTypeViewEnum.valueOf(type.getSelectedValue());
  }

  @Override
  public ColumnFacetTypeViewEnum getFacetType() {
    return ColumnFacetTypeViewEnum.getEnumForFriendlyName(
      facet.getSelectedValue()
    );
  }

  @Override
  public String getMaxSize() {
    return maxSize.getText();
  }

  @Override
  public String getMaxListLength() {
    return maxListLength.getText();
  }

  @Override
  public String getDefaultValue() {
    return defaultWidget.getValue();
  }

  @Override
  public void setTypePresenter(TypePresenter presenterIn) {
    this.presenter = presenterIn;
  }

  @Override
  public Widget asWidget() {
    return row;
  }

  @Override
  public void setId(String id) {
    this.id = id;
  }

  @Override
  public void setColumnType(ColumnTypeViewEnum columnType) {
    typeStatic.setText(columnType.name());
    int index = 0;
    String targetName = columnType.name();
    for (int i = 0; i < type.getItemCount(); i++) {
      if (type.getValue(i).equals(targetName)) {
        index = i;
        break;
      }
    }
    type.setSelectedIndex(index);
  }

  @Override
  public void addColumnType(ColumnTypeViewEnum item) {
    type.addItem(item.toString());
  }

  @Override
  public void setFacetType(ColumnFacetTypeViewEnum type) {
    int index = 0;
    String targetName = type.toString();
    for (int i = 0; i < this.facet.getItemCount(); i++) {
      if (this.facet.getValue(i).equals(targetName)) {
        index = i;
        break;
      }
    }
    this.facet.setSelectedIndex(index);
  }

  @Override
  public void setColumnName(String name) {
    nameStatic.setText(name);
    this.name.setText(name);
  }

  @Override
  public void setMaxSize(String maxSize) {
    maxSizeStatic.setText(maxSize);
    this.maxSize.setText(maxSize);
  }

  @Override
  public void setMaxListLength(String maxListLength) {
    this.maxListLength.setText(maxListLength);
  }

  @Override
  public void setDefaultValue(String defaultValue) {
    this.defaultWidget.setValue(defaultValue);
  }

  @Override
  public void setSizeFieldVisible(boolean visible) {
    maxSize.setVisible(visible);
  }

  @Override
  public void setMaxListLengthFieldVisible(boolean visible) {
    maxListLength.setVisible(visible);
  }

  @Override
  public void setEnumValues(List<String> enums) {
    restrictValues.setText(ColumnModelUtils.listToCSV(enums));
  }

  @Override
  public List<String> getEnumValues() {
    return ColumnModelUtils.csvToList(restrictValues.getText());
  }

  @Override
  public void setDefaultEditor(final CellEditor defaultEditor) {
    this.defaultWidget = defaultEditor;
    defaultPanel.clear();
    defaultPanel.add(defaultEditor);
  }

  @Override
  public void setDefaultEditorVisible(boolean visible) {
    defaultPanel.setVisible(visible);
  }

  @Override
  public void setNameError(String error) {
    nameHelp.setVisible(true);
    this.nameHelp.setText(error);
  }

  @Override
  public void clearNameError() {
    nameHelp.setVisible(false);
    this.nameHelp.setText("");
  }

  @Override
  public boolean validateDefault() {
    return this.defaultWidget.isValid();
  }

  @Override
  public void setSizeError(String error) {
    this.sizeHelp.setVisible(true);
    this.sizeHelp.setText(error);
  }

  @Override
  public void clearSizeError() {
    this.sizeHelp.setVisible(false);
    this.sizeHelp.setText("");
  }

  @Override
  public void setRestrictValuesVisible(boolean showRestrictValues) {
    this.restrictValues.setVisible(showRestrictValues);
    this.restrictValues.clear();
  }

  @Override
  public void setFacetVisible(boolean showFacetTypes) {
    facet.setVisible(showFacetTypes);
  }

  @Override
  public void setFacetValues(String... items) {
    facet.clear();
    for (String item : items) {
      facet.addItem(item);
    }
  }

  @Override
  public void setToBeDefaultFileViewColumn() {
    nameGroup.setVisible(false);
    sizeGroup.setVisible(false);
    type.setVisible(false);
    maxListLengthGroup.setVisible(false);

    nameStatic.setVisible(true);
    typeStatic.setVisible(true);
    maxSizeStatic.setVisible(true);
  }

  @Override
  public void setMaxListLengthError(String error) {
    this.maxListLengthHelp.setVisible(true);
    this.maxListLengthHelp.setText(error);
  }

  @Override
  public void clearMaxListLengthError() {
    this.maxListLengthHelp.setVisible(false);
    this.maxListLengthHelp.setText("");
  }
}
