package org.sagebionetworks.web.unitclient.widget.entity.file;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.sagebionetworks.web.client.widget.entity.file.AddToDownloadList.DOWNLOAD_ACTION_EVENT_NAME;
import static org.sagebionetworks.web.client.widget.entity.file.AddToDownloadList.FILES_ADDED_TO_DOWNLOAD_LIST_EVENT_NAME;
import static org.sagebionetworks.web.client.widget.entity.file.AddToDownloadList.FILE_SIZE_QUERY_PART_MASK;
import static org.sagebionetworks.web.client.widget.entity.file.AddToDownloadList.PLEASE_LOGIN_TO_ADD_TO_DOWNLOAD_LIST;
import static org.sagebionetworks.web.client.widget.entity.file.AddToDownloadList.ZERO_FILES_IN_FOLDER_MESSAGE;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.sagebionetworks.repo.model.EntityChildrenRequest;
import org.sagebionetworks.repo.model.EntityChildrenResponse;
import org.sagebionetworks.repo.model.file.AddFileToDownloadListRequest;
import org.sagebionetworks.repo.model.file.AddFileToDownloadListResponse;
import org.sagebionetworks.repo.model.file.FileHandleAssociation;
import org.sagebionetworks.repo.model.table.Query;
import org.sagebionetworks.repo.model.table.QueryBundleRequest;
import org.sagebionetworks.repo.model.table.QueryResultBundle;
import org.sagebionetworks.repo.model.table.SumFileSizes;
import org.sagebionetworks.web.client.PopupUtilsView;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.SynapseJavascriptClient;
import org.sagebionetworks.web.client.events.DownloadListUpdatedEvent;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.asynch.AsynchronousProgressHandler;
import org.sagebionetworks.web.client.widget.asynch.AsynchronousProgressWidget;
import org.sagebionetworks.web.client.widget.asynch.InlineAsynchronousProgressViewImpl;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.client.widget.entity.file.AddToDownloadList;
import org.sagebionetworks.web.client.widget.entity.file.AddToDownloadListView;
import org.sagebionetworks.web.client.widget.entity.file.downloadlist.PackageSizeSummary;
import org.sagebionetworks.web.shared.asynch.AsynchType;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.rpc.AsyncCallback;

@RunWith(MockitoJUnitRunner.class)
public class AddToDownloadListTest {

	AddToDownloadList widget;
	@Mock
	AddToDownloadListView mockView;
	@Mock
	EventBus mockEventBus;
	@Mock
	AsynchronousProgressWidget mockAsynchronousProgressWidget;
	@Mock
	InlineAsynchronousProgressViewImpl mockInlineAsynchronousProgressView;
	@Mock
	PopupUtilsView mockPopupUtils;
	@Mock
	Query mockQuery;
	@Mock
	SynapseAlert mockSynAlert;
	@Mock
	SynapseJSNIUtils mockSynapseJSNIUtils;
	@Mock
	PackageSizeSummary mockPackageSizeSummary;
	@Mock
	SynapseJavascriptClient mockJsClient;
	@Mock
	AddFileToDownloadListResponse mockAddFileToDownloadListResponse;
	@Mock
	EntityChildrenResponse mockEntityChildrenResponse;
	@Mock
	QueryResultBundle mockQueryResultBundle;
	@Captor
	ArgumentCaptor<Callback> callbackCaptor;
	@Captor
	ArgumentCaptor<AddFileToDownloadListRequest> requestCaptor;
	@Captor
	ArgumentCaptor<QueryBundleRequest> queryBundleRequestCaptor;
	@Captor
	ArgumentCaptor<AsynchronousProgressHandler> asyncProgressHandlerCaptor;
	@Captor
	ArgumentCaptor<EntityChildrenRequest> entityChildrenRequestCaptor;
	@Mock
	FileHandleAssociation mockFileHandleAssociation;
	@Mock
	SumFileSizes mockSumFileSizes;
	String folderId = "syn10932", queryEntityId = "syn9";
	Long testFolderChildCount = 22L, testFolderSumFileSizesBytes = 7777777L;
	Long queryCount = 4L;
	Long querySumFileSize = 88L;
	@Mock
	AuthenticationController mockAuthController;

	@Before
	public void setUp() throws Exception {
		widget = new AddToDownloadList(mockView, mockAsynchronousProgressWidget, mockInlineAsynchronousProgressView, mockPopupUtils, mockEventBus, mockSynAlert, mockPackageSizeSummary, mockJsClient, mockSynapseJSNIUtils, mockAuthController);
		when(mockEntityChildrenResponse.getTotalChildCount()).thenReturn(testFolderChildCount);
		when(mockEntityChildrenResponse.getSumFileSizesBytes()).thenReturn(testFolderSumFileSizesBytes);
		when(mockQueryResultBundle.getQueryCount()).thenReturn(queryCount);
		// mock query sum file size
		querySumFileSize = 20L;
		when(mockSumFileSizes.getSumFileSizesBytes()).thenReturn(querySumFileSize);
		when(mockQueryResultBundle.getSumFileSizes()).thenReturn(mockSumFileSizes);
		when(mockAuthController.isLoggedIn()).thenReturn(true);
	}

