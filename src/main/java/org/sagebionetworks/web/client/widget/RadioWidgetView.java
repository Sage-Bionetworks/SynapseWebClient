package org.sagebionetworks.web.client.widget;

import java.util.Iterator;

import com.google.gwt.user.client.ui.Widget;

public interface RadioWidgetView {
	public interface Presenter{
	}

	Widget asWidget();

	void add(Widget widget);

	void clear();

	Iterator<Widget> iterator();

	boolean remove(Widget widget);

}
