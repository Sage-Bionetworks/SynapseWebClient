package org.sagebionetworks.web.client.view;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;

/**
 * A basic div view.  One use case is when a widget view is composed of other complex widgets that need to be attached to the dom.
 * @author jayhodgson
 */
public interface DivView extends IsWidget {
	void add(Widget child);
	void add(IsWidget child);
	void clear();
}
