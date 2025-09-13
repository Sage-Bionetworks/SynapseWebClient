package org.sagebionetworks.web.client.widget.docker.modal;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.Modal;
import org.gwtbootstrap3.client.ui.TextBox;
import org.gwtbootstrap3.client.ui.html.Div;

public class AddDockerCommitModalViewImpl implements AddDockerCommitModalView {

  public interface Binder
    extends UiBinder<Widget, AddDockerCommitModalViewImpl> {}

  @UiField
  Button saveButton;

  @UiField
  Button cancelButton;

  @UiField
  Modal addDockerCommitModal;

  @UiField
  TextBox digestTextBox;

  @UiField
  TextBox tagTextBox;

  @UiField
  Div synAlertContainer;

  private Widget widget;
  private Presenter presenter;

  @Inject
  public AddDockerCommitModalViewImpl(Binder binder) {
    widget = binder.createAndBindUi(this);
    saveButton.addClickHandler(
      new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
          presenter.onSave();
        }
      }
    );
    cancelButton.addClickHandler(
      new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
          presenter.onCancel();
        }
      }
    );
  }

  @Override
  public void setPresenter(Presenter presenter) {
    this.presenter = presenter;
  }

  @Override
  public void show() {
    addDockerCommitModal.show();
  }

  @Override
  public void hide() {
    addDockerCommitModal.hide();
  }

  @Override
  public String getDigest() {
    return digestTextBox.getText().trim();
  }

  @Override
  public String getTag() {
    return tagTextBox.getText().trim();
  }

  @Override
  public void clear() {
    digestTextBox.setText("");
    tagTextBox.setText("");
  }

  @Override
  public void setAlert(Widget widget) {
    synAlertContainer.clear();
    synAlertContainer.add(widget);
  }

  @Override
  public void setModalTitle(String title) {
    addDockerCommitModal.setTitle(title);
  }

  @Override
  public Widget asWidget() {
    return widget;
  }
}
