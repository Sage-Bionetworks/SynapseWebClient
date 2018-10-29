package org.sagebionetworks.web.unitclient.widget.entity;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.Date;

import org.gwtbootstrap3.client.ui.constants.ButtonSize;
import org.gwtbootstrap3.client.ui.constants.IconType;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.repo.model.EntityBundle;
import org.sagebionetworks.repo.model.FileEntity;
import org.sagebionetworks.repo.model.Project;
import org.sagebionetworks.repo.model.Reference;
import org.sagebionetworks.repo.model.file.S3FileHandle;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.client.DateTimeUtils;
import org.sagebionetworks.web.client.PopupUtilsView;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.SynapseJavascriptClient;
import org.sagebionetworks.web.client.events.DownloadListUpdatedEvent;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.entity.EntityBadge;
import org.sagebionetworks.web.client.widget.entity.EntityListRowBadge;
import org.sagebionetworks.web.client.widget.entity.EntityListRowBadgeView;
import org.sagebionetworks.web.client.widget.entity.file.AddToDownloadList;
import org.sagebionetworks.web.client.widget.entity.file.FileDownloadMenuItem;
import org.sagebionetworks.web.client.widget.lazyload.LazyLoadHelper;
import org.sagebionetworks.web.client.widget.user.UserBadge;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;

public class EntityListRowBadgeTest {

	EntityListRowBadge widget;
	String entityId = "syn123";
	String entityName = "an entity";
	@Mock
	EntityListRowBadgeView mockView;
	@Mock
	UserBadge mockUserBadge;
	@Mock
	DateTimeUtils mockDateTimeUtils;
	@Mock
	LazyLoadHelper mockLazyLoadHelper;
	@Mock
	SynapseJavascriptClient mockSynapseJavascriptClient;
	@Mock
	PopupUtilsView mockPopupUtils;
	@Mock
	EventBus mockEventBus;
	@Mock
	SynapseJSNIUtils mockSynapseJSNIUtils;
	@Mock
	FileEntity mockFileEntity;
	@Mock
	S3FileHandle mockDataFileHandle;
	@Before
	public void before() throws JSONObjectAdapterException {
		MockitoAnnotations.initMocks(this);
		widget = new EntityListRowBadge(mockView, mockUserBadge, mockSynapseJavascriptClient,
				mockLazyLoadHelper, mockDateTimeUtils, mockPopupUtils, mockEventBus, mockSynapseJSNIUtils);
		AsyncMockStubber.callSuccessWith(null).when(mockSynapseJavascriptClient).addFileToDownloadList(anyString(), anyString(), any(AsyncCallback.class));
	}
	@Test
	public void testConstruction() {
		verify(mockView).setPresenter(widget);
		verify(mockView).setCreatedByWidget(any(Widget.class));
	}
	
	private EntityBundle setupEntity(Entity entity) {
		EntityBundle bundle = mock(EntityBundle.class);
		when(bundle.getEntity()).thenReturn(entity);
		when(bundle.getFileHandles()).thenReturn(Collections.singletonList(mockDataFileHandle));
		
		AsyncMockStubber.callSuccessWith(bundle).when(mockSynapseJavascriptClient).getEntityBundle(anyString(), anyInt(), any(AsyncCallback.class));
		AsyncMockStubber.callSuccessWith(bundle).when(mockSynapseJavascriptClient).getEntityBundleForVersion(anyString(), anyLong(), anyInt(), any(AsyncCallback.class));
		return bundle;
	}
	
	/**
	 * This tests the standard case when the badge is outside the viewport and scrolled into view.  
	 */
	@Test
	public void testCheckForInViewAndLoadData() {
		//set up entity
		String entityId = "syn12345";
		String entityName = "a project";
		String createdByUserId = "4444";
		String description = "an old entity description";
		Project testProject = new Project();
		testProject.setName(entityName);
		testProject.setCreatedBy(createdByUserId);
		testProject.setDescription(description);
		testProject.setCreatedOn(new Date());
		
		//note: can't test modified on because it format it using the gwt DateUtils (calls GWT.create())
		testProject.setId(entityId);
		setupEntity(testProject);
		
		//configure
		Reference ref = new Reference();
		ref.setTargetId(entityId);
		widget.configure(ref);
		
		ArgumentCaptor<Callback> captor = ArgumentCaptor.forClass(Callback.class);
		verify(mockLazyLoadHelper).configure(captor.capture(), eq(mockView));
		captor.getValue().invoke();
		
		verify(mockSynapseJavascriptClient).getEntityBundle(anyString(), anyInt(), any(AsyncCallback.class));
		verify(mockView).showLoading();
		verify(mockView).setIcon(IconType.LIST_ALT); //project icon
		verify(mockView).setEntityLink(entityName, "#!Synapse:"+entityId);
		verify(mockUserBadge).configure(createdByUserId);
		verify(mockView).setCreatedOn(anyString());
		verify(mockView).setDescription(description);
		verify(mockView, never()).showAddToDownloadList();
		verify(mockView).setVersion(EntityListRowBadge.N_A);
	}
	
