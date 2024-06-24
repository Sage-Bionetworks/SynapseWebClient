package org.sagebionetworks.web.client.view;

import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import org.sagebionetworks.web.client.context.SynapseReactClientFullContextPropsProvider;
import org.sagebionetworks.web.client.jsinterop.React;
import org.sagebionetworks.web.client.jsinterop.ReactNode;
import org.sagebionetworks.web.client.jsinterop.SRC;
import org.sagebionetworks.web.client.jsinterop.SynapseHomepageProps;
import org.sagebionetworks.web.client.widget.ReactComponent;
import org.sagebionetworks.web.client.widget.header.Header;

public class HomeViewImpl extends Composite implements HomeView {

  public interface HomeViewImplUiBinder
    extends UiBinder<Widget, HomeViewImpl> {}

  @UiField
  ReactComponent container;

  private static final String PROJECT_VIEW_ID = "syn23593547.3";

  private Header headerWidget;
  private SynapseReactClientFullContextPropsProvider propsProvider;

  @Inject
  public HomeViewImpl(
    HomeViewImplUiBinder binder,
    Header headerWidget,
    final SynapseReactClientFullContextPropsProvider propsProvider
  ) {
    initWidget(binder.createAndBindUi(this));

    this.headerWidget = headerWidget;
    this.propsProvider = propsProvider;

    headerWidget.configure();
  }

  @Override
  public void render() {
    scrollToTop();
    SynapseHomepageProps props = SynapseHomepageProps.create(PROJECT_VIEW_ID);
    ReactNode component = React.createElementWithSynapseContext(
      SRC.SynapseComponents.SynapseHomepage,
      props,
      propsProvider.getJsInteropContextProps()
    );
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
