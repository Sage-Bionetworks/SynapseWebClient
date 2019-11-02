package org.sagebionetworks.web.client.widget.entity.menu.v2;

import org.gwtbootstrap3.client.ui.constants.IconType;
import com.google.gwt.user.client.ui.IsWidget;

/**
 * This widget view is composed of a toolbar that contains buttons and a "tools" drop-down menu.
 * Each button or menu is an ActionViews.
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
	 * Add a controller widget. These are often hidden modal widgets that need to be on the page.
	 * 
	 * @param controllerWidget
	 */
	void addControllerWidget(IsWidget controllerWidget);

	void setToolsButtonIcon(String text, IconType icon);

	void setACTDividerVisible(boolean visible);

	void setNoActionsAvailableVisible(boolean visible);

	void setPresenter(Presenter p);

	public interface Presenter {
	}
}
