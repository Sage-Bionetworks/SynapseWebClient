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
import org.sagebionetworks.web.client.context.SynapseReactClientFullContextPropsProvider;
import org.sagebionetworks.web.client.jsinterop.React;
import org.sagebionetworks.web.client.jsinterop.ReactNode;
import org.sagebionetworks.web.client.jsinterop.SRC;
import org.sagebionetworks.web.client.jsinterop.SynapseNavDrawerProps;
import org.sagebionetworks.web.client.place.Home;
import org.sagebionetworks.web.client.place.LoginPlace;
import org.sagebionetworks.web.client.widget.FullWidthAlert;
import org.sagebionetworks.web.client.widget.OrientationBanner;
import org.sagebionetworks.web.client.widget.ReactComponent;

public class HeaderViewImpl extends Composite implements HeaderView {

  public interface Binder extends UiBinder<Widget, HeaderViewImpl> {}

  @UiField
  Div header;

  @UiField
  Div donationBannerContainer;

  @UiField
  FullWidthAlert cookieNotificationAlert;

  @UiField
  FullWidthAlert nihNotificationAlert;

  @UiField
  Div portalAlert;

  @UiField
  Image portalLogo;

  @UiField
  Span portalName;

  @UiField
  FocusPanel portalLogoFocusPanel;

  @UiField
  ReactComponent synapseNavDrawerContainer;

  @UiField
  Alert stagingAlert;

  @UiField
  FocusPanel editModeNavBarClickBlocker;

  private Presenter presenter;
  String portalHref = "";
  SynapseReactClientFullContextPropsProvider propsProvider;
  PortalGinInjector ginInjector;

  @Inject
  public HeaderViewImpl(
    Binder binder,
    SynapseReactClientFullContextPropsProvider propsProvider,
    PortalGinInjector ginInjector,
    OrientationBanner donationBanner
  ) {
    this.initWidget(binder.createAndBindUi(this));
    this.ginInjector = ginInjector;
    this.propsProvider = propsProvider;
    cookieNotificationAlert.addPrimaryCTAClickHandler(event -> {
      presenter.onCookieNotificationDismissed();
    });
    nihNotificationAlert.setOnClose(() -> {
      presenter.onNIHNotificationDismissed();
    });

    donationBanner.configure(
      "Donate",
      "Support Open Science and Radical Collaboration with Sage Bionetworks",
      "Join us as we advance collaborative biomedical research tackling today's most pressing health challenges. Your contribution is crucial to breaking down barriers and accelerating the creation of transformative treatments and technologies. Thank you for being a part of this vital mission and helping us drive innovation forward.",
      "Donate to Sage",
      event -> {
        Window.open("https://sagebionetworks.org/donate", "_blank", "");
      },
      null,
      null
    );
    donationBannerContainer.add(donationBanner.asWidget());
    initClickHandlers();
    clear();
    rerenderNavBar();
  }

  @Override
  public void clear() {}

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
    GlobalApplicationState globalAppState =
      ginInjector.getGlobalApplicationState();
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

  @Override
  public void setNIHAlertVisible(boolean visible) {
    nihNotificationAlert.setVisible(visible);
    donationBannerContainer.setVisible(!visible);
  }
}
