package org.sagebionetworks.web.client.widget.header;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Document;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.binder.EventBinder;
import org.gwtbootstrap3.client.ui.Alert;
import org.gwtbootstrap3.client.ui.html.Div;
import org.gwtbootstrap3.client.ui.html.Span;
import org.sagebionetworks.schema.adapter.JSONObjectAdapter;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.context.SynapseContextPropsProvider;
import org.sagebionetworks.web.client.jsinterop.React;
import org.sagebionetworks.web.client.jsinterop.ReactNode;
import org.sagebionetworks.web.client.jsinterop.SRC;
import org.sagebionetworks.web.client.jsinterop.SynapseNavDrawerProps;
import org.sagebionetworks.web.client.place.Home;
import org.sagebionetworks.web.client.place.LoginPlace;
import org.sagebionetworks.web.client.widget.FullWidthAlert;
import org.sagebionetworks.web.client.widget.ReactComponentDiv;

public class HeaderViewImpl extends Composite implements HeaderView {

  public interface Binder extends UiBinder<Widget, HeaderViewImpl> {}

  @UiField
  Div header;

  @UiField
  FullWidthAlert cookieNotificationAlert;

  @UiField
  Div portalAlert;

  @UiField
  Image portalLogo;

  @UiField
  Span portalName;

  @UiField
  FocusPanel portalLogoFocusPanel;

  @UiField
  ReactComponentDiv synapseNavDrawerContainer;

  @UiField
  Alert stagingAlert;

  @UiField
  FocusPanel editModeNavBarClickBlocker;

  private Presenter presenter;
  String portalHref = "";
  SynapseContextPropsProvider propsProvider;
  PortalGinInjector ginInjector;

  @Inject
  public HeaderViewImpl(
    Binder binder,
    SynapseContextPropsProvider propsProvider,
    PortalGinInjector ginInjector
  ) {
    this.initWidget(binder.createAndBindUi(this));
    this.ginInjector = ginInjector;
    this.propsProvider = propsProvider;
    cookieNotificationAlert.addPrimaryCTAClickHandler(event -> {
      presenter.onCookieNotificationDismissed();
    });

    initClickHandlers();
    clear();
    rerenderNavBar();
  }

  @Override
  public void clear() {}

  public void rerenderNavBar() {
    SynapseNavDrawerProps props = SynapseNavDrawerProps.create(() -> {
      ginInjector.getAuthenticationController().logoutUser();
    });
    ReactNode component = React.createElementWithSynapseContext(
      SRC.SynapseComponents.SynapseNavDrawer,
      props,
      propsProvider.getJsInteropContextProps()
    );
    synapseNavDrawerContainer.render(component);
  }

  public void initClickHandlers() {
    editModeNavBarClickBlocker.addClickHandler(event -> {
      event.preventDefault();
      event.stopPropagation();
    });
    portalLogoFocusPanel.addClickHandler(event -> {
      if (DisplayUtils.isDefined(portalHref)) {
        Window.Location.assign(portalHref);
      }
    });
  }

  @Override
  public void setPresenter(Presenter presenter) {
    this.presenter = presenter;
    refresh();
  }

  private void hideNavBar() {
    synapseNavDrawerContainer.setVisible(false);
    Document.get().getBody().removeClassName("SynapseNavDrawerIsShowing");
  }

  private void showNavBar() {
    synapseNavDrawerContainer.setVisible(true);
    Document.get().getBody().addClassName("SynapseNavDrawerIsShowing");
  }

  @Override
  public void refresh() {
    GlobalApplicationState globalAppState = ginInjector.getGlobalApplicationState();
    if (
      globalAppState.getCurrentPlace() == null ||
      globalAppState.getCurrentPlace() instanceof Home ||
      globalAppState.getCurrentPlace() instanceof LoginPlace
    ) {
      hideNavBar();
    } else {
      rerenderNavBar();
      showNavBar();
      // cover with a click catcher if in editing mode
      editModeNavBarClickBlocker.setVisible(globalAppState.isEditing());
    }
  }

  @Override
  public void openNewWindow(String url) {
    DisplayUtils.newWindow(url, "", "");
  }

  @Override
  public void setStagingAlertVisible(boolean visible) {
    stagingAlert.setVisible(visible);
  }

  @Override
  public void setCookieNotificationVisible(boolean visible) {
    cookieNotificationAlert.setVisible(visible);
  }

  /** Event binder code **/
  interface EBinder extends EventBinder<Header> {}

  private final EBinder eventBinder = GWT.create(EBinder.class);

  @Override
  public EventBinder<Header> getEventBinder() {
    return eventBinder;
  }

  @Override
  public void setPortalAlertVisible(boolean visible, JSONObjectAdapter json) {
    if (visible) {
      try {
        if (json.has("callbackUrl")) {
          String href = json.getString("callbackUrl");
          portalHref = href;
        }
        if (json.has("portalName")) {
          String name = json.getString("portalName");
          if (!name.trim().isEmpty()) {
            portalName.setText(name);
            portalName.setVisible(true);
            portalLogo.setVisible(false);
          }
        }
        if (json.has("logoUrl")) {
          String logoUrl = json.getString("logoUrl");
          if (!logoUrl.trim().isEmpty()) {
            portalLogo.setUrl(logoUrl);
            portalName.setVisible(false);
            portalLogo.setVisible(true);
          }
        }
        portalAlert.setVisible(true);
      } catch (JSONObjectAdapterException e) {
        e.printStackTrace();
      }
    }
    portalAlert.setVisible(visible);
  }
}
