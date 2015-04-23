package org.sagebionetworks.web.client.view.users;

import org.gwtbootstrap3.client.ui.Button;
import org.sagebionetworks.web.client.DisplayUtils;

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

	@Inject
	public RegisterWidgetViewImpl(Binder binder) {
		widget = binder.createAndBindUi(this);
		emailAddressField.getElement().setAttribute("placeholder", " Enter email address");
		initClickHandlers();
	}

	public void initClickHandlers() {
		registerBtn.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				presenter.registerUser(emailAddressField.getValue());
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
	public void showErrorMessage(String message) {
		DisplayUtils.showErrorMessage(message);
	}

	@Override
	public void showLoading() {
	}

	@Override
	public void showInfo(String title, String message) {
		DisplayUtils.showInfo(title, message);
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
		emailAddressField.setText("");
	}

	/*
	 * Private Methods
	 */


}
