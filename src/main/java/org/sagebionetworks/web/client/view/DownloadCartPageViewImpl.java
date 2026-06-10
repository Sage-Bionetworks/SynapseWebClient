package org.sagebionetworks.web.client.view;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Widget;
import javax.inject.Inject;
import org.sagebionetworks.web.client.jsinterop.DownloadCartPageProps;
import org.sagebionetworks.web.client.jsinterop.React;
import org.sagebionetworks.web.client.jsinterop.ReactElement;
import org.sagebionetworks.web.client.jsinterop.SRC;
import org.sagebionetworks.web.client.widget.ReactComponent;
import org.sagebionetworks.web.client.widget.header.Header;

public class DownloadCartPageViewImpl implements DownloadCartPageView {

  ReactComponent container;

  private Header headerWidget;
  private Presenter presenter;

  @Inject
  public DownloadCartPageViewImpl(Header headerWidget) {
    container = new ReactComponent();
    this.headerWidget = headerWidget;
  }

  @Override
  public void setPresenter(Presenter presenter) {
    this.presenter = presenter;
  }

  @Override
  public void render() {
    Window.scrollTo(0, 0); // scroll user to top of page
    headerWidget.configure();
    DownloadCartPageProps props = DownloadCartPageProps.create(entityId -> {
      presenter.onViewSharingSettingsClicked(entityId);
    });
    ReactElement component = React.createElementWithSynapseContext(
      SRC.SynapseComponents.DownloadCartPage,
      props
    );
    container.render(component);
  }

  @Override
  public Widget asWidget() {
    return container.asWidget();
  }
}
