package org.sagebionetworks.web.client.widget.entity.tabs;

import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import org.gwtbootstrap3.client.ui.TabPane;
import org.sagebionetworks.repo.model.entitybundle.v2.EntityBundle;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.GWTWrapper;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.PopupUtilsView;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.place.Synapse;
import org.sagebionetworks.web.client.place.Synapse.EntityArea;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.entity.controller.EntityActionController;
import org.sagebionetworks.web.client.widget.entity.file.AddToDownloadListV2;
import org.sagebionetworks.web.client.widget.entity.menu.v3.EntityActionMenu;

public class Tab implements TabView.Presenter {

  TabView view;
  GlobalApplicationState globalAppState;
  SynapseJSNIUtils synapseJSNIUtils;

  Synapse place;
  String entityName;
  List<CallbackP<Tab>> onClickCallbacks;
  boolean isContentStale;
  GWTWrapper gwt;
  Callback deferredShowTabCallback;
  boolean pushState;

  EntityActionController entityActionController;
  EntityActionMenu entityActionMenu;
  PopupUtilsView popupUtils;
  EntityArea area;
  String tabTitle;

  @Inject
  public Tab(
    TabView view,
    GlobalApplicationState globalAppState,
    SynapseJSNIUtils synapseJSNIUtils,
    GWTWrapper gwt,
    EntityActionController entityActionController,
    EntityActionMenu entityActionMenu,
    PopupUtilsView popupUtils
  ) {
    this.view = view;
    this.globalAppState = globalAppState;
    this.synapseJSNIUtils = synapseJSNIUtils;
    this.gwt = gwt;
    view.setPresenter(this);
    this.entityActionController = entityActionController;
    this.entityActionMenu = entityActionMenu;
    entityActionMenu.addControllerWidget(entityActionController.asWidget());
    this.popupUtils = popupUtils;
    deferredShowTabCallback =
      new Callback() {
        @Override
        public void invoke() {
          showTab(pushState);
        }
      };
  }

  public void configure(
    String tabTitle,
    String iconName,
    String helpMarkdown,
    String helpLink,
    EntityArea area
  ) {
    this.tabTitle = tabTitle;
    view.configure(tabTitle, iconName, helpMarkdown, helpLink);
    onClickCallbacks = new ArrayList<CallbackP<Tab>>();
    this.area = area;
  }

  public void configureOrientationBanner(
    String name,
    String title,
    String text,
    String primaryButtonText,
    ClickHandler primaryButtonClickHandler,
    String secondaryButtonText,
    String secondaryButtonHref
  ) {
    view.configureOrientationBanner(
      name,
      title,
      text,
      primaryButtonText,
      primaryButtonClickHandler,
      secondaryButtonText,
      secondaryButtonHref
    );
  }

  public void setContent(Widget widget) {
    view.setContent(widget);
  }

  public Widget getTabListItem() {
    return view.getTabListItem();
  }

  public void addTabListItemStyle(String style) {
    view.addTabListItemStyle(style);
  }

  public void setTabListItemVisible(boolean visible) {
    view.setTabListItemVisible(visible);
  }

  public boolean isTabListItemVisible() {
    return view.isTabListItemVisible();
  }

  public TabPane getTabPane() {
    return view.getTabPane();
  }

  public boolean isTabPaneVisible() {
    return getTabPane().isVisible();
  }

  public void setEntityNameAndPlace(String entityName, Synapse place) {
    this.place = place;
    this.entityName = entityName;
    updatePageTitle();
    view.updateHref(place);
  }

  public void showTab() {
    showTab(true);
  }

  public void showTab(boolean pushState) {
    this.pushState = pushState;
    if (place == null) {
      // try again later
      gwt.scheduleExecution(deferredShowTabCallback, 200);
      return;
    }
    if (pushState) {
      globalAppState.pushCurrentPlace(place);
    } else {
      globalAppState.replaceCurrentPlace(place);
    }

    view.setActive(true);
    updatePageTitle();
  }

  public void updatePageTitle() {
    if (view.isActive()) {
      if (entityName != null) {
        String entityId = "";
        if (place != null) {
          entityId = " - " + place.getEntityId();
        }
        synapseJSNIUtils.setPageTitle(entityName + entityId + " - " + tabTitle);
      }
    }
  }

  public void hideTab() {
    view.setActive(false);
  }

  public void addTabClickedCallback(CallbackP<Tab> onClickCallback) {
    onClickCallbacks.add(0, onClickCallback);
  }

  @Override
  public void onTabClicked() {
    if (globalAppState.isEditing()) {
      Callback yesCallback = () -> {
        globalAppState.setIsEditing(false);
        postOnTabClicked();
      };
      popupUtils.showConfirmDialog(
        "",
        DisplayConstants.NAVIGATE_AWAY_CONFIRMATION_MESSAGE,
        yesCallback
      );
    } else {
      postOnTabClicked();
    }
  }

  private void postOnTabClicked() {
    for (CallbackP<Tab> callbackP : onClickCallbacks) {
      callbackP.invoke(this);
    }
  }

  public boolean isContentStale() {
    return isContentStale;
  }

  public void setContentStale(boolean isContentStale) {
    this.isContentStale = isContentStale;
  }

  public EntityActionMenu getEntityActionMenu() {
    return entityActionMenu;
  }

  public void configureEntityActionController(
    EntityBundle bundle,
    boolean isCurrentVersion,
    String wikiPageKey,
    AddToDownloadListV2 addToDownloadListWidget
  ) {
    entityActionController.configure(
      entityActionMenu,
      bundle,
      isCurrentVersion,
      wikiPageKey,
      area,
      addToDownloadListWidget
    );
  }

  /**
   * For testing purposes only
   *
   * @param entityName
   */
  public void setEntityName(String entityName) {
    this.entityName = entityName;
  }
}
