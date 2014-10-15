package org.sagebionetworks.web.unitclient.widget.table.modal.upload;

import static org.mockito.Matchers.any;
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
import org.sagebionetworks.repo.model.table.TableEntity;
import org.sagebionetworks.repo.model.table.UploadToTablePreviewRequest;
import org.sagebionetworks.repo.model.table.UploadToTablePreviewResult;
import org.sagebionetworks.repo.model.table.UploadToTableRequest;
import org.sagebionetworks.repo.model.table.UploadToTableResult;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.widget.table.modal.upload.ContentTypeDelimiter;
import org.sagebionetworks.web.client.widget.table.modal.upload.ModalPage.ModalPresenter;
import org.sagebionetworks.web.client.widget.table.modal.upload.UploadCSVPreviewPageImpl;
import org.sagebionetworks.web.client.widget.table.modal.upload.UploadCSVPreviewPageView;
import org.sagebionetworks.web.client.widget.table.modal.upload.UploadPreviewWidget;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;
import org.sagebionetworks.web.unitclient.widget.asynch.JobTrackingWidgetStub;

import com.google.gwt.user.client.rpc.AsyncCallback;

public class UploadCSVConfigurationPageImplTest {
	
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
		jobTrackingWidgetStub = new JobTrackingWidgetStub();
		mockPresenter = Mockito.mock(ModalPresenter.class);
		page = new UploadCSVPreviewPageImpl(mockView, mockSynapseClient, mockUploadPreviewWidget, jobTrackingWidgetStub);
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
		verify(mockView).setTableName(fileName);
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
		verify(mockView).showSpinner(UploadCSVPreviewPageImpl.CREATING_TABLE_COLUMNS);
		verify(mockPresenter).setLoading(true);
		verify(mockView).hideSpinner();
		verify(mockPresenter).setErrorMessage(error);
	}
	
	@Test
	public void testOnPrimaryFailedColumnCreate(){
		String error = "an error";
		AsyncMockStubber.callFailureWith(new IllegalArgumentException(error)).when(mockSynapseClient).createTableColumns(any(List.class), any(AsyncCallback.class));
		List<ColumnModel> columns = new ArrayList<ColumnModel>();
		when(mockUploadPreviewWidget.getCurrentModel()).thenReturn(columns);
		page.setModalPresenter(mockPresenter);
		reset(mockView);
		reset(mockPresenter);
		// the test call
		page.onPrimary();
		verify(mockView).showSpinner(UploadCSVPreviewPageImpl.CREATING_TABLE_COLUMNS);
		verify(mockPresenter).setLoading(true);
		verify(mockView).hideSpinner();
		verify(mockPresenter).setErrorMessage(error);
	}
	
	@Test
	public void testOnPrimarySuccess(){
		// This test does a full success train.
		TableEntity table = new TableEntity();
		List<ColumnModel> columns = new ArrayList<ColumnModel>();
		AsyncMockStubber.callSuccessWith(columns).when(mockSynapseClient).createTableColumns(any(List.class), any(AsyncCallback.class));
		AsyncMockStubber.callSuccessWith(table).when(mockSynapseClient).createTableEntity(any(TableEntity.class), any(AsyncCallback.class));
		when(mockUploadPreviewWidget.getCurrentModel()).thenReturn(columns);
		when(mockUploadPreviewWidget.getUploadRequest()).thenReturn(new UploadToTableRequest());
		page.setModalPresenter(mockPresenter);
		reset(mockView);
		reset(mockPresenter);
		jobTrackingWidgetStub.setResponse(new UploadToTableResult());
		// the test call
		page.onPrimary();
		verify(mockView).showSpinner(UploadCSVPreviewPageImpl.CREATING_TABLE_COLUMNS);
		verify(mockPresenter).setLoading(true);
		// should start the create column call
		verify(mockView).showSpinner(UploadCSVPreviewPageImpl.CREATING_THE_TABLE);
		verify(mockView).hideSpinner();
		verify(mockPresenter).onTableCreated(table);
	}
}
