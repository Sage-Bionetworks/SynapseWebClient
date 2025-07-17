package org.sagebionetworks.web.client.view;

import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.jsinterop.React;
import org.sagebionetworks.web.client.jsinterop.ReactElement;
import org.sagebionetworks.web.client.jsinterop.SRC;
import org.sagebionetworks.web.client.jsinterop.SynapseHomepageV2Props;
import org.sagebionetworks.web.client.widget.ReactComponent;
import org.sagebionetworks.web.client.widget.header.Header;

public class HomeViewImpl extends Composite implements HomeView {

  public interface HomeViewImplUiBinder
    extends UiBinder<Widget, HomeViewImpl> {}

  @UiField
  ReactComponent container;

  private Header headerWidget;
  private GlobalApplicationState globalAppState;

  @Inject
  public HomeViewImpl(
    HomeViewImplUiBinder binder,
    Header headerWidget,
    GlobalApplicationState globalAppState
  ) {
    initWidget(binder.createAndBindUi(this));

    this.headerWidget = headerWidget;
    this.globalAppState = globalAppState;
    headerWidget.configure();
  }

  @Override
  public void render() {
    scrollToTop();
    ReactElement component;

    SynapseHomepageV2Props props = SynapseHomepageV2Props.create(href -> {
      globalAppState.handleRelativePathClick(href);
    });
    component =
      React.createElementWithSynapseContext(
        SRC.SynapseComponents.SynapseHomepageV2,
        props
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
