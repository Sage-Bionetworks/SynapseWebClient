package org.sagebionetworks.web.client.widget.doi;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;
import javax.inject.Inject;
import org.gwtbootstrap3.client.ui.html.Span;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.widget.CopyTextModal;
import org.sagebionetworks.web.client.widget.TextBoxWithCopyToClipboardWidget;

public class DoiWidgetV2ViewImpl implements DoiWidgetV2View {

  @UiField
  Span doiLabel;

  @UiField
  TextBoxWithCopyToClipboardWidget copyToClipboardWidget;

  @UiField
  Span synAlertContainer;

  boolean isLabelVisible = true;

  Widget widget;

  public interface Binder extends UiBinder<Widget, DoiWidgetV2ViewImpl> {}

  CopyTextModal copyTextModal;

  private final Binder uiBinder = GWT.create(Binder.class);

  @Inject
  public DoiWidgetV2ViewImpl(CopyTextModal copyTextModal) {
    widget = uiBinder.createAndBindUi(this);
    this.copyTextModal = copyTextModal;
    copyTextModal.setTitle("DOI");
  }

  @Override
  public void showDoi(String doiText) {
    widget.setVisible(true);
    copyToClipboardWidget.setText(doiText);
    copyToClipboardWidget.setCopyIconVisible(true);
    doiLabel.setVisible(isLabelVisible);
  }

  @Override
  public void showLoading() {}

  @Override
  public Widget asWidget() {
    return widget;
  }

  @Override
  public void clear() {
    copyToClipboardWidget.setText("");
    doiLabel.setVisible(false);
  }

  @Override
  public void showInfo(String message) {
    DisplayUtils.showInfo(message);
  }

  @Override
  public void showErrorMessage(String message) {
    DisplayUtils.showErrorMessage(message);
  }

  @Override
  public void hide() {
    widget.setVisible(false);
  }

  @Override
  public void setLabelVisible(boolean visible) {
    isLabelVisible = visible;
  }
}
