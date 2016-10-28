package org.sagebionetworks.web.client.widget.entity.act;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.Modal;
import org.gwtbootstrap3.client.ui.html.Div;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Widget;

public class EmailMessagePreviewModalViewImpl implements EmailMessagePreviewModalView {
	
	public interface Binder extends UiBinder<Widget, EmailMessagePreviewModalViewImpl> {}
	private static Binder uiBinder = GWT.create(Binder.class);
	
	@UiField
	Modal modal;
	@UiField
	Button closeButton;
	@UiField
	Div messageBody;
	
	private Presenter presenter;
	
	Widget widget;
	
	public EmailMessagePreviewModalViewImpl() {
		widget = uiBinder.createAndBindUi(this);
		closeButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				modal.hide();
			}
		});
	}
	
	@Override
	public void setMessageBody(String message) {
		HTML display = new HTML();
		display.setHTML(message);
		messageBody.clear();
		messageBody.add(display.asWidget());
	}
	
	@Override
	public Widget asWidget() {
		return widget;
	}
	
	@Override
	public void setPresenter(Presenter presenter) {
		this.presenter = presenter;
	}
	
	@Override
	public void show() {
		modal.show();
	}
	
	@Override
	public void hide() {
		modal.hide();
	}
	
}
