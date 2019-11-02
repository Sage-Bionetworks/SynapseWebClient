package org.sagebionetworks.web.unitclient.widget.table.modal.upload;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.util.Arrays;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.sagebionetworks.repo.model.table.ColumnModel;
import org.sagebionetworks.repo.model.table.CsvTableDescriptor;
import org.sagebionetworks.repo.model.table.UploadToTablePreviewRequest;
import org.sagebionetworks.repo.model.table.UploadToTablePreviewResult;
import org.sagebionetworks.repo.model.table.UploadToTableRequest;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.widget.table.modal.upload.CSVOptionsWidget;
import org.sagebionetworks.web.client.widget.table.modal.upload.ContentTypeDelimiter;
import org.sagebionetworks.web.client.widget.table.modal.upload.UploadCSVAppendPage;
import org.sagebionetworks.web.client.widget.table.modal.upload.UploadCSVFinishPage;
import org.sagebionetworks.web.client.widget.table.modal.upload.UploadCSVPreviewPageImpl;
import org.sagebionetworks.web.client.widget.table.modal.upload.UploadCSVPreviewPageView;
import org.sagebionetworks.web.client.widget.table.modal.upload.UploadPreviewWidget;
import org.sagebionetworks.web.client.widget.table.modal.upload.UploadRequestUtils;
import org.sagebionetworks.web.client.widget.table.modal.wizard.ModalPage.ModalPresenter;
import org.sagebionetworks.web.unitclient.widget.asynch.JobTrackingWidgetStub;

public class UploadCSVPreviewPageImplTest {

	UploadCSVAppendPage mockAppendNextPage;
	UploadCSVFinishPage mockCreateNextPage;
	UploadCSVPreviewPageView mockView;
	CSVOptionsWidget mockCSVOptionsWidget;
	SynapseClientAsync mockSynapseClient;
	UploadPreviewWidget mockUploadPreviewWidget;
	JobTrackingWidgetStub jobTrackingWidgetStub;
	ModalPresenter mockPresenter;
	ContentTypeDelimiter type;
	String fileName;
	String parentId;
	String fileHandleId;
	String tableId;
	UploadToTablePreviewRequest previewRequest;
	UploadToTableRequest uploadRequest;
	UploadToTablePreviewResult uploadPreviewResults;
	ColumnModel column;
	List<ColumnModel> schema;
	UploadCSVPreviewPageImpl page;

	@Before
	public void before() {
		mockView = Mockito.mock(UploadCSVPreviewPageView.class);
		mockSynapseClient = Mockito.mock(SynapseClientAsync.class);
		mockUploadPreviewWidget = Mockito.mock(UploadPreviewWidget.class);
		mockCreateNextPage = Mockito.mock(UploadCSVFinishPage.class);
		mockAppendNextPage = Mockito.mock(UploadCSVAppendPage.class);
		mockCSVOptionsWidget = Mockito.mock(CSVOptionsWidget.class);

		jobTrackingWidgetStub = new JobTrackingWidgetStub();
		mockPresenter = Mockito.mock(ModalPresenter.class);
		page = new UploadCSVPreviewPageImpl(mockView, mockUploadPreviewWidget, mockCSVOptionsWidget, jobTrackingWidgetStub, mockCreateNextPage, mockAppendNextPage);
		type = ContentTypeDelimiter.CSV;
		fileName = "testing.csv";
		parentId = "syn123";
		fileHandleId = "456";
		tableId = "987654";
		column = new ColumnModel();
		column.setId("007");
		previewRequest = new UploadToTablePreviewRequest();
		previewRequest.setUploadFileHandleId(fileHandleId);
		uploadRequest = UploadRequestUtils.createFromPreview(previewRequest);
		uploadPreviewResults = new UploadToTablePreviewResult();
		schema = Arrays.asList(column);
		uploadPreviewResults.setSuggestedColumns(schema);
		when(mockCSVOptionsWidget.getCurrentOptions()).thenReturn(previewRequest);
	}

	@Test
	public void setSetModalPresenterSuccess() {
		page.configure(type, fileName, parentId, fileHandleId, null);
		UploadToTablePreviewResult results = new UploadToTablePreviewResult();
		jobTrackingWidgetStub.setResponse(new UploadToTablePreviewResult());
		page.setModalPresenter(mockPresenter);
		verify(mockView).setPreviewVisible(false);
		verify(mockView).setTrackerVisible(true);
		verify(mockPresenter).setPrimaryButtonText(UploadCSVPreviewPageImpl.NEXT);
		verify(mockPresenter).setInstructionMessage(UploadCSVPreviewPageImpl.PREPARING_A_PREVIEW);
		verify(mockPresenter).setLoading(true);
		// We expect this to be the first request.
		UploadToTablePreviewRequest expectedRequst = new UploadToTablePreviewRequest();
		CsvTableDescriptor expectedDescriptor = new CsvTableDescriptor();
		expectedDescriptor.setSeparator(type.getDelimiter());
		// expect full scan the first time.
		expectedRequst.setDoFullFileScan(true);
		expectedRequst.setUploadFileHandleId(fileHandleId);
		expectedRequst.setCsvTableDescriptor(expectedDescriptor);
		verify(mockUploadPreviewWidget).configure(results);
		verify(mockPresenter).setInstructionMessage("");
		verify(mockPresenter).setLoading(false);
		verify(mockView).setTrackerVisible(false);
		verify(mockView).setPreviewVisible(true);
	}

	@Test
	public void setSetModalPresenterFailed() {
		page.configure(type, fileName, parentId, fileHandleId, null);
		// setup the stub to fail
		String error = "error";
		jobTrackingWidgetStub.setError(new Throwable(error));
		page.setModalPresenter(mockPresenter);
		verify(mockPresenter).setErrorMessage(error);
	}

	@Test
	public void setSetModalPresenterCanceled() {
		page.configure(type, fileName, parentId, fileHandleId, null);
		jobTrackingWidgetStub.setOnCancel(true);
		page.setModalPresenter(mockPresenter);
		verify(mockPresenter).onCancel();
	}

	@Test
	public void testOnPrimaryCreate() {
		// A null tableId indicates a create.
		tableId = null;
		jobTrackingWidgetStub.setResponse(uploadPreviewResults);
		page.configure(type, fileName, parentId, fileHandleId, tableId);
		page.setModalPresenter(mockPresenter);
		page.onPrimary();
		verify(mockCreateNextPage).configure(this.fileName, this.parentId, this.uploadRequest, schema);
		verify(mockPresenter).setNextActivePage(mockCreateNextPage);
	}

	@Test
	public void testOnPrimaryAppend() {
		// a non-null tableId indicates an append
		jobTrackingWidgetStub.setResponse(uploadPreviewResults);
		page.configure(type, fileName, parentId, fileHandleId, tableId);
		page.setModalPresenter(mockPresenter);
		page.onPrimary();
		this.uploadRequest.setTableId(tableId);
		verify(mockAppendNextPage).configure(this.uploadRequest, schema);
		verify(mockPresenter).setNextActivePage(mockAppendNextPage);
	}
}
