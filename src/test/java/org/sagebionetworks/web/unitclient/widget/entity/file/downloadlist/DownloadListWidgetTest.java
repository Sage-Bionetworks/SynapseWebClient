package org.sagebionetworks.web.unitclient.widget.entity.file.downloadlist;

import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.sagebionetworks.repo.model.file.BulkFileDownloadRequest;
import org.sagebionetworks.repo.model.file.BulkFileDownloadResponse;
import org.sagebionetworks.repo.model.file.DownloadList;
import org.sagebionetworks.repo.model.file.DownloadOrder;
import org.sagebionetworks.repo.model.file.FileHandleAssociation;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.SynapseJavascriptClient;
import org.sagebionetworks.web.client.events.DownloadListUpdatedEvent;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.asynch.AsynchronousProgressHandler;
import org.sagebionetworks.web.client.widget.asynch.AsynchronousProgressWidget;
import org.sagebionetworks.web.client.widget.asynch.InlineAsynchronousProgressViewImpl;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.client.widget.entity.file.AddToDownloadList;
import org.sagebionetworks.web.client.widget.entity.file.downloadlist.DownloadListWidget;
import org.sagebionetworks.web.client.widget.entity.file.downloadlist.DownloadListWidgetView;
import org.sagebionetworks.web.client.widget.entity.file.downloadlist.FileHandleAssociationTable;
import org.sagebionetworks.web.client.widget.entity.file.downloadlist.PackageSizeSummary;
import org.sagebionetworks.web.shared.asynch.AsynchType;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.rpc.AsyncCallback;

@RunWith(MockitoJUnitRunner.class)
public class DownloadListWidgetTest {
	DownloadListWidget widget;
	@Mock
	DownloadListWidgetView mockView; 
	@Mock
	SynapseAlert mockSynAlert;
	@Mock
	SynapseJavascriptClient mockJsClient;
	@Mock
	EventBus mockEventBus;
	@Mock
	FileHandleAssociationTable mockFhaTable;
	@Mock
	PackageSizeSummary mockPackageSizeSummary;
	@Mock
	AsynchronousProgressWidget mockProgressWidget;
	@Mock
	SynapseJSNIUtils mockJsniUtils;
	@Captor
	ArgumentCaptor<CallbackP<Double>> addToPackageSizeCallbackCaptor;
	@Captor
	ArgumentCaptor<CallbackP<FileHandleAssociation>> onRemoveFileHandleAssociationCaptor;
	
	@Mock
	DownloadOrder mockDownloadOrder;
	@Mock
	DownloadList mockDownloadList;
	@Mock
	List<FileHandleAssociation> mockFhas;
	@Mock
	FileHandleAssociation mockFha;
	@Captor
	ArgumentCaptor<BulkFileDownloadRequest> requestCaptor;
	@Captor
	ArgumentCaptor<AsynchronousProgressHandler> progressHandlerCaptor;
	@Mock
	BulkFileDownloadResponse mockBulkFileDownloadResponse;
	@Mock
	InlineAsynchronousProgressViewImpl mockInlineAsynchronousProgressView;
	
	@Before
	public void setUp() throws Exception {
		widget = new DownloadListWidget(mockView, mockSynAlert, mockJsClient, mockEventBus, mockFhaTable, mockPackageSizeSummary, mockProgressWidget, mockInlineAsynchronousProgressView, mockJsniUtils);
		when(mockDownloadList.getFilesToDownload()).thenReturn(mockFhas);
		when(mockDownloadOrder.getFiles()).thenReturn(mockFhas);
		AsyncMockStubber.callSuccessWith(mockDownloadList).when(mockJsClient).getDownloadList(any(AsyncCallback.class));
		AsyncMockStubber.callSuccessWith(mockDownloadOrder).when(mockJsClient).createDownloadOrderFromUsersDownloadList(anyString(), any(AsyncCallback.class));
		AsyncMockStubber.callSuccessWith(null).when(mockJsClient).clearDownloadList(any(AsyncCallback.class));
		AsyncMockStubber.callSuccessWith(mockDownloadList).when(mockJsClient).removeFileFromDownloadList(any(FileHandleAssociation.class), any(AsyncCallback.class));
	}

	@Test
	public void testConstructor() {
		verify(mockView).setSynAlert(mockSynAlert);
		verify(mockView).setFileHandleAssociationTable(mockFhaTable);
		verify(mockView).setPackageSizeSummary(mockPackageSizeSummary);
		verify(mockView).setProgressTrackingWidgetVisible(false);
		verify(mockView).setProgressTrackingWidget(mockProgressWidget);
		verify(mockView).setPresenter(widget);
	}

	@Test
	public void testRefresh() {
		widget.refresh();
		
		verify(mockView).setCreatePackageUIVisible(true);
		verify(mockView).setDownloadPackageUIVisible(false);
		verify(mockPackageSizeSummary, times(2)).clear();
		verify(mockFhaTable).configure(eq(mockFhas), addToPackageSizeCallbackCaptor.capture(), onRemoveFileHandleAssociationCaptor.capture());
	
		//verify add file size 
		Double fileSize = 4.0;
		addToPackageSizeCallbackCaptor.getValue().invoke(fileSize);
		verify(mockPackageSizeSummary).addFile(fileSize);
		
		//verify remove file
		onRemoveFileHandleAssociationCaptor.getValue().invoke(mockFha);
		verify(mockJsClient).removeFileFromDownloadList(eq(mockFha), any(AsyncCallback.class));
	}

	@Test
	public void testOnCreatePackageEmptyZipfileName() {
		widget.onCreatePackage("");
		
		verify(mockSynAlert).showError(DownloadListWidget.EMPTY_FILENAME_MESSAGE_);
		verify(mockJsClient, never()).createDownloadOrderFromUsersDownloadList(anyString(), any(AsyncCallback.class));
	}
	
