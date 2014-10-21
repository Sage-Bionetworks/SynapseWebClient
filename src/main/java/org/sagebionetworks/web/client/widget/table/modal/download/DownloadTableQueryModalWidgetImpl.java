package org.sagebionetworks.web.client.widget.table.modal.download;

import org.sagebionetworks.repo.model.asynch.AsynchronousResponseBody;
import org.sagebionetworks.repo.model.file.FileHandle;
import org.sagebionetworks.repo.model.table.CsvTableDescriptor;
import org.sagebionetworks.repo.model.table.DownloadFromTableRequest;
import org.sagebionetworks.repo.model.table.DownloadFromTableResult;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.widget.asynch.AsynchronousProgressHandler;
import org.sagebionetworks.web.client.widget.asynch.JobTrackingWidget;
import org.sagebionetworks.web.shared.asynch.AsynchType;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class DownloadTableQueryModalWidgetImpl implements DownloadTableQueryModalWidget, DownloadTableQueryModalView.Presenter {
	
	// injected dependencies
	DownloadTableQueryModalView view;
	JobTrackingWidget jobTrackingWidget;
	SynapseClientAsync synapseClient;
	
	// Configured values
	String sql;
	
	@Inject
	public DownloadTableQueryModalWidgetImpl(DownloadTableQueryModalView view, JobTrackingWidget jobTrackingWidget, SynapseClientAsync synapseClient) {
		super();
		this.view = view;
		this.jobTrackingWidget = jobTrackingWidget;
		this.synapseClient = synapseClient;
		this.view.setPresenter(this);
		this.view.addTrackerWidget(jobTrackingWidget);
	}

	@Override
	public void configure(String sql) {
		this.sql = sql;
		view.setFileType(FileType.CSV);
		view.setIncludeHeaders(true);
		view.setIncludeRowMetadata(true);
		view.setErrorMessageVisible(false);
		view.setTrackerVisible(false);
		view.setLoading(false);
	}

	@Override
	public Widget asWidget() {
		return view.asWidget();
	}

	@Override
	public void showDialog() {
		this.view.show();
	}

	@Override
	public void onPrimary() {
		view.setLoading(true);
		view.setTrackerVisible(true);
		view.setErrorMessageVisible(false);
		
		DownloadFromTableRequest request = getDownloadFromTableRequest();
		
		this.jobTrackingWidget.startAndTrackJob("Creating the file...", false, AsynchType.TableCSVDownload, request, new AsynchronousProgressHandler() {
			
			@Override
			public void onFailure(Throwable failure) {
				setError(failure.getMessage());
			}
			
			@Override
			public void onComplete(AsynchronousResponseBody response) {
				DownloadFromTableResult results = (DownloadFromTableResult) response;
				setResults(results);
			}
			
			@Override
			public void onCancel() {
				view.hide();
			}
		});
	}
	
	private void setError(String error){
		view.setLoading(false);
		view.setTrackerVisible(false);
		view.setErrorMessageVisible(true);
		view.setErrorMessage(error);
	}
	
	private void setResults(DownloadFromTableResult results){
		// Get the pre-signed url for the file.
		synapseClient.getFileHandle(results.getResultsFileHandleId(), new AsyncCallback<FileHandle>() {
			
			@Override
			public void onSuccess(FileHandle result) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onFailure(Throwable caught) {
				// TODO Auto-generated method stub
				
			}
		});
	}
	
	/**
	 * Extract the request.
	 * @return
	 */
	public DownloadFromTableRequest getDownloadFromTableRequest (){
		DownloadFromTableRequest request = new DownloadFromTableRequest();
		CsvTableDescriptor descriptor = new CsvTableDescriptor();
		descriptor.setSeparator(view.getFileType().getSeparator());
		request.setCsvTableDescriptor(descriptor);
		request.setWriteHeader(view.getIncludeHeaders());
		request.setIncludeRowIdAndRowVersion(view.getIncludeRowMetadata());
		request.setSql(this.sql);
		return request;
	}

}
