package org.sagebionetworks.web.client.widget.evaluation;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.Modal;
import org.gwtbootstrap3.client.ui.TextBox;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;

public class EvaluationEditorModalViewImpl implements EvaluationEditorModalView {
	@UiField
	Modal modal;
	@UiField
	TextBox nameField;
	@UiField
	TextBox submissionInstructionsField;
	@UiField
	TextBox submissionReceiptField;
	
	@UiField
	Button saveButton;
	@UiField
	Button cancelButton;
	Widget widget;
	
	private Presenter presenter;
	
	public interface Binder extends UiBinder<Widget, EvaluationEditorModalViewImpl> {}
	private static Binder uiBinder = GWT.create(Binder.class);
	
	public EvaluationEditorModalViewImpl() {
		widget = uiBinder.createAndBindUi(this);
		saveButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				presenter.onSave();
			}
		});
		cancelButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				modal.hide();
			}
		});
	}
	
	@Override
	public void setPresenter(Presenter presenter) {
		this.presenter = presenter;
	}
	
	@Override
	public String getEvaluationName() {
		return nameField.getText();
	}
	@Override
	public void setEvaluationName(String name) {
		nameField.setText(name);
	}
	@Override
	public String getSubmissionInstructionsMessage() {
		return submissionInstructionsField.getText();
	}
	@Override
	public void setSubmissionInstructionsMessage(String message) {
		submissionInstructionsField.setText(message);
	}
	@Override
	public String getSubmissionReceiptMessage() {
		return submissionReceiptField.getText();
	}
	@Override
	public void setSubmissionReceiptMessage(String message) {
		submissionReceiptField.setText(message);
	}
	@Override
	public Widget asWidget() {
		return widget;
	}
	@Override
	public void show() {
		modal.show();
	}
}
