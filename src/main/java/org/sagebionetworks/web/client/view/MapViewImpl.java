package org.sagebionetworks.web.client.view;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Widget;
import javax.inject.Inject;
import org.gwtbootstrap3.client.ui.html.Div;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.widget.header.Header;

public class MapViewImpl implements MapView {

  public interface MapViewImplUiBinder extends UiBinder<Widget, MapViewImpl> {}

  @UiField
  Div teamBadgeContainer;

  @UiField
  Div allUsersTitle;

  @UiField
  Div mapPanel;

  Widget widget;

  private Header headerWidget;

  private final MapViewImplUiBinder binder = GWT.create(
    MapViewImplUiBinder.class
  );

  @Inject
  public MapViewImpl(Header headerWidget, SynapseJSNIUtils synapseJSNIUtils) {
    widget = binder.createAndBindUi(this);
    this.headerWidget = headerWidget;
    headerWidget.configure();
  }

  @Override
  public void setPresenter(Presenter presenter) {
    headerWidget.configure();
    headerWidget.refresh();
    Window.scrollTo(0, 0); // scroll user to top of page
  }

  @Override
  public void setMap(Widget w) {
    mapPanel.clear();
    mapPanel.add(w);
  }

  @Override
  public Widget asWidget() {
    return widget;
  }

  @Override
  public int getClientHeight() {
    return Window.getClientHeight();
  }

  @Override
  public void setAllUsersTitleVisible(boolean visible) {
    allUsersTitle.setVisible(visible);
  }

  @Override
  public void setTeamBadge(Widget w) {
    teamBadgeContainer.clear();
    teamBadgeContainer.add(w);
  }

  @Override
  public void setTeamBadgeVisible(boolean visible) {
    teamBadgeContainer.setVisible(visible);
  }
}
