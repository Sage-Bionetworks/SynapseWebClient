package org.sagebionetworks.web.client.widget.table.modal.upload;

import org.sagebionetworks.repo.model.asynch.AsynchronousResponseBody;
import org.sagebionetworks.repo.model.table.CsvTableDescriptor;
import org.sagebionetworks.repo.model.table.UploadToTablePreviewRequest;
import org.sagebionetworks.repo.model.table.UploadToTablePreviewResult;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.widget.asynch.AsynchronousProgressHandler;
import org.sagebionetworks.web.client.widget.asynch.AsynchronousProgressWidget;
import org.sagebionetworks.web.shared.asynch.AsynchType;

import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class UploadCSVConfigurationWidgetImpl implements UploadCSVConfigurationWidget, UploadCSVConfigurationView.Presenter, AsynchronousProgressHandler{
	
	UploadCSVConfigurationView view;
	SynapseClientAsync synapseClient;
	UploadPreviewWidget uploadPreviewWidget;
	AsynchronousProgressWidget asynchronousProgressWidget;
	PreviewUploadHandler handler;
	
	@Inject
	public UploadCSVConfigurationWidgetImpl(UploadCSVConfigurationView view, SynapseClientAsync synapseClient, UploadPreviewWidget uploadPreviewWidget, AsynchronousProgressWidget asynchronousProgressWidget){
		this.view = view;
		this.synapseClient = synapseClient;
		this.uploadPreviewWidget = uploadPreviewWidget;
		this.asynchronousProgressWidget = asynchronousProgressWidget;
		view.setPresenter(this);
		this.view.setPreviewWidget(this.uploadPreviewWidget.asWidget());
		this.view.setTrackingWidget(this.asynchronousProgressWidget.asWidget());
	}

	@Override
	public Widget asWidget() {
		return this.view.asWidget();
	}

	@Override
	public void configure(ContentTypeDelimiter type, String fileName, String parentId, String fileHandleId, final PreviewUploadHandler handler) {
		this.handler = handler;
		this.handler.setLoading(true);
		this.view.setTableName(fileName);
		this.view.setPreviewVisible(false);
		this.view.setTrackerVisible(true);
		// Setup the privew
		UploadToTablePreviewRequest request = new UploadToTablePreviewRequest();
		CsvTableDescriptor descriptor = new CsvTableDescriptor();
		descriptor.setSeparator(type.getDelimiter());
		request.setCsvTableDescriptor(descriptor);
		request.setUploadFileHandleId(fileHandleId);
		request.setDoFullFileScan(false);
		// Start the job
		asynchronousProgressWidget.startAndTrackJob("Analyzing file...", false, AsynchType.TableCSVUploadPreview, request, this);
	}

	@Override
	public void onCancel() {
		handler.onCancel();
	}

	@Override
	public void onComplete(AsynchronousResponseBody response) {
		UploadToTablePreviewResult results = (UploadToTablePreviewResult) response;
		this.view.setTrackerVisible(false);
		this.uploadPreviewWidget.configure(results);
		this.view.setPreviewVisible(true);
		this.handler.setLoading(false);
	}

	@Override
	public void onFailure(Throwable failure) {
		handler.uploadFailed(failure.getMessage());
	}

}
