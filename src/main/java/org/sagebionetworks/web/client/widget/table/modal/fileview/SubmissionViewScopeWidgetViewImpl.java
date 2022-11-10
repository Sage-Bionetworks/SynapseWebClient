package org.sagebionetworks.web.client.widget.table.modal.fileview;

import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.Modal;
import org.sagebionetworks.web.client.DisplayUtils;

public class SubmissionViewScopeWidgetViewImpl
  implements SubmissionViewScopeWidgetView {

  public interface Binder
    extends UiBinder<Widget, SubmissionViewScopeWidgetViewImpl> {}

  @UiField
  SimplePanel viewScopeContainer;

  @UiField
  SimplePanel editScopeContainer;

  @UiField
  SimplePanel editScopeAlertContainer;

  @UiField
  Button saveButton;

  @UiField
  Button editButton;

  @UiField
  Modal editModal;

  Widget widget;
  Presenter presenter;
  String originalButtonText;

  @Inject
  public SubmissionViewScopeWidgetViewImpl(Binder binder) {
    widget = binder.createAndBindUi(this);
    editButton.addClickHandler(event -> {
      presenter.onEdit();
    });
    saveButton.addClickHandler(event -> {
      presenter.onSave();
    });
    originalButtonText = saveButton.getText();
  }

  @Override
  public Widget asWidget() {
    return widget;
  }

  @Override
  public void setVisible(boolean visible) {
    widget.setVisible(visible);
  }

  @Override
  public void setEvaluationListWidget(IsWidget entityListWidget) {
    viewScopeContainer.clear();
    viewScopeContainer.setWidget(entityListWidget);
  }

  @Override
  public void setSubmissionViewScopeEditor(IsWidget entityListWidget) {
    editScopeContainer.clear();
    editScopeContainer.setWidget(entityListWidget);
  }

  @Override
  public void showModal() {
    editModal.show();
  }

  @Override
  public void hideModal() {
    editModal.hide();
  }

  @Override
  public void setSynAlert(IsWidget w) {
    editScopeAlertContainer.clear();
    editScopeAlertContainer.setWidget(w);
  }

  @Override
  public void setPresenter(Presenter presenter) {
    this.presenter = presenter;
  }

  @Override
  public void setEditButtonVisible(boolean visible) {
    editButton.setVisible(visible);
  }

  @Override
  public void setLoading(boolean loading) {
    DisplayUtils.showLoading(saveButton, loading, originalButtonText);
  }
}
