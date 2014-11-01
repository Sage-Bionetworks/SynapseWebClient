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
	 * @return
	 */
	Iterable<ActionView> listActionViews();
}
