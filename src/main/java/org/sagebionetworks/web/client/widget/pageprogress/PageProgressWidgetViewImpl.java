package org.sagebionetworks.web.client.widget.pageprogress;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import javax.inject.Inject;
import org.sagebionetworks.web.client.jsinterop.PageProgressProps;
import org.sagebionetworks.web.client.jsinterop.React;
import org.sagebionetworks.web.client.jsinterop.ReactElement;
import org.sagebionetworks.web.client.jsinterop.SRC;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.ReactComponent;

public class PageProgressWidgetViewImpl
  implements PageProgressWidgetView, IsWidget {

  public interface PageProgressWidgetViewImplUiBinder
    extends UiBinder<Widget, PageProgressWidgetViewImpl> {}

  @UiField
  ReactComponent srcContainer;

  Widget widget;
  boolean isConfigured = false;

  private final PageProgressWidgetViewImplUiBinder binder = GWT.create(
    PageProgressWidgetViewImplUiBinder.class
  );

  @Inject
  public PageProgressWidgetViewImpl() {
    widget = binder.createAndBindUi(this);
  }

  @Override
  public void configure(
    String barColor,
    int barPercent,
    String backBtnLabel,
    Callback backBtnCallback,
    String forwardBtnLabel,
    Callback forwardBtnCallback,
    boolean isForwardActive
  ) {
    PageProgressProps props = PageProgressProps.create(
      barColor,
      barPercent,
      backBtnLabel,
      () -> backBtnCallback.invoke(),
      forwardBtnLabel,
      () -> forwardBtnCallback.invoke(),
      isForwardActive
    );
    ReactElement component = React.createElementWithSynapseContext(
      SRC.SynapseComponents.PageProgress,
      props
    );
    srcContainer.render(component);
    isConfigured = true;
  }

  @Override
  public Widget asWidget() {
    return widget;
  }

  @Override
  public void clear() {}

  @Override
  public void setVisible(boolean visible) {
    widget.setVisible(visible);
  }
}
