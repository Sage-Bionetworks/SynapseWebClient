package org.sagebionetworks.web.client.widget.table.v2.results.cell;

import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.Modal;
import org.gwtbootstrap3.client.ui.constants.ButtonSize;
import org.gwtbootstrap3.client.ui.constants.ButtonType;
import org.gwtbootstrap3.client.ui.constants.IconType;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.view.bootstrap.table.Table;
import org.sagebionetworks.web.client.view.bootstrap.table.TableData;
import org.sagebionetworks.web.client.view.bootstrap.table.TableRow;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlertView;

public class EditJSONListModalViewImpl implements EditJSONListModalView {

  public interface Binder extends UiBinder<Widget, EditJSONListModalViewImpl> {}

  @UiField
  Table editorsPanel;

  @UiField
  Modal editModal;

  @UiField
  Button saveButton;

  @UiField
  Button cancelButton;

  @UiField
  Button pasteNewValuesButton;

  @UiField
  FlowPanel pasteNewValuesPanel;

  @UiField
  SynapseAlertView alert;

  Presenter presenter;
  Button addNewValueButton;

  Widget widget;

  @Inject
  public EditJSONListModalViewImpl(final Binder uiBinder) {
    widget = uiBinder.createAndBindUi(this);
    saveButton.addClickHandler(event -> {
      presenter.onSave();
    });
    pasteNewValuesButton.addClickHandler(clickEvent -> {
      presenter.onClickPasteNewValues();
    });
    saveButton.addDomHandler(
      DisplayUtils.getPreventTabHandler(saveButton),
      KeyDownEvent.getType()
    );
    cancelButton.addClickHandler(clickEvent -> hideEditor());

    addNewValueButton =
      new Button(
        "",
        IconType.PLUS,
        clickEvent -> presenter.onAddNewEmptyValue()
      );
    addNewValueButton.addStyleName("center-in-div");
    addNewValueButton.setType(ButtonType.PRIMARY);
    addNewValueButton.setSize(ButtonSize.EXTRA_SMALL);
  }

  @Override
  public void setPresenter(final Presenter presenter) {
    this.presenter = presenter;
  }

  @Override
  public void showError(String message) {
    alert.showError(message);
  }

  @Override
  public void clearEditors() {
    editorsPanel.clear();
  }

  @Override
  public void addCommaSeparatedValuesParser(Widget commaSeparatedValuesParser) {
    pasteNewValuesPanel.add(commaSeparatedValuesParser);
  }

  @Override
  public void showEditor() {
    alert.clearState();
    editModal.show();
  }

  @Override
  public void hideEditor() {
    editModal.hide();
  }

  @Override
  public void addNewEditor(final CellEditor editor) {
    TableRow row = CellFactory.appendDeleteButton(
      editor,
      presenter::onValueDeleted
    );

    //additional column placeholder for the addNewAnnotationValueButton
    TableData addButtonTableData = new TableData();
    row.add(addButtonTableData);
    addButtonTableData.setWidth("35px");

    editorsPanel.add(row);

    addNewValueButton.removeFromParent();
    addButtonTableData.add(addNewValueButton);
  }

  @Override
  public void moveAddNewAnnotationValueButtonToRowToLastRow() {
    TableRow lastRow = (TableRow) editorsPanel.getWidget(
      editorsPanel.getWidgetCount() - 1
    );
    addNewValueButton.removeFromParent();
    //add the button to last cell of last row
    ((TableData) lastRow.getWidget(lastRow.getWidgetCount() - 1)).add(
        addNewValueButton
      );
  }

  @Override
  public Widget asWidget() {
    return widget;
  }
}
