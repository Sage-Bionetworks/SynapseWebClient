package org.sagebionetworks.web.client.widget.upload;

import org.sagebionetworks.web.client.utils.CallbackP;

import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class FileHandleUploadWidgetImpl implements FileHandleUploadWidget,  FileHandleUploadView.Presenter {

	private FileHandleUploadView view;
	private MultipartUploader multipartUploader;
	private CallbackP<String> callback;
	
	@Inject
	public FileHandleUploadWidgetImpl(FileHandleUploadView view, MultipartUploader multipartUploader) {
		super();
		this.view = view;
		this.multipartUploader = multipartUploader;
		this.view.setPresenter(this);
	}

	@Override
	public Widget asWidget() {
		return this.view.asWidget();
	}


	@Override
	public void configure(String buttonText, CallbackP<String> callback) {
		this.callback = callback;
		view.showProgress(false);
		view.hideError();
		view.setButtonText(buttonText);
	}


	@Override
	public void onFileSelected() {
		view.updateProgress(1, "1%");
		view.showProgress(true);
		view.setInputEnabled(false);
		view.hideError();
		doMultipartUpload();
	}

	
	private void doMultipartUpload() {
		// The uploader does the real work
		multipartUploader.uploadSelectedFile(view.getInputId(),
				new ProgressingFileUploadHandler() {
					@Override
					public void uploadSuccess(String fileHandleId) {
						// Set the view at 100%
						view.updateProgress(100, "100%");
						view.showProgress(false);
						view.setInputEnabled(true);
						callback.invoke(fileHandleId);
					}

					@Override
					public void uploadFailed(String error) {
						view.showProgress(false);
						view.setInputEnabled(true);
						view.showError(error);
					}

					@Override
					public void updateProgress(double currentProgress,
							String progressText) {
						view.updateProgress(currentProgress*100, progressText);
					}
				});
	}
}
