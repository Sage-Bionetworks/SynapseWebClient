package org.sagebionetworks.web.client.widget.entity.act;

import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import java.util.ArrayList;
import org.gwtbootstrap3.client.ui.Alert;
import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.CheckBox;
import org.gwtbootstrap3.client.ui.Modal;
import org.gwtbootstrap3.client.ui.TextArea;
import org.gwtbootstrap3.client.ui.html.Div;
import org.sagebionetworks.web.client.DisplayUtils;

public class RejectReasonViewImpl implements RejectReasonView {

  public interface Binder extends UiBinder<Modal, RejectReasonViewImpl> {}

  @UiField
  Modal modal;

  // Generated Response Preview
  @UiField
  TextArea nameField;

  @UiField
  Div reasonsContainer;

  @UiField
  CheckBox customTextOption;

  // Generate response button
  @UiField
  Button generateButton;

  // Text Box for custom checkbox
  @UiField
  TextArea customText;

  // alert if no responses submitted
  @UiField
  Alert alert;

  // Cancel and Submit Buttons
  @UiField
  Button primaryButton;

  @UiField
  Button defaultButton;

  Widget widget;
  ArrayList<CheckBox> checkboxes = new ArrayList<>();
  // Presenter
  Presenter presenter;

  @Inject
  public RejectReasonViewImpl(Binder binder) {
    widget = binder.createAndBindUi(this);

    defaultButton.addClickHandler(event -> modal.hide());
    primaryButton.addClickHandler(event -> presenter.onSave());
    primaryButton.addDomHandler(
      DisplayUtils.getPreventTabHandler(primaryButton),
      KeyDownEvent.getType()
    );

    generateButton.addClickHandler(event -> presenter.updateResponse());
    customTextOption.addClickHandler(event ->
      customText.setVisible(customTextOption.getValue())
    );
  }

  public void setPresenter(Presenter presenter) {
    this.presenter = presenter;
  }

  public void setValue(String value) {
    nameField.setText(value);
  }

  @Override
  public Widget asWidget() {
    return widget;
  }

  @Override
  public String getValue() {
    return nameField.getText();
  }

  @Override
  public void showError(String error) {
    alert.setVisible(true);
    alert.setText(error);
  }

  @Override
  public void hide() {
    modal.hide();
  }

  @Override
  public void show() {
    modal.show();
    nameField.setFocus(true);
  }

  @Override
  public void clear() {
    this.clearError();
    this.customText.clear();
    this.nameField.clear();
    for (CheckBox cb : checkboxes) {
      cb.setValue(false);
    }
    this.customTextOption.setValue(false);
    this.customText.setVisible(false);
  }

  @Override
  public void clearError() {
    this.alert.setVisible(false);
  }

  @Override
  public void clearReasons() {
    reasonsContainer.clear();
    checkboxes = new ArrayList<CheckBox>();
  }

  @Override
  public void addReason(String reason) {
    CheckBox cb = new CheckBox(reason);
    cb.addStyleName("margin-top-20");
    checkboxes.add(cb);
    reasonsContainer.add(cb);
  }

  @Override
  public String getSelectedCheckboxText() {
    String output = "";
    for (CheckBox checkBox : checkboxes) {
      if (checkBox.getValue()) {
        output += "\n" + checkBox.getText() + "\n";
      }
    }
    if (customTextOption.getValue()) {
      output += "\n" + customText.getText() + "\n";
    }

    return output;
  }
}
