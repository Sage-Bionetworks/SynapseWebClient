package org.sagebionetworks.web.unitclient.presenter;

import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import java.util.Collections;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.repo.model.EntityBundle;
import org.sagebionetworks.repo.model.EntityHeader;
import org.sagebionetworks.repo.model.EntityPath;
import org.sagebionetworks.repo.model.Project;
import org.sagebionetworks.repo.model.file.FileHandleResults;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.GWTWrapper;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.PlaceChanger;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.SynapseJavascriptClient;
import org.sagebionetworks.web.client.events.EntityUpdatedEvent;
import org.sagebionetworks.web.client.place.Synapse;
import org.sagebionetworks.web.client.place.Synapse.EntityArea;
import org.sagebionetworks.web.client.presenter.EntityPresenter;
import org.sagebionetworks.web.client.presenter.EntityPresenterEventBinder;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.view.EntityView;
import org.sagebionetworks.web.client.widget.entity.EntityPageTop;
import org.sagebionetworks.web.client.widget.entity.controller.StuAlert;
import org.sagebionetworks.web.client.widget.header.Header;
import org.sagebionetworks.web.client.widget.team.OpenTeamInvitationsWidget;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.web.bindery.event.shared.binder.EventBinder;

@RunWith(MockitoJUnitRunner.class)
public class EntityPresenterTest {
	@Mock
	SynapseJavascriptClient mockSynapseJavascriptClient;
	@Mock
	EventBinder mockEventBinder;
	@Mock
	EntityPresenterEventBinder mockEntityPresenterEventBinder;
	EntityPresenter entityPresenter;
	@Mock
	EntityView mockView;
	@Mock
	GlobalApplicationState mockGlobalApplicationState;
	@Mock
	AuthenticationController mockAuthenticationController;
	@Mock
	PlaceChanger mockPlaceChanger;
	@Mock
	SynapseJSNIUtils mockSynapseJSNIUtils;
	@Mock
	StuAlert mockSynAlert;
	@Mock
	OpenTeamInvitationsWidget mockOpenInviteWidget;
	@Mock
	Header mockHeaderWidget;
	@Mock
	EntityPageTop mockEntityPageTop;
	String EntityId = "1";
	Synapse place = new Synapse("Synapse:"+ EntityId);
	Entity EntityModel1;
	EntityBundle eb;
	String entityId = "syn43344";
	Synapse.EntityArea area = Synapse.EntityArea.FILES;
	String areaToken = null;
	long id;
	@Mock
	EntityHeader mockProjectEntityHeader;
	String rootWikiId = "12333";
	FileHandleResults rootWikiAttachments;
	@Mock
	GWTWrapper mockGwtWrapper;
	@Mock
	EventBus mockEventBus;
	
	@Before
	public void setup() throws Exception{
		when(mockEntityPresenterEventBinder.getEventBinder()).thenReturn(mockEventBinder);
		when(mockGlobalApplicationState.getPlaceChanger()).thenReturn(mockPlaceChanger);
		entityPresenter = new EntityPresenter(mockView, mockEntityPresenterEventBinder, mockGlobalApplicationState, mockAuthenticationController, mockSynapseJavascriptClient,
				mockSynAlert, mockEntityPageTop, mockHeaderWidget, mockOpenInviteWidget, mockGwtWrapper, mockEventBus);
		Entity testEntity = new Project();
		eb = new EntityBundle();
		eb.setEntity(testEntity);
		EntityPath path = new EntityPath();
		path.setPath(Collections.singletonList(mockProjectEntityHeader));
		when(mockProjectEntityHeader.getType()).thenReturn(Project.class.getName());
		eb.setPath(path);
		AsyncMockStubber.callSuccessWith(eb).when(mockSynapseJavascriptClient).getEntityBundle(anyString(), anyInt(), any(AsyncCallback.class));
		AsyncMockStubber.callSuccessWith(eb).when(mockSynapseJavascriptClient).getEntityBundleForVersion(anyString(), anyLong(), anyInt(), any(AsyncCallback.class));
		id=0L;
	}	
	
	@Test
	public void testConstruction() {
		verify(mockHeaderWidget, never()).configure(); // waits to configure for entity header
		verify(mockEventBinder).bindEventHandlers(entityPresenter, mockEventBus);
	}
	
