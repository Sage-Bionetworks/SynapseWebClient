package org.sagebionetworks.web.unitclient.widget.table.modal.upload;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.sagebionetworks.repo.model.table.ColumnModel;
import org.sagebionetworks.repo.model.table.ColumnType;
import org.sagebionetworks.repo.model.table.CsvTableDescriptor;
import org.sagebionetworks.repo.model.table.TableEntity;
import org.sagebionetworks.repo.model.table.UploadToTablePreviewRequest;
import org.sagebionetworks.repo.model.table.UploadToTablePreviewResult;
import org.sagebionetworks.repo.model.table.UploadToTableRequest;
import org.sagebionetworks.repo.model.table.UploadToTableResult;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.widget.asynch.JobTrackingWidget;
import org.sagebionetworks.web.client.widget.table.KeyboardNavigationHandler;
import org.sagebionetworks.web.client.widget.table.modal.upload.ContentTypeDelimiter;
import org.sagebionetworks.web.client.widget.table.modal.upload.UploadCSVFinishPage;
import org.sagebionetworks.web.client.widget.table.modal.upload.UploadCSVFinishPageImpl;
import org.sagebionetworks.web.client.widget.table.modal.upload.UploadCSVFinishPageView;
import org.sagebionetworks.web.client.widget.table.modal.upload.UploadCSVPreviewPageImpl;
import org.sagebionetworks.web.client.widget.table.modal.upload.UploadCSVPreviewPageView;
import org.sagebionetworks.web.client.widget.table.modal.upload.UploadPreviewWidget;
import org.sagebionetworks.web.client.widget.table.modal.upload.UploadPreviewWidgetImpl;
import org.sagebionetworks.web.client.widget.table.modal.upload.ModalPage.ModalPresenter;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;
import org.sagebionetworks.web.unitclient.widget.asynch.JobTrackingWidgetStub;

import com.google.gwt.user.client.rpc.AsyncCallback;

public class UploadCSVFinalPageImplTest {

	UploadCSVFinishPageView mockView;
	PortalGinInjector mockPortalGinInjector;
	JobTrackingWidget jobTrackingWidget;
	KeyboardNavigationHandler mockKeyboardNavigationHandler;
	SynapseClientAsync mockSynapseClient;
	JobTrackingWidgetStub jobTrackingWidgetStub;
	ModalPresenter mockPresenter;
	ContentTypeDelimiter type;
	String fileName;
	String parentId;
	String fileHandleId;
	UploadToTableRequest request;
	List<ColumnModel> schema;
	UploadCSVFinishPageImpl page;
	
	@Before
	public void before(){
		mockView = Mockito.mock(UploadCSVFinishPageView.class);
		mockSynapseClient = Mockito.mock(SynapseClientAsync.class);
		mockPortalGinInjector = Mockito.mock(PortalGinInjector.class);
		mockKeyboardNavigationHandler = Mockito.mock(KeyboardNavigationHandler.class);
		jobTrackingWidgetStub = new JobTrackingWidgetStub();
		mockPresenter = Mockito.mock(ModalPresenter.class);
		page = new UploadCSVFinishPageImpl(mockView, mockSynapseClient, mockPortalGinInjector, jobTrackingWidgetStub, mockKeyboardNavigationHandler);

		ColumnModel one = new ColumnModel();
		one.setMaximumSize(100L);
		one.setColumnType(ColumnType.STRING);
		ColumnModel two = new ColumnModel();
		two.setMaximumSize(null);
		two.setColumnType(ColumnType.STRING);
		schema = Arrays.asList(one, two);
		request = new UploadToTableRequest();
		request.setCsvTableDescriptor(new CsvTableDescriptor());
		request.getCsvTableDescriptor().setSeparator(",");
		type = ContentTypeDelimiter.CSV;
		fileName = "testing.csv";
		parentId = "syn123";
		fileHandleId = "456";
		page.configure(fileName, parentId, request, schema);
	}
	
	@Test
	public void testOnPrimaryFailedColumnCreate(){
		String error = "an error";
		AsyncMockStubber.callFailureWith(new IllegalArgumentException(error)).when(mockSynapseClient).createTableColumns(any(List.class), any(AsyncCallback.class));
		List<ColumnModel> columns = new ArrayList<ColumnModel>();
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
		page.setModalPresenter(mockPresenter);
		reset(mockView);
		reset(mockPresenter);
		jobTrackingWidgetStub.setResponse(new UploadToTableResult());
		// the test call
		page.onPrimary();
		verify(mockPresenter).setLoading(true);;
		verify(mockPresenter).onTableCreated(table);
	}
	
	@Test
	public void testPreProcessColumns(){	
		// the call under test
		List<ColumnModel> results = page.getCurrentSchema();
		// expected
		ColumnModel oneExpected = new ColumnModel();
		oneExpected.setColumnType(ColumnType.STRING);
		// size should be increased by the buffer
		oneExpected.setMaximumSize((long)(100+(100*UploadCSVFinishPageImpl.COLUMN_SIZE_BUFFER)));
		ColumnModel twoExpected = new ColumnModel();
		twoExpected.setColumnType(ColumnType.STRING);
		twoExpected.setMaximumSize(null);
		List<ColumnModel> expected = Arrays.asList(oneExpected, twoExpected);
		assertEquals(expected, results);
	}
}
