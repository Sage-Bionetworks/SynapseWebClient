package org.sagebionetworks.web.unitclient.presenter;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.util.Collections;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.repo.model.EntityHeader;
import org.sagebionetworks.repo.model.EntityPath;
import org.sagebionetworks.repo.model.FileEntity;
import org.sagebionetworks.repo.model.Project;
import org.sagebionetworks.repo.model.entitybundle.v2.EntityBundle;
import org.sagebionetworks.repo.model.entitybundle.v2.EntityBundleRequest;
import org.sagebionetworks.repo.model.file.FileHandleResults;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.GWTWrapper;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.PlaceChanger;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.SynapseJavascriptClient;
import org.sagebionetworks.web.client.cache.EntityId2BundleCache;
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
	EntityId2BundleCache mockEntityId2BundleCache;
	@Mock
	EventBus mockEventBus;
	@Mock
	Synapse mockPlace;
	@Mock
	FileEntity mockFileEntity;

	@Before
	public void setup() throws Exception {
		when(mockEntityPresenterEventBinder.getEventBinder()).thenReturn(mockEventBinder);
		when(mockGlobalApplicationState.getPlaceChanger()).thenReturn(mockPlaceChanger);
		entityPresenter = new EntityPresenter(mockView, mockEntityPresenterEventBinder, mockGlobalApplicationState, mockAuthenticationController, mockSynapseJavascriptClient, mockSynAlert, mockEntityPageTop, mockHeaderWidget, mockOpenInviteWidget, mockGwtWrapper, mockEventBus);
		Entity testEntity = new Project();
		eb = new EntityBundle();
		eb.setEntity(testEntity);
		EntityPath path = new EntityPath();
		path.setPath(Collections.singletonList(mockProjectEntityHeader));
		when(mockProjectEntityHeader.getType()).thenReturn(Project.class.getName());
		eb.setPath(path);
		AsyncMockStubber.callSuccessWith(eb).when(mockSynapseJavascriptClient).getEntityBundle(anyString(), any(EntityBundleRequest.class), any(AsyncCallback.class));
		AsyncMockStubber.callSuccessWith(eb).when(mockSynapseJavascriptClient).getEntityBundleForVersion(anyString(), anyLong(), any(EntityBundleRequest.class), any(AsyncCallback.class));
		AsyncMockStubber.callSuccessWith(testEntity).when(mockSynapseJavascriptClient).getEntity(anyString(),any(AsyncCallback.class));
		id = 0L;
	}

	@Test
	public void testConstruction() {
		verify(mockHeaderWidget, never()).configure(); // waits to configure for entity header
		verify(mockEventBinder).bindEventHandlers(entityPresenter, mockEventBus);
	}

	@Test
	public void testSetPlaceAndRefreshWithVersion() {
		Long versionNumber = 1L;
		when(mockPlace.getVersionNumber()).thenReturn(1L);
		when(mockPlace.getEntityId()).thenReturn(entityId);

		entityPresenter.setPlace(mockPlace);
		// verify that background image is cleared
		verify(mockSynapseJavascriptClient).getEntityBundleForVersion(eq(entityId), eq(versionNumber), eq(EntityPageTop.ALL_PARTS_REQUEST), any(AsyncCallback.class));
		verify(mockView, times(2)).setLoadingVisible(Mockito.anyBoolean());
		verify(mockView).setEntityPageTopVisible(true);
		verify(mockEntityPageTop, atLeastOnce()).clearState();
		verify(mockEntityPageTop).configure(eq(eb), eq(versionNumber), any(EntityHeader.class), any(EntityArea.class), anyString());
		verify(mockView, times(2)).setEntityPageTopWidget(mockEntityPageTop);
		verify(mockView).setOpenTeamInvitesWidget(mockOpenInviteWidget);
		verify(mockHeaderWidget).refresh();
		verify(mockHeaderWidget).configure(any(EntityHeader.class));
		verify(mockSynAlert, times(3)).clear();
	}
	
	@Test
	public void testRefreshWithCurrentFileVersion() {
		Long versionNumber = 3L;
		when(mockPlace.getVersionNumber()).thenReturn(versionNumber);
		when(mockPlace.getEntityId()).thenReturn(entityId);
		when(mockFileEntity.getVersionNumber()).thenReturn(versionNumber);
		AsyncMockStubber.callSuccessWith(mockFileEntity).when(mockSynapseJavascriptClient).getEntity(anyString(),any(AsyncCallback.class));

		entityPresenter.setPlace(mockPlace);
		
		verify(mockSynapseJavascriptClient).getEntityBundle(eq(entityId), eq(EntityPageTop.ALL_PARTS_REQUEST), any(AsyncCallback.class));
	}


	@Test
	public void testSetPlaceAndRefreshWithoutVersion() {
		Long versionNumber = 1L;
		Synapse place = Mockito.mock(Synapse.class);
		when(place.getVersionNumber()).thenReturn(versionNumber);
		when(place.getEntityId()).thenReturn(entityId);
		entityPresenter.setPlace(place);
		// verify that background image is cleared
		verify(mockSynapseJavascriptClient).getEntityBundleForVersion(eq(entityId), eq(versionNumber), any(EntityBundleRequest.class), any(AsyncCallback.class));
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

		verify(mockSynapseJavascriptClient).getEntityBundleForVersion(eq(entityId), eq(versionNumber), any(EntityBundleRequest.class), any(AsyncCallback.class));
		verify(mockSynAlert).showError(DisplayConstants.ERROR_GENERIC_RELOAD);
	}

	@Test
	public void testStart() {
		entityPresenter.setPlace(mockPlace);
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
		assertFalse(EntityPresenter.isValidEntityId(""));
		assertFalse(EntityPresenter.isValidEntityId(null));
		assertFalse(EntityPresenter.isValidEntityId("syn"));
		assertFalse(EntityPresenter.isValidEntityId("sy"));
		assertFalse(EntityPresenter.isValidEntityId("synFOOBAR"));
		assertTrue(EntityPresenter.isValidEntityId("SyN198327"));
	}
}
