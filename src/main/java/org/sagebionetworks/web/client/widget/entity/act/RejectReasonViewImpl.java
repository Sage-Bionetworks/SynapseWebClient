package org.sagebionetworks.web.client.widget.entity.act;

import org.gwtbootstrap3.client.ui.Alert;
import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.CheckBox;
import org.gwtbootstrap3.client.ui.Modal;
import org.gwtbootstrap3.client.ui.TextArea;
import org.sagebionetworks.web.client.DisplayUtils;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class RejectReasonViewImpl implements RejectReasonView {

	public interface Binder extends UiBinder<Modal, RejectReasonViewImpl> {
	}

	@UiField
	Modal modal;

	// Generated Response Preview
	@UiField
	TextArea nameField;

	// Checkboxes
	@UiField
	CheckBox synapseQuizOption;
	@UiField
	CheckBox addInfoOption;
	@UiField
	CheckBox orcIDPublicOption;
	@UiField
	CheckBox physicallyInitialOption;
	@UiField
	CheckBox submitDocsOption;
	@UiField
	CheckBox customTextOption;


	// Generate response button
	@UiField
	Button generateButton;

	// Text Box for custom checkbox
	@UiField
	TextArea customText;

	// alert if no responses submitted
	@UiField
	Alert alert;

	// Cancel and Submit Buttons
	@UiField
	Button primaryButton;
	@UiField
	Button defaultButton;

	Widget widget;

	// Presenter
	Presenter presenter;

	@Inject
	public RejectReasonViewImpl(Binder binder) {
		widget = binder.createAndBindUi(this);

		defaultButton.addClickHandler(event -> modal.hide());
		primaryButton.addClickHandler(event -> presenter.onSave());
		primaryButton.addDomHandler(DisplayUtils.getPreventTabHandler(primaryButton), KeyDownEvent.getType());

		generateButton.addClickHandler(event -> presenter.updateResponse());
		customTextOption.addClickHandler(event -> customText.setVisible(customTextOption.getValue()));
	}

	public void setPresenter(Presenter presenter) {
		this.presenter = presenter;
	}

	public void setValue(String value) {
		nameField.setText(value);
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
		this.clearError();
		this.primaryButton.state().reset();
		this.defaultButton.state().reset();
		this.defaultButton.state().reset();
		this.customText.clear();
		this.nameField.clear();
		this.synapseQuizOption.setValue(false);
		this.addInfoOption.setValue(false);
		this.orcIDPublicOption.setValue(false);
		this.physicallyInitialOption.setValue(false);
		this.submitDocsOption.setValue(false);
		this.customTextOption.setValue(false);
		this.customText.setVisible(false);
	}

	@Override
	public void clearError() {
		this.alert.setVisible(false);
	}

	@Override
	public String getSelectedCheckboxText() {
		// add your textbox here if we have a new option
		CheckBox[] checkboxes = new CheckBox[] {synapseQuizOption, addInfoOption, orcIDPublicOption, physicallyInitialOption, submitDocsOption};

		String output = "";
		for (CheckBox checkBox : checkboxes) {
			if (checkBox.getValue()) {
				output += "\n" + checkBox.getText() + "\n";
			}
		}
		if (customTextOption.getValue()) {
			output += "\n" + customText.getText() + "\n";
		}

		return output;
	}
}
