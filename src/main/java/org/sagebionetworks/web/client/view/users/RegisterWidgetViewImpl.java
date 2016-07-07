package org.sagebionetworks.web.client.view.users;

import org.gwtbootstrap3.client.ui.Alert;
import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.html.Div;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class RegisterWidgetViewImpl implements RegisterWidgetView {
	public interface Binder extends UiBinder<Widget, RegisterWidgetViewImpl> {}
	private Presenter presenter;
	Widget widget;
	@UiField
	Button registerBtn;
	@UiField
	TextBox emailAddressField;
	@UiField
	Div synAlertContainer;
	@UiField
	Div blockUI;
	@UiField
	Div inlineUI;
	@UiField
	Alert emailSentAlert;
	
	@UiField
	TextBox emailAddressField2;
	@UiField
	Button registerBtn2;
	
	@Inject
	public RegisterWidgetViewImpl(Binder binder) {
		widget = binder.createAndBindUi(this);
		emailAddressField.getElement().setAttribute("placeholder", "Your email");
		emailAddressField2.getElement().setAttribute("placeholder", "Your email");
		initClickHandlers();
	}

	public void initClickHandlers() {
		registerBtn.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				presenter.registerUser(emailAddressField.getValue());
			}
		});
		registerBtn2.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				presenter.registerUser(emailAddressField2.getValue());
			}
		});
		
		emailAddressField.addKeyDownHandler(new KeyDownHandler() {
		    @Override
		    public void onKeyDown(KeyDownEvent event) {
		        if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
		        	registerBtn.click();
		        }
		    }
		});
		emailAddressField2.addKeyDownHandler(new KeyDownHandler() {
		    @Override
		    public void onKeyDown(KeyDownEvent event) {
		        if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
		        	registerBtn2.click();
		        }
		    }
		});
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
	public void setVisible(boolean isVisible) {
		widget.setVisible(isVisible);
	}
	@Override
	public void enableRegisterButton(boolean enable) {
		registerBtn.setEnabled(enable);
	}
	
	@Override
	public void clear() {
		emailSentAlert.setVisible(false);
		emailAddressField.setText("");
		emailAddressField.setText("");
	}

	@Override
	public void setSynAlert(Widget w) {
		synAlertContainer.clear();
		synAlertContainer.add(w);
	}
	
	@Override
	public void setInlineUI(boolean isInline) {
		inlineUI.setVisible(isInline);
		blockUI.setVisible(!isInline);
	}
	
	@Override
	public void setEmail(String email) {
		emailAddressField.setText(email);
		emailAddressField2.setText(email);
	}
	
	@Override
	public void setEmailSentAlert(boolean isVisible) {
		emailSentAlert.setVisible(isVisible);
	}
}
