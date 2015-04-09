package org.sagebionetworks.web.client.widget.entity;

import org.gwtbootstrap3.client.shared.event.ModalShownEvent;
import org.gwtbootstrap3.client.shared.event.ModalShownHandler;
import org.gwtbootstrap3.client.ui.Alert;
import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.FormLabel;
import org.gwtbootstrap3.client.ui.Modal;
import org.gwtbootstrap3.client.ui.TextBox;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

/**
 * A basic implementation of PromptTwoValuesModalView with zero business logic.
 *
 */
public class PromptTwoValuesModalViewImpl implements PromptTwoValuesModalView {
	
	public interface Binder extends UiBinder<Modal, PromptTwoValuesModalViewImpl> {}
	
	@UiField
	Modal modal;
	@UiField
	FormLabel label1;
	@UiField
	TextBox valueField1;
	@UiField
	FormLabel label2;
	@UiField
	TextBox valueField2;
	@UiField
	Alert alert;
	@UiField
	Button primaryButton;
	
	Modal createTableModal;

	@Inject
	public PromptTwoValuesModalViewImpl(Binder binder){
		createTableModal = binder.createAndBindUi(this);
		modal.addShownHandler(new ModalShownHandler() {
			
			@Override
			public void onShown(ModalShownEvent evt) {
				valueField1.setFocus(true);
				valueField1.selectAll();
			}
		});
	}
	
	@Override
	public Widget asWidget() {
		return createTableModal;
	}

	@Override
	public String getValue1() {
		return valueField1.getText();
	}
	
	@Override
	public String getValue2() {
		return valueField2.getText();
	}

	@Override
	public void showError(String error) {
		alert.setVisible(true);
		alert.setText(error);
	}

	@Override
	public void setPresenter(final Presenter presenter) {
		this.primaryButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent arg0) {
				presenter.onPrimary();
			}
		});
		KeyDownHandler handler = new KeyDownHandler() {
			@Override
			public void onKeyDown(KeyDownEvent event) {
				if(KeyCodes.KEY_ENTER == event.getNativeKeyCode()){
					presenter.onPrimary();
				}
			}
		};
		this.valueField1.addKeyDownHandler(handler);
		this.valueField2.addKeyDownHandler(handler);
	}

	@Override
	public void hide() {
		createTableModal.hide();
	}

	@Override
	public void show() {
		createTableModal.show();
		valueField1.setFocus(true);
	}

	@Override
	public void clear() {
		this.primaryButton.state().reset();
		this.alert.setVisible(false);
		this.valueField1.clear();
		this.valueField2.clear();
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
	public void configure(String title, String label1, String value1, String label2, String value2, String buttonText) {
		this.modal.setTitle(title);
		this.label1.setText(label1);
		this.valueField1.setText(value1);
		this.label2.setText(label2);
		this.valueField2.setText(value2);
		this.primaryButton.setText(buttonText);
	}

}
