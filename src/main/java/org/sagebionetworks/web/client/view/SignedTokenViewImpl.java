package org.sagebionetworks.web.client.view;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.Heading;
import org.gwtbootstrap3.client.ui.Row;
import org.gwtbootstrap3.client.ui.html.Div;
import org.sagebionetworks.web.client.widget.footer.Footer;
import org.sagebionetworks.web.client.widget.header.Header;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class SignedTokenViewImpl implements SignedTokenView {

	public interface SignedTokenViewImplUiBinder extends UiBinder<Widget, SignedTokenViewImpl> {}
	
	@UiField
	SimplePanel header;
	@UiField
	SimplePanel footer;
	
	@UiField
	SimplePanel synapseAlertContainer;
	@UiField
	Button okButton;
	@UiField
	Button confirmUnsubscribe;
	@UiField
	Button cancelUnsubscribe;
	
	@UiField
	Row successUI;
	@UiField
	Heading successMessage;
	
	@UiField
	Div confirmUnsubscribeUI;
	@UiField
	SimplePanel unsubscribeUserBadgeContainer;
	
	private Presenter presenter;
	private Header headerWidget;
	private Footer footerWidget;
	
	Widget widget;
	
	@Inject
	public SignedTokenViewImpl(
			SignedTokenViewImplUiBinder binder,
			Header headerWidget, 
			Footer footerWidget) {		
		widget = binder.createAndBindUi(this);
		
		this.headerWidget = headerWidget;
		this.footerWidget = footerWidget;
		headerWidget.configure(false);
		header.add(headerWidget.asWidget());
		footer.add(footerWidget.asWidget());
		ClickHandler okClickHandler = new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				presenter.okClicked();
			}
		};
		okButton.addClickHandler(okClickHandler);
		cancelUnsubscribe.addClickHandler(okClickHandler);
		
		confirmUnsubscribe.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				presenter.unsubscribeConfirmed();
			}
		});
	}

	@Override
	public Widget asWidget() {
		return widget;
	}

	@Override
	public void setPresenter(final Presenter presenter) {
		this.presenter = presenter;
		header.clear();
		headerWidget.configure(false);
		header.add(headerWidget.asWidget());
		footer.clear();
		footer.add(footerWidget.asWidget());
		headerWidget.refresh();
		Window.scrollTo(0, 0); // scroll user to top of page
	}

	@Override
	public void clear() {
		successUI.setVisible(false);
		confirmUnsubscribeUI.setVisible(false);
		okButton.setVisible(true);
	}

	@Override
	public void setSynapseAlert(Widget w) {
		synapseAlertContainer.setWidget(w);
	}
	
	@Override
	public void showSuccess(String message) {
		successMessage.setText(message);
		successUI.setVisible(true);
	}
	
	@Override
	public void showConfirmUnsubscribe() {
		confirmUnsubscribeUI.setVisible(true);
		okButton.setVisible(false);
	}
	@Override
	public void setUnsubscribingUserBadge(Widget w) {
		unsubscribeUserBadgeContainer.setWidget(w);
	}
}
