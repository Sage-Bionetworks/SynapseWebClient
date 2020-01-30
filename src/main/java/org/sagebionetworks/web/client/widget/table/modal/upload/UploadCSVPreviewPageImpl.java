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

public class UploadCSVPreviewPageImpl implements UploadCSVPreviewPage, UploadCSVPreviewPageView.Presenter {

	public static final String CREATING_TABLE_COLUMNS = "Creating table columns...";
	public static final String CREATING_THE_TABLE = "Creating the table...";
	public static final String ANALYZING_FILE = "Analyzing file...";
	public static final String APPLYING_CSV_TO_THE_TABLE = "Applying CSV to the Table...";
	public static final String PREPARING_A_PREVIEW = "Preparing a preview...";
	public static final String NEXT = "Next";
	// Injected dependencies.
	UploadCSVPreviewPageView view;
	UploadPreviewWidget uploadPreviewWidget;
	CSVOptionsWidget csvOptionsWidget;
	JobTrackingWidget jobTrackingWidget;
	UploadCSVFinishPage createNextPage;
	UploadCSVAppendPage appendNextPage;

	// dynamic data fields
	ContentTypeDelimiter type;
	String fileName;
	String parentId;
	String fileHandleId;
	String tableId;
	ModalPresenter presenter;
	List<ColumnModel> suggestedSchema;

	@Inject
	public UploadCSVPreviewPageImpl(UploadCSVPreviewPageView view, UploadPreviewWidget uploadPreviewWidget, CSVOptionsWidget csvOptionsWidget, JobTrackingWidget jobTrackingWidget, UploadCSVFinishPage createNextPage, UploadCSVAppendPage appendNextPage) {
		this.view = view;
		this.uploadPreviewWidget = uploadPreviewWidget;
		this.jobTrackingWidget = jobTrackingWidget;
		this.csvOptionsWidget = csvOptionsWidget;
		this.createNextPage = createNextPage;
		this.appendNextPage = appendNextPage;
		view.setPresenter(this);
		this.view.setPreviewWidget(this.uploadPreviewWidget);
		this.view.setTrackingWidget(this.jobTrackingWidget);
		this.view.setCSVOptionsWidget(this.csvOptionsWidget);
	}

	@Override
	public Widget asWidget() {
		return this.view.asWidget();
	}

	@Override
	public void configure(ContentTypeDelimiter type, String fileName, String parentId, String fileHandleId, String tableId) {
		this.type = type;
		this.fileName = fileName;
		this.parentId = parentId;
		this.fileHandleId = fileHandleId;
		this.tableId = tableId;
	}

	@Override
	public void onPrimary() {
		// Get the current options
		UploadToTablePreviewRequest currentOptions = csvOptionsWidget.getCurrentOptions();
		UploadToTableRequest uploadRequest = UploadRequestUtils.createFromPreview(currentOptions);
		if (this.tableId != null) {
			// This is an append.
			uploadRequest.setTableId(this.tableId);
			this.appendNextPage.configure(uploadRequest, suggestedSchema);
			this.presenter.setNextActivePage(this.appendNextPage);
			// For now just execute the next page. This may change in the future.
			this.appendNextPage.onPrimary();
		} else {
			// This is a create
			this.createNextPage.configure(fileName, parentId, uploadRequest, suggestedSchema);
			this.presenter.setNextActivePage(this.createNextPage);
		}
	}

	@Override
	public void setModalPresenter(final ModalPresenter presenter) {
		this.presenter = presenter;
		// Setup the CSV options using what we know about the file.
		this.csvOptionsWidget.configure(createDefaultPreviewRequest(), () -> {
			generatePreview();
		});
		generatePreview();
	}

	/**
	 * Build a default UploadToTablePreviewRequest using what we know about the file.
	 * 
	 * @return
	 */
	private UploadToTablePreviewRequest createDefaultPreviewRequest() {
		UploadToTablePreviewRequest previewRequest = new UploadToTablePreviewRequest();
		CsvTableDescriptor descriptor = new CsvTableDescriptor();
		descriptor.setSeparator(type.getDelimiter());
		previewRequest.setCsvTableDescriptor(descriptor);
		previewRequest.setUploadFileHandleId(fileHandleId);
		previewRequest.setDoFullFileScan(true);
		return previewRequest;
	}

	/**
	 * Generate a new preview using the current options.
	 */
	private void generatePreview() {
		this.view.setPreviewVisible(false);
		this.view.setTrackerVisible(true);
		this.presenter.setPrimaryButtonText(NEXT);
		this.presenter.setInstructionMessage(PREPARING_A_PREVIEW);
		this.presenter.setLoading(true);
		final UploadToTablePreviewRequest previewRequest = csvOptionsWidget.getCurrentOptions();
		// Start the job
		jobTrackingWidget.startAndTrackJob(ANALYZING_FILE, false, AsynchType.TableCSVUploadPreview, previewRequest, new AsynchronousProgressHandler() {

			@Override
			public void onFailure(Throwable failure) {
				presenter.setErrorMessage(failure.getMessage());
			}

			@Override
			public void onComplete(AsynchronousResponseBody response) {
				previewCreated((UploadToTablePreviewResult) response);
			}

			@Override
			public void onCancel() {
				presenter.onCancel();
			}
		});
	}

	/**
	 * Called after a preview is created.
	 * 
	 * @param results
	 */
	private void previewCreated(UploadToTablePreviewResult results) {
		this.suggestedSchema = results.getSuggestedColumns();
		this.presenter.setInstructionMessage("");
		this.view.setTrackerVisible(false);
		this.uploadPreviewWidget.configure(results);
		this.view.setPreviewVisible(true);
		this.presenter.setLoading(false);
	}

}
