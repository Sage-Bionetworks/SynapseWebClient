package org.sagebionetworks.web.client.widget.upload;

import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

/**
 * All business logic for this widget can be found here.
 * 
 * @author jhill
 *
 *
 */
@Deprecated // Use org.sagebionetworks.web.client.widget.upload.FileHandleUploadWidget
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
	public void uploadSelectedFile(final FileUploadHandler handler) {
		this.handler = handler;
		view.updateProgress(1, "1%");
		view.showProgress(true);
		view.setInputEnabled(false);
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
				}, null);
	}

	@Override
	public void reset() {
		view.showProgress(false);
		view.setInputEnabled(true);
		view.resetForm();
	}

	@Override
	public FileMetadata[] getSelectedFileMetadata() {
		return this.multipartUploader.getSelectedFileMetadata(view.getInputId());
	}

}
