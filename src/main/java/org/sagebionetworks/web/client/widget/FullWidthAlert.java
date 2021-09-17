package org.sagebionetworks.web.client.widget;

import org.gwtbootstrap3.client.ui.constants.AlertType;
import org.sagebionetworks.web.client.context.SynapseContextPropsProvider;
import org.sagebionetworks.web.client.context.SynapseContextPropsProviderImpl;
import org.sagebionetworks.web.client.jsinterop.FullWidthAlertProps;
import org.sagebionetworks.web.client.jsinterop.React;
import org.sagebionetworks.web.client.jsinterop.ReactDOM;
import org.sagebionetworks.web.client.jsinterop.ReactElement;
import org.sagebionetworks.web.client.jsinterop.SRC;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;

public class FullWidthAlert implements IsWidget {
	ReactComponentDiv container;
	String title, message, primaryButtonText, secondaryButtonText, secondaryButtonHref, alertType;
	FullWidthAlertProps.Callback onPrimaryClick;
	FullWidthAlertProps.Callback onClose = new FullWidthAlertProps.Callback() {
		@Override
		public void run() {
			setVisible(false);
		}
	};
	Boolean isGlobal = true;
	/**
	 * This is a full width info Alert component, with an icon, message and link.
	 * 
	 */
	public FullWidthAlert() {
		container = new ReactComponentDiv();
	}
	
	private void rerender() {
		Double autoCloseAfterDelayInSeconds = null;
		FullWidthAlertProps props = FullWidthAlertProps.create(title, message, primaryButtonText, onPrimaryClick, secondaryButtonText, secondaryButtonHref, onClose, autoCloseAfterDelayInSeconds, isGlobal, alertType);
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
		this.onPrimaryClick = new FullWidthAlertProps.Callback() {
			@Override
			public void run() {
				c.onClick(null);
			}
		};
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
		this.secondaryButtonHref = href;
		rerender();
	}

	public void setAlertType(AlertType type) {
		this.alertType = type.name().toLowerCase();
		rerender();
	}

	public void setGlobal(boolean isGlobal) {
		this.isGlobal = isGlobal;
	}
}
