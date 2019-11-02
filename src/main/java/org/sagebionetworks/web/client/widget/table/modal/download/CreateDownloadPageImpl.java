package org.sagebionetworks.web.client.widget.table.modal.download;

import java.util.List;
import org.sagebionetworks.repo.model.asynch.AsynchronousResponseBody;
import org.sagebionetworks.repo.model.table.CsvTableDescriptor;
import org.sagebionetworks.repo.model.table.DownloadFromTableRequest;
import org.sagebionetworks.repo.model.table.DownloadFromTableResult;
import org.sagebionetworks.repo.model.table.FacetColumnRequest;
import org.sagebionetworks.web.client.widget.asynch.AsynchronousProgressHandler;
import org.sagebionetworks.web.client.widget.asynch.JobTrackingWidget;
import org.sagebionetworks.web.shared.asynch.AsynchType;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class CreateDownloadPageImpl implements CreateDownloadPage {

	public static final String CREATING_THE_FILE = "Creating the file...";
	public static final String NEXT = "Next";
	// Injected dependencies.
	CreateDownloadPageView view;
	JobTrackingWidget jobTrackingWidget;
	DownloadFilePage nextPage;

	// Configured values
	String sql;
	List<FacetColumnRequest> selectedFacets;
	ModalPresenter presenter;
	String tableId;

	@Inject
	public CreateDownloadPageImpl(CreateDownloadPageView view, JobTrackingWidget jobTrackingWidget, DownloadFilePage nextPage) {
		super();
		this.view = view;
		this.jobTrackingWidget = jobTrackingWidget;
		this.nextPage = nextPage;
		this.view.addTrackerWidget(jobTrackingWidget);
	}

	@Override
	public void onPrimary() {
		presenter.setLoading(true);
		view.setTrackerVisible(true);

		DownloadFromTableRequest request = getDownloadFromTableRequest();
		request.setIncludeEntityEtag(true);
		request.setEntityId(this.tableId);

		this.jobTrackingWidget.startAndTrackJob(CREATING_THE_FILE, false, AsynchType.TableCSVDownload, request, new AsynchronousProgressHandler() {

			@Override
			public void onFailure(Throwable failure) {
				presenter.setErrorMessage(failure.getMessage());
			}

			@Override
			public void onComplete(AsynchronousResponseBody response) {
				DownloadFromTableResult results = (DownloadFromTableResult) response;
				setResults(results);
			}

			@Override
			public void onCancel() {
				presenter.onCancel();
			}
		});

	}

	/**
	 * Called when the file is created and ready for download.
	 * 
	 * @param results
	 */
	private void setResults(DownloadFromTableResult results) {
		this.nextPage.configure(results.getResultsFileHandleId());
		this.presenter.setNextActivePage(this.nextPage);
	}

	@Override
	public void setModalPresenter(ModalPresenter presenter) {
		this.presenter = presenter;
		this.presenter.setPrimaryButtonText(NEXT);
		view.setFileType(FileType.CSV);
		view.setIncludeHeaders(true);
		view.setIncludeRowMetadata(true);
		view.setTrackerVisible(false);
	}

	@Override
	public Widget asWidget() {
		return view.asWidget();
	}

	@Override
	public void configure(String sql, String tableId, List<FacetColumnRequest> selectedFacets) {
		this.sql = sql;
		this.tableId = tableId;
		this.selectedFacets = selectedFacets;
	}

	/**
	 * Extract the request.
	 * 
	 * @return
	 */
	public DownloadFromTableRequest getDownloadFromTableRequest() {
		DownloadFromTableRequest request = new DownloadFromTableRequest();
		CsvTableDescriptor descriptor = new CsvTableDescriptor();
		descriptor.setSeparator(view.getFileType().getSeparator());
		request.setCsvTableDescriptor(descriptor);
		request.setWriteHeader(view.getIncludeHeaders());
		request.setIncludeRowIdAndRowVersion(view.getIncludeRowMetadata());
		request.setSql(this.sql);
		request.setSelectedFacets(selectedFacets);
		return request;
	}

}
