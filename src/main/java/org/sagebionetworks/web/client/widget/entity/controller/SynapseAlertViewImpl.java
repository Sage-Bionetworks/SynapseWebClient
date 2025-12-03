package org.sagebionetworks.web.client.widget.entity.controller;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Widget;
import org.gwtbootstrap3.client.ui.html.Div;
import org.gwtbootstrap3.client.ui.html.Span;
import org.sagebionetworks.web.client.widget.FullWidthAlert;

public class SynapseAlertViewImpl implements SynapseAlertView {

  public interface Binder extends UiBinder<Widget, SynapseAlertViewImpl> {}

  private static Binder uiBinder = GWT.create(Binder.class);

  Widget widget = null;

  @UiField
  FullWidthAlert alert;

  @UiField
  Div loginWidgetContainer;

  Span synapseAlertContainer = new Span();

  public SynapseAlertViewImpl() {}

  public ClickHandler reloadClickHandler = event -> {
    reload();
  };

  private void lazyConstruct() {
    if (widget == null) {
      synapseAlertContainer.setVisible(false);
      widget = uiBinder.createAndBindUi(this);
      synapseAlertContainer.add(widget);
      alert.addPrimaryCTAClickHandler(reloadClickHandler);

      clearState();
    }
  }

  @Override
  public void setRetryButtonVisible(boolean visible) {
    lazyConstruct();
    alert.setPrimaryCTAText(visible ? "Retry" : null);
    alert.addPrimaryCTAClickHandler(reloadClickHandler);
  }

  @Override
  public void setServiceDeskButtonVisible(boolean visible) {
    lazyConstruct();
    alert.setPrimaryCTAText(visible ? "Report Issue" : null);
    alert.addPrimaryCTAClickHandler(event -> {
      Window.open(
        "https://sagebionetworks.jira.com/servicedesk/customer/portal/9/group/16/create/84",
        "_blank",
        ""
      );
    });
  }

  @Override
  public Widget asWidget() {
    return synapseAlertContainer;
  }

  @Override
  public void clearState() {
    if (widget != null) {
      alert.setVisible(false);
      alert.setMessage("");
      loginWidgetContainer.setVisible(false);
      alert.setPrimaryCTAText(null);
    }
  }

  @Override
  public void showLogin() {
    lazyConstruct();
    synapseAlertContainer.setVisible(true);
    loginWidgetContainer.setVisible(true);
  }

  @Override
  public void showError(String error) {
    lazyConstruct();
    synapseAlertContainer.setVisible(true);
    alert.setMessage(error);
    alert.setVisible(true);
  }

  @Override
  public void reload() {
    Window.Location.reload();
  }
}
