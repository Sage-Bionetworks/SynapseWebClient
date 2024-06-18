package org.sagebionetworks.web.client.view;

import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import org.sagebionetworks.web.client.context.SynapseReactClientFullContextPropsProvider;
import org.sagebionetworks.web.client.jsinterop.React;
import org.sagebionetworks.web.client.jsinterop.ReactNode;
import org.sagebionetworks.web.client.jsinterop.SRC;
import org.sagebionetworks.web.client.widget.ReactComponent;
import org.sagebionetworks.web.client.widget.header.Header;

public class DataAccessManagementViewImpl implements DataAccessManagementView {

  public interface DataAccessManagementViewImplUiBinder
    extends UiBinder<Widget, DataAccessManagementViewImpl> {}

  private SynapseReactClientFullContextPropsProvider propsProvider;
  private Header headerWidget;

  @UiField
  ReactComponent reactComponent;

  Widget widget;

  @Inject
  public DataAccessManagementViewImpl(
    DataAccessManagementViewImplUiBinder binder,
    Header headerWidget,
    SynapseReactClientFullContextPropsProvider propsProvider
  ) {
    widget = binder.createAndBindUi(this);
    this.headerWidget = headerWidget;
    this.propsProvider = propsProvider;
    headerWidget.configure();
  }

  @Override
  public void render() {
    headerWidget.configure();
    headerWidget.refresh();
    Window.scrollTo(0, 0);

    ReactNode node = React.createElementWithSynapseContext(
      SRC.SynapseComponents.ReviewerDashboard,
      null,
      propsProvider.getJsInteropContextProps()
    );
    reactComponent.render(node);
  }

  @Override
  public Widget asWidget() {
    return widget;
  }
}
