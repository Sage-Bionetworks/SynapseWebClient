package org.sagebionetworks.web.unitclient.widget.entity;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.repo.model.EntityBundle;
import org.sagebionetworks.repo.model.EntityHeader;
import org.sagebionetworks.repo.model.FileEntity;
import org.sagebionetworks.repo.model.Project;
import org.sagebionetworks.repo.model.auth.UserEntityPermissions;
import org.sagebionetworks.repo.model.file.ExternalS3UploadDestination;
import org.sagebionetworks.repo.model.file.ExternalUploadDestination;
import org.sagebionetworks.repo.model.file.UploadDestination;
import org.sagebionetworks.repo.model.table.TableEntity;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.events.EntityUpdatedEvent;
import org.sagebionetworks.web.client.events.EntityUpdatedHandler;
import org.sagebionetworks.web.client.place.Synapse;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.entity.EntityMetadata;
import org.sagebionetworks.web.client.widget.entity.EntityPageTop;
import org.sagebionetworks.web.client.widget.entity.EntityPageTopView;
import org.sagebionetworks.web.client.widget.entity.WikiPageWidget;
import org.sagebionetworks.web.client.widget.entity.controller.EntityActionController;
import org.sagebionetworks.web.client.widget.entity.menu.v2.ActionMenuWidget;
import org.sagebionetworks.web.client.widget.entity.tabs.ChallengeTab;
import org.sagebionetworks.web.client.widget.entity.tabs.FilesTab;
import org.sagebionetworks.web.client.widget.entity.tabs.Tab;
import org.sagebionetworks.web.client.widget.entity.tabs.TablesTab;
import org.sagebionetworks.web.client.widget.entity.tabs.Tabs;
import org.sagebionetworks.web.client.widget.entity.tabs.WikiTab;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;

public class EntityPageTopTest {

	@Mock
	EntityPageTopView mockView;
	@Mock
	EntityBundle mockProjectBundle;
	@Mock
	EntityHeader mockProjectHeader;
	@Mock
	Project mockProjectEntity;
	@Mock
	FileEntity mockFileEntity;
	@Mock
	TableEntity mockTableEntity;
	@Mock
	SynapseClientAsync mockSynapseClientAsync;
	@Mock
	Tabs mockTabs;
	@Mock
	EntityMetadata mockEntityMetadata;
	@Mock
	WikiTab mockWikiTab;
	@Mock
	Tab mockWikiInnerTab;
	@Mock
	FilesTab mockFilesTab;
	@Mock
	Tab mockFilesInnerTab;
	@Mock
	TablesTab mockTablesTab;
	@Mock
	Tab mockTablesInnerTab;
	@Mock
	ChallengeTab mockChallengeTab;
	@Mock
	Tab mockChallengeInnerTab;
	@Mock
	UserEntityPermissions mockPermissions;
	@Mock
	GlobalApplicationState mockGlobalAppState;
	@Mock
	EntityActionController mockEntityActionController;
	@Mock
	ActionMenuWidget mockActionMenuWidget;
	@Mock
	EntityUpdatedHandler mockEntityUpdatedHandler;
	
