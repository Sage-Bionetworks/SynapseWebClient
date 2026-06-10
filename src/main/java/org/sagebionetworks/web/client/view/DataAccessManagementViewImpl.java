package org.sagebionetworks.web.client.view;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Widget;
import javax.inject.Inject;
import org.sagebionetworks.web.client.jsinterop.React;
import org.sagebionetworks.web.client.jsinterop.ReactElement;
import org.sagebionetworks.web.client.jsinterop.ReviewerDashboardProps;
import org.sagebionetworks.web.client.jsinterop.SRC;
import org.sagebionetworks.web.client.widget.ReactComponent;
import org.sagebionetworks.web.client.widget.header.Header;

public class DataAccessManagementViewImpl implements DataAccessManagementView {

  public interface DataAccessManagementViewImplUiBinder
    extends UiBinder<Widget, DataAccessManagementViewImpl> {}

  private Header headerWidget;

  @UiField
  ReactComponent reactComponent;

  Widget widget;

  private final DataAccessManagementViewImplUiBinder binder = GWT.create(
    DataAccessManagementViewImplUiBinder.class
  );

  @Inject
  public DataAccessManagementViewImpl(Header headerWidget) {
    widget = binder.createAndBindUi(this);
    this.headerWidget = headerWidget;
    headerWidget.configure();
  }

  @Override
  public void render() {
    headerWidget.configure();
    headerWidget.refresh();
    Window.scrollTo(0, 0);

    ReviewerDashboardProps props = ReviewerDashboardProps.create(
      "/DataAccessManagement:default"
    );

    ReactElement node = React.createElementWithSynapseContext(
      SRC.SynapseComponents.ReviewerDashboard,
      props
    );
    reactComponent.render(node);
  }

  @Override
  public Widget asWidget() {
    return widget;
  }
}
