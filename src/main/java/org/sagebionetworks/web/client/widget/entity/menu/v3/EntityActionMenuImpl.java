package org.sagebionetworks.web.client.widget.entity.menu.v3;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import org.sagebionetworks.repo.model.EntityType;
import org.sagebionetworks.web.client.jsinterop.ReactMouseEvent;
import org.sagebionetworks.web.client.jsinterop.entity.actionmenu.ActionConfiguration;
import org.sagebionetworks.web.client.jsinterop.entity.actionmenu.EntityActionMenuDropdownConfiguration;
import org.sagebionetworks.web.client.jsinterop.entity.actionmenu.EntityActionMenuDropdownMap;
import org.sagebionetworks.web.client.jsinterop.entity.actionmenu.EntityActionMenuLayout;

public class EntityActionMenuImpl implements EntityActionMenu {

  private EntityActionMenuLayout currentLayout;
  private final EntityActionMenuDropdownMap dropdownMenuConfigurations;
  private final Map<Action, ActionConfiguration> actionConfigurations;

  private final EntityActionMenuView view;

  private Consumer<EntityActionMenuProps> propUpdateListener;
  private boolean isLoading;

  @Inject
  public EntityActionMenuImpl(EntityActionMenuView view) {
    this.view = view;
    actionConfigurations =
      DefaultActionConfigurationUtil.getDefaultActionConfiguration();
    dropdownMenuConfigurations =
      EntityActionMenuDropdownMap.create(
        EntityActionMenuDropdownConfiguration.create(true, "", false),
        EntityActionMenuDropdownConfiguration.create(true, "", false)
      );
    currentLayout =
      DefaultEntityActionMenuLayoutUtil.getLayout(EntityType.project);

    synchronizeView();
  }

  @Override
  public void setActionListener(Action action, ActionListener actionListener) {
    actionConfigurations.get(action).clearActionListeners();
    actionConfigurations.get(action).setHref(null);
    addActionListener(action, actionListener);
  }

  @Override
  public void addActionListener(Action action, ActionListener actionListener) {
    actionConfigurations.get(action).addActionListener(actionListener);
  }

  @Override
  public void setActionHref(Action action, String href) {
    actionConfigurations.get(action).clearActionListeners();
    addActionHref(action, href);
  }

  @Override
  public void addActionHref(Action action, String href) {
    actionConfigurations.get(action).setHref(href);
    synchronizeView();
  }

  @Override
  public void setActionVisible(Action action, boolean visible) {
    actionConfigurations.get(action).setVisible(visible);
    synchronizeView();
  }

  @Override
  public void setDownloadMenuEnabled(boolean enabled) {
    dropdownMenuConfigurations
      .getDownloadMenuConfiguration()
      .setDisabled(!enabled);
    synchronizeView();
  }

  @Override
  public void setActionEnabled(Action action, boolean enabled) {
    actionConfigurations.get(action).setDisabled(!enabled);
    synchronizeView();
  }

  public void setDownloadMenuTooltipText(String tooltipText) {
    dropdownMenuConfigurations
      .getDownloadMenuConfiguration()
      .setTooltipText(tooltipText);
    synchronizeView();
  }

  @Override
  public void setActionTooltipText(Action action, String tooltipText) {
    actionConfigurations.get(action).setTooltipText(tooltipText);
    synchronizeView();
  }

  @Override
  public void setActionText(Action action, String text) {
    actionConfigurations.get(action).setText(text);
    synchronizeView();
  }

  @Override
  public void setLayout(EntityActionMenuLayout layout) {
    this.currentLayout = layout;
    synchronizeView();
  }

  @Override
  public void reset() {
    // Hide all widgets and clear all Listeners
    hideAllActions();
    this.actionConfigurations.forEach((key, value) ->
        value.clearActionListeners()
      );
    synchronizeView();
  }

  @Override
  public void addControllerWidget(IsWidget controllerWidget) {
    this.view.addControllerWidget(controllerWidget);
  }

  @Override
  public void setIsLoading(boolean isLoading) {
    this.isLoading = isLoading;
    synchronizeView();
  }

  @Override
  public void hideAllActions() {
    this.actionConfigurations.forEach((key, value) -> value.setVisible(false));
    synchronizeView();
  }

  @Override
  public void setPropUpdateListener(Consumer<EntityActionMenuProps> listener) {
    propUpdateListener = listener;
  }

  @Override
  public EntityActionMenuProps getProps() {
    return new EntityActionMenuProps(
      this.actionConfigurations,
      dropdownMenuConfigurations,
      currentLayout
    );
  }

  private void synchronizeView() {
    if (propUpdateListener != null) {
      propUpdateListener.accept(getProps());
    }
    this.view.setIsLoading(this.isLoading);
    if (!this.isLoading) {
      this.view.configure(this.getProps());
    }
  }

  @Override
  public void onAction(Action action, ReactMouseEvent mouseEvent) {
    // forward to the listeners
    List<ActionListener> listeners = actionConfigurations
      .get(action)
      .getActionListeners();
    if (listeners.isEmpty()) {
      throw new IllegalArgumentException(
        "Attempted to invoke action " +
        action.name() +
        " with no listeners present"
      );
    }
    for (ActionListener listener : listeners) {
      listener.onAction(action, mouseEvent);
    }
  }

  @Override
  public Widget asWidget() {
    return this.view.asWidget();
  }
}
