package org.sagebionetworks.web.unitclient.widget.entity.file;

import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;

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
import org.sagebionetworks.repo.model.file.DownloadList;
import org.sagebionetworks.repo.model.file.FileHandleAssociation;
import org.sagebionetworks.repo.model.table.Query;
import org.sagebionetworks.web.client.PopupUtilsView;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.SynapseJavascriptClient;
import org.sagebionetworks.web.client.events.DownloadListUpdatedEvent;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.view.DivView;
import org.sagebionetworks.web.client.widget.asynch.AsynchronousProgressHandler;
import org.sagebionetworks.web.client.widget.asynch.AsynchronousProgressWidget;
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
	PortalGinInjector mockGinInjector;
	@Mock
	EventBus mockEventBus;
	@Mock
	AsynchronousProgressWidget mockAsynchronousProgressWidget;
	@Mock
	PopupUtilsView mockPopupUtils;
	@Mock
	Query mockQuery;
	@Mock
	SynapseAlert mockSynAlert;
	@Mock
	PackageSizeSummary mockPackageSizeSummary;
	@Mock
	SynapseJavascriptClient mockJsClient;
	@Mock
	DownloadList mockDownloadListBefore;
	@Mock
	AddFileToDownloadListResponse mockAddFileToDownloadListResponse;
	@Mock
	DownloadList mockDownloadListAfter;
	@Mock
	EntityChildrenResponse mockEntityChildrenResponse;
	@Captor
	ArgumentCaptor<Callback> callbackCaptor;
	@Captor
	ArgumentCaptor<AddFileToDownloadListRequest> requestCaptor;
	@Captor
	ArgumentCaptor<AsynchronousProgressHandler> asyncProgressHandlerCaptor;
	@Captor
	ArgumentCaptor<EntityChildrenRequest> entityChildrenRequestCaptor;
	@Mock
	FileHandleAssociation mockFileHandleAssociation;
	String folderId = "syn10932";
	Long testFolderChildCount = 22L, testFolderSumFileSizesBytes = 7777777L;
	List<FileHandleAssociation> downloadListFilesBefore, downloadListFilesAfter;
	@Before
	public void setUp() throws Exception {
		downloadListFilesBefore = new ArrayList<FileHandleAssociation>();
		downloadListFilesAfter = new ArrayList<FileHandleAssociation>();
		widget = new AddToDownloadList(mockView, mockGinInjector, mockPopupUtils, mockEventBus, mockSynAlert, mockPackageSizeSummary, mockJsClient);
		when(mockGinInjector.creatNewAsynchronousProgressWidget()).thenReturn(mockAsynchronousProgressWidget);
		when(mockGinInjector.getSynapseAlertWidget()).thenReturn(mockSynAlert);
		AsyncMockStubber.callSuccessWith(mockDownloadListBefore).when(mockJsClient).getDownloadList(any(AsyncCallback.class));
		when(mockAddFileToDownloadListResponse.getDownloadList()).thenReturn(mockDownloadListAfter);
		when(mockEntityChildrenResponse.getTotalChildCount()).thenReturn(testFolderChildCount);
		when(mockEntityChildrenResponse.getSumFileSizesBytes()).thenReturn(testFolderSumFileSizesBytes);
		when(mockDownloadListBefore.getFilesToDownload()).thenReturn(downloadListFilesBefore);
		when(mockDownloadListAfter.getFilesToDownload()).thenReturn(downloadListFilesAfter);
	}

	@Test
	public void testAddQueryToDownloadList() {
		widget.addToDownloadList(mockQuery);
		
		verify(mockPopupUtils).showConfirmDialog(anyString(), eq(AddToDownloadList.ADD_QUERY_FILES_CONFIRMATION_MESSAGE), callbackCaptor.capture());
		
		//simulate user confirmation
		callbackCaptor.getValue().invoke();
		
		verify(mockView, times(3)).hideAll();
		verify(mockJsClient).getDownloadList(any(AsyncCallback.class));
		verify(mockView).setAsynchronousProgressWidget(mockAsynchronousProgressWidget);
		verify(mockAsynchronousProgressWidget).startAndTrackJob(anyString(), eq(false), eq(AsynchType.AddFileToDownloadList), requestCaptor.capture(), asyncProgressHandlerCaptor.capture());
		
		//verify request
		AddFileToDownloadListRequest request = requestCaptor.getValue();
		assertEquals(mockQuery, request.getQuery());

		//simulate completing job
		asyncProgressHandlerCaptor.getValue().onComplete(mockAddFileToDownloadListResponse);
		
		verify(mockView, times(4)).hideAll();
		verify(mockPopupUtils).showInfo(AddToDownloadList.NO_NEW_FILES_ADDED_MESSAGE);
		verify(mockEventBus).fireEvent(any(DownloadListUpdatedEvent.class));
	}
	
	@Test
	public void testAddFolderToDownloadList() {
		AsyncMockStubber.callSuccessWith(mockEntityChildrenResponse).when(mockJsClient).getEntityChildren(any(EntityChildrenRequest.class), any(AsyncCallback.class));
		widget.addToDownloadList(folderId);
		
		verify(mockJsClient).getEntityChildren(entityChildrenRequestCaptor.capture(), any(AsyncCallback.class));
		//verify that we're asking for the file size sum and child count
		EntityChildrenRequest entityChildrenRequest = entityChildrenRequestCaptor.getValue();
		assertEquals(folderId, entityChildrenRequest.getParentId());
		assertTrue(entityChildrenRequest.getIncludeTotalChildCount());
		assertTrue(entityChildrenRequest.getIncludeSumFileSizes());
		
		verify(mockPackageSizeSummary).addFiles(testFolderChildCount.intValue(), testFolderSumFileSizesBytes.doubleValue());
		verify(mockView).showConfirmAdd();
		
		//going to simulate a new file is added to the download list
		downloadListFilesAfter.add(mockFileHandleAssociation);
		
		//simulate user confirmation
		widget.onConfirmAddToDownloadList();
		
		verify(mockView, times(3)).hideAll();
		verify(mockJsClient).getDownloadList(any(AsyncCallback.class));
		verify(mockView).setAsynchronousProgressWidget(mockAsynchronousProgressWidget);
		verify(mockAsynchronousProgressWidget).startAndTrackJob(anyString(), eq(false), eq(AsynchType.AddFileToDownloadList), requestCaptor.capture(), asyncProgressHandlerCaptor.capture());
		
		//verify request
		AddFileToDownloadListRequest request = requestCaptor.getValue();
		assertEquals(folderId, request.getFolderId());

		//simulate completing job
		asyncProgressHandlerCaptor.getValue().onComplete(mockAddFileToDownloadListResponse);

		verify(mockView).showSuccess(downloadListFilesAfter.size() - downloadListFilesBefore.size());

		//execute job failure code
		Exception e = new Exception();
		asyncProgressHandlerCaptor.getValue().onFailure(e);
		
		verify(mockView, times(5)).hideAll();
		verify(mockSynAlert).handleException(e);
	}
}
