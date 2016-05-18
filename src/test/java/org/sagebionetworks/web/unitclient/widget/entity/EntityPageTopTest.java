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

import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.repo.model.AccessControlList;
import org.sagebionetworks.repo.model.EntityBundle;
import org.sagebionetworks.repo.model.EntityHeader;
import org.sagebionetworks.repo.model.FileEntity;
import org.sagebionetworks.repo.model.Project;
import org.sagebionetworks.repo.model.auth.UserEntityPermissions;
import org.sagebionetworks.repo.model.table.TableEntity;
import org.sagebionetworks.web.client.GWTWrapper;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.events.EntityUpdatedEvent;
import org.sagebionetworks.web.client.events.EntityUpdatedHandler;
import org.sagebionetworks.web.client.place.Synapse;
import org.sagebionetworks.web.client.place.Synapse.EntityArea;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.entity.EntityMetadata;
import org.sagebionetworks.web.client.widget.entity.EntityPageTop;
import org.sagebionetworks.web.client.widget.entity.EntityPageTopView;
import org.sagebionetworks.web.client.widget.entity.WikiPageWidget;
import org.sagebionetworks.web.client.widget.entity.controller.EntityActionController;
import org.sagebionetworks.web.client.widget.entity.menu.v2.Action;
import org.sagebionetworks.web.client.widget.entity.menu.v2.ActionMenuWidget;
import org.sagebionetworks.web.client.widget.entity.menu.v2.ActionMenuWidget.ActionListener;
import org.sagebionetworks.web.client.widget.entity.tabs.ChallengeTab;
import org.sagebionetworks.web.client.widget.entity.tabs.DiscussionTab;
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
	DiscussionTab mockDiscussionTab;
	@Mock
	Tab mockDiscussionInnerTab;
	@Mock
	UserEntityPermissions mockPermissions;
	@Mock
	EntityActionController mockEntityActionController;
	@Mock
	ActionMenuWidget mockActionMenuWidget;
	@Mock
	EntityUpdatedHandler mockEntityUpdatedHandler;
	@Mock
	GWTWrapper mockGWTWrapper;
	@Mock
	AccessControlList mockACL;
	
	EntityPageTop pageTop;
	String projectEntityId = "syn123";
	String projectName = "fooooo";
	String projectWikiId = "31415926666";
	boolean canEdit = true;
	boolean canModerate = false;
	Set<Long> moderatorIds;
	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
		when(mockFilesTab.asTab()).thenReturn(mockFilesInnerTab);
		when(mockWikiTab.asTab()).thenReturn(mockWikiInnerTab);
		when(mockTablesTab.asTab()).thenReturn(mockTablesInnerTab);
		when(mockChallengeTab.asTab()).thenReturn(mockChallengeInnerTab);
		when(mockDiscussionTab.asTab()).thenReturn(mockDiscussionInnerTab);
		pageTop = new EntityPageTop(mockView, mockSynapseClientAsync, mockTabs, mockEntityMetadata,
				mockWikiTab, mockFilesTab, mockTablesTab, mockChallengeTab, mockDiscussionTab, mockEntityActionController, 
				mockActionMenuWidget, mockGWTWrapper);
		pageTop.setEntityUpdatedHandler(mockEntityUpdatedHandler);
		AsyncMockStubber.callWithInvoke().when(mockGWTWrapper).scheduleDeferred(any(Callback.class));
		AsyncMockStubber.callSuccessWith(mockProjectBundle).when(mockSynapseClientAsync).getEntityBundle(anyString(), anyInt(), any(AsyncCallback.class));
		when(mockProjectBundle.getEntity()).thenReturn(mockProjectEntity);
		when(mockProjectEntity.getId()).thenReturn(projectEntityId);
		when(mockProjectBundle.getRootWikiId()).thenReturn(projectWikiId);
		when(mockProjectHeader.getId()).thenReturn(projectEntityId);
		when(mockProjectHeader.getName()).thenReturn(projectName);
		when(mockProjectBundle.getPermissions()).thenReturn(mockPermissions);
		when(mockPermissions.getCanCertifiedUserEdit()).thenReturn(canEdit);
		when(mockPermissions.getCanModerate()).thenReturn(canModerate);
		when(mockProjectBundle.getAccessControlList()).thenReturn(mockACL);
		moderatorIds = new HashSet<Long>();
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
		verify(mockTabs).addTab(mockDiscussionInnerTab);
		
		verify(mockActionMenuWidget).addActionListener(eq(Action.TOGGLE_ANNOTATIONS), any(ActionListener.class));
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
		
		
		//when wiki or admin tab is clicked, then project info is shown when a project bundle is configured
		ArgumentCaptor<CallbackP> tabCaptor = ArgumentCaptor.forClass(CallbackP.class);
		verify(mockWikiTab).setTabClickedCallback(tabCaptor.capture());
		CallbackP showProjectInfoCallback = tabCaptor.getValue();
		showProjectInfoCallback.invoke(null);
		
		verify(mockView).setProjectInformationVisible(false);
		verify(mockEntityMetadata).setEntityUpdatedHandler(mockEntityUpdatedHandler);
		
		pageTop.configure(mockProjectEntity, null, mockProjectHeader, null, null);
		showProjectInfoCallback.invoke(null);
		verify(mockView).setProjectInformationVisible(true);
	}
	
	@Test
	public void testConfigureWithProject(){
		Synapse.EntityArea area = null;
		String areaToken = null;
		Long versionNumber = null;
		pageTop.configure(mockProjectEntity, versionNumber, mockProjectHeader, area, areaToken);
		//Area was not defined for this project, should try to go to wiki tab by default.
		//Once to show the active tab, and once after configuration so that the place is pushed into the history.
		verify(mockTabs, times(2)).showTab(mockWikiInnerTab, EntityPageTop.PUSH_TAB_URL_TO_BROWSER_HISTORY);
		
		verify(mockEntityMetadata).setEntityBundle(mockProjectBundle, null);
		
		verify(mockWikiTab).configure(eq(projectEntityId), eq(projectName), eq(projectWikiId), eq(canEdit), any(WikiPageWidget.Callback.class));
		verify(mockFilesTab).setProject(projectEntityId, mockProjectBundle, null);
		verify(mockFilesTab).configure(mockProjectEntity, mockEntityUpdatedHandler, versionNumber);
		verify(mockTablesTab).setProject(projectEntityId, mockProjectBundle, null);
		verify(mockTablesTab).configure(mockProjectEntity, mockEntityUpdatedHandler, areaToken);
		verify(mockChallengeTab).configure(projectEntityId, projectName);
		verify(mockDiscussionTab).configure(projectEntityId, projectName, areaToken, canModerate, moderatorIds);
		verify(mockEntityActionController).configure(mockActionMenuWidget, mockProjectBundle, true, projectWikiId, mockEntityUpdatedHandler);
	}
	
	@Test
	public void testConfigureWithProjectWikiToken(){
		Synapse.EntityArea area = EntityArea.WIKI;
		//verify this wiki id area token is passed to the wiki tab configuration and the entity action controller configuration
		String areaToken = "1234";
		Long versionNumber = null;
		pageTop.configure(mockProjectEntity, versionNumber, mockProjectHeader, area, areaToken);
		
		verify(mockWikiTab).configure(eq(projectEntityId), eq(projectName), eq(areaToken), eq(canEdit), any(WikiPageWidget.Callback.class));
		verify(mockFilesTab).setProject(projectEntityId, mockProjectBundle, null);
		verify(mockFilesTab).configure(mockProjectEntity, mockEntityUpdatedHandler, versionNumber);
		verify(mockTablesTab).setProject(projectEntityId, mockProjectBundle, null);
		verify(mockTablesTab).configure(mockProjectEntity, mockEntityUpdatedHandler, null);
		verify(mockChallengeTab).configure(projectEntityId, projectName);
		verify(mockDiscussionTab).configure(projectEntityId, projectName, null, canModerate, moderatorIds);
		verify(mockEntityActionController).configure(mockActionMenuWidget, mockProjectBundle, true, areaToken, mockEntityUpdatedHandler);
	}
	
	@Test
	public void testConfigureWithFile(){
		Synapse.EntityArea area = null;
		String areaToken = null;
		Long versionNumber = 5L;
		pageTop.configure(mockFileEntity, versionNumber, mockProjectHeader, area, areaToken);
		verify(mockTabs).showTab(mockFilesInnerTab, EntityPageTop.PUSH_TAB_URL_TO_BROWSER_HISTORY);
		verify(mockEntityMetadata).setEntityBundle(mockProjectBundle, null);
		
		verify(mockWikiTab).configure(eq(projectEntityId), eq(projectName), eq(projectWikiId), eq(canEdit), any(WikiPageWidget.Callback.class));
		verify(mockFilesTab).setProject(projectEntityId, mockProjectBundle, null);
		verify(mockFilesTab).configure(mockFileEntity, mockEntityUpdatedHandler, versionNumber);
		verify(mockTablesTab).setProject(projectEntityId, mockProjectBundle, null);
		verify(mockTablesTab).configure(mockFileEntity, mockEntityUpdatedHandler, areaToken);
		verify(mockChallengeTab).configure(projectEntityId, projectName);
		verify(mockDiscussionTab).configure(projectEntityId, projectName, null, canModerate, moderatorIds);
	}
	

	@Test
	public void testConfigureWithFileAndFailureToLoadProject(){
		Exception projectLoadError = new Exception("failed to load project");
		AsyncMockStubber.callFailureWith(projectLoadError).when(mockSynapseClientAsync).getEntityBundle(anyString(), anyInt(), any(AsyncCallback.class));
		Synapse.EntityArea area = null;
		String areaToken = null;
		Long versionNumber = 5L;
		pageTop.configure(mockFileEntity, versionNumber, mockProjectHeader, area, areaToken);
		verify(mockTabs).showTab(mockFilesInnerTab, EntityPageTop.PUSH_TAB_URL_TO_BROWSER_HISTORY);
		
		verify(mockEntityMetadata, Mockito.never()).setEntityBundle(mockProjectBundle, null);
		EntityBundle expectedProjectEntityBundle = null;
		verify(mockWikiTab).configure(eq(projectEntityId), eq(projectName), eq((String)null), eq(false), any(WikiPageWidget.Callback.class));
		verify(mockFilesTab).setProject(projectEntityId, expectedProjectEntityBundle, projectLoadError);
		verify(mockFilesTab).configure(mockFileEntity, mockEntityUpdatedHandler, versionNumber);
		verify(mockTablesTab).setProject(projectEntityId, expectedProjectEntityBundle, projectLoadError);
		verify(mockTablesTab).configure(mockFileEntity, mockEntityUpdatedHandler, areaToken);
		verify(mockChallengeTab).configure(projectEntityId, projectName);
		verify(mockDiscussionTab).configure(projectEntityId, projectName, null, false, moderatorIds);
	}
	
	@Test
	public void testConfigureWithTable(){
		Synapse.EntityArea area = null;
		String areaToken = "a table query area token";
		Long versionNumber = null;
		pageTop.configure(mockTableEntity, versionNumber, mockProjectHeader, area, areaToken);
		verify(mockTabs).showTab(mockTablesInnerTab, EntityPageTop.PUSH_TAB_URL_TO_BROWSER_HISTORY);
		
		verify(mockEntityMetadata).setEntityBundle(mockProjectBundle, null);
		
		
		verify(mockWikiTab).configure(eq(projectEntityId), eq(projectName), eq(projectWikiId), eq(canEdit), any(WikiPageWidget.Callback.class));
		verify(mockFilesTab).setProject(projectEntityId, mockProjectBundle, null);
		verify(mockFilesTab).configure(mockTableEntity, mockEntityUpdatedHandler, versionNumber);
		verify(mockTablesTab).setProject(projectEntityId, mockProjectBundle, null);
		verify(mockTablesTab).configure(mockTableEntity, mockEntityUpdatedHandler, areaToken);
		verify(mockChallengeTab).configure(projectEntityId, projectName);
		verify(mockDiscussionTab).configure(projectEntityId, projectName, null, canModerate, moderatorIds);
	}
	
	@Test
	public void testConfigureWithFileGoToChallengeAdminTab(){
		Synapse.EntityArea area = Synapse.EntityArea.ADMIN;
		String areaToken = null;
		Long versionNumber = null;
		pageTop.configure(mockFileEntity, versionNumber, mockProjectHeader, area, areaToken);
		verify(mockTabs).showTab(mockChallengeInnerTab, EntityPageTop.PUSH_TAB_URL_TO_BROWSER_HISTORY);
		
		verify(mockEntityMetadata).setEntityBundle(mockProjectBundle, null);
		
		verify(mockWikiTab).configure(eq(projectEntityId), eq(projectName), eq(projectWikiId), eq(canEdit), any(WikiPageWidget.Callback.class));
		verify(mockFilesTab).setProject(projectEntityId, mockProjectBundle, null);
		verify(mockFilesTab).configure(mockFileEntity, mockEntityUpdatedHandler, versionNumber);
		verify(mockTablesTab).setProject(projectEntityId, mockProjectBundle, null);
		verify(mockTablesTab).configure(mockFileEntity, mockEntityUpdatedHandler, areaToken);
		verify(mockChallengeTab).configure(projectEntityId, projectName);
		verify(mockDiscussionTab).configure(projectEntityId, projectName, null, canModerate, moderatorIds);
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
}
