package org.sagebionetworks.web.client.view;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Widget;
import javax.inject.Inject;
import org.gwtbootstrap3.client.ui.html.Div;
import org.sagebionetworks.web.client.widget.header.Header;

public class SynapseForumViewImpl implements SynapseForumView {

  @UiField
  Div forumWidgetContainer;

  Widget widget;

  public interface SynapseForumViewImplUiBinder
    extends UiBinder<Widget, SynapseForumViewImpl> {}

  private final SynapseForumViewImplUiBinder binder = GWT.create(
    SynapseForumViewImplUiBinder.class
  );

  @Inject
  public SynapseForumViewImpl(Header headerWidget) {
    widget = binder.createAndBindUi(this);
    headerWidget.configure();
    headerWidget.refresh();
    Window.scrollTo(0, 0); // scroll user to top of page
  }

  @Override
  public Widget asWidget() {
    return widget;
  }

  @Override
  public void setForumWidget(Widget widget) {
    forumWidgetContainer.add(widget);
  }
}
