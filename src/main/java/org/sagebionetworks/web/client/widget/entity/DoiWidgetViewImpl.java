package org.sagebionetworks.web.client.widget.entity;

import org.gwtbootstrap3.client.ui.Alert;
import org.gwtbootstrap3.client.ui.Anchor;
import org.gwtbootstrap3.client.ui.html.Span;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.security.AuthenticationController;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class DoiWidgetViewImpl implements DoiWidgetView {
	
	private Presenter presenter;
	GlobalApplicationState globalApplicationState;
	AuthenticationController authenticationController;
	
	@UiField
	Alert errorCreatingDoi;
			
	@UiField
	SimplePanel doiProcessing;
	
	@UiField
	HTMLPanel doiHTML;
	
	Widget widget;
	
	public interface Binder extends UiBinder<Widget, DoiWidgetViewImpl> {}
	
	@Inject
	public DoiWidgetViewImpl(GlobalApplicationState globalApplicationState,
			AuthenticationController authenticationController,
			Binder uiBinder) {
		this.globalApplicationState = globalApplicationState;
		this.authenticationController = authenticationController;
		widget = uiBinder.createAndBindUi(this);
	}
	
	private void hideAllChildren() {
		errorCreatingDoi.setVisible(false);
		doiProcessing.setVisible(false);
		doiHTML.getElement().setInnerHTML("");
	}
	
	@Override
	public void showDoiError() {
		widget.setVisible(true);
		hideAllChildren();
		//show error UI
		errorCreatingDoi.setVisible(true);
	}
	
	@Override
	public void showDoiInProgress() {
		widget.setVisible(true);
		hideAllChildren();
		//show in process UI
		doiProcessing.setVisible(true);
	}
	
	@Override
	public void showDoiCreated(String doiText) {
		widget.setVisible(true);
		hideAllChildren();
		//ask for the doi prefix from the presenter, and show a link to that!
		//first clear old handler, if there is one
		doiHTML.setVisible(true);
		doiHTML.getElement().setInnerHTML(doiText);
	}
	
	@Override
	public void setPresenter(Presenter p) {
		presenter = p;
	}

	@Override
	public void showLoading() {
	}

	@Override
	public Widget asWidget() {
		return widget;
	}
	
	@Override
	public void clear() {
		hideAllChildren();
	}

	@Override
	public void showInfo(String title, String message) {
		DisplayUtils.showInfo(title, message);
	}

	@Override
	public void showErrorMessage(String message) {
		DisplayUtils.showErrorMessage(message);
	}
	
	@Override
	public void setVisible(boolean visible) {
		widget.setVisible(visible);
	}

}
