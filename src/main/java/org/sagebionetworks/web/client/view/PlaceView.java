package org.sagebionetworks.web.client.view;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;

public interface PlaceView extends IsWidget {
	void add(Widget w);
	void clearBody();
	void clearAboveBody();
	void clearBelowBody();
	void addAboveBody(Widget w);
	void addBelowBody(Widget w);
	void initHeaderAndFooter();
}
