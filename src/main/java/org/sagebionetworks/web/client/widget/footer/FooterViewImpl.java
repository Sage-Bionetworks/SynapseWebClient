package org.sagebionetworks.web.client.widget.footer;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.GWTWrapper;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.context.SynapseReactClientFullContextPropsProvider;
import org.sagebionetworks.web.client.jsinterop.React;
import org.sagebionetworks.web.client.jsinterop.ReactNode;
import org.sagebionetworks.web.client.jsinterop.SRC;
import org.sagebionetworks.web.client.jsinterop.SynapseFooterProps;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.ReactComponent;

public class FooterViewImpl implements FooterView, IsWidget {

  SimplePanel wrapper = new SimplePanel();
  ReactComponent container = new ReactComponent();
  FooterView.Presenter presenter;

  String portalVersion, repoVersion, srcVersion;
  PortalGinInjector ginInjector;
  SynapseReactClientFullContextPropsProvider propsProvider;

  @Inject
  public FooterViewImpl(
    PortalGinInjector ginInjector,
    GWTWrapper gwt,
    SynapseReactClientFullContextPropsProvider propsProvider
  ) {
    this.ginInjector = ginInjector;
    this.propsProvider = propsProvider;
    // defer constructing this view (to give a chance for other page components to load first)
    Callback constructViewCallback = () -> {
      wrapper.add(container);
      refresh();
    };
    gwt.scheduleExecution(constructViewCallback, 2000);
  }

  @Override
  public Widget asWidget() {
    return wrapper;
  }

  @Override
  public void setPresenter(Presenter presenter) {
    this.presenter = presenter;
  }

  @Override
  public void setVersion(
    String portalVersion,
    String repoVersion,
    String srcVersion
  ) {
    if (portalVersion == null) portalVersion = "--";
    if (repoVersion == null) repoVersion = "--";
    if (srcVersion == null) srcVersion = "--";
    this.portalVersion = portalVersion;
    this.repoVersion = repoVersion;
    this.srcVersion = srcVersion;

    refresh();
  }

  @Override
  public void open(String url) {
    DisplayUtils.newWindow(url, "_blank", "");
  }

  @Override
  public void refresh() {
    if (container.isAttached()) {
      SynapseFooterProps props = SynapseFooterProps.create(
        portalVersion,
        srcVersion,
        repoVersion,
        href -> {
          GlobalApplicationState globalAppState =
            ginInjector.getGlobalApplicationState();
          globalAppState.handleRelativePathClick(href);
        },
        newMode -> {
          GlobalApplicationState globalAppState =
            ginInjector.getGlobalApplicationState();
          globalAppState.refreshPage();
        }
      );
      ReactNode component = React.createElementWithSynapseContext(
        SRC.SynapseComponents.SynapseFooter,
        props,
        propsProvider.getJsInteropContextProps()
      );
      container.render(component);
    }
  }
}
