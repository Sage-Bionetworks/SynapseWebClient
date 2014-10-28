package org.sagebionetworks.web.client.widget.entity.menu.v2;

import com.google.gwt.user.client.ui.IsWidget;

/**
 * This widget view is composed of a toolbar that contains buttons and a "tools"
 * drop-down menu. ActionViews can either be added as buttons or tools.
 * 
 * @author jhill
 *
 */
public interface ActionMenuWidgetView extends IsWidget {

	/**
	 * Clear all actions from the view.
	 */
	public void clear();

	/**
	 * Add a button to the toolbar
	 * 
	 * @param actionView
	 */
	public void addButton(ActionView actionView);

	/**
	 * Add a menu item to the tools menu
	 * 
	 * @param actionView
	 */
	public void addMenuItem(ActionView actionView);
}
