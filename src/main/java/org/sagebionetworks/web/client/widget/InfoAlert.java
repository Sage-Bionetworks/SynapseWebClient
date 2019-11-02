package org.sagebionetworks.web.client.widget;

import org.gwtbootstrap3.client.ui.Alert;
import org.gwtbootstrap3.client.ui.Anchor;
import org.gwtbootstrap3.client.ui.Icon;
import org.gwtbootstrap3.client.ui.constants.AlertType;
import org.gwtbootstrap3.client.ui.constants.IconType;
import org.gwtbootstrap3.client.ui.html.Span;
import com.google.gwt.core.shared.GWT;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;

public class InfoAlert implements IsWidget {
	public interface Binder extends UiBinder<Alert, InfoAlert> {
	}

	private static Binder uiBinder = GWT.create(Binder.class);

	@UiField
	Icon icon;
	@UiField
	Span messageSpan;
	@UiField
	Anchor link;
	Alert w;

	/**
	 * This is a full width info Alert component, with an icon, message and link.
	 * 
	 * ## Usage In your ui.xml, how to add the InfoAlert.
	 * 
	 * You can customize the icon, the message and the link target. ```
	 * xmlns:w="urn:import:org.sagebionetworks.web.client.widget"
	 * <w:InfoAlert icon="CHECK_CIRCLE" message="x files were added to your Download List" linkTarget=""
	 * linkText="view download list" /> ```
	 */
	public InfoAlert() {
		w = uiBinder.createAndBindUi(this);
	}

	@Override
	public Widget asWidget() {
		return w;
	}

	public void setVisible(boolean visible) {
		w.setVisible(visible);
	}

	public void setAddStyleNames(String styleNames) {
		w.addStyleName(styleNames);
	}

	public boolean isVisible() {
		return w.isVisible();
	}

	public boolean isAttached() {
		return w.isAttached();
	}

	public void setIcon(IconType type) {
		icon.setType(type);;
	}

	public void setMessage(String message) {
		messageSpan.setText(message);
	}

	public void addClickHandler(ClickHandler c) {
		link.addClickHandler(c);
	}

	public void setLinkTarget(String href) {
		link.setTarget(href);
	}

	public void setLinkText(String text) {
		link.setText(text.toUpperCase());
	}

	public void setLinkHref(String href) {
		link.setHref(href);
	}

	public void setAlertType(AlertType type) {
		w.setType(type);
	}

	public void setDismissable(boolean dismissable) {
		w.setDismissable(dismissable);
	}
}
