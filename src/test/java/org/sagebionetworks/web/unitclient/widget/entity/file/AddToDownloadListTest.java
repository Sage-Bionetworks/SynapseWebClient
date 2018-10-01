package org.sagebionetworks.web.unitclient.widget.entity.file;

import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.sagebionetworks.repo.model.file.AddFileToDownloadListRequest;
import org.sagebionetworks.repo.model.file.AddFileToDownloadListResponse;
import org.sagebionetworks.repo.model.file.DownloadList;
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
	@Captor
	ArgumentCaptor<Callback> callbackCaptor;
	@Captor
	ArgumentCaptor<AddFileToDownloadListRequest> requestCaptor;
	@Captor
	ArgumentCaptor<AsynchronousProgressHandler> asyncProgressHandlerCaptor;
	
	String folderId = "syn10932";
	
	@Before
	public void setUp() throws Exception {
		widget = new AddToDownloadList(mockView, mockGinInjector, mockPopupUtils, mockEventBus, mockSynAlert, mockPackageSizeSummary, mockJsClient);
		when(mockGinInjector.creatNewAsynchronousProgressWidget()).thenReturn(mockAsynchronousProgressWidget);
		when(mockGinInjector.getSynapseAlertWidget()).thenReturn(mockSynAlert);
		AsyncMockStubber.callSuccessWith(mockDownloadListBefore).when(mockJsClient).getDownloadList(any(AsyncCallback.class));
		when(mockAddFileToDownloadListResponse.getDownloadList()).thenReturn(mockDownloadListAfter);
	}

	@Test
	public void testAddQueryToDownloadList() {
		widget.addToDownloadList(mockQuery);
		
		verify(mockPopupUtils).showConfirmDialog(anyString(), eq(AddToDownloadList.ADD_QUERY_FILES_CONFIRMATION_MESSAGE), callbackCaptor.capture());
		
		//simulate user confirmation
		callbackCaptor.getValue().invoke();
		
		verify(mockView, times(3)).hideAll();
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
	
//	@Test
//	public void testAddFolderToDownloadList() {
//		widget.addToDownloadList(folderId);
//		
//		verify(mockPopupUtils).showConfirmDialog(anyString(), eq(AddToDownloadList.ADD_FOLDER_FILES_CONFIRMATION_MESSAGE), callbackCaptor.capture());
//		
//		//simulate user confirmation
//		callbackCaptor.getValue().invoke();
//		
//		verify(mockView).clear();
//		verify(mockView).add(mockAsynchronousProgressWidget);
//		verify(mockAsynchronousProgressWidget).startAndTrackJob(anyString(), eq(false), eq(AsynchType.AddFileToDownloadList), requestCaptor.capture(), asyncProgressHandlerCaptor.capture());
//		
//		//verify request
//		AddFileToDownloadListRequest request = requestCaptor.getValue();
//		assertEquals(folderId, request.getFolderId());
//
//		//simulate job failure
//		Exception e = new Exception();
//		asyncProgressHandlerCaptor.getValue().onFailure(e);
//		
//		verify(mockView, times(2)).clear();
//		verify(mockView).add(mockSynAlert);
//		verify(mockSynAlert).handleException(e);
//	}
}
