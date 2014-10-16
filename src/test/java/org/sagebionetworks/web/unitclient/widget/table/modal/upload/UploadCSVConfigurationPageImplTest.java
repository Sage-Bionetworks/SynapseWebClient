package org.sagebionetworks.web.unitclient.widget.table.modal.upload;

import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.sagebionetworks.repo.model.table.ColumnModel;
import org.sagebionetworks.repo.model.table.CsvTableDescriptor;
import org.sagebionetworks.repo.model.table.UploadToTablePreviewRequest;
import org.sagebionetworks.repo.model.table.UploadToTablePreviewResult;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.widget.table.modal.upload.ContentTypeDelimiter;
import org.sagebionetworks.web.client.widget.table.modal.upload.ModalPage.ModalPresenter;
import org.sagebionetworks.web.client.widget.table.modal.upload.UploadCSVFinalPage;
import org.sagebionetworks.web.client.widget.table.modal.upload.UploadCSVPreviewPageImpl;
import org.sagebionetworks.web.client.widget.table.modal.upload.UploadCSVPreviewPageView;
import org.sagebionetworks.web.client.widget.table.modal.upload.UploadPreviewWidget;
import org.sagebionetworks.web.unitclient.widget.asynch.JobTrackingWidgetStub;

public class UploadCSVConfigurationPageImplTest {
	
	UploadCSVFinalPage mockNextPage;
	UploadCSVPreviewPageView mockView;
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
		mockNextPage = Mockito.mock(UploadCSVFinalPage.class);
		
		jobTrackingWidgetStub = new JobTrackingWidgetStub();
		mockPresenter = Mockito.mock(ModalPresenter.class);
		page = new UploadCSVPreviewPageImpl(mockView, mockUploadPreviewWidget, jobTrackingWidgetStub, mockNextPage);
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
		verify(mockPresenter).setPrimaryButtonText(UploadCSVPreviewPageImpl.CREATE);
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
		verify(mockUploadPreviewWidget).configure(expectedRequst, results);
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
	
	@Test
	public void testOnPrimaryBadColumns(){
		List<ColumnModel> columns = new ArrayList<ColumnModel>();
		// simulate bad columns
		String error = "some error";
		when(mockUploadPreviewWidget.getCurrentModel()).thenThrow(new IllegalArgumentException(error));
		page.setModalPresenter(mockPresenter);
		reset(mockView);
		reset(mockPresenter);
		// the test calls
		page.onPrimary();
		verify(mockPresenter).setLoading(true);
		verify(mockPresenter).setErrorMessage(error);
	}
	
}
