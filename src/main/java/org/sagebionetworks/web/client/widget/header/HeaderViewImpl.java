package org.sagebionetworks.web.client.widget.header;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Document;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.binder.EventBinder;
import org.gwtbootstrap3.client.ui.Alert;
import org.gwtbootstrap3.client.ui.html.Div;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.jsinterop.CookieNotificationProps;
import org.sagebionetworks.web.client.jsinterop.EmptyProps;
import org.sagebionetworks.web.client.jsinterop.React;
import org.sagebionetworks.web.client.jsinterop.ReactElement;
import org.sagebionetworks.web.client.jsinterop.SRC;
import org.sagebionetworks.web.client.jsinterop.SynapseNavDrawerProps;
import org.sagebionetworks.web.client.place.Home;
import org.sagebionetworks.web.client.place.LoginPlace;
import org.sagebionetworks.web.client.place.PlansPlace;
import org.sagebionetworks.web.client.widget.FullWidthAlert;
import org.sagebionetworks.web.client.widget.ReactComponent;

public class HeaderViewImpl extends Composite implements HeaderView {

  public interface Binder extends UiBinder<Widget, HeaderViewImpl> {}

  @UiField
  Div header;

  @UiField
  ReactComponent cookieNotificationContainer;

  @UiField
  ReactComponent googleAnalyticsContainer;

  @UiField
  FullWidthAlert nihNotificationAlert;

  @UiField
  ReactComponent synapseNavDrawerContainer;

  @UiField
  Alert stagingAlert;

  @UiField
  FocusPanel editModeNavBarClickBlocker;

  private Presenter presenter;
  PortalGinInjector ginInjector;

  @Inject
  public HeaderViewImpl(Binder binder, PortalGinInjector ginInjector) {
    this.initWidget(binder.createAndBindUi(this));
    this.ginInjector = ginInjector;
    nihNotificationAlert.setOnClose(() -> {
      presenter.onNIHNotificationDismissed();
    });
    initClickHandlers();
    clear();
    rerenderNavBar();

    CookieNotificationProps props = CookieNotificationProps.create(prefs -> {
      rerenderGoogleAnalytics();
    });
    ReactElement component = React.createElementWithSynapseContext(
      SRC.SynapseComponents.CookiesNotification,
      props
    );
    cookieNotificationContainer.render(component);

    rerenderGoogleAnalytics();
  }

  @Override
  public void clear() {}

  private void rerenderGoogleAnalytics() {
    EmptyProps props = EmptyProps.create();
    ReactElement component = React.createElementWithSynapseContext(
      SRC.SynapseComponents.GoogleAnalytics,
      props
    );
    googleAnalyticsContainer.render(component);
  }

  public void rerenderNavBar() {
    SynapseNavDrawerProps props = SynapseNavDrawerProps.create(
      () -> {
        ginInjector.getAuthenticationController().logoutUser();
      },
      href -> {
        GlobalApplicationState globalAppState =
          ginInjector.getGlobalApplicationState();
        globalAppState.handleRelativePathClick(href);
      }
    );
    ReactElement component = React.createElementWithSynapseContext(
      SRC.SynapseComponents.SynapseNavDrawer,
      props
    );
    synapseNavDrawerContainer.render(component);
  }

  public void initClickHandlers() {
    editModeNavBarClickBlocker.addClickHandler(event -> {
      event.preventDefault();
      event.stopPropagation();
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
    GlobalApplicationState globalAppState =
      ginInjector.getGlobalApplicationState();
    if (
      globalAppState.getCurrentPlace() == null ||
      globalAppState.getCurrentPlace() instanceof Home ||
      globalAppState.getCurrentPlace() instanceof LoginPlace ||
      globalAppState.getCurrentPlace() instanceof PlansPlace
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

  /** Event binder code **/
  interface EBinder extends EventBinder<Header> {}

  private final EBinder eventBinder = GWT.create(EBinder.class);

  @Override
  public EventBinder<Header> getEventBinder() {
    return eventBinder;
  }

  @Override
  public void setNIHAlertVisible(boolean visible) {
    nihNotificationAlert.setVisible(visible);
  }
}
