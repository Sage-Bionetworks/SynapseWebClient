package org.sagebionetworks.web.client.widget.entity.menu.v2;

import org.gwtbootstrap3.client.ui.constants.IconType;
import com.google.gwt.user.client.ui.IsWidget;

/**
 * Abstraction for an ActionMenu widget that knows nothing about the actions it maintains. This
 * widget can be used to listen to action events and maintain action state.
 * 
 * @author John
 *
 */
public interface ActionMenuWidget extends IsWidget {

	public void hideAllActions();

	/**
	 * Reset this action menu. This will clear all listeners and hide all action.
	 */
	public void reset();

	/**
	 * Show or hide an action.
	 * 
	 * @param action
	 * @param visible
	 */
	public void setActionVisible(Action action, boolean visible);

	/**
	 * The text shown for this action.
	 * 
	 * @param action
	 * @param text
	 */
	public void setActionText(Action action, String text);

	/**
	 * Set the icon for this action.
	 * 
	 * @param action
	 * @param icon
	 */
	public void setActionIcon(Action action, IconType icon);

	public void setToolsButtonIcon(String text, IconType icon);

	/**
	 * Add a new listener to an action.
	 * 
	 * All listeners are cleared each time the widget is configured.
	 * 
	 * @param action
	 * @param listner
	 */
	public void addActionListener(Action action, ActionListener listner);

	/**
	 * Set the listener for an action.
	 * 
	 * All listeners are cleared each time the widget is configured.
	 * 
	 * @param action
	 * @param listner
	 */
	public void setActionListener(Action action, ActionListener listner);

	/**
	 * Listen to action events.
	 */
	public interface ActionListener {
		/**
		 * Called when the users selects an action.
		 * 
		 * @param action The selected action.
		 */
		void onAction(Action action);

	}

	/**
	 * Add a controller widget. These are often hidden modal widgets that need to be on the page.
	 * 
	 * @param controllerWidget
	 */
	public void addControllerWidget(IsWidget controllerWidget);

	void setACTDividerVisible(boolean visible);

}
