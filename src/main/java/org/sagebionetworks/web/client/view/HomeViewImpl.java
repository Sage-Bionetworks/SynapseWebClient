package org.sagebionetworks.web.client.view;

import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.FeatureFlagConfig;
import org.sagebionetworks.web.client.FeatureFlagKey;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.context.SynapseReactClientFullContextPropsProvider;
import org.sagebionetworks.web.client.cookie.CookieProvider;
import org.sagebionetworks.web.client.jsinterop.React;
import org.sagebionetworks.web.client.jsinterop.ReactNode;
import org.sagebionetworks.web.client.jsinterop.SRC;
import org.sagebionetworks.web.client.jsinterop.SynapseHomepageProps;
import org.sagebionetworks.web.client.jsinterop.SynapseHomepageV2Props;
import org.sagebionetworks.web.client.widget.ReactComponent;
import org.sagebionetworks.web.client.widget.header.Header;

public class HomeViewImpl extends Composite implements HomeView {

  public interface HomeViewImplUiBinder
    extends UiBinder<Widget, HomeViewImpl> {}

  @UiField
  ReactComponent container;

  private Header headerWidget;
  private SynapseReactClientFullContextPropsProvider propsProvider;
  private GlobalApplicationState globalAppState;
  private FeatureFlagConfig featureFlagConfig;
  private static final String PROJECT_VIEW_ID = "syn23593547.3";

  @Inject
  public HomeViewImpl(
    HomeViewImplUiBinder binder,
    Header headerWidget,
    final SynapseReactClientFullContextPropsProvider propsProvider,
    GlobalApplicationState globalAppState,
    FeatureFlagConfig featureFlagConfig
  ) {
    initWidget(binder.createAndBindUi(this));

    this.headerWidget = headerWidget;
    this.propsProvider = propsProvider;
    this.globalAppState = globalAppState;
    this.featureFlagConfig = featureFlagConfig;
    headerWidget.configure();
  }

  @Override
  public void render() {
    scrollToTop();
    ReactNode component;

    if (
      featureFlagConfig.isFeatureEnabled(FeatureFlagKey.HOMEPAGE_V2.getKey())
    ) {
      SynapseHomepageV2Props props = SynapseHomepageV2Props.create(href -> {
        globalAppState.handleRelativePathClick(href);
      });
      component =
        React.createElementWithSynapseContext(
          SRC.SynapseComponents.SynapseHomepageV2,
          props,
          propsProvider.getJsInteropContextProps()
        );
    } else {
      //TODO: SWC-6999: Once V2 is released, delete this conditional
      SynapseHomepageProps props = SynapseHomepageProps.create(PROJECT_VIEW_ID);
      component =
        React.createElementWithSynapseContext(
          SRC.SynapseComponents.SynapseHomepage,
          props,
          propsProvider.getJsInteropContextProps()
        );
    }

    container.render(component);
  }

  @Override
  public void refresh() {
    headerWidget.configure();
    headerWidget.refresh();
  }

  @Override
  public void scrollToTop() {
    Window.scrollTo(0, 0);
  }
}
