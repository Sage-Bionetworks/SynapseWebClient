package org.sagebionetworks.web.client.widget;

import java.util.Iterator;

import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class RadioWidget implements IsWidget, HasWidgets, RadioWidgetView.Presenter{

	private RadioWidgetView view;

	@Inject
	public RadioWidget(
			RadioWidgetView view
			) {
		this.view = view;
	}

	@Override
	public Widget asWidget() {
		return view.asWidget();
	}

	@Override
	public void add(Widget widget) {
		view.add(widget);
	}

	@Override
	public void clear() {
		view.clear();
	}

	@Override
	public Iterator<Widget> iterator() {
		return view.iterator();
	}

	@Override
	public boolean remove(Widget widget) {
		return view.remove(widget);
	}

}
