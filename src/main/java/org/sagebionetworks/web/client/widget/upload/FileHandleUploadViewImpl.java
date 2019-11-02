package org.sagebionetworks.web.client.widget.upload;

import org.gwtbootstrap3.client.ui.Alert;
import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.Form;
import org.gwtbootstrap3.client.ui.Input;
import org.gwtbootstrap3.client.ui.Progress;
import org.gwtbootstrap3.client.ui.ProgressBar;
import org.gwtbootstrap3.client.ui.html.Span;
import com.google.gwt.dom.client.InputElement;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.AttachEvent.Handler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class FileHandleUploadViewImpl implements FileHandleUploadView {

	private static final String PREFIX_FILE_INPUT_WIDGET = "fileHandleInputWidget";

	/**
	 * Used to ensure each new instance of this widget has its own ID. This is important because the ID
	 * is used when interacting with the actual DOM element.
	 */
	private static long ID_SEQUENCE = 0;

	private Widget widget;

	public interface Binder extends UiBinder<Widget, FileHandleUploadViewImpl> {
	}

	@UiField
	Form form;
	@UiField
	Input fileInput;
	@UiField
	Button uploadbutton;
	@UiField
	Progress progressContainer;
	@UiField
	ProgressBar progressBar;
	@UiField
	Alert alert;
	@UiField
	Span uploadedFileNameField;

	@Inject
	public FileHandleUploadViewImpl(Binder binder) {
		widget = binder.createAndBindUi(this);
		// Create a unique for each new instance.
		this.fileInput.getElement().setId(PREFIX_FILE_INPUT_WIDGET + ID_SEQUENCE++);
	}

	@Override
	public Widget asWidget() {
		return widget;
	}

	@Override
	public HandlerRegistration addAttachHandler(Handler handler) {
		return widget.addAttachHandler(handler);
	}

	@Override
	public boolean isAttached() {
		return widget.isAttached();
	}

	@Override
	public void setPresenter(final Presenter presenter) {
		// when a file is selected notify the presenter
		this.fileInput.addChangeHandler(new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent event) {
				presenter.onFileSelected();
			}
		});

		this.uploadbutton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				// When they press the button trigger the input box
				fileInput.getElement().<InputElement>cast().click();
			}
		});

	}

	@Override
	public void setUploadedFileText(String text) {
		uploadedFileNameField.setText(text);
	}

	@Override
	public String getInputId() {
		return fileInput.getElement().getId();
	}

	@Override
	public void updateProgress(double currentProgress, String progressText) {
		progressBar.setPercent(currentProgress);
		progressBar.setText(progressText);
	}

	@Override
	public void showProgress(boolean visible) {
		progressContainer.setVisible(visible);
	}

	@Override
	public void resetForm() {
		this.form.reset();
		setUploadedFileText("");
	}

	@Override
	public void setInputEnabled(boolean enabled) {
		this.uploadbutton.setEnabled(enabled);
	}

	@Override
	public void showError(String error) {
		this.alert.setVisible(true);
		this.alert.setText(error);
	}

	@Override
	public void hideError() {
		this.alert.setVisible(false);
	}

	@Override
	public void setButtonText(String buttonText) {
		this.uploadbutton.setText(buttonText);
	}

	@Override
	public void allowMultipleFileUpload(boolean enabled) {
		if (enabled) {
			this.fileInput.getElement().setAttribute("multiple", null);
		} else {
			this.fileInput.getElement().removeAttribute("multiple");
		}
	}

	@Override
	public void fireEvent(GwtEvent<?> event) {
		widget.fireEvent(event);
	}

}
