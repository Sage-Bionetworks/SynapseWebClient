package org.sagebionetworks.web.client.widget.entity.menu.v2;

import org.gwtbootstrap3.client.ui.base.HasIcon;
import org.sagebionetworks.web.client.widget.entity.menu.v2.ActionMenuWidget.ActionListener;
import com.google.gwt.user.client.ui.HasEnabled;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.HasVisibility;
import com.google.gwt.user.client.ui.IsWidget;

/**
 * Abstraction for an individual action.
 * 
 * @author jhill
 *
 */
public interface ActionView extends IsWidget, HasText, HasIcon, HasEnabled, HasVisibility {

	/**
	 * Bind an action to this view.
	 * 
	 * @param action
	 */
	void setAction(Action action);

	/**
	 * Get the action bound to this view.
	 * 
	 * @return
	 */
	Action getAction();

	/**
	 * Add an action listener to this view.
	 * 
	 * @param listner
	 */
	void addActionListener(ActionListener listener);

}
