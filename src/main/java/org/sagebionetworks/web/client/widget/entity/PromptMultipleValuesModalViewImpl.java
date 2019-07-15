package org.sagebionetworks.web.client.widget.entity;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.gwtbootstrap3.client.shared.event.ModalShownEvent;
import org.gwtbootstrap3.client.shared.event.ModalShownHandler;
import org.gwtbootstrap3.client.ui.Alert;
import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.FormGroup;
import org.gwtbootstrap3.client.ui.FormLabel;
import org.gwtbootstrap3.client.ui.Modal;
import org.gwtbootstrap3.client.ui.TextBox;
import org.gwtbootstrap3.client.ui.html.Div;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;

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
public class PromptMultipleValuesModalViewImpl implements PromptMultipleValuesModalView {
	
	public interface Binder extends UiBinder<Modal, PromptMultipleValuesModalViewImpl> {}
	
	Modal modal;
	@UiField
	Div form;
	@UiField
	Div synAlertContainer;
	@UiField
	Button primaryButton;
	CallbackP<List<String>> newValuesCallback;
	List<TextBox> textBoxes;
	SynapseAlert synAlert;
	KeyDownHandler handler;
	
	@Inject
	public PromptMultipleValuesModalViewImpl(Binder binder, SynapseAlert synAlert){
		modal = binder.createAndBindUi(this);
		this.synAlert = synAlert;
		synAlertContainer.add(synAlert);
		modal.addShownHandler(event -> {
			if (textBoxes.size() > 0) {
				textBoxes.get(0).setFocus(true);
				textBoxes.get(0).selectAll();
			}
		});
		
		this.primaryButton.addClickHandler(event -> {
			onPrimary();
		});
		handler = event -> {
			if(KeyCodes.KEY_ENTER == event.getNativeKeyCode()){
				onPrimary();
			}
		};
	}
	
	private void onPrimary() {
		// collect values
		List<String> values = new ArrayList<>();
		for (TextBox textBox : textBoxes) {
			values.add(textBox.getValue());
		}
		modal.hide();
		newValuesCallback.invoke(values);
	}
	
	@Override
	public Widget asWidget() {
		return modal;
	}

	@Override
	public void configureAndShow(String title, List<String> prompts, List<String> initialValues,
			CallbackP<List<String>> newValuesCallback) {
		modal.setTitle(title);
		this.newValuesCallback = newValuesCallback;
		textBoxes = new ArrayList<>();
		form.clear();
		if (initialValues != null && prompts.size() != initialValues.size() ) {
			throw new IllegalArgumentException("If set, initialValues size must equal prompts size.");
		}
		/**
			<b:FormGroup>
				<b:FormLabel ui:field="label1" for="value1">Label1 goes here</b:FormLabel>
				<b:TextBox b:id="value1" ui:field="valueField1" />
			</b:FormGroup>
		 */
		for (int i = 0; i < prompts.size(); i++) {
			FormGroup group = new FormGroup();
			FormLabel label = new FormLabel();
			TextBox textBox = new TextBox();
			textBox.addKeyDownHandler(handler);
			group.add(label);
			group.add(textBox);
			String prompt = prompts.get(i);
			label.setText(prompt);
			if (initialValues != null) {
				String initialValue = initialValues.get(i);
				textBox.setValue(initialValue);
			}
			
			form.add(group);
			textBoxes.add(textBox);
		}
		modal.show();
	}
	
	@Override
	public void showError(String error) {
		synAlert.showError(error);
	}

	@Override
	public void clear() {
		this.primaryButton.state().reset();
		synAlert.clear();
	}

	@Override
	public void setLoading(boolean isLoading) {
		if(isLoading){
			this.primaryButton.state().loading();
		}else{
			this.primaryButton.state().reset();
		}	
	}
}
