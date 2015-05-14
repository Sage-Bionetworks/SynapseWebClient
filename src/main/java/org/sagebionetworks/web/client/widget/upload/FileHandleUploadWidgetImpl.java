package org.sagebionetworks.web.client.widget.upload;

import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.utils.CallbackP;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class FileHandleUploadWidgetImpl implements FileHandleUploadWidget,  FileHandleUploadView.Presenter {

	private FileHandleUploadView view;
	private MultipartUploader multipartUploader;
	private CallbackP<String> finishedUploadingCallback;
	private Callback startedUploadingCallback;
	private FileMetadata[] fileMeta;
	
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
	public void configure(String buttonText, CallbackP<String> finishedUploadingCallback) {
		configure(buttonText, null, finishedUploadingCallback);
	}
	
	@Override
	public void configure(String buttonText, Callback startedUploadingCallback,
			CallbackP<String> finishedUploadingCallback) {
		this.finishedUploadingCallback = finishedUploadingCallback;
		this.startedUploadingCallback = startedUploadingCallback;
		view.showProgress(false);
		view.hideError();
		view.setButtonText(buttonText);
		
	}

	@Override
	public void onFileSelected() {
		start();
		view.showError("Upload in progress");
		
	}
	
	public void start() {
		if (startedUploadingCallback != null) {
			startedUploadingCallback.invoke();
		}
		fileMeta = multipartUploader.getSelectedFileMetadata(view.getInputId());
		view.updateProgress(1, "1%");
		view.showProgress(true);
		view.setInputEnabled(false);
		view.hideError();
		doMultipartUpload();
	}
	
	@Override
	public void reset() {
		fileMeta = null;
		view.setInputEnabled(true);
		view.showProgress(false);
		view.hideError();
	}
	
	@Override
	public FileMetadata[] getFileMetadata() {
		return fileMeta;
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
						finishedUploadingCallback.invoke(fileHandleId);
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
				}, null);
	}

}
