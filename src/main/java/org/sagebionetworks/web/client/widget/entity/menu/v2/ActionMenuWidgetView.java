package org.sagebionetworks.web.client.widget.entity.menu.v2;

import com.google.gwt.user.client.ui.IsWidget;

/**
 * This widget view is composed of a toolbar that contains buttons and a "tools"
 * drop-down menu. Each button or menu is an ActionViews.
 * 
 * @author jhill
 * 
 */
public interface ActionMenuWidgetView extends IsWidget {

	/**
	 * Iterate over the ActionViews found in this view.
	 * 
	 * @return
	 */
	Iterable<ActionView> listActionViews();

	/**
	 * Add a controller widget. These are often hidden modal widgets that need
	 * to be on the page.
	 * 
	 * @param controllerWidget
	 */
	void addControllerWidget(IsWidget controllerWidget);

	/**
	 * Show/hide the basic divider.
	 * @param visible
	 */
	void setBasicDividerVisible(boolean visible);

	/**
	 * Show/hide the Tools button
	 * @param visible
	 */
	void setToolsButtonVisible(boolean visible);
}