	@Test
	public void testSetPlaceAndRefreshWithVersion() {
		Long versionNumber = 1L;
		Synapse place = Mockito.mock(Synapse.class);
		when(place.getVersionNumber()).thenReturn(1L);
		when(place.getEntityId()).thenReturn(entityId);
		
		entityPresenter.setPlace(place);
		//verify that background image is cleared
		verify(mockSynapseJavascriptClient).getEntityBundleForVersion(eq(entityId), eq(versionNumber), eq(EntityPageTop.ALL_PARTS_MASK), any(AsyncCallback.class));
		verify(mockView, times(2)).setLoadingVisible(Mockito.anyBoolean());
		verify(mockView).setEntityPageTopVisible(true);
		verify(mockEntityPageTop, atLeastOnce()).clearState();
		verify(mockEntityPageTop).configure(eq(eb), eq(versionNumber), any(EntityHeader.class), any(EntityArea.class), anyString());
		verify(mockView, times(2)).setEntityPageTopWidget(mockEntityPageTop);
		verify(mockView).setOpenTeamInvitesWidget(mockOpenInviteWidget);
		verify(mockHeaderWidget).refresh();
		verify(mockHeaderWidget).configure(any(EntityHeader.class));
	}
	
	@Test
	public void testSetPlaceAndRefreshWithoutVersion() {
		Long versionNumber = 1L;
		Synapse place = Mockito.mock(Synapse.class);
		when(place.getVersionNumber()).thenReturn(versionNumber);
		when(place.getEntityId()).thenReturn(entityId);
		entityPresenter.setPlace(place);
		//verify that background image is cleared
		verify(mockSynapseJavascriptClient).getEntityBundleForVersion(eq(entityId), eq(versionNumber), anyInt(), any(AsyncCallback.class));
		verify(mockView, times(2)).setLoadingVisible(Mockito.anyBoolean());
		verify(mockView).setEntityPageTopVisible(true);
		verify(mockEntityPageTop, atLeastOnce()).clearState();
		verify(mockEntityPageTop).configure(eq(eb), eq(versionNumber), any(EntityHeader.class), any(EntityArea.class), anyString());
		
		verify(mockView, times(2)).setEntityPageTopWidget(mockEntityPageTop);
	}
	
	@Test
	public void testInvalidEntityPath() {
		EntityPath emptyPath = new EntityPath();
		emptyPath.setPath(Collections.EMPTY_LIST);
		eb.setPath(emptyPath);
		Long versionNumber = 1L;
		Synapse place = Mockito.mock(Synapse.class);
		when(place.getVersionNumber()).thenReturn(versionNumber);
		when(place.getEntityId()).thenReturn(entityId);
		
		entityPresenter.setPlace(place);
		
		verify(mockSynapseJavascriptClient).getEntityBundleForVersion(eq(entityId), eq(versionNumber), anyInt(), any(AsyncCallback.class));
		verify(mockSynAlert).showError(DisplayConstants.ERROR_GENERIC_RELOAD);
	}
	
	@Test
	public void testStart() {
		entityPresenter.setPlace(place);		
		AcceptsOneWidget panel = mock(AcceptsOneWidget.class);
		EventBus eventBus = mock(EventBus.class);		
		entityPresenter.start(panel, eventBus);		
		verify(panel).setWidget(mockView);		
	}
	
	@Test
	public void testClear() {
		entityPresenter.clear();
		verify(mockView, times(2)).clear();
		verify(mockSynAlert, times(2)).clear();
		verify(mockOpenInviteWidget, times(2)).clear();
	}
	
	@Test
	public void testShow403() {
		entityPresenter.setEntityId("123");
		entityPresenter.show403();
		verify(mockSynAlert).show403(anyString());
		verify(mockView).setEntityPageTopVisible(false);
		verify(mockView).setOpenTeamInvitesVisible(true);
	}
	
	@Test
	public void testEntityUpdatedHandler() {
		entityPresenter.onEntityUpdatedEvent(new EntityUpdatedEvent());
		
		verify(mockGlobalApplicationState).refreshPage();
	}
	
	@Test
	public void testIsValidEntityId() {
		assertFalse(entityPresenter.isValidEntityId(""));
		assertFalse(entityPresenter.isValidEntityId(null));
		assertFalse(entityPresenter.isValidEntityId("syn"));
		assertFalse(entityPresenter.isValidEntityId("sy"));
		assertFalse(entityPresenter.isValidEntityId("synFOOBAR"));
		assertTrue(entityPresenter.isValidEntityId("SyN198327"));
	}
}
