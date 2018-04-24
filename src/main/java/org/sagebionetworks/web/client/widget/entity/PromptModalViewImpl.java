package org.sagebionetworks.web.client.widget.entity;

import org.gwtbootstrap3.client.shared.event.ModalShownEvent;
import org.gwtbootstrap3.client.shared.event.ModalShownHandler;
import org.gwtbootstrap3.client.ui.Alert;
import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.FormLabel;
import org.gwtbootstrap3.client.ui.Modal;
import org.gwtbootstrap3.client.ui.TextBox;
import org.sagebionetworks.web.client.DisplayUtils;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

/**
 * A basic implementation of PromptModalView with zero business logic.
 * 
 * @author jhill
 *
 */
public class PromptModalViewImpl implements PromptModalView {
	
	public interface Binder extends UiBinder<Modal, PromptModalViewImpl> {}
	
	@UiField
	Modal modal;
	@UiField
	FormLabel nameLabel;
	@UiField
	TextBox nameField;
	@UiField
	Alert alert;
	@UiField
	Button primaryButton;
	@UiField
	Button defaultButton;
	Widget widget;
	HandlerRegistration primaryButtonHandlerRegistration;
	
	@Inject
	public PromptModalViewImpl(Binder binder){
		widget = binder.createAndBindUi(this);
		modal.addShownHandler(new ModalShownHandler() {
			@Override
			public void onShown(ModalShownEvent evt) {
				nameField.setFocus(true);
				nameField.selectAll();
			}
		});
		defaultButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				modal.hide();
			}
		});
		primaryButton.addDomHandler(DisplayUtils.getPreventTabHandler(primaryButton), KeyDownEvent.getType());
		this.nameField.addKeyDownHandler(new KeyDownHandler() {
			@Override
			public void onKeyDown(KeyDownEvent event) {
				if(KeyCodes.KEY_ENTER == event.getNativeKeyCode()){
					primaryButton.click();
				}
			}
		});
	}
	
	@Override
	public Widget asWidget() {
		return widget;
	}

	@Override
	public String getValue() {
		return nameField.getText();
	}

	@Override
	public void showError(String error) {
		alert.setVisible(true);
		alert.setText(error);
	}

	@Override
	public void setPresenter(final Presenter presenter) {
		if (primaryButtonHandlerRegistration != null) {
			primaryButtonHandlerRegistration.removeHandler();
		}
		primaryButtonHandlerRegistration = this.primaryButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent arg0) {
				presenter.onPrimary();
			}
		});
	}

	@Override
	public void hide() {
		modal.hide();
	}

	@Override
	public void show() {
		modal.show();
		nameField.setFocus(true);
	}

	@Override
	public void clear() {
		this.primaryButton.state().reset();
		this.alert.setVisible(false);
		this.nameField.clear();
	}

	@Override
	public void setLoading(boolean isLoading) {
		if(isLoading){
			this.primaryButton.state().loading();
		}else{
			this.primaryButton.state().reset();
		}	
	}

	@Override
	public void configure(String title, String label, String buttonText, String name) {
		clear();
		this.modal.setTitle(title);
		this.nameLabel.setText(label);
		// TODO: SWC-3541: the primary button text is not being changed by this line.  We need to find out why.
		// Or duplicates the button text value!  SWC-1606
//		this.primaryButton.setText(buttonText);
		this.nameField.setText(name);
	}

}
