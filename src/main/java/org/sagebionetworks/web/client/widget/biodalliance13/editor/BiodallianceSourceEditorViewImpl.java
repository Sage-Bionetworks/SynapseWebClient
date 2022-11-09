package org.sagebionetworks.web.client.widget.biodalliance13.editor;

import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.CheckBox;
import org.gwtbootstrap3.client.ui.Input;
import org.gwtbootstrap3.client.ui.TextBox;
import org.sagebionetworks.web.client.DisplayUtils;

public class BiodallianceSourceEditorViewImpl
  implements IsWidget, BiodallianceSourceEditorView {

  public interface BiodallianceSourceViewImplUiBinder
    extends UiBinder<Widget, BiodallianceSourceEditorViewImpl> {}

  Widget widget;

  @UiField
  TextBox sourceNameTextbox;

  @UiField
  TextBox entityPickerTextbox;

  @UiField
  Button entityPickerButton;

  @UiField
  TextBox indexEntityPickerTextbox;

  @UiField
  Button indexEntityPickerButton;

  @UiField
  Input colorPicker;

  @UiField
  TextBox heightField;

  @UiField
  CheckBox select;

  Presenter presenter;

  @Inject
  public BiodallianceSourceEditorViewImpl(
    BiodallianceSourceViewImplUiBinder binder
  ) {
    widget = binder.createAndBindUi(this);
    entityPickerTextbox.addClickHandler(event -> entityPicker());
    entityPickerButton.addClickHandler(event -> entityPicker());
    indexEntityPickerTextbox.addClickHandler(event -> indexEntityPicker());
    indexEntityPickerButton.addClickHandler(event -> indexEntityPicker());
    select.addClickHandler(event -> presenter.onSelectionChanged());
  }

  public void entityPicker() {
    entityPickerTextbox.selectAll();
    presenter.entityPickerClicked();
  }

  public void indexEntityPicker() {
    indexEntityPickerTextbox.selectAll();
    presenter.indexEntityPickerClicked();
  }

  public void setEntityFinderText(String text) {
    this.entityPickerTextbox.setValue(text);
  }

  @Override
  public void setIndexEntityFinderText(String text) {
    this.indexEntityPickerTextbox.setValue(text);
  }

  public String getColor() {
    return colorPicker.getValue();
  }

  public void setColor(String color) {
    colorPicker.setValue(color);
  }

  public String getHeight() {
    return heightField.getValue();
  }

  public void setHeight(String height) {
    heightField.setValue(height);
  }

  @Override
  public String getSourceName() {
    return sourceNameTextbox.getValue();
  }

  @Override
  public void setSourceName(String sourceName) {
    sourceNameTextbox.setValue(sourceName);
  }

  @Override
  public Widget asWidget() {
    return widget;
  }

  @Override
  public void setPresenter(Presenter presenter) {
    this.presenter = presenter;
  }

  @Override
  public void initView() {}

  @Override
  public void checkParams() throws IllegalArgumentException {}

  @Override
  public void showLoading() {}

  @Override
  public void showInfo(String message) {}

  @Override
  public void clear() {}

  @Override
  public void showErrorMessage(String message) {
    DisplayUtils.showErrorMessage(message);
  }

  @Override
  public boolean isSelected() {
    return select.getValue();
  }

  @Override
  public void setSelected(boolean selected) {
    select.setValue(selected);
  }
}
