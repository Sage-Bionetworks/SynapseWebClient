package org.sagebionetworks.web.client.widget.table.modal.fileview;

import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import org.gwtbootstrap3.client.ui.FormGroup;
import org.gwtbootstrap3.client.ui.TextArea;
import org.gwtbootstrap3.client.ui.TextBox;
import org.gwtbootstrap3.client.ui.html.Div;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.cookie.CookieProvider;

public class CreateTableViewWizardStep1ViewImpl
  implements CreateTableViewWizardStep1View {

  public interface Binder
    extends UiBinder<Widget, CreateTableViewWizardStep1ViewImpl> {}

  @UiField
  TextBox nameField;

  @UiField
  FormGroup descriptionFormGroup;

  @UiField
  TextArea descriptionField;

  @UiField
  SimplePanel entityViewScopeContainer;

  @UiField
  FormGroup entityViewScopeUI;

  @UiField
  SimplePanel submissionViewScopeContainer;

  @UiField
  FormGroup submissionViewScopeUI;

  @UiField
  Div viewOptionsContainer;

  Widget widget;
  Presenter p;
  FileViewOptions viewOptions;

  @Inject
  public CreateTableViewWizardStep1ViewImpl(
    Binder binder,
    FileViewOptions viewOptions,
    CookieProvider cookies
  ) {
    widget = binder.createAndBindUi(this);
    this.viewOptions = viewOptions;
    viewOptionsContainer.add(viewOptions);
    viewOptions.addClickHandler(event -> {
      p.updateViewTypeMask();
    });
    // This constructor won't re-run unless the page is refreshed, so the FormGroup won't be visible after enabling Experimental Mode w/o a refresh
    descriptionFormGroup.setVisible(DisplayUtils.isInTestWebsite(cookies));
  }

  @Override
  public boolean isFileSelected() {
    return viewOptions.isIncludeFiles();
  }

  @Override
  public void setIsFileSelected(boolean value) {
    viewOptions.setIsIncludeFiles(value);
  }

  @Override
  public boolean isFolderSelected() {
    return viewOptions.isIncludeFolders();
  }

  @Override
  public void setIsFolderSelected(boolean value) {
    viewOptions.setIsIncludeFolders(value);
  }

  @Override
  public boolean isDatasetSelected() {
    return viewOptions.isIncludeDatasets();
  }

  @Override
  public void setIsDatasetSelected(boolean value) {
    viewOptions.setIsIncludeDatasets(value);
  }

  @Override
  public boolean isTableSelected() {
    return viewOptions.isIncludeTables();
  }

  @Override
  public void setIsTableSelected(boolean value) {
    viewOptions.setIsIncludeTables(value);
  }

  @Override
  public Widget asWidget() {
    return widget;
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
  public void setEntityViewScopeWidget(IsWidget scopeWidget) {
    entityViewScopeContainer.clear();
    entityViewScopeContainer.setWidget(scopeWidget);
  }

  @Override
  public void setEntityViewScopeWidgetVisible(boolean visible) {
    entityViewScopeUI.setVisible(visible);
  }

  @Override
  public void setSubmissionViewScopeWidget(IsWidget scopeWidget) {
    submissionViewScopeContainer.clear();
    submissionViewScopeContainer.setWidget(scopeWidget);
  }

  @Override
  public void setSubmissionViewScopeWidgetVisible(boolean visible) {
    submissionViewScopeUI.setVisible(visible);
  }

  @Override
  public void setViewTypeOptionsVisible(boolean visible) {
    viewOptionsContainer.setVisible(visible);
  }

  @Override
  public void setPresenter(Presenter presenter) {
    p = presenter;
  }
}
