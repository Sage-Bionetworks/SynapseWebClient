package org.sagebionetworks.web.client.widget.entity.menu.v2;

public interface ActionListener {
	/**
	 * Called when the users selects an action.
	 *
	 * @param action The selected action.
	 */
	void onAction(Action action);
}