	@Test
	public void testOnCreatePackage() {
		String fileName = "package";
		when(mockDownloadOrder.getZipFileName()).thenReturn(fileName + DownloadListWidget.ZIP_EXTENSION);
		
		widget.onCreatePackage(fileName);
		
		verify(mockJsClient).createDownloadOrderFromUsersDownloadList(eq(fileName + DownloadListWidget.ZIP_EXTENSION), any(AsyncCallback.class));
		verify(mockView).setProgressTrackingWidgetVisible(true);
		verify(mockView).setCreatePackageUIVisible(false);
		verify(mockJsniUtils).sendAnalyticsEvent(AddToDownloadList.DOWNLOAD_ACTION_EVENT_NAME, DownloadListWidget.DOWNLOAD_LIST_PACKAGE_CREATED_EVENT_NAME);
		verify(mockProgressWidget).startAndTrackJob(anyString(), eq(false), eq(AsynchType.BulkFileDownload), requestCaptor.capture(), progressHandlerCaptor.capture());
		
		//verify request
		BulkFileDownloadRequest request = requestCaptor.getValue();
		assertEquals(mockFhas, request.getRequestedFiles());
		assertEquals(fileName + DownloadListWidget.ZIP_EXTENSION, request.getZipFileName());
		
		//verify successful async job
		String downloadUrl = "presignedurl";
		when(mockJsniUtils.getRawFileHandleUrl(anyString())).thenReturn(downloadUrl);
		String resultFileHandleId = "87654";
		when(mockBulkFileDownloadResponse.getResultZipFileHandleId()).thenReturn(resultFileHandleId);
		progressHandlerCaptor.getValue().onComplete(mockBulkFileDownloadResponse);
		verify(mockEventBus).fireEvent(any(DownloadListUpdatedEvent.class));
		verify(mockView, times(2)).setProgressTrackingWidgetVisible(false);
		verify(mockView).setPackageDownloadURL(downloadUrl);
	}

	@Test
	public void testOnDownloadPackageFailureCreatingOrder() {
		Exception ex = new Exception("failed to create order");
		AsyncMockStubber.callFailureWith(ex).when(mockJsClient).createDownloadOrderFromUsersDownloadList(anyString(), any(AsyncCallback.class));
		
		widget.onCreatePackage("test");
		
		verify(mockSynAlert).handleException(ex);
	}
	
	@Test
	public void testFailureCreatingPackage() {
		Exception ex = new Exception("failed to create package");
		widget.startDownload(mockDownloadOrder);
		verify(mockProgressWidget).startAndTrackJob(anyString(), eq(false), eq(AsynchType.BulkFileDownload), requestCaptor.capture(), progressHandlerCaptor.capture());
		
		progressHandlerCaptor.getValue().onFailure(ex);
		
		verify(mockSynAlert).handleException(ex);
		verify(mockView, times(2)).setProgressTrackingWidgetVisible(false);
		verify(mockView).setCreatePackageUIVisible(true);
	}

	@Test
	public void testOnClearDownloadList() {
		widget.onClearDownloadList();
		
		verify(mockSynAlert, times(2)).clear();
		verify(mockJsClient).clearDownloadList(any(AsyncCallback.class));
		verify(mockEventBus).fireEvent(any(DownloadListUpdatedEvent.class));
		verify(mockJsClient).getDownloadList(any(AsyncCallback.class));
		verify(mockJsniUtils).sendAnalyticsEvent(AddToDownloadList.DOWNLOAD_ACTION_EVENT_NAME, DownloadListWidget.DOWNLOAD_LIST_CLEARED_EVENT_NAME);
	}
	
	@Test
	public void testOnClearDownloadListFailure() {
		Exception ex = new Exception("failed to clear");
		AsyncMockStubber.callFailureWith(ex).when(mockJsClient).clearDownloadList(any(AsyncCallback.class));
		
		widget.onClearDownloadList();
		
		verify(mockSynAlert).handleException(ex);
	}

	@Test
	public void testOnRemoveFileHandleAssociation() {
		widget.onRemoveFileHandleAssociation(mockFha);
		
		verify(mockJsClient).removeFileFromDownloadList(eq(mockFha), any(AsyncCallback.class));
		verify(mockEventBus).fireEvent(any(DownloadListUpdatedEvent.class));
		verify(mockFhaTable).configure(eq(mockFhas), addToPackageSizeCallbackCaptor.capture(), onRemoveFileHandleAssociationCaptor.capture());
	}
	
	@Test
	public void testOnRemoveFileHandleAssociationFailure() {
		Exception ex = new Exception("failed to remove fha");
		AsyncMockStubber.callFailureWith(ex).when(mockJsClient).removeFileFromDownloadList(any(FileHandleAssociation.class), any(AsyncCallback.class));
		
		widget.onRemoveFileHandleAssociation(mockFha);
		
		verify(mockSynAlert).handleException(ex);
	}

	@Test
	public void testOnDownloadPackage() {
		widget.onDownloadPackage();
		
		//verify when the user clicks download package, the package list is refreshed (and view reset)
		verify(mockView).hideFilesDownloadedAlert();
		verify(mockView).setPackageName("");
		verify(mockView).setCreatePackageUIVisible(true);
		verify(mockView).setDownloadPackageUIVisible(false);
		verify(mockSynAlert).clear();
		verify(mockPackageSizeSummary, times(2)).clear();
		verify(mockView).setMultiplePackagesRequiredVisible(false);
		verify(mockJsClient).getDownloadList(any(AsyncCallback.class));
	}

	@Test
	public void testAsWidget() {
		widget.asWidget();
		
		verify(mockView).asWidget();
	}

}
