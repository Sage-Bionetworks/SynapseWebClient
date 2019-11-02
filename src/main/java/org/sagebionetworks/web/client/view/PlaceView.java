package org.sagebionetworks.web.client.view;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;

public interface PlaceView extends IsWidget {
	void add(Widget w);

	void clearBody();

	void clearAboveBody();

	void clearBelowBody();

	void addAboveBody(IsWidget w);

	void addBelowBody(IsWidget w);

	void initHeaderAndFooter();

	void addTitle(String text);

	void addTitle(Widget w);
}
