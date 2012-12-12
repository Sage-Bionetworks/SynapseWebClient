package org.sagebionetworks.web.client.utils;

import com.extjs.gxt.ui.client.Style.Direction;
import com.extjs.gxt.ui.client.fx.FxConfig;
import com.extjs.gxt.ui.client.widget.Component;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.shared.HandlerRegistration;

public class AnimationProtectorViewImpl implements AnimationProtectorView {

	private HasClickHandlers trigger = null;
	private Component container = null;

	public AnimationProtectorViewImpl(HasClickHandlers trigger, Component container) {
		this.trigger = trigger;
		this.container = container;
	}

	@Override
	public HandlerRegistration addClickHandler(ClickHandler handler) {
		return trigger.addClickHandler(handler);
	}

	@Override
	public boolean isContainerVisible() {
		return container.isVisible();
	}

	@Override
	public boolean isContainerRendered() {
		return container.isRendered();
	}

	@Override
	public void setContainerVisible(boolean setVisible) {
		container.setVisible(setVisible);
	}

	@Override
	public void slideContainerIn(Direction direction, FxConfig config) {
		container.el().slideIn(direction, config);
	}

	@Override
	public void slideContainerOut(Direction direction, FxConfig config) {
		container.el().slideOut(direction, config);
	}
}
