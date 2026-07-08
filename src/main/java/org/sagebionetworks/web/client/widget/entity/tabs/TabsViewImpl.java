package org.sagebionetworks.web.client.widget.entity.tabs;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;
import javax.inject.Inject;
import org.gwtbootstrap3.client.ui.NavTabs;
import org.gwtbootstrap3.client.ui.TabContent;
import org.sagebionetworks.web.client.jsinterop.EntitySidebarProps;
import org.sagebionetworks.web.client.jsinterop.React;
import org.sagebionetworks.web.client.jsinterop.ReactElement;
import org.sagebionetworks.web.client.jsinterop.SRC;
import org.sagebionetworks.web.client.widget.ReactComponent;

public class TabsViewImpl implements TabsView {

  @UiField
  NavTabs navTabs;

  @UiField
  TabContent tabContent;

  @UiField
  ReactComponent entitySidebar;

  public interface TabsViewImplUiBinder
    extends UiBinder<Widget, TabsViewImpl> {}

  Widget widget;

  @Inject
  public TabsViewImpl() {
    // empty constructor, you can include this widget in the ui xml
    TabsViewImplUiBinder binder = GWT.create(TabsViewImplUiBinder.class);
    widget = binder.createAndBindUi(this);
  }

  @Override
  public void addTab(Tab tab) {
    navTabs.add(tab.getTabListItem());
    tabContent.add(tab.getTabPane());
  }

  @Override
  public void clear() {
    navTabs.clear();
    tabContent.clear();
  }

  @Override
  public Widget asWidget() {
    return widget;
  }

  @Override
  public void setNavTabsVisible(boolean visible) {
    navTabs.setVisible(visible);
  }

  @Override
  public void setEntitySidebar(String entityId, Double versionNumber) {
    EntitySidebarProps props = EntitySidebarProps.create(
      entityId,
      versionNumber
    );
    ReactElement component = React.createElementWithSynapseContext(
      SRC.SynapseComponents.EntitySidebar,
      props
    );
    entitySidebar.render(component);
  }
}
