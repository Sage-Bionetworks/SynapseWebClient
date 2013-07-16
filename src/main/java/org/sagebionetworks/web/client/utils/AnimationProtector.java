package org.sagebionetworks.web.client.utils;

import com.extjs.gxt.ui.client.Style.Direction;
import com.extjs.gxt.ui.client.event.FxEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.fx.FxConfig;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;

public class AnimationProtector {

	private AnimationProtectorView view;

	private FxConfig hideConfig     = null;
	private FxConfig userHideConfig = null;
	private FxConfig showConfig     = null;
	private FxConfig userShowConfig = null;

	private boolean animating = false;

	private HandlerRegistration clickRegistration = null;

	public AnimationProtector(AnimationProtectorView view) {
		setView(view);
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

	public void setView(AnimationProtectorView view) {
		if (clickRegistration != null)
			clickRegistration.removeHandler();

		this.view = view;
		clickRegistration = view.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				toggle();
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
		setVisible(!view.isContainerVisible());
	}

	private void setVisible(boolean shouldBeVisible) {
		if (!view.isSlideSupportedByView()) {
			//if it's not supported, then just set visibility and explicitly invoke the config listener
			view.setContainerVisible(shouldBeVisible);
			Listener<FxEvent> listener = null;
			if (shouldBeVisible)
				listener = showConfig.getEffectCompleteListener();
			else
				listener = hideConfig.getEffectCompleteListener();
			if (listener != null)
				listener.handleEvent(new FxEvent(null, null));
		} else if (view.isContainerRendered() && !animating) {

			boolean isCurrentlyVisible = view.isContainerVisible();

			if (!shouldBeVisible && isCurrentlyVisible) {
				animating = true;
				view.slideContainerOut(Direction.UP, hideConfig);

			} else if (shouldBeVisible && !isCurrentlyVisible) {
				animating = true;
				view.setContainerVisible(true);
				view.slideContainerIn(Direction.DOWN, showConfig);
			}
		}
	}
	
	public boolean isVisible() {
		if (view.isContainerRendered())
			 return false;
		else return view.isContainerVisible();
	}
}
