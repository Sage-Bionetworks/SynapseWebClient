package org.sagebionetworks.web.client.widget.table;

import com.google.gwt.user.client.ui.IsWidget;

/**
 * Abstraction for a focus setter.
 * 
 * @author John
 *
 */
public interface FocusSetter {

	/**
	 * Attempt to set the focus on the passed widget.
	 * 
	 * @param widget If the widget is focusable, attempt to give the widget focus.
	 * @param shouldSelectAll If true, and the widget is a text widget, selectAll() will be called on
	 *        the widget after focus is set.
	 */
	public void attemptSetFocus(final IsWidget widget, boolean shouldSelectAll);

}
