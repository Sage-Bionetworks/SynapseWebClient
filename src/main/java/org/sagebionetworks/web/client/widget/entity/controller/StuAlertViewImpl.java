package org.sagebionetworks.web.client.widget.entity.controller;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import org.gwtbootstrap3.client.ui.html.Div;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.context.SynapseReactClientFullContextPropsProvider;
import org.sagebionetworks.web.client.jsinterop.ErrorPageProps;
import org.sagebionetworks.web.client.jsinterop.React;
import org.sagebionetworks.web.client.jsinterop.ReactNode;
import org.sagebionetworks.web.client.jsinterop.SRC;
import org.sagebionetworks.web.client.view.DownViewImpl.ErrorPageType;
import org.sagebionetworks.web.client.widget.ReactComponent;

public class StuAlertViewImpl implements StuAlertView {

  public interface Binder extends UiBinder<Widget, StuAlertViewImpl> {}

  Widget widget;
  SynapseReactClientFullContextPropsProvider propsProvider;

  @UiField
  ReactComponent errorPageContainer;

  @UiField
  Div synAlertContainer;

  Widget synAlertWidget;
  Div container = new Div();
  boolean is404, is403;
  String entityId;
  Long entityVersion;
  boolean isLoggedIn;
  GlobalApplicationState globalAppState;

  @Inject
  public StuAlertViewImpl(
    SynapseReactClientFullContextPropsProvider propsProvider,
    GlobalApplicationState globalAppState
  ) {
    this.propsProvider = propsProvider;
    this.globalAppState = globalAppState;
  }

  @Override
  public void showInfo(String message) {
    DisplayUtils.showInfo(message);
  }

  @Override
  public Widget asWidget() {
    return container;
  }

  @Override
  public void clearState() {
    container.setVisible(false);
    if (widget != null) {
      entityId = null;
      entityVersion = null;
      is404 = false;
      is403 = false;
      errorPageContainer.setVisible(false);
    }
  }

  private void lazyConstruct() {
    if (widget == null) {
      Binder b = GWT.create(Binder.class);
      widget = b.createAndBindUi(this);
      synAlertContainer.add(synAlertWidget);
      container.add(widget);
      widget.addAttachHandler(event -> {
        if (event.isAttached()) {
          updateErrorPage();
        }
      });
    }
  }

  private void renderErrorPage(ErrorPageType type) {
    ErrorPageProps props = ErrorPageProps.create(
      type.name(),
      null, //custom message
      entityId,
      entityVersion,
      href -> {
        globalAppState.handleRelativePathClick(href);
      }
    );
    ReactNode component = React.createElementWithSynapseContext(
      SRC.SynapseComponents.ErrorPage,
      props,
      propsProvider.getJsInteropContextProps()
    );
    errorPageContainer.render(component);
    errorPageContainer.setVisible(true);
  }

  private void updateErrorPage() {
    if (is404) {
      renderErrorPage(ErrorPageType.NOT_FOUND);
    }
    if (is403) {
      renderErrorPage(ErrorPageType.ACCESS_DENIED);
    }
  }

  @Override
  public void show403(String entityId, Long entityVersion, boolean isLoggedIn) {
    is403 = true;
    this.isLoggedIn = isLoggedIn;
    this.entityId = entityId;
    this.entityVersion = entityVersion;
    lazyConstruct();
    container.setVisible(true);
    updateErrorPage();
  }

  @Override
  public void show404(String entityId, Long entityVersion, boolean isLoggedIn) {
    is404 = true;
    this.isLoggedIn = isLoggedIn;
    this.entityId = entityId;
    this.entityVersion = entityVersion;
    lazyConstruct();
    container.setVisible(true);
    updateErrorPage();
  }

  @Override
  public void setSynAlert(Widget w) {
    synAlertWidget = w;
  }

  @Override
  public void setVisible(boolean visible) {
    if (visible) {
      lazyConstruct();
    }
    container.setVisible(visible);
  }
}