	@Test
	public void testAddQueryToDownloadList() {
		verify(mockAsynchronousProgressWidget).setView(mockInlineAsynchronousProgressView);
		verify(mockInlineAsynchronousProgressView).setProgressMessageVisible(false);

		widget.addToDownloadList(queryEntityId, mockQuery);

		verify(mockAsynchronousProgressWidget).startAndTrackJob(anyString(), eq(false), eq(AsynchType.TableQuery), queryBundleRequestCaptor.capture(), asyncProgressHandlerCaptor.capture());

		// verify query request params
		QueryBundleRequest queryBundleRequest = queryBundleRequestCaptor.getValue();
		assertEquals(mockQuery, queryBundleRequest.getQuery());
		assertEquals(queryEntityId, queryBundleRequest.getEntityId());
		assertEquals(FILE_SIZE_QUERY_PART_MASK, queryBundleRequest.getPartMask());
		verify(mockView).showAsynchronousProgressWidget();

		// simulate success
		AsynchronousProgressHandler queryBundleRequestProgressHandler = asyncProgressHandlerCaptor.getValue();
		queryBundleRequestProgressHandler.onComplete(mockQueryResultBundle);
		verify(mockView, times(3)).hideAll();
		verify(mockPackageSizeSummary).addFiles(queryCount.intValue(), querySumFileSize.doubleValue());
		verify(mockView).showConfirmAdd();

		// simulate user confirms
		widget.onConfirmAddToDownloadList();

		verify(mockView, times(4)).hideAll();
		verify(mockView).setAsynchronousProgressWidget(mockAsynchronousProgressWidget);
		verify(mockAsynchronousProgressWidget).startAndTrackJob(anyString(), eq(false), eq(AsynchType.AddFileToDownloadList), requestCaptor.capture(), asyncProgressHandlerCaptor.capture());

		// verify add file to download list request
		AddFileToDownloadListRequest request = requestCaptor.getValue();
		assertEquals(mockQuery, request.getQuery());

		// simulate completing job
		AsynchronousProgressHandler addFileToDownloadListProgressHandler = asyncProgressHandlerCaptor.getValue();
		addFileToDownloadListProgressHandler.onComplete(mockAddFileToDownloadListResponse);

		verify(mockView, times(5)).hideAll();
		verify(mockView).showSuccess(queryCount.intValue());
		verify(mockSynapseJSNIUtils).sendAnalyticsEvent(DOWNLOAD_ACTION_EVENT_NAME, FILES_ADDED_TO_DOWNLOAD_LIST_EVENT_NAME, queryCount.toString());
		verify(mockEventBus).fireEvent(any(DownloadListUpdatedEvent.class));

		// verify error handling in query bundle progress handler
		Exception ex = new Exception("unable to get query bundle");
		queryBundleRequestProgressHandler.onFailure(ex);
		verify(mockSynAlert).handleException(ex);
		verify(mockView, times(6)).hideAll();
	}

	@Test
	public void testAddFolderToDownloadList() {
		AsyncMockStubber.callSuccessWith(mockEntityChildrenResponse).when(mockJsClient).getEntityChildren(any(EntityChildrenRequest.class), any(AsyncCallback.class));
		widget.addToDownloadList(folderId);

		verify(mockJsClient).getEntityChildren(entityChildrenRequestCaptor.capture(), any(AsyncCallback.class));
		// verify that we're asking for the file size sum and child count
		EntityChildrenRequest entityChildrenRequest = entityChildrenRequestCaptor.getValue();
		assertEquals(folderId, entityChildrenRequest.getParentId());
		assertTrue(entityChildrenRequest.getIncludeTotalChildCount());
		assertTrue(entityChildrenRequest.getIncludeSumFileSizes());

		verify(mockPackageSizeSummary).addFiles(testFolderChildCount.intValue(), testFolderSumFileSizesBytes.doubleValue());
		verify(mockView).showConfirmAdd();

		// simulate user confirmation
		widget.onConfirmAddToDownloadList();

		verify(mockView, times(3)).hideAll();
		verify(mockView).setAsynchronousProgressWidget(mockAsynchronousProgressWidget);
		verify(mockAsynchronousProgressWidget).startAndTrackJob(anyString(), eq(false), eq(AsynchType.AddFileToDownloadList), requestCaptor.capture(), asyncProgressHandlerCaptor.capture());

		// verify request
		AddFileToDownloadListRequest request = requestCaptor.getValue();
		assertEquals(folderId, request.getFolderId());

		// simulate completing job
		asyncProgressHandlerCaptor.getValue().onComplete(mockAddFileToDownloadListResponse);

		verify(mockView).showSuccess(testFolderChildCount.intValue());

		// execute job failure code
		Exception e = new Exception();
		asyncProgressHandlerCaptor.getValue().onFailure(e);

		verify(mockView, times(5)).hideAll();
		verify(mockSynAlert).handleException(e);
	}

	@Test
	public void testAddEmptyFolderToDownloadList() {
		when(mockEntityChildrenResponse.getTotalChildCount()).thenReturn(0L);
		AsyncMockStubber.callSuccessWith(mockEntityChildrenResponse).when(mockJsClient).getEntityChildren(any(EntityChildrenRequest.class), any(AsyncCallback.class));
		widget.addToDownloadList(folderId);

		verify(mockSynAlert).showError(ZERO_FILES_IN_FOLDER_MESSAGE);
	}

	@Test
	public void testAddFolderToDownloadListAsAnonymous() {
		when(mockAuthController.isLoggedIn()).thenReturn(false);

		widget.addToDownloadList(folderId);

		verify(mockSynAlert).showError(PLEASE_LOGIN_TO_ADD_TO_DOWNLOAD_LIST);
	}

	@Test
	public void testAddQueryToDownloadListAsAnonymous() {
		when(mockAuthController.isLoggedIn()).thenReturn(false);

		widget.addToDownloadList(queryEntityId, mockQuery);

		verify(mockSynAlert).showError(PLEASE_LOGIN_TO_ADD_TO_DOWNLOAD_LIST);
	}
}
