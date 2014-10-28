package org.sagebionetworks.web.client.widget.entity.menu.v2;

import org.gwtbootstrap3.client.ui.constants.IconType;

import com.google.gwt.user.client.ui.IsWidget;

public interface ActionMenuWidget extends IsWidget{

	/**
	 * Reset this action menu.
	 */
	public void reset();
	
	/**
	 * Enable/disable an action in the menu.
	 * @param action
	 * @param enabled
	 */
	public void setActionEnabled(Action action, boolean enabled);
	
	/**
	 * Show or hide an action.
	 * @param action
	 * @param visible
	 */
	public void setActionVisible(Action action, boolean visible);
	
	/**
	 * The text shown for this action.
	 * @param action
	 * @param text
	 */
	public void setActionText(Action action, String text);
	
	/**
	 * Set the icon for this action.
	 * @param action
	 * @param icon
	 */
	public void setActionIcon(Action action, IconType icon);
	
	/**
	 * Add a new listener to an action.
	 * 
	 * All listeners are cleared each time the widget is configured.
	 * @param action
	 * @param listner
	 */
	public void addActionListener(Action action, ActionListener listner);
	
	/**
	 * Listen to action events.
	 */
	public interface ActionListener{
		/**
		 * Called when the users selects an action.
		 * @param action The selected action.
		 */
		void onAction(Action action);

	}
}
