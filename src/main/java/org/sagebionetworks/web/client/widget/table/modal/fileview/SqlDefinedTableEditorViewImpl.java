package org.sagebionetworks.web.client.widget.table.modal.fileview;

import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.FormGroup;
import org.gwtbootstrap3.client.ui.Heading;
import org.gwtbootstrap3.client.ui.Modal;
import org.gwtbootstrap3.client.ui.TextArea;
import org.gwtbootstrap3.client.ui.TextBox;
import org.gwtbootstrap3.client.ui.html.Div;
import org.gwtbootstrap3.client.ui.html.Span;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.cookie.CookieProvider;
import org.sagebionetworks.web.client.widget.HelpWidget;

public class SqlDefinedTableEditorViewImpl
  implements SqlDefinedTableEditorView {

  public interface Binder
    extends UiBinder<Widget, SqlDefinedTableEditorViewImpl> {}

  @UiField
  Heading modalHeading;

  @UiField
  TextBox nameField;

  @UiField
  FormGroup descriptionFormGroup;

  @UiField
  TextArea descriptionField;

  @UiField
  TextArea definingSqlField;

  @UiField
  Span helpContainer;

  @UiField
  Div synapseAlertContainer;

  @UiField
  Button primaryButton;

  @UiField
  Button defaultButton;

  Modal modal;
  Presenter p;

  @Inject
  public SqlDefinedTableEditorViewImpl(Binder binder, CookieProvider cookies) {
    modal = (Modal) binder.createAndBindUi(this);
    // This constructor won't re-run unless the page is refreshed, so the FormGroup won't be visible after enabling Experimental Mode w/o a refresh
    descriptionFormGroup.setVisible(DisplayUtils.isInTestWebsite(cookies));
    defaultButton.addClickHandler(event -> {
      modal.hide();
    });
    primaryButton.addClickHandler(event -> {
      p.onSave();
    });
  }

  @Override
  public Widget asWidget() {
    return modal;
  }

  @Override
  public String getName() {
    return nameField.getText();
  }

  @Override
  public void setName(String name) {
    nameField.setText(name);
  }

  @Override
  public String getDescription() {
    return descriptionField.getText();
  }

  @Override
  public void setDescription(String description) {
    descriptionField.setText(description);
  }

  @Override
  public void setDefiningSql(String definingSql) {
    definingSqlField.setText(definingSql);
  }

  @Override
  public String getDefiningSql() {
    return definingSqlField.getText();
  }

  @Override
  public void setModalTitle(String title) {
    modalHeading.setText(title);
  }

  @Override
  public void setHelp(String helpMarkdown, String helpUrl) {
    helpContainer.clear();
    HelpWidget help = new HelpWidget();
    help.setHref(helpUrl);
    help.setHelpMarkdown(helpMarkdown);
    help.setAddStyleNames("margin-left-5");
    helpContainer.add(help);
  }

  @Override
  public void setSynAlert(IsWidget w) {
    synapseAlertContainer.clear();
    synapseAlertContainer.add(w);
  }

  @Override
  public void setPresenter(Presenter presenter) {
    p = presenter;
  }

  @Override
  public void setLoading(boolean loading) {
    primaryButton.setEnabled(!loading);
  }

  @Override
  public void show() {
    modal.show();
  }

  @Override
  public void hide() {
    modal.hide();
  }

  @Override
  public void reset() {
    nameField.setText("");
    descriptionField.setText("");
    definingSqlField.setText("");
    setLoading(false);
  }
}
