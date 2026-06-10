package org.sagebionetworks.web.client.view;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;
import javax.inject.Inject;
import org.gwtbootstrap3.client.ui.Anchor;
import org.gwtbootstrap3.client.ui.Lead;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.widget.header.Header;

public class ErrorViewImpl implements ErrorView {

  @UiField
  Anchor goBackLink;

  @UiField
  Lead message;

  private Header headerWidget;

  public interface Binder extends UiBinder<Widget, ErrorViewImpl> {}

  public Widget widget;

  private final Binder uiBinder = GWT.create(Binder.class);

  @Inject
  public ErrorViewImpl(
    Header headerWidget,
    GlobalApplicationState globalAppState
  ) {
    widget = uiBinder.createAndBindUi(this);
    this.headerWidget = headerWidget;
    headerWidget.configure();
    goBackLink.addClickHandler(event -> globalAppState.gotoLastPlace());
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
  public void setErrorMessage(String error) {
    message.setText(error);
  }
}
