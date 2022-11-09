package org.sagebionetworks.web.client.view;

import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.TextArea;
import org.gwtbootstrap3.client.ui.html.Div;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.widget.header.Header;

public class DataAccessApprovalTokenViewImpl
  implements DataAccessApprovalTokenView {

  private Header headerWidget;

  @UiField
  Button submitButton;

  @UiField
  TextArea textArea;

  @UiField
  Div synAlertContainer;

  Presenter presenter;
  String originalButtonText;

  public interface Binder
    extends UiBinder<Widget, DataAccessApprovalTokenViewImpl> {}

  public Widget widget;

  @Inject
  public DataAccessApprovalTokenViewImpl(
    Binder uiBinder,
    Header headerWidget,
    GlobalApplicationState globalAppState
  ) {
    widget = uiBinder.createAndBindUi(this);
    this.headerWidget = headerWidget;
    headerWidget.configure();
    submitButton.addClickHandler(event -> presenter.onSubmitToken());
    originalButtonText = submitButton.getText();
  }

  @Override
  public void setSynAlert(IsWidget w) {
    synAlertContainer.clear();
    synAlertContainer.add(w);
  }

  @Override
  public void setPresenter(Presenter presenter) {
    this.presenter = presenter;
  }

  @Override
  public String getAccessApprovalToken() {
    return textArea.getText();
  }

  @Override
  public void setAccessApprovalToken(String token) {
    textArea.setText(token);
  }

  @Override
  public void refreshHeader() {
    headerWidget.configure();
    headerWidget.refresh();
    com.google.gwt.user.client.Window.scrollTo(0, 0); // scroll user to top of page
  }

  @Override
  public Widget asWidget() {
    return widget;
  }

  @Override
  public void setLoading(boolean isLoading) {
    DisplayUtils.showLoading(submitButton, isLoading, originalButtonText);
    textArea.setEnabled(!isLoading);
  }
}
