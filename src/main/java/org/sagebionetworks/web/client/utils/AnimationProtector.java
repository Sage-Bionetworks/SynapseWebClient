package org.sagebionetworks.web.client.utils;

import com.extjs.gxt.ui.client.Style.Direction;
import com.extjs.gxt.ui.client.event.FxEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.fx.FxConfig;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;

public class AnimationProtector {

	private final HasClickHandlers trigger;
	private final LayoutContainer container;

	private FxConfig hideConfig     = null;
	private FxConfig userHideConfig = null;
	private FxConfig showConfig     = null;
	private FxConfig userShowConfig = null;

	boolean animating = false;

	public AnimationProtector(HasClickHandlers trigger, LayoutContainer container) {
		super();
		this.trigger = trigger;
		this.container = container;
		this.trigger.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				toggle();
			}
		});
		this.showConfig = new FxConfig();
		this.showConfig.setEffectCompleteListener(new Listener<FxEvent>() {
			@Override
			public void handleEvent(FxEvent be) {
				animating = false;
				if (userShowConfig != null)
					userShowConfig.getEffectCompleteListener().handleEvent(be);
			}
		});
		this.hideConfig = new FxConfig();
		this.hideConfig.setEffectCompleteListener(new Listener<FxEvent>() {
			@Override
			public void handleEvent(FxEvent be) {
				animating = false;
				if (userHideConfig != null)
					userHideConfig.getEffectCompleteListener().handleEvent(be);
			}
		});
	}

	public FxConfig getShowConfig() {
		return userShowConfig;
	}

	public void setShowConfig(FxConfig showConfig) {
		this.userShowConfig = showConfig;
		this.showConfig.setDuration(userShowConfig.getDuration());
	}

	public FxConfig getHideConfig() {
		return userHideConfig;
	}

	public void setHideConfig(final FxConfig hideConfig) {
		this.userHideConfig = hideConfig;
		this.hideConfig.setDuration(userHideConfig.getDuration());
	}

	public boolean isAnimating() {
		return animating;
	}

	public void show() {
		setVisible(true);
	}

	public void hide() {
		setVisible(false);
	}

	public void toggle() {
		setVisible(!container.isVisible());
	}

	private void setVisible(boolean shouldBeVisible) {
		if (container.isRendered() && !animating) {

			boolean isCurrentlyVisible = container.el().isVisible();

			if (!shouldBeVisible && isCurrentlyVisible) {
				animating = true;
				container.el().slideOut(Direction.UP, hideConfig);

			} else if (shouldBeVisible && !isCurrentlyVisible) {
				animating = true;
				container.setVisible(true);
				container.el().slideIn(Direction.DOWN, showConfig);

			}
		}
	}
}
