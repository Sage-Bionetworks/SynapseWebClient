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
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.sagebionetworks.repo.model.table.ColumnModel;
import org.sagebionetworks.repo.model.table.ColumnType;
import org.sagebionetworks.repo.model.table.CsvTableDescriptor;
import org.sagebionetworks.repo.model.table.TableEntity;
import org.sagebionetworks.repo.model.table.UploadToTableRequest;
import org.sagebionetworks.repo.model.table.UploadToTableResult;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.widget.asynch.JobTrackingWidget;
import org.sagebionetworks.web.client.widget.table.KeyboardNavigationHandler;
import org.sagebionetworks.web.client.widget.table.modal.upload.ContentTypeDelimiter;
import org.sagebionetworks.web.client.widget.table.modal.upload.UploadCSVFinishPageImpl;
import org.sagebionetworks.web.client.widget.table.modal.upload.UploadCSVFinishPageView;
import org.sagebionetworks.web.client.widget.table.modal.upload.UploadRequestUtils;
import org.sagebionetworks.web.client.widget.table.modal.wizard.ModalPage.ModalPresenter;
import org.sagebionetworks.web.client.widget.table.v2.schema.ColumnModelTableRowEditor;
import org.sagebionetworks.web.client.widget.table.v2.schema.ColumnModelView;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;
import org.sagebionetworks.web.unitclient.widget.asynch.JobTrackingWidgetStub;
import org.sagebionetworks.web.unitclient.widget.table.v2.schema.ColumnModelTableRowEditorStub;

import com.google.gwt.user.client.rpc.AsyncCallback;

public class UploadCSVFinalPageImplTest {

	ColumnModelView mockEditor;
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
	public void before() {
		mockView = Mockito.mock(UploadCSVFinishPageView.class);
		mockSynapseClient = Mockito.mock(SynapseClientAsync.class);
		mockPortalGinInjector = Mockito.mock(PortalGinInjector.class);
		mockKeyboardNavigationHandler = Mockito
				.mock(KeyboardNavigationHandler.class);
		jobTrackingWidgetStub = new JobTrackingWidgetStub();
		mockPresenter = Mockito.mock(ModalPresenter.class);
		page = new UploadCSVFinishPageImpl(mockView, mockSynapseClient,
				mockPortalGinInjector, jobTrackingWidgetStub,
				mockKeyboardNavigationHandler);

		ColumnModel one = new ColumnModel();
		one.setMaximumSize(100L);
		one.setColumnType(ColumnType.STRING);
		one.setName("a column name");
		ColumnModel two = new ColumnModel();
		two.setMaximumSize(null);
		two.setColumnType(ColumnType.STRING);
		two.setName(null);
		schema = Arrays.asList(one, two);
		request = new UploadToTableRequest();
		request.setCsvTableDescriptor(new CsvTableDescriptor());
		request.getCsvTableDescriptor().setSeparator(",");
		type = ContentTypeDelimiter.CSV;
		fileName = "testing.csv";
		parentId = "syn123";
		fileHandleId = "456";
		when(mockPortalGinInjector.createNewColumnModelTableRowEditor()).thenAnswer(new Answer<ColumnModelTableRowEditor>() {
			@Override
			public ColumnModelTableRowEditor answer(InvocationOnMock invocation)
					throws Throwable {
				return new ColumnModelTableRowEditorStub();
			}
		});
	}