	@Test
	public void testGetFileEntityBundle() {
		//verify download button is configured and shown
		String entityId = "syn12345";
		String entityName = "file.txt";
		String createdByUserId = "4444";
		String description = "an old entity description";
		Long version = 4L;
		FileEntity testFile = new FileEntity();
		testFile.setName(entityName);
		testFile.setCreatedBy(createdByUserId);
		testFile.setVersionNumber(version);
		testFile.setDescription(description);
		testFile.setId(entityId);
		setupEntity(testFile);
		
		Reference ref = new Reference();
		ref.setTargetId(entityId);
		ref.setTargetVersionNumber(version);
		widget.configure(ref);
		
		widget.getEntityBundle();
		
		verify(mockSynapseJavascriptClient).getEntityBundleForVersion(anyString(), anyLong(), anyInt(), any(AsyncCallback.class));
		verify(mockView).showLoading();
		verify(mockView).setIcon(IconType.FILE); //file icon
		verify(mockView).setEntityLink(entityName, "#!Synapse:"+entityId+"."+version);
		verify(mockUserBadge).configure(createdByUserId);
		verify(mockView).setCreatedOn(anyString());
		verify(mockView).setDescription(description);
		verify(mockView).showAddToDownloadList();
		verify(mockView).setVersion(version.toString());
	}
	
	@Test
	public void testCheckForInViewAndLoadDataFailure() {
		Reference ref = new Reference();
		ref.setTargetId(entityId);
		widget.configure(ref);
		
		//test failure response from getEntityBundle
		String errorMessage = "problem occurred while asking for entity bundle";
		Exception ex = new Exception(errorMessage);
		AsyncMockStubber.callFailureWith(ex).when(mockSynapseJavascriptClient).getEntityBundle(anyString(), anyInt(), any(AsyncCallback.class));
		widget.getEntityBundle();
		verify(mockView).showErrorIcon(errorMessage);
		verify(mockView).setEntityLink(entityId, "#!Synapse:"+entityId);
	}
	
	

	@Test
	public void testOnAddToDownloadList() {
		String fileHandleId = "9999";
		when(mockDataFileHandle.getId()).thenReturn(fileHandleId);
		when(mockFileEntity.getId()).thenReturn(entityId);
		when(mockFileEntity.getName()).thenReturn(entityName);
		when(mockFileEntity.getVersionNumber()).thenReturn(2L);
		setupEntity(mockFileEntity);
		Reference ref = new Reference();
		ref.setTargetId(entityId);
		widget.configure(ref);
		widget.getEntityBundle();
		
		widget.onAddToDownloadList();
		
		verify(mockSynapseJavascriptClient).addFileToDownloadList(eq(fileHandleId), eq(entityId), any(AsyncCallback.class));
		verify(mockPopupUtils).showInfo(entityName + EntityBadge.ADDED_TO_DOWNLOAD_LIST);
		verify(mockEventBus).fireEvent(any(DownloadListUpdatedEvent.class));
		verify(mockSynapseJSNIUtils).sendAnalyticsEvent(AddToDownloadList.DOWNLOAD_ACTION_EVENT_NAME, AddToDownloadList.FILES_ADDED_TO_DOWNLOAD_LIST_EVENT_NAME, Integer.toString(1));
	}
	
	@Test
	public void testOnAddToDownloadListError() {
		String errorMessage = "a simulated error";
		AsyncMockStubber.callFailureWith(new Exception(errorMessage)).when(mockSynapseJavascriptClient).addFileToDownloadList(anyString(), anyString(), any(AsyncCallback.class));
		String fileHandleId = "9999";
		when(mockDataFileHandle.getId()).thenReturn(fileHandleId);
		when(mockFileEntity.getId()).thenReturn(entityId);
		when(mockFileEntity.getName()).thenReturn(entityName);
		when(mockFileEntity.getVersionNumber()).thenReturn(2L);
		setupEntity(mockFileEntity);
		Reference ref = new Reference();
		ref.setTargetId(entityId);
		widget.configure(ref);
		widget.getEntityBundle();
		
		widget.onAddToDownloadList();
		
		verify(mockSynapseJavascriptClient).addFileToDownloadList(eq(fileHandleId), eq(entityId), any(AsyncCallback.class));
		verifyZeroInteractions(mockPopupUtils, mockEventBus);
		verify(mockView).showErrorIcon(errorMessage);
	}
}
