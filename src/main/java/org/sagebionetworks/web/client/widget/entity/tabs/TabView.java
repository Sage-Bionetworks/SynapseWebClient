package org.sagebionetworks.web.client.widget.entity.tabs;

import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import org.gwtbootstrap3.client.ui.TabPane;
import org.sagebionetworks.web.client.place.Synapse;

public interface TabView extends IsWidget {
  void setPresenter(Presenter presenter);

  void updateHref(Synapse place);

  void configure(
    String tabTitle,
    String iconName,
    String helpMarkdown,
    String helpLink
  );

  void configureOrientationBanner(
    String name,
    String title,
    String text,
    String primaryButtonText,
    ClickHandler primaryButtonClickHandler,
    String secondaryButtonText,
    String secondaryButtonHref
  );

  void setContent(Widget content);

  Widget getTabListItem();

  void setTabListItemVisible(boolean visible);

  boolean isTabListItemVisible();

  void addTabListItemStyle(String style);

  TabPane getTabPane();

  void setActive(boolean isActive);

  boolean isActive();

  public interface Presenter {
    void onTabClicked();
  }
}