	@Test
	public void testOnPrimaryFailedColumnCreate() {
		page.configure(fileName, parentId, request, schema);
		String error = "an error";
		AsyncMockStubber.callFailureWith(new IllegalArgumentException(error))
				.when(mockSynapseClient)
				.createTableColumns(any(List.class), any(AsyncCallback.class));
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
	public void testOnPrimarySuccess() {
		page.configure(fileName, parentId, request, schema);
		// This test does a full success train.
		TableEntity table = new TableEntity();
		List<ColumnModel> columns = new ArrayList<ColumnModel>();
		AsyncMockStubber.callSuccessWith(columns).when(mockSynapseClient)
				.createTableColumns(any(List.class), any(AsyncCallback.class));
		AsyncMockStubber
				.callSuccessWith(table)
				.when(mockSynapseClient)
				.createTableEntity(any(TableEntity.class),
						any(AsyncCallback.class));
		page.setModalPresenter(mockPresenter);
		reset(mockView);
		reset(mockPresenter);
		jobTrackingWidgetStub.setResponse(new UploadToTableResult());
		// the test call
		page.onPrimary();
		verify(mockPresenter).setLoading(true);
		verify(mockPresenter).onFinished();
	}

	@Test
	public void testPreProcessColumns() {
		page.configure(fileName, parentId, request, schema);
		// the call under test
		List<ColumnModel> results = page.getCurrentSchema();
		// expected
		ColumnModel oneExpected = new ColumnModel();
		oneExpected.setColumnType(ColumnType.STRING);
		// size should be increased by the buffer
		oneExpected
				.setMaximumSize((long) (100 + (100 * UploadCSVFinishPageImpl.COLUMN_SIZE_BUFFER)));
		oneExpected.setName("a column name");
		ColumnModel twoExpected = new ColumnModel();
		twoExpected.setColumnType(ColumnType.STRING);
		twoExpected.setMaximumSize(null);
		twoExpected.setName("col2");
		List<ColumnModel> expected = Arrays.asList(oneExpected, twoExpected);
		assertEquals(expected, results);
	}
	
	@Test
	public void testPreProcessUploadRequestFirstLineHaderTrueLinesToSkipNull(){
		request = new UploadToTableRequest();
		request.setCsvTableDescriptor(new CsvTableDescriptor());
		request.getCsvTableDescriptor().setSeparator(",");
		request.getCsvTableDescriptor().setIsFirstLineHeader(Boolean.TRUE);
		request.setLinesToSkip(null);
		page.configure(fileName, parentId, request, schema);
		
		UploadToTableRequest expected = UploadRequestUtils.cloneUploadToTableRequest(request);
		expected.getCsvTableDescriptor().setIsFirstLineHeader(Boolean.FALSE);
		expected.setLinesToSkip(1L);
		
		UploadToTableRequest result = page.getUploadToTableRequest();
		assertEquals(expected, result);
	}
	
	@Test
	public void testPreProcessUploadRequestFirstLineHaderTrueLinesToSkipExists(){
		request = new UploadToTableRequest();
		request.setCsvTableDescriptor(new CsvTableDescriptor());
		request.getCsvTableDescriptor().setSeparator(",");
		request.getCsvTableDescriptor().setIsFirstLineHeader(Boolean.TRUE);
		long startLinesToSkip = 3;
		request.setLinesToSkip(startLinesToSkip);
		page.configure(fileName, parentId, request, schema);
		
		UploadToTableRequest expected = UploadRequestUtils.cloneUploadToTableRequest(request);
		expected.getCsvTableDescriptor().setIsFirstLineHeader(Boolean.FALSE);
		expected.setLinesToSkip(startLinesToSkip+1);
		
		UploadToTableRequest result = page.getUploadToTableRequest();
		assertEquals(expected, result);
	}
	
	@Test
	public void testPreProcessUploadRequestFirstLineHaderFalsLinesToSkipNull(){
		request = new UploadToTableRequest();
		request.setCsvTableDescriptor(new CsvTableDescriptor());
		request.getCsvTableDescriptor().setSeparator(",");
		request.getCsvTableDescriptor().setIsFirstLineHeader(Boolean.FALSE);
		request.setLinesToSkip(null);
		page.configure(fileName, parentId, request, schema);
		
		UploadToTableRequest expected = UploadRequestUtils.cloneUploadToTableRequest(request);
		
		UploadToTableRequest result = page.getUploadToTableRequest();
		assertEquals(expected, result);
	}
	
}
