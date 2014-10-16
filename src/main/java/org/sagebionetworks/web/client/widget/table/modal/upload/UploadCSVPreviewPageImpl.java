package org.sagebionetworks.web.client.widget.table.modal.upload;

import java.util.List;

import org.sagebionetworks.repo.model.asynch.AsynchronousResponseBody;
import org.sagebionetworks.repo.model.table.ColumnModel;
import org.sagebionetworks.repo.model.table.CsvTableDescriptor;
import org.sagebionetworks.repo.model.table.UploadToTablePreviewRequest;
import org.sagebionetworks.repo.model.table.UploadToTablePreviewResult;
import org.sagebionetworks.repo.model.table.UploadToTableRequest;
import org.sagebionetworks.web.client.widget.asynch.AsynchronousProgressHandler;
import org.sagebionetworks.web.client.widget.asynch.JobTrackingWidget;
import org.sagebionetworks.web.shared.asynch.AsynchType;

import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class UploadCSVPreviewPageImpl implements UploadCSVPreviewPage, UploadCSVPreviewPageView.Presenter{
	
	public static final String CREATING_TABLE_COLUMNS = "Creating table columns...";
	public static final String CREATING_THE_TABLE = "Creating the table...";
	public static final String ANALYZING_FILE = "Analyzing file...";
	public static final String APPLYING_CSV_TO_THE_TABLE = "Applying CSV to the Table...";
	public static final String PREPARING_A_PREVIEW = "Preparing a preview...";
	public static final String CREATE = "Create";
	// Injected dependencies.
	UploadCSVPreviewPageView view;
	UploadPreviewWidget uploadPreviewWidget;
	JobTrackingWidget jobTrackingWidget;
	UploadCSVFinalPage nextPage;

	// dynamic data fields
	ContentTypeDelimiter type;
	String fileName;
	String parentId;
	String fileHandleId;
	ModalPresenter presenter;
	List<ColumnModel> suggestedSchema;
	UploadToTableRequest uploadTableRequest;
	
	@Inject
	public UploadCSVPreviewPageImpl(UploadCSVPreviewPageView view, UploadPreviewWidget uploadPreviewWidget, JobTrackingWidget jobTrackingWidget, UploadCSVFinalPage nextPage){
		this.view = view;
		this.uploadPreviewWidget = uploadPreviewWidget;
		this.jobTrackingWidget = jobTrackingWidget;
		this.nextPage = nextPage;
		view.setPresenter(this);
		this.view.setPreviewWidget(this.uploadPreviewWidget.asWidget());
		this.view.setTrackingWidget(this.jobTrackingWidget.asWidget());
	}

	@Override
	public Widget asWidget() {
		return this.view.asWidget();
	}

	@Override
	public void configure(ContentTypeDelimiter type, String fileName, String parentId, String fileHandleId) {
		this.type = type;
		this.fileName = fileName;
		this.parentId = parentId;
		this.fileHandleId = fileHandleId;
	}

	@Override
	public void onPrimary() {
		this.nextPage.configure(fileName, parentId, uploadTableRequest, suggestedSchema);
		this.presenter.setNextActivePage(this.nextPage);
	}


	@Override
	public void setModalPresenter(final ModalPresenter presenter) {
		this.presenter = presenter;
		this.view.setPreviewVisible(false);
		this.view.setTrackerVisible(true);
		this.presenter.setPrimaryButtonText(CREATE);
		this.presenter.setInstructionMessage(PREPARING_A_PREVIEW);
		this.presenter.setLoading(true);
		// Setup the preview request
		final UploadToTablePreviewRequest previewRequest = new UploadToTablePreviewRequest();
		CsvTableDescriptor descriptor = new CsvTableDescriptor();
		descriptor.setSeparator(type.getDelimiter());
		previewRequest.setCsvTableDescriptor(descriptor);
		previewRequest.setUploadFileHandleId(fileHandleId);
		previewRequest.setDoFullFileScan(true);
		// Start the job
		jobTrackingWidget.startAndTrackJob(ANALYZING_FILE, false, AsynchType.TableCSVUploadPreview, previewRequest, new AsynchronousProgressHandler() {
			
			@Override
			public void onFailure(Throwable failure) {
				presenter.setErrorMessage(failure.getMessage());
			}
			
			@Override
			public void onComplete(AsynchronousResponseBody response) {
				previewCreated(previewRequest, (UploadToTablePreviewResult) response);
			}
			
			@Override
			public void onCancel() {
				presenter.onCancel();
			}
		});
	}
	
	private void previewCreated(UploadToTablePreviewRequest previewRequest, UploadToTablePreviewResult results){
		this.uploadTableRequest = new UploadToTableRequest();
		this.uploadTableRequest.setCsvTableDescriptor(previewRequest.getCsvTableDescriptor());
		this.uploadTableRequest.setLinesToSkip(previewRequest.getLinesToSkip());
		this.uploadTableRequest.setUploadFileHandleId(this.fileHandleId);
		this.suggestedSchema = results.getSuggestedColumns();
		this.presenter.setInstructionMessage("");
		this.view.setTrackerVisible(false);
		this.uploadPreviewWidget.configure(previewRequest, results);
		this.view.setPreviewVisible(true);
		this.presenter.setLoading(false);
	}
	

}
