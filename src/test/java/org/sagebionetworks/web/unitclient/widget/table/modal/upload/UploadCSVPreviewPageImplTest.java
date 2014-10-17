package org.sagebionetworks.web.unitclient.widget.table.modal.upload;

import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.sagebionetworks.repo.model.table.CsvTableDescriptor;
import org.sagebionetworks.repo.model.table.UploadToTablePreviewRequest;
import org.sagebionetworks.repo.model.table.UploadToTablePreviewResult;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.widget.table.modal.upload.CSVOptionsWidget;
import org.sagebionetworks.web.client.widget.table.modal.upload.ContentTypeDelimiter;
import org.sagebionetworks.web.client.widget.table.modal.upload.ModalPage.ModalPresenter;
import org.sagebionetworks.web.client.widget.table.modal.upload.UploadCSVFinishPage;
import org.sagebionetworks.web.client.widget.table.modal.upload.UploadCSVPreviewPageImpl;
import org.sagebionetworks.web.client.widget.table.modal.upload.UploadCSVPreviewPageView;
import org.sagebionetworks.web.client.widget.table.modal.upload.UploadPreviewWidget;
import org.sagebionetworks.web.unitclient.widget.asynch.JobTrackingWidgetStub;

public class UploadCSVPreviewPageImplTest {
	
	UploadCSVFinishPage mockNextPage;
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
	UploadCSVPreviewPageImpl page;

	
	@Before
	public void before(){
		mockView = Mockito.mock(UploadCSVPreviewPageView.class);
		mockSynapseClient = Mockito.mock(SynapseClientAsync.class);
		mockUploadPreviewWidget = Mockito.mock(UploadPreviewWidget.class);
		mockNextPage = Mockito.mock(UploadCSVFinishPage.class);
		mockCSVOptionsWidget = Mockito.mock(CSVOptionsWidget.class);
		
		jobTrackingWidgetStub = new JobTrackingWidgetStub();
		mockPresenter = Mockito.mock(ModalPresenter.class);
		page = new UploadCSVPreviewPageImpl(mockView, mockUploadPreviewWidget, mockCSVOptionsWidget, jobTrackingWidgetStub, mockNextPage);
		type = ContentTypeDelimiter.CSV;
		fileName = "testing.csv";
		parentId = "syn123";
		fileHandleId = "456";
		page.configure(type, fileName, parentId, fileHandleId);
	}

	@Test
	public void setSetModalPresenterSuccess(){
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
		//expect full scan the first time.
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
	public void setSetModalPresenterFailed(){
		// setup the stub to fail
		String error = "error";
		jobTrackingWidgetStub.setError(new Throwable(error));
		page.setModalPresenter(mockPresenter);
		verify(mockPresenter).setErrorMessage(error);
	}
	
	@Test
	public void setSetModalPresenterCanceled(){
		jobTrackingWidgetStub.setOnCancel(true);
		page.setModalPresenter(mockPresenter);
		verify(mockPresenter).onCancel();
	}

	
}
