package org.sagebionetworks.web.client.widget.entity.controller;

import org.gwtbootstrap3.client.ui.html.Div;
import org.sagebionetworks.web.client.DisplayUtils;
import com.google.gwt.core.shared.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class StuAlertViewImpl implements StuAlertView {

	public interface Binder extends UiBinder<Widget, StuAlertViewImpl> {
	}

	Widget widget;

	@UiField
	HTMLPanel httpCode403;
	@UiField
	HTMLPanel httpCode404;
	@UiField
	Div synAlertContainer;
	Widget synAlertWidget;
	Div container = new Div();

	@Inject
	public StuAlertViewImpl() {}

	@Override
	public void showInfo(String message) {
		DisplayUtils.showInfo(message);
	}

	@Override
	public Widget asWidget() {
		return container;
	}

	@Override
	public void clearState() {
		container.setVisible(false);
		if (widget != null) {
			httpCode403.setVisible(false);
			httpCode404.setVisible(false);
		}
	}

	private void lazyConstruct() {
		if (widget == null) {
			Binder b = GWT.create(Binder.class);
			widget = b.createAndBindUi(this);
			synAlertContainer.add(synAlertWidget);
			container.add(widget);
		}
	}

	@Override
	public void show403() {
		lazyConstruct();
		container.setVisible(true);
		httpCode403.setVisible(true);
	}

	@Override
	public void show404() {
		lazyConstruct();
		container.setVisible(true);
		httpCode404.setVisible(true);
	}

	@Override
	public void setSynAlert(Widget w) {
		synAlertWidget = w;
	}

	@Override
	public void setVisible(boolean visible) {
		if (visible) {
			lazyConstruct();
		}
		container.setVisible(visible);
	}
}
