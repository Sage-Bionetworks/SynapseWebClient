package org.sagebionetworks.web.client.widget;

import java.util.Iterator;
import org.gwtbootstrap3.client.ui.Radio;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class RadioWidgetViewImpl implements RadioWidget {
	@UiField
	FocusPanel item;
	@UiField
	Radio radio;

	private Widget widget;

	public interface Binder extends UiBinder<Widget, RadioWidgetViewImpl> {
	}

	@Inject
	public RadioWidgetViewImpl(Binder binder) {
		widget = binder.createAndBindUi(this);
		item.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				radio.setValue(true);
			}
		});

	}

	@Override
	public void addClickHandler(ClickHandler handler) {
		radio.addClickHandler(handler);
		item.addClickHandler(handler);
	}

	@Override
	public Widget asWidget() {
		return widget;
	}

	@Override
	public void add(Widget widget) {
		item.setWidget(widget);
	}

	@Override
	public void clear() {
		item.clear();
	}

	@Override
	public Iterator<Widget> iterator() {
		return item.iterator();
	}

	@Override
	public boolean remove(Widget widget) {
		return item.remove(widget);
	}

	@Override
	public void setGroupName(String groupName) {
		radio.setName(groupName);
	}

}
