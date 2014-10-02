package org.sagebionetworks.web.client.widget.upload;

import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

/**
 * All business logic for this widget can be found here.
 * 
 * @author jhill
 *
 */
public class FileInputWidgetImpl implements FileInputWidget,
		FileInputView.Presenter {

	FileInputView view;
	MultipartUploader multipartUploader;
	FileUploadHandler handler;
	
	@Inject
	public FileInputWidgetImpl(FileInputView view,
			MultipartUploader multipartUploader) {
		this.view = view;
		this.view.setPresenter(this);
		this.multipartUploader = multipartUploader;
	}

	@Override
	public Widget asWidget() {
		return view.asWidget();
	}

	@Override
	public void uploadSelectedFile() {
		view.setInputEnabled(false);
		view.updateProgress(1, "1%");
		view.showProgress(true);
		view.setInputEnabled(false);
		// The uploader does the real work
		multipartUploader.uploadSelectedFile(view.getInputElement().getId(),
				new ProgressingFileUploadHandler() {
					@Override
					public void uploadSuccess(String fileHandleId) {
						view.showProgress(false);
						view.setInputEnabled(true);
						handler.uploadSuccess(fileHandleId);
					}

					@Override
					public void uploadFailed(String error) {
						view.showProgress(false);
						view.setInputEnabled(true);
						handler.uploadFailed(error);
					}

					@Override
					public void updateProgress(double currentProgress,
							String progressText) {
						view.updateProgress(currentProgress*100, progressText);
					}
				});

	}

	@Override
	public void configure(final FileUploadHandler handler) {
		this.handler = handler;
		view.showProgress(false);
		view.resetForm();
		view.setInputEnabled(true);
	}

}
