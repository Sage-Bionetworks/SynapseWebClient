package org.sagebionetworks.web.client.widget.upload;

import org.gwtbootstrap3.client.ui.Form;
import org.gwtbootstrap3.client.ui.Input;
import org.gwtbootstrap3.client.ui.Progress;
import org.gwtbootstrap3.client.ui.ProgressBar;

import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class FileInputViewImpl implements FileInputView {
	
	@UiField
	Form form;
	@UiField
	Input fileInput;
	@UiField
	Progress progressContainer;
	@UiField
	ProgressBar progressBar;
	
	Widget widget;
	
	public interface Binder extends UiBinder<Widget, FileInputViewImpl> {	}
	
	@Inject
	public FileInputViewImpl(Binder binder){
		this.widget = binder.createAndBindUi(this);
	}

	@Override
	public void setPresenter(Presenter presenter) {
		
	}

	@Override
	public Widget asWidget() {
		return widget;
	}

	@Override
	public Input getInputElement() {
		return fileInput;
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
	}

	@Override
	public void setInputEnabled(boolean enabled) {
		this.fileInput.setEnabled(enabled);
	}

}
