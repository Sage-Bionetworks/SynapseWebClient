package org.sagebionetworks.web.client.widget.entity.annotation;

import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.Modal;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.widget.CommaSeparatedValuesParserView;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlertView;

public class EditAnnotationsDialogViewImpl
  implements EditAnnotationsDialogView {

  public interface Binder
    extends UiBinder<Widget, EditAnnotationsDialogViewImpl> {}

  @UiField
  FlowPanel editorsPanel;

  @UiField
  Modal editModal;

  @UiField
  Button saveButton;

  @UiField
  Button cancelButton;

  @UiField
  Button addAnnotationButton;

  @UiField
  Button pasteNewValuesButton;

  @UiField
  FlowPanel pasteNewValuesPanel;

  @UiField
  SynapseAlertView alert;

  Presenter presenter;

  Widget widget;
  String originalButtonText;

  @Inject
  public EditAnnotationsDialogViewImpl(final Binder uiBinder) {
    widget = uiBinder.createAndBindUi(this);
    saveButton.addClickHandler(event -> {
      presenter.onSave();
    });
    addAnnotationButton.addClickHandler(event -> {
      presenter.onAddNewAnnotation(null);
    });
    pasteNewValuesButton.addClickHandler(clickEvent -> {
      presenter.onClickPasteNewValues();
    });
    saveButton.addDomHandler(
      DisplayUtils.getPreventTabHandler(saveButton),
      KeyDownEvent.getType()
    );
    originalButtonText = saveButton.getText();
  }

  @Override
  public void setPresenter(final Presenter presenter) {
    this.presenter = presenter;
  }

  @Override
  public void showEditor() {
    setLoading(false);
    alert.clearState();
    editModal.show();
  }

  @Override
  public void hideEditor() {
    editModal.hide();
  }

  @Override
  public void setLoading() {
    setLoading(true);
  }

  private void setLoading(boolean isLoading) {
    DisplayUtils.showLoading(saveButton, isLoading, originalButtonText);
  }

  @Override
  public void showError(String message) {
    alert.showError(message);
    // enable the save button after an error
    setLoading(false);
  }

  @Override
  public void hideErrors() {
    alert.clearState();
  }

  @Override
  public void addAnnotationEditor(Widget editor) {
    editorsPanel.add(editor);
  }

  @Override
  public void removeAnnotationEditor(Widget editor) {
    editorsPanel.remove(editor);
  }

  @Override
  public void addCommaSeparatedValuesParser(Widget commaSeparatedValuesParser) {
    pasteNewValuesPanel.add(commaSeparatedValuesParser);
  }

  @Override
  public Widget asWidget() {
    return widget;
  }

  @Override
  public void clearAnnotationEditors() {
    editorsPanel.clear();
  }

  @Override
  public void showInfo(String message) {
    DisplayUtils.showInfo(message);
  }
}
