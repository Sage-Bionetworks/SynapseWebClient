package org.sagebionetworks.web.client.widget;

import org.gwtbootstrap3.client.ui.constants.AlertType;
import org.sagebionetworks.web.client.jsinterop.AlertButtonConfig;
import org.sagebionetworks.web.client.jsinterop.FullWidthAlertProps;
import org.sagebionetworks.web.client.jsinterop.React;
import org.sagebionetworks.web.client.jsinterop.ReactDOM;
import org.sagebionetworks.web.client.jsinterop.ReactElement;
import org.sagebionetworks.web.client.jsinterop.SRC;

import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;

public class FullWidthAlert implements IsWidget {
	ReactComponentDiv container;
	String title, message, primaryButtonText, secondaryButtonText, alertType, secondaryButtonTooltipText;
	AlertButtonConfig.Callback onPrimaryClick;
	AlertButtonConfig.Callback onSecondaryClick;
	FullWidthAlertProps.Callback onClose = () -> setVisible(false);
	Boolean isGlobal = true;
	boolean secondaryButtonEnabled = true;
	/**
	 * This is a full width info Alert component, with an icon, message and link.
	 * 
	 */
	public FullWidthAlert() {
		container = new ReactComponentDiv();
	}
	
	private void rerender() {
		Double autoCloseAfterDelayInSeconds = null;
		AlertButtonConfig primaryButtonConfig = AlertButtonConfig.create(primaryButtonText, onPrimaryClick);
		AlertButtonConfig secondaryButtonConfig = AlertButtonConfig.create(secondaryButtonText, onSecondaryClick, !secondaryButtonEnabled, secondaryButtonTooltipText);
		FullWidthAlertProps props = FullWidthAlertProps.create(title, message, primaryButtonConfig, secondaryButtonConfig, onClose, autoCloseAfterDelayInSeconds, isGlobal, alertType);
		ReactElement component = React.createElement(SRC.SynapseComponents.FullWidthAlert, props);
		ReactDOM.render(component, container.getElement());	
	}

	@Override
	public Widget asWidget() {
		return container;
	}

	public void setVisible(boolean visible) {
		container.setVisible(visible);
	}

	public void setAddStyleNames(String styleNames) {
		container.addStyleName(styleNames);
	}

	public boolean isVisible() {
		return container.isVisible();
	}

	public boolean isAttached() {
		return container.isAttached();
	}

	public void setMessageTitle(String title) {
		this.title = title;
		rerender();
	}
	public void setMessage(String message) {
		this.message = message;
		rerender();
	}

	public void setPrimaryCTAHref(String href) {
		setPrimaryCTAHref(href, "_blank");
	}

	public void setPrimaryCTAHrefTargetSelf(String href) {
		setPrimaryCTAHref(href, "_self");
	}

	private void setPrimaryCTAHref(String href, String target) {
		addPrimaryCTAClickHandler(event -> {
			Window.open(href, target, "");
		});
	}
	
	public void addPrimaryCTAClickHandler(ClickHandler c) {
		this.onPrimaryClick = () -> c.onClick(null);
		rerender();
	}

	public void setSecondaryButtonEnabled(boolean enabled) {
		this.secondaryButtonEnabled = enabled;
		rerender();
	}

	public void setSecondaryButtonTooltipText(String tooltipText) {
		this.secondaryButtonTooltipText = tooltipText;
		rerender();
	}

	public void addSecondaryCTAClickHandler(ClickHandler c) {
		this.onSecondaryClick = () -> c.onClick(null);
		rerender();
	}


	public void setPrimaryCTAText(String text) {
		String newText = text != null ? text.toUpperCase() : null;
		this.primaryButtonText = newText;
		rerender();
	}

	public void setSecondaryCTAText(String text) {
		String newText = text != null ? text.toUpperCase() : null;
		this.secondaryButtonText = newText;
		rerender();
	}

	public void setSecondaryCTAHref(String href) {
		addSecondaryCTAClickHandler(event -> {
			Window.open(href, "_blank", "");
		});
	}

	public void setAlertType(AlertType type) {
		this.alertType = type.name().toLowerCase();
		rerender();
	}

	public void setGlobal(boolean isGlobal) {
		this.isGlobal = isGlobal;
	}
}
