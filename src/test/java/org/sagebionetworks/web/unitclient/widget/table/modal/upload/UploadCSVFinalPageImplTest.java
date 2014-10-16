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
import org.sagebionetworks.repo.model.table.TableEntity;
import org.sagebionetworks.repo.model.table.UploadToTableRequest;
import org.sagebionetworks.repo.model.table.UploadToTableResult;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.widget.table.modal.upload.ContentTypeDelimiter;
import org.sagebionetworks.web.client.widget.table.modal.upload.UploadCSVFinalPage;
import org.sagebionetworks.web.client.widget.table.modal.upload.UploadCSVPreviewPageImpl;
import org.sagebionetworks.web.client.widget.table.modal.upload.UploadCSVPreviewPageView;
import org.sagebionetworks.web.client.widget.table.modal.upload.UploadPreviewWidget;
import org.sagebionetworks.web.client.widget.table.modal.upload.ModalPage.ModalPresenter;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;
import org.sagebionetworks.web.unitclient.widget.asynch.JobTrackingWidgetStub;

import com.google.gwt.user.client.rpc.AsyncCallback;

public class UploadCSVFinalPageImplTest {

	
	SynapseClientAsync mockSynapseClient;
	JobTrackingWidgetStub jobTrackingWidgetStub;
	ModalPresenter mockPresenter;
	ContentTypeDelimiter type;
	String fileName;
	String parentId;
	String fileHandleId;
	UploadCSVPreviewPageImpl page;
	
	@Before
	public void before(){

		mockSynapseClient = Mockito.mock(SynapseClientAsync.class);
		jobTrackingWidgetStub = new JobTrackingWidgetStub();
		mockPresenter = Mockito.mock(ModalPresenter.class);

		type = ContentTypeDelimiter.CSV;
		fileName = "testing.csv";
		parentId = "syn123";
		fileHandleId = "456";
		page.configure(type, fileName, parentId, fileHandleId);
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
		verify(mockPresenter).setLoading(true);
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
		verify(mockPresenter).setLoading(true);;
		verify(mockPresenter).onTableCreated(table);
	}
}
