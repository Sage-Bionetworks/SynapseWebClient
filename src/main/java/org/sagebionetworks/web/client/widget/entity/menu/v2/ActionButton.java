package org.sagebionetworks.web.client.widget.entity.menu.v2;

import org.gwtbootstrap3.client.ui.Button;
import org.sagebionetworks.web.client.widget.entity.menu.v2.ActionMenuWidget.ActionListener;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;

public class ActionButton extends Button implements ActionView {

	Action action;

	@Override
	public void setAction(Action action) {
		this.action = action;
	}

	@Override
	public Action getAction() {
		return this.action;
	}

	@Override
	public void addActionListener(final ActionListener listener) {
		this.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				listener.onAction(action);
			}
		});
	}
}
