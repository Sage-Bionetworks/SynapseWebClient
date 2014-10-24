package org.sagebionetworks.web.client.widget.entity.menu.v2;

import java.util.List;

import com.google.gwt.user.client.ui.IsWidget;

public interface ActionMenuWidget extends IsWidget{

	/**
	 * Configure this widget before using.
	 * @param actions The list of actions that should be shown
	 * @param listener Listener for action events.
	 */
	public void configure(List<Action> actions, ActionListener listener);
	
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
	 * Listen to action events.
	 */
	public interface ActionListener{
		/**
		 * Called when the users selects an action.
		 * @param action The selected action.
		 */
		public void onAction(Action action);
	}
}
