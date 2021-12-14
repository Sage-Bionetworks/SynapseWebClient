package org.sagebionetworks.web.client.widget.entity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.FormGroup;
import org.gwtbootstrap3.client.ui.FormLabel;
import org.gwtbootstrap3.client.ui.Heading;
import org.gwtbootstrap3.client.ui.Modal;
import org.gwtbootstrap3.client.ui.TextArea;
import org.gwtbootstrap3.client.ui.TextBox;
import org.gwtbootstrap3.client.ui.html.Div;
import org.gwtbootstrap3.client.ui.html.Paragraph;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.HelpWidget;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;

import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.ValueBoxBase;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

/**
 * A basic implementation of PromptTwoValuesModalView with zero business logic.
 *
 */
public class PromptForValuesModalViewImpl implements PromptForValuesModalView {

	protected static final InputType DEFAULT_INPUT_TYPE = InputType.TEXTBOX;

	/**
	 * We map the enum to a new instance of each class, since we can't use reflection in client side code.
	 */
	private ValueBoxBase<String> getNewInstanceOfInputField(InputType type) {
		switch (type) {
			case TEXTBOX:
				return new TextBox();
			case TEXTAREA:
				return new TextArea();
			default:
				throw new RuntimeException("Invalid input type: " + type.name());
		}
	}

	public interface Binder extends UiBinder<Modal, PromptForValuesModalViewImpl> {
	}

	@UiField
	Modal modal;
	@UiField
	Heading modalTitle;
	@UiField
	Div form;
	@UiField
	Paragraph bodyCopy;
	@UiField
	Div synAlertContainer;
	@UiField
	Button primaryButton;
	@UiField
	Button defaultButton;
	@UiField
	HelpWidget helpWidget;
	CallbackP<List<String>> newValuesCallback;
	List<ValueBoxBase<String>> textBoxes;
	SynapseAlert synAlert;
	KeyDownHandler handler;
	String originalButtonText;

	@Inject
	public PromptForValuesModalViewImpl(Binder binder, SynapseAlert synAlert) {
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
		defaultButton.addClickHandler(event -> {
			// cancelled
			modal.hide();
		});
		handler = event -> {
			if (KeyCodes.KEY_ENTER == event.getNativeKeyCode()) {
				onPrimary();
			}
		};
		originalButtonText = primaryButton.getText();
	}

	private void onPrimary() {
		// collect values
		List<String> values = new ArrayList<>();
		for (ValueBoxBase<String> formField : textBoxes) {
			values.add(formField.getValue());
		}
		newValuesCallback.invoke(values);
	}

	@Override
	public Widget asWidget() {
		return modal;
	}

	@Override
	public void configureAndShow(String title, String prompt, String initialValue, CallbackP<String> newValueCallback) {
		List<String> promptListWrapper = Collections.singletonList(prompt);
		List<String> initialValueListWrapper = null;
		if (initialValue != null) {
			initialValueListWrapper = Collections.singletonList(initialValue);
		}
		CallbackP<List<String>> newValueCallbackWrapper = list -> {
			newValueCallback.invoke(list.get(0));
		};
		configureAndShow(title, promptListWrapper, initialValueListWrapper, newValueCallbackWrapper);
	}

	@Override
	public void configureAndShow(String title, List<String> prompts, List<String> initialValues, CallbackP<List<String>> newValuesCallback) {
		// By default, use a TextBox for each field
		List<InputType> textboxForEach = new ArrayList<>(prompts.size());
		for (int i = 0; i < prompts.size(); i++) {
			textboxForEach.add(DEFAULT_INPUT_TYPE);
		}
		configureAndShow(title, prompts, initialValues, textboxForEach, newValuesCallback);
	}

	@Override
	public void configureAndShow(String title, List<String> prompts, List<String> initialValues, List<InputType> inputTypes, CallbackP<List<String>> newValuesCallback) {
		Configuration configuration = new PromptForValuesModalConfigurationImpl();
		configuration.setTitle(title);
		configuration.setPrompts(prompts);
		configuration.setInitialValues(initialValues);
		configuration.setInputTypes(inputTypes);
		configuration.setNewValuesCallback(newValuesCallback);
		configureAndShow(configuration);
	}

	@Override
	public void configureAndShow(PromptForValuesModalView.Configuration configuration) {
		clear();

		modalTitle.setText(configuration.getTitle());

		// Configure the optional body copy
		if (configuration.getBodyCopy() != null) {
			this.bodyCopy.setText(configuration.getBodyCopy());
			this.bodyCopy.setVisible(true);
		} else {
			this.bodyCopy.setVisible(false);
		}

		// Configure the optional HelpWidget
		if (configuration.getHelpPopoverMarkdown() != null) {
			this.helpWidget.setHelpMarkdown(configuration.getHelpPopoverMarkdown());
			if (configuration.getHelpPopoverHref() != null) {
				this.helpWidget.setHref(configuration.getHelpPopoverHref());
			}
			this.helpWidget.setVisible(true);
		} else {
			this.helpWidget.setVisible(false);
		}

		this.newValuesCallback = configuration.getNewValuesCallback();
		if (configuration.getInitialValues() != null && configuration.getPrompts().size() != configuration.getInitialValues().size()) {
			throw new IllegalArgumentException("If set, initialValues size must equal prompts size.");
		}
		/**
		 * <b:FormGroup> <b:FormLabel ui:field="label1" for="value1">Label1 goes here</b:FormLabel>
		 * <b:TextBox b:id="value1" ui:field="valueField1" /> </b:FormGroup>
		 */
		for (int i = 0; i < configuration.getPrompts().size(); i++) {
			FormGroup group = new FormGroup();
			FormLabel label = new FormLabel();
			ValueBoxBase<String> inputBox = getNewInstanceOfInputField(configuration.getInputTypes().get(i));
			inputBox.addKeyDownHandler(handler);
			group.add(label);
			group.add(inputBox);
			String prompt = configuration.getPrompts().get(i);
			label.setText(prompt);
			if (configuration.getInitialValues() != null) {
				String initialValue = configuration.getInitialValues().get(i);
				inputBox.setValue(initialValue);
			}

			form.add(group);
			textBoxes.add(inputBox);
		}
		modal.show();
	}



	@Override
	public void showError(String error) {
		synAlert.showError(error);
	}

	@Override
	public void clear() {
		bodyCopy.clear();
		form.clear();
		textBoxes = new ArrayList<>();
		synAlert.clear();
		helpWidget.setVisible(false);
		setLoading(false);
	}

	@Override
	public void setLoading(boolean isLoading) {
		DisplayUtils.showLoading(primaryButton, isLoading, originalButtonText);
	}

	@Override
	public void hide() {
		modal.hide();
	}
}
