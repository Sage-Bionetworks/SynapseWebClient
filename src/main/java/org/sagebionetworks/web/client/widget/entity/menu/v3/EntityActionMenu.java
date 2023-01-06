package org.sagebionetworks.web.client.widget.entity.menu.v3;

import com.google.gwt.user.client.ui.IsWidget;
import java.util.function.Consumer;
import org.sagebionetworks.web.client.jsinterop.entity.actionmenu.EntityActionMenuLayout;

public interface EntityActionMenu extends IsWidget, ActionListener {
  /**
   * Toggle the visibility of a particular action.
   */
  void setActionVisible(Action action, boolean visible);

  /**
   * Replace all listeners and href of the action with the new listener.
   */
  void setActionListener(Action action, ActionListener actionListener);

  /**
   * Add a new listener to the action. Does not remove existing listeners or href.
   */
  void addActionListener(Action action, ActionListener actionListener);

  /**
   * Replace all listeners and href of the action with the new href.
   */
  void setActionHref(Action action, String href);

  /**
   * Sets the href of the action without removing existing listeners.
   */
  void addActionHref(Action action, String href);

  /**
   * Controls whether the download menu should be enabled or not. Does not affect visibility.
   */
  void setDownloadMenuEnabled(boolean enabled);

  /**
   * Controls whether a particular action is enabled or not. Does not affect visibility.
   */
  void setActionEnabled(Action action, boolean enabled);

  /**
   * Set the tooltip text shown on the download menu.
   * @param tooltipText the text to show. Empty string or null will remove the tooltip.
   */
  void setDownloadMenuTooltipText(String tooltipText);

  /**
   * Set the tooltip text shown on the action.
   * @param tooltipText the text to show. Empty string or null will remove the tooltip.
   */
  void setActionTooltipText(Action action, String tooltipText);

  void setActionText(Action action, String text);

  /**
   * The layout configuration dictates which dropdown menus and menu groups each action is grouped in.
   * Actions that are visible but not specified in the layout will be shown in the primary menu.
   */
  void setLayout(EntityActionMenuLayout layout);

  void hideAllActions();

  /**
   * Reset this action menu. This will clear all listeners and hide all action.
   */
  void reset();

  /**
   * Add a controller widget. These are often hidden modal widgets that need to be on the page.
   */
  void addControllerWidget(IsWidget controllerWidget);

  void setIsLoading(boolean isLoading);

  EntityActionMenuProps getProps();

  void setPropUpdateListener(Consumer<EntityActionMenuProps> listener);
}
