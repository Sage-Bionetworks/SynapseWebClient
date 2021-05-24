package org.sagebionetworks.web.client.widget.entity.controller;

import org.gwtbootstrap3.client.ui.html.Div;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.context.SynapseContextPropsProvider;
import org.sagebionetworks.web.client.jsni.SynapseContextProviderPropsJSNIObject;
import org.sagebionetworks.web.client.view.DownViewImpl;
import org.sagebionetworks.web.client.view.DownViewImpl.ErrorPageType;
import org.sagebionetworks.web.client.widget.ReactComponentDiv;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class StuAlertViewImpl implements StuAlertView {

	public interface Binder extends UiBinder<Widget, StuAlertViewImpl> {
	}

	Widget widget;
	SynapseContextPropsProvider propsProvider;

	@UiField
	ReactComponentDiv errorPageContainer;
	@UiField
	Div synAlertContainer;
	Widget synAlertWidget;
	Div container = new Div();
	boolean is404, is403;
	@Inject
	public StuAlertViewImpl(SynapseContextPropsProvider propsProvider) {
		this.propsProvider = propsProvider;
	}

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
			is404 = false;
			is403 = false;
			errorPageContainer.setVisible(false);
		}
	}

	private void lazyConstruct() {
		if (widget == null) {
			Binder b = GWT.create(Binder.class);
			widget = b.createAndBindUi(this);
			synAlertContainer.add(synAlertWidget);
			container.add(widget);
			widget.addAttachHandler(event -> {
				if (event.isAttached()) {
					updateErrorPage();
				}
			});
		}
	}

	private void updateErrorPage() {
		if (is404) {
			DownViewImpl._createSRCErrorPage(errorPageContainer.getElement(), ErrorPageType.unavailable.name(), "Sorry, this page isn’t available.", "The link you followed may be broken, or the page may have been removed.", propsProvider.getJsniContextProps());
			errorPageContainer.setVisible(true);
		}
		if (is403) {
			DownViewImpl._createSRCErrorPage(errorPageContainer.getElement(), ErrorPageType.noAccess.name(), "Sorry, no access to this page.", "You are not authorized to access the page requested.", propsProvider.getJsniContextProps());
			errorPageContainer.setVisible(true);
		}
	}
	@Override
	public void show403() {
		is403 = true;
		lazyConstruct();
		container.setVisible(true);
		updateErrorPage();
	}

	@Override
	public void show404() {
		is404 = true;
		lazyConstruct();
		container.setVisible(true);
		updateErrorPage();
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
