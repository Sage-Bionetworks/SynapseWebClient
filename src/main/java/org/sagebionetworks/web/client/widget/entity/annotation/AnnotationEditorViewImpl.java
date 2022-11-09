package org.sagebionetworks.web.client.widget.entity.annotation;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import java.util.List;
import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.FormGroup;
import org.gwtbootstrap3.client.ui.HelpBlock;
import org.gwtbootstrap3.client.ui.TextBox;
import org.gwtbootstrap3.client.ui.constants.ButtonSize;
import org.gwtbootstrap3.client.ui.constants.ButtonType;
import org.gwtbootstrap3.client.ui.constants.IconType;
import org.gwtbootstrap3.client.ui.constants.ValidationState;
import org.sagebionetworks.web.client.view.bootstrap.table.Table;
import org.sagebionetworks.web.client.view.bootstrap.table.TableData;
import org.sagebionetworks.web.client.view.bootstrap.table.TableRow;
import org.sagebionetworks.web.client.widget.table.v2.results.cell.CellEditor;
import org.sagebionetworks.web.client.widget.table.v2.results.cell.CellFactory;

public class AnnotationEditorViewImpl implements AnnotationEditorView {

  public interface Binder extends UiBinder<Widget, AnnotationEditorViewImpl> {}

  private Presenter presenter;
  private Widget widget;

  @UiField
  ListBox typeComboBox;

  @UiField
  TextBox keyField;

  @UiField
  Table editorsContainer;

  @UiField
  FormGroup formGroup;

  @UiField
  HelpBlock helpBlock;

  //Not in UI XML because it is moved around a lot
  Button addNewAnnotationValueButton;

  @Inject
  public AnnotationEditorViewImpl(Binder uiBinder) {
    widget = uiBinder.createAndBindUi(this);
    typeComboBox.addChangeHandler(
      new ChangeHandler() {
        @Override
        public void onChange(ChangeEvent event) {
          presenter.onTypeChange(typeComboBox.getSelectedIndex());
        }
      }
    );

    addNewAnnotationValueButton =
      new Button("", IconType.PLUS, clickEvent -> presenter.onAddNewValue());
    addNewAnnotationValueButton.addStyleName("center-in-div");
    addNewAnnotationValueButton.setType(ButtonType.PRIMARY);
    addNewAnnotationValueButton.setSize(ButtonSize.EXTRA_SMALL);
  }

  @Override
  public void clearValueEditors() {
    editorsContainer.clear();
  }

  @Override
  public void setTypeOptions(List<String> types) {
    typeComboBox.clear();
    for (String type : types) {
      typeComboBox.addItem(type);
    }
  }

  @Override
  public void setPresenter(Presenter presenter) {
    this.presenter = presenter;
  }

  @Override
  public Widget asWidget() {
    return widget;
  }

  @Override
  public void configure(String key, int typeIndex) {
    keyField.setValue(key);
    typeComboBox.setSelectedIndex(typeIndex);
  }

  @Override
  public void addNewEditor(final CellEditor editor) {
    TableRow row = CellFactory.appendDeleteButton(
      editor,
      presenter::onValueDeleted
    );

    //additional column placeholder for the addNewAnnotationValueButton
    TableData addButtonTableData = new TableData();
    addButtonTableData.setWidth("35px");
    row.add(addButtonTableData);

    editorsContainer.add(row);

    addNewAnnotationValueButton.removeFromParent();
    addButtonTableData.add(addNewAnnotationValueButton);
  }

  @Override
  public void moveAddNewAnnotationValueButtonToRowToLastRow() {
    TableRow lastRow = (TableRow) editorsContainer.getWidget(
      editorsContainer.getWidgetCount() - 1
    );
    addNewAnnotationValueButton.removeFromParent();
    //add the button to last cell of last row
    ((TableData) lastRow.getWidget(lastRow.getWidgetCount() - 1)).add(
        addNewAnnotationValueButton
      );
  }

  @Override
  public String getKey() {
    return keyField.getValue();
  }

  @Override
  public void setKeyValidationState(ValidationState state) {
    this.formGroup.setValidationState(state);
  }

  @Override
  public void setKeyHelpText(String help) {
    this.helpBlock.setText(help);
  }
}
