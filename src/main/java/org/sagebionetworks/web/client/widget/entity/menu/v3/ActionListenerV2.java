package org.sagebionetworks.web.client.widget.entity.menu.v3;

import org.sagebionetworks.web.client.jsinterop.ReactMouseEvent;
import org.sagebionetworks.web.client.widget.entity.menu.v2.Action;

@FunctionalInterface
public interface ActionListenerV2 {
  /**
   * Called when a user invokes an action.
   *
   * @param action The selected action.
   * @param event The event that triggered the action. May be null.
   */
  void onAction(Action action, ReactMouseEvent event);
}
