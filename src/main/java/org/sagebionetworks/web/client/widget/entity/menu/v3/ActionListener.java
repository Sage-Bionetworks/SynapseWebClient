package org.sagebionetworks.web.client.widget.entity.menu.v3;

import org.sagebionetworks.web.client.jsinterop.ReactMouseEvent;

@FunctionalInterface
public interface ActionListener {
  /**
   * Called when a user invokes an action.
   *
   * @param action The selected action.
   * @param event The event that triggered the action. May be null.
   */
  void onAction(Action action, ReactMouseEvent event);
}