	EntityPageTop pageTop;
	String projectEntityId = "syn123";
	String projectWikiId = "31415926666";
	boolean canEdit = true;
	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
		when(mockFilesTab.asTab()).thenReturn(mockFilesInnerTab);
		when(mockWikiTab.asTab()).thenReturn(mockWikiInnerTab);
		when(mockTablesTab.asTab()).thenReturn(mockTablesInnerTab);
		when(mockChallengeTab.asTab()).thenReturn(mockChallengeInnerTab);
		pageTop = new EntityPageTop(mockView, mockSynapseClientAsync, mockTabs, mockEntityMetadata,
				mockWikiTab, mockFilesTab, mockTablesTab, mockChallengeTab, mockEntityActionController, 
				mockActionMenuWidget, mockGlobalAppState);
		pageTop.setEntityUpdatedHandler(mockEntityUpdatedHandler);
		AsyncMockStubber.callSuccessWith(mockProjectBundle).when(mockSynapseClientAsync).getEntityBundle(anyString(), anyInt(), any(AsyncCallback.class));
		when(mockProjectBundle.getEntity()).thenReturn(mockProjectEntity);
		when(mockProjectEntity.getId()).thenReturn(projectEntityId);
		when(mockProjectBundle.getRootWikiId()).thenReturn(projectWikiId);
		when(mockProjectHeader.getId()).thenReturn(projectEntityId);
		when(mockProjectBundle.getPermissions()).thenReturn(mockPermissions);
		when(mockPermissions.getCanCertifiedUserEdit()).thenReturn(canEdit);
	}
	
	@Test
	public void testConstruction(){
		verify(mockView).setTabs(any(Widget.class));
		verify(mockView).setProjectMetadata(any(Widget.class));
		verify(mockView).setPresenter(pageTop);
		verify(mockActionMenuWidget).addControllerWidget(any(Widget.class));
		verify(mockView).setActionMenu(any(Widget.class));
		verify(mockTabs).addTab(mockFilesInnerTab);
		verify(mockTabs).addTab(mockWikiInnerTab);
		verify(mockTabs).addTab(mockTablesInnerTab);
		verify(mockTabs).addTab(mockChallengeInnerTab);
		
		ArgumentCaptor<CallbackP> captor = ArgumentCaptor.forClass(CallbackP.class);
		verify(mockFilesTab).setShowProjectInfoCallback(captor.capture());
		
		//when this is invoked, the message is sent to the view
		CallbackP showHideProjectInfoCallback = captor.getValue();
		reset(mockView);
		showHideProjectInfoCallback.invoke(true);
		verify(mockView).setProjectInformationVisible(true);
		reset(mockView);
		showHideProjectInfoCallback.invoke(false);
		verify(mockView).setProjectInformationVisible(false);
		reset(mockView);
		verify(mockTablesTab).setShowProjectInfoCallback(any(CallbackP.class));
		
		//when wiki or admin tab is clicked, then project info is always shown
		ArgumentCaptor<CallbackP> tabCaptor = ArgumentCaptor.forClass(CallbackP.class);
		verify(mockWikiTab).setTabClickedCallback(tabCaptor.capture());
		CallbackP showProjectInfoCallback = tabCaptor.getValue();
		showProjectInfoCallback.invoke(null);
		verify(mockView).setProjectInformationVisible(true);
		verify(mockEntityMetadata).setEntityUpdatedHandler(mockEntityUpdatedHandler);
	}
	
	@Test
	public void testConfigureWithProject(){
		AsyncMockStubber.callSuccessWith(null).when(mockSynapseClientAsync).getUploadDestinations(anyString(), any(AsyncCallback.class));
		Synapse.EntityArea area = null;
		String areaToken = null;
		Long versionNumber = null;
		pageTop.configure(mockProjectEntity, versionNumber, mockProjectHeader, area, areaToken);
		//Area was not defined for this project, should try to go to wiki tab by default.
		//Once to show the active tab, and once after configuration so that the place is pushed into the history.
		verify(mockTabs, times(2)).showTab(mockWikiInnerTab);
		verify(mockView).setPageTitle(anyString());
		
		verify(mockEntityMetadata).setEntityBundle(mockProjectBundle, null);
		
		verify(mockWikiTab).configure(eq(projectEntityId), eq(projectWikiId), eq(canEdit), any(WikiPageWidget.Callback.class));
		verify(mockFilesTab).configure(mockProjectEntity, mockProjectBundle, mockEntityUpdatedHandler, versionNumber);		
		verify(mockTablesTab).configure(mockProjectEntity, mockProjectBundle, mockEntityUpdatedHandler, areaToken);
		verify(mockChallengeTab).configure(projectEntityId);
		verify(mockEntityMetadata).setStorageLocationText("Synapse Storage");
	}
	
	@Test
	public void testConfigureWithFile(){
		Synapse.EntityArea area = null;
		String areaToken = null;
		Long versionNumber = 5L;
		pageTop.configure(mockFileEntity, versionNumber, mockProjectHeader, area, areaToken);
		verify(mockTabs).showTab(mockFilesInnerTab);
		verify(mockView).setPageTitle(anyString());
		verify(mockEntityMetadata).setEntityBundle(mockProjectBundle, null);
		
		verify(mockWikiTab).configure(eq(projectEntityId), eq(projectWikiId), eq(canEdit), any(WikiPageWidget.Callback.class));
		verify(mockFilesTab).configure(mockFileEntity, mockProjectBundle, mockEntityUpdatedHandler, versionNumber);		
		verify(mockTablesTab).configure(mockFileEntity, mockProjectBundle, mockEntityUpdatedHandler, areaToken);
		verify(mockChallengeTab).configure(projectEntityId);
	}
	
	@Test
	public void testConfigureWithTable(){
		Synapse.EntityArea area = null;
		String areaToken = "a table query area token";
		Long versionNumber = null;
		pageTop.configure(mockTableEntity, versionNumber, mockProjectHeader, area, areaToken);
		verify(mockTabs).showTab(mockTablesInnerTab);
		verify(mockView).setPageTitle(anyString());
		
		verify(mockEntityMetadata).setEntityBundle(mockProjectBundle, null);
		
		verify(mockWikiTab).configure(eq(projectEntityId), eq(projectWikiId), eq(canEdit), any(WikiPageWidget.Callback.class));
		verify(mockFilesTab).configure(mockTableEntity, mockProjectBundle, mockEntityUpdatedHandler, versionNumber);		
		verify(mockTablesTab).configure(mockTableEntity, mockProjectBundle, mockEntityUpdatedHandler, areaToken);
		verify(mockChallengeTab).configure(projectEntityId);
	}
	
	@Test
	public void testConfigureWithFileGoToChallengeAdminTab(){
		Synapse.EntityArea area = Synapse.EntityArea.ADMIN;
		String areaToken = null;
		Long versionNumber = null;
		pageTop.configure(mockFileEntity, versionNumber, mockProjectHeader, area, areaToken);
		verify(mockTabs).showTab(mockChallengeInnerTab);
		verify(mockView).setPageTitle(anyString());
		
		verify(mockEntityMetadata).setEntityBundle(mockProjectBundle, null);
		
		verify(mockWikiTab).configure(eq(projectEntityId), eq(projectWikiId), eq(canEdit), any(WikiPageWidget.Callback.class));
		verify(mockFilesTab).configure(mockFileEntity, mockProjectBundle, mockEntityUpdatedHandler, versionNumber);		
		verify(mockTablesTab).configure(mockFileEntity, mockProjectBundle, mockEntityUpdatedHandler, areaToken);
		verify(mockChallengeTab).configure(projectEntityId);
	}
	
	@Test
	public void testFireEntityUpdatedEvent() {
		pageTop.fireEntityUpdatedEvent();
		verify(mockEntityUpdatedHandler).onPersistSuccess(any(EntityUpdatedEvent.class));
	}
	
	@Test
	public void testGetWikiPageId() {
		String areaToken = "123";
		String rootWikiId = "456";
		//should use wiki area token wiki id if available
		assertEquals(areaToken, pageTop.getWikiPageId(areaToken, rootWikiId));
		//and the root wiki id if area token is not defined
		assertEquals(rootWikiId, pageTop.getWikiPageId("", rootWikiId));
		assertEquals(rootWikiId, pageTop.getWikiPageId(null, rootWikiId));
	}

	@Test
	public void testConfigureStorageLocationExternalS3() {
		List<UploadDestination> uploadDestinations = new ArrayList<UploadDestination>();
		ExternalS3UploadDestination exS3Destination = new ExternalS3UploadDestination();
		exS3Destination.setBucket("testBucket");
		exS3Destination.setBaseKey("testBaseKey");
		uploadDestinations.add(exS3Destination);
		AsyncMockStubber.callSuccessWith(uploadDestinations).when(mockSynapseClientAsync).getUploadDestinations(anyString(), any(AsyncCallback.class));
		Long versionNumber = 5L;
		pageTop.configure(mockProjectEntity, versionNumber, mockProjectHeader, null, null);
		verify(mockEntityMetadata).setStorageLocationText("s3://testBucket/testBaseKey");
		verify(mockEntityMetadata).setStorageLocationVisible(true);
	}
	
	@Test
	public void testConfigureStorageLocationExternal() {
		List<UploadDestination> uploadDestinations = new ArrayList<UploadDestination>();
		ExternalUploadDestination exS3Destination = new ExternalUploadDestination();
		exS3Destination.setUrl("testUrl.com");
		uploadDestinations.add(exS3Destination);
		AsyncMockStubber.callSuccessWith(uploadDestinations).when(mockSynapseClientAsync).getUploadDestinations(anyString(), any(AsyncCallback.class));
		Long versionNumber = 5L;
		pageTop.configure(mockProjectEntity, versionNumber, mockProjectHeader, null, null);
		verify(mockEntityMetadata).setStorageLocationText("testUrl.com");
		verify(mockEntityMetadata).setStorageLocationVisible(true);
	}
	
	@Test
	public void testConfigureStorageLocationSynapseStorage() {
		AsyncMockStubber.callSuccessWith(null).when(mockSynapseClientAsync).getUploadDestinations(anyString(), any(AsyncCallback.class));
		Long versionNumber = 5L;
		pageTop.configure(mockProjectEntity, versionNumber, mockProjectHeader, null, null);
		verify(mockEntityMetadata).setStorageLocationText("Synapse Storage");
		verify(mockEntityMetadata).setStorageLocationVisible(true);
	}
}
