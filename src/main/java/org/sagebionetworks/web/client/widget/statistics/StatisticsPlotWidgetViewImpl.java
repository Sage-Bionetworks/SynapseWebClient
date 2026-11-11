package org.sagebionetworks.web.client.widget.statistics;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import javax.inject.Inject;
import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.Modal;
import org.sagebionetworks.web.client.jsinterop.React;
import org.sagebionetworks.web.client.jsinterop.SRC;
import org.sagebionetworks.web.client.jsinterop.StatisticsPlotProps;
import org.sagebionetworks.web.client.widget.ReactComponent;

public class StatisticsPlotWidgetViewImpl
  implements StatisticsPlotWidgetView, IsWidget {

  public static final String ROOT_PORTAL_URL =
    Window.Location.getProtocol() + "//" + Window.Location.getHost() + "/";
  public static final String GOOGLE_OAUTH_CALLBACK_URL =
    ROOT_PORTAL_URL + "Portal/oauth2callback?oauth2provider=GOOGLE_OAUTH_2_0";
  public static final String GOOGLE_OAUTH_WITH_STATE_CALLBACK_URL =
    GOOGLE_OAUTH_CALLBACK_URL + "&state=";

  public interface StatisticsPlotWidgetViewImplUiBinder
    extends UiBinder<Widget, StatisticsPlotWidgetViewImpl> {}

  @UiField
  ReactComponent srcContainer;

  @UiField
  Button closeButton;

  @UiField
  Modal statsPlotModal;

  Widget widget;

  private final StatisticsPlotWidgetViewImplUiBinder binder = GWT.create(
    StatisticsPlotWidgetViewImplUiBinder.class
  );

  @Inject
  public StatisticsPlotWidgetViewImpl() {
    widget = binder.createAndBindUi(this);
    closeButton.addClickHandler(event -> {
      statsPlotModal.hide();
    });
  }

  @Override
  public void configureAndShow(String projectId) {
    srcContainer.render(
      React.createElementWithSynapseContext(
        SRC.SynapseComponents.StatisticsPlot,
        StatisticsPlotProps.create(projectId, "Project Statistics")
      )
    );
    statsPlotModal.show();
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
