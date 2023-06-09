package org.sagebionetworks.web.client.widget.entity.tabs;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.InlineHTML;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import org.gwtbootstrap3.client.ui.Anchor;
import org.gwtbootstrap3.client.ui.ListItem;
import org.gwtbootstrap3.client.ui.TabPane;
import org.gwtbootstrap3.client.ui.constants.Placement;
import org.gwtbootstrap3.client.ui.html.Div;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.place.Synapse;
import org.sagebionetworks.web.client.widget.ClickableDiv;
import org.sagebionetworks.web.client.widget.HelpWidget;
import org.sagebionetworks.web.client.widget.IconSvg;
import org.sagebionetworks.web.client.widget.OrientationBanner;

public class TabViewImpl implements TabView {

  @UiField
  Anchor tabItem;

  @UiField
  TabPane tabPane;

  HelpWidget helpWidget;

  @UiField
  ListItem tabListItem;

  @UiField
  SimplePanel orientationBannerPanel;

  @UiField
  Div contentDiv;

  boolean isActive = false;

  public interface TabViewImplUiBinder extends UiBinder<Widget, TabViewImpl> {}

  Presenter presenter;
  Widget widget;
  ClickHandler tabClickedHandler;
  Anchor anchor;
  OrientationBanner orientationBanner;

  @Inject
  public TabViewImpl(
    HelpWidget helpWidget,
    OrientationBanner orientationBanner
  ) {
    // empty constructor, you can include this widget in the ui xml
    TabViewImplUiBinder binder = GWT.create(TabViewImplUiBinder.class);
    widget = binder.createAndBindUi(this);
    this.helpWidget = helpWidget;
    this.orientationBanner = orientationBanner;
    tabClickedHandler =
      new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
          if (!DisplayUtils.isAnyModifierKeyDown(event)) {
            event.preventDefault();
            presenter.onTabClicked();
          }
        }
      };
  }

  @Override
  public void setPresenter(Presenter presenter) {
    this.presenter = presenter;
  }

  @Override
  public void configureOrientationBanner(
    String name,
    String title,
    String text,
    String primaryButtonText,
    ClickHandler primaryButtonClickHandler,
    String secondaryButtonText,
    String secondaryButtonHref
  ) {
    orientationBannerPanel.clear();
    orientationBanner.configure(
      name,
      title,
      text,
      primaryButtonText,
      primaryButtonClickHandler,
      secondaryButtonText,
      secondaryButtonHref
    );
    orientationBannerPanel.setWidget(orientationBanner.asWidget());
  }

  @Override
  public void setContent(Widget content) {
    contentDiv.clear();
    contentDiv.add(content);
  }

  @Override
  public void configure(
    String tabTitle,
    String iconName,
    String helpMarkdown,
    String helpLink
  ) {
    helpWidget.setHelpMarkdown(helpMarkdown);
    helpWidget.setHref(helpLink);
    helpWidget.setPlacement(Placement.BOTTOM);
    tabItem.clear();
    anchor = new Anchor();
    anchor.add(new InlineHTML(tabTitle));
    anchor.addStyleName("textDecorationNone");

    ClickableDiv fp = new ClickableDiv();
    fp.addClickHandler(tabClickedHandler);
    fp.addStyleName("margin-right-5 displayInline");
    IconSvg icon = new IconSvg();
    icon.configure(iconName, null);
    fp.add(icon);
    fp.add(anchor);
    tabItem.add(fp);
    tabItem.add(helpWidget.asWidget());
  }

  @Override
  public void updateHref(Synapse place) {
    anchor.setHref("#!Synapse:" + place.toToken());
  }

  @Override
  public Widget getTabListItem() {
    return tabListItem;
  }

  @Override
  public void addTabListItemStyle(String style) {
    tabListItem.addStyleName(style);
  }

  @Override
  public void setTabListItemVisible(boolean visible) {
    tabListItem.setVisible(visible);
  }

  @Override
  public boolean isTabListItemVisible() {
    return tabListItem.isVisible();
  }

  @Override
  public TabPane getTabPane() {
    return tabPane;
  }

  @Override
  public void setActive(boolean isActive) {
    this.isActive = isActive;
    if (isActive) {
      tabListItem.addStyleName("active");
    } else {
      tabListItem.removeStyleName("active");
    }

    tabPane.setVisible(isActive);
  }

  @Override
  public boolean isActive() {
    return isActive;
  }

  @Override
  public Widget asWidget() {
    return widget;
  }
}
