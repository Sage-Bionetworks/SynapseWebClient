package org.sagebionetworks.web.unitclient.widget.entity;

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.repo.model.EntityBundle;
import org.sagebionetworks.repo.model.FileEntity;
import org.sagebionetworks.repo.model.Project;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.entity.EntityMetadata;
import org.sagebionetworks.web.client.widget.entity.EntityPageTop;
import org.sagebionetworks.web.client.widget.entity.EntityPageTopView;
import org.sagebionetworks.web.client.widget.entity.controller.EntityActionController;
import org.sagebionetworks.web.client.widget.entity.menu.v2.ActionMenuWidget;
import org.sagebionetworks.web.client.widget.entity.tabs.ChallengeTab;
import org.sagebionetworks.web.client.widget.entity.tabs.FilesTab;
import org.sagebionetworks.web.client.widget.entity.tabs.Tab;
import org.sagebionetworks.web.client.widget.entity.tabs.TablesTab;
import org.sagebionetworks.web.client.widget.entity.tabs.Tabs;
import org.sagebionetworks.web.client.widget.entity.tabs.WikiTab;

import com.google.gwt.user.client.ui.Widget;

public class EntityPageTopTest {

	@Mock
	EntityPageTopView mockView;
	@Mock
	EntityBundle entityBundle;
	@Mock
	Project projectEntity;
	@Mock
	FileEntity fileEntity;
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
	EntityActionController mockEntityActionController;
	@Mock
	ActionMenuWidget mockActionMenuWidget;
	
	EntityPageTop pageTop;
	String entityId = "syn123";
	Long entityVersion = 1L;
	
	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
		when(mockFilesTab.asTab()).thenReturn(mockFilesInnerTab);
		when(mockWikiTab.asTab()).thenReturn(mockWikiInnerTab);
		when(mockTablesTab.asTab()).thenReturn(mockTablesInnerTab);
		when(mockChallengeTab.asTab()).thenReturn(mockChallengeInnerTab);
		
		pageTop = new EntityPageTop(mockView, mockSynapseClientAsync, mockTabs, mockEntityMetadata,
				mockWikiTab, mockFilesTab, mockTablesTab, mockChallengeTab, mockEntityActionController, 
				mockActionMenuWidget);
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
	}
	
	@Test
	public void testConfigure(){
		
	}
	
}
