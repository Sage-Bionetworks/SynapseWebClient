package org.sagebionetworks.web.unitclient.widget.entity;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.sagebionetworks.repo.model.EntityHeader;
import org.sagebionetworks.repo.model.ExampleEntity;
import org.sagebionetworks.repo.model.Project;
import org.sagebionetworks.repo.model.attachment.AttachmentData;
import org.sagebionetworks.schema.adapter.JSONObjectAdapter;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.schema.adapter.org.json.JSONObjectAdapterImpl;
import org.sagebionetworks.web.client.EntitySchemaCache;
import org.sagebionetworks.web.client.EntityTypeProvider;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.IconsImageBundle;
import org.sagebionetworks.web.client.PlaceChanger;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.model.EntityBundle;
import org.sagebionetworks.web.client.place.Synapse;
import org.sagebionetworks.web.client.place.Synapse.EntityArea;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.transform.NodeModelCreator;
import org.sagebionetworks.web.client.widget.WidgetRendererPresenter;
import org.sagebionetworks.web.client.widget.entity.EntityPageTop;
import org.sagebionetworks.web.client.widget.entity.EntityPageTopView;
import org.sagebionetworks.web.client.widget.entity.JiraURLHelper;
import org.sagebionetworks.web.client.widget.entity.registration.WidgetConstants;
import org.sagebionetworks.web.client.widget.entity.registration.WidgetRegistrar;
import org.sagebionetworks.web.client.widget.entity.renderer.YouTubeWidget;
import org.sagebionetworks.web.client.widget.entity.renderer.YouTubeWidgetView;
import org.sagebionetworks.web.shared.WikiPageKey;

import com.google.gwt.event.shared.EventBus;

public class EntityPageTopTest {

	SynapseClientAsync mockSynapseClient;
	AuthenticationController mockAuthenticationController;
	NodeModelCreator mockNodeModelCreator;
	GlobalApplicationState mockGlobalApplicationState;
	PlaceChanger mockPlaceChanger;
	EntityPageTopView mockView;
	EntitySchemaCache mockSchemaCache;
	JSONObjectAdapter jsonObjectAdapter;
	EntityTypeProvider mockEntityTypeProvider;
	IconsImageBundle mockIconsImageBundle;
	EventBus mockEventBus;
	JiraURLHelper mockJiraURLHelper;
	WidgetRegistrar mockWidgetRegistrar;
	EntityPageTop pageTop;
	ExampleEntity entity;
	AttachmentData attachment1;
	WidgetRendererPresenter testWidgetRenderer;
	String entityId = "syn123";
	Long entityVersion = 1L;
	String projectId = "syn456";
	EntityBundle entityBundle;
	EntityHeader projectHeader;
	
	@Before
	public void before() throws JSONObjectAdapterException {
		mockAuthenticationController = mock(AuthenticationController.class);
		mockGlobalApplicationState = mock(GlobalApplicationState.class);
		mockPlaceChanger = mock(PlaceChanger.class);
		when(mockGlobalApplicationState.getPlaceChanger()).thenReturn(mockPlaceChanger);
		mockNodeModelCreator = mock(NodeModelCreator.class);
		
		mockSynapseClient = mock(SynapseClientAsync.class);
		mockView = mock(EntityPageTopView.class);
		mockSchemaCache = mock(EntitySchemaCache.class);
		jsonObjectAdapter = new JSONObjectAdapterImpl();
		mockEntityTypeProvider = mock(EntityTypeProvider.class);
		mockIconsImageBundle = mock(IconsImageBundle.class);
		mockEventBus = mock(EventBus.class);
		mockJiraURLHelper = mock(JiraURLHelper.class);
		mockWidgetRegistrar = mock(WidgetRegistrar.class);
		pageTop = new EntityPageTop(mockView, mockSynapseClient,
				mockNodeModelCreator, mockAuthenticationController,
				mockSchemaCache,
				mockEntityTypeProvider,
				mockIconsImageBundle, 
				mockWidgetRegistrar, 
				mockGlobalApplicationState, mockEventBus, new JSONObjectAdapterImpl());
		
		// Setup the the entity
		entity = new ExampleEntity();
		entity.setId(entityId);
		entity.setEntityType(ExampleEntity.class.getName());
		List<AttachmentData> entityAttachments = new ArrayList<AttachmentData>();
		String attachment1Name = "attachment1";
		attachment1 = new AttachmentData();
		attachment1.setName(attachment1Name);
		attachment1.setTokenId("token1");
		attachment1.setContentType(WidgetConstants.YOUTUBE_CONTENT_TYPE);
		entityAttachments.add(attachment1);
		entity.setAttachments(entityAttachments);
		testWidgetRenderer = new YouTubeWidget(mock(YouTubeWidgetView.class));
		when(mockWidgetRegistrar.getWidgetRendererForWidgetDescriptor(any(WikiPageKey.class), anyString(), any(Map.class), anyBoolean())).thenReturn(testWidgetRenderer);

		entityBundle = new EntityBundle(entity, null, null, null, null, null, null, null);
		projectHeader = new EntityHeader();
		projectHeader.setId(projectId);
		
		pageTop.configure(entityBundle, entityVersion, projectHeader, EntityArea.FILES, null);
	}
	
	@Test 
	public void testConfigureProjectAreaStateAndGotoProjectArea() {
		ArgumentCaptor<Synapse> capture;
		Synapse gotoPlace;
		EntityBundle projectBundle;
		
		// fresh project, non project entity
		assertTrue(pageTop.isPlaceChangeForArea(EntityArea.FILES));
		assertFalse(pageTop.isPlaceChangeForArea(EntityArea.WIKI));
		// click files to test
		pageTop.gotoProjectArea(EntityArea.FILES);
		capture = ArgumentCaptor.forClass(Synapse.class);
		verify(mockPlaceChanger).goTo(capture.capture());
		gotoPlace = capture.getValue();
		assertEquals(EntityArea.FILES, gotoPlace.getArea());
		assertEquals(entityId, gotoPlace.getEntityId());
		assertEquals(entityVersion, gotoPlace.getVersionNumber());		
			
		// now visit a specific wiki sub page
		reset(mockPlaceChanger);
		String wikiSubpage = "987654";
		Project projectEntity = new Project();
		projectEntity.setId(projectId);
		projectBundle = new EntityBundle(projectEntity, null, null, null, null, null, null, null);
		pageTop.configure(projectBundle, entityVersion, projectHeader, EntityArea.WIKI, wikiSubpage);
		assertTrue(pageTop.isPlaceChangeForArea(EntityArea.FILES));
		assertFalse(pageTop.isPlaceChangeForArea(EntityArea.WIKI));
		// click wiki to test
		pageTop.gotoProjectArea(EntityArea.WIKI);
		capture = ArgumentCaptor.forClass(Synapse.class);
		verify(mockPlaceChanger).goTo(capture.capture());
		gotoPlace = capture.getValue();
		assertEquals(EntityArea.WIKI, gotoPlace.getArea());
		assertEquals(projectId, gotoPlace.getEntityId());
		assertEquals(wikiSubpage, gotoPlace.getAreaToken());
		
		// lets visit the default project wiki now
		reset(mockPlaceChanger);
		projectBundle = new EntityBundle(projectEntity, null, null, null, null, null, null, null);
		pageTop.configure(projectBundle, entityVersion, projectHeader, EntityArea.WIKI, null);
		assertTrue(pageTop.isPlaceChangeForArea(EntityArea.FILES));
		assertFalse(pageTop.isPlaceChangeForArea(EntityArea.WIKI));
		// click wiki to test
		pageTop.gotoProjectArea(EntityArea.WIKI);
		capture = ArgumentCaptor.forClass(Synapse.class);
		verify(mockPlaceChanger).goTo(capture.capture());
		gotoPlace = capture.getValue();
		assertEquals(EntityArea.WIKI, gotoPlace.getArea());
		assertEquals(projectId, gotoPlace.getEntityId());		
		assertNull(gotoPlace.getAreaToken());
		
		// visit wiki subpage then visit project with null area
		reset(mockPlaceChanger);
		pageTop.configure(projectBundle, entityVersion, projectHeader, EntityArea.WIKI, wikiSubpage);
		pageTop.configure(projectBundle, entityVersion, projectHeader, null, null);
		assertTrue(pageTop.isPlaceChangeForArea(EntityArea.FILES));
		assertFalse(pageTop.isPlaceChangeForArea(EntityArea.WIKI));
		// click wiki to test
		pageTop.gotoProjectArea(null);
		capture = ArgumentCaptor.forClass(Synapse.class);
		verify(mockPlaceChanger).goTo(capture.capture());
		gotoPlace = capture.getValue();
		assertNull(gotoPlace.getArea());
		assertEquals(projectId, gotoPlace.getEntityId());		
		assertNull(gotoPlace.getAreaToken());
		
		// with wiki subpage state and file entity state, on wiki tab, make sure wiki subpage still exists
		reset(mockPlaceChanger);
		pageTop.configure(projectBundle, entityVersion, projectHeader, EntityArea.WIKI, wikiSubpage);
		pageTop.configure(entityBundle, entityVersion, projectHeader, EntityArea.FILES, null);
		assertTrue(pageTop.isPlaceChangeForArea(EntityArea.FILES));
		assertTrue(pageTop.isPlaceChangeForArea(EntityArea.WIKI));
		// click wiki to test
		pageTop.gotoProjectArea(EntityArea.WIKI);
		capture = ArgumentCaptor.forClass(Synapse.class);
		verify(mockPlaceChanger).goTo(capture.capture());
		gotoPlace = capture.getValue();
		assertEquals(EntityArea.WIKI, gotoPlace.getArea());
		assertEquals(projectId, gotoPlace.getEntityId());		
		assertEquals(wikiSubpage, gotoPlace.getAreaToken());
		
		
		// visit a new project, make sure all area state is wiped out
		reset(mockPlaceChanger);
		String newProjectId = "syn789";
		assertFalse(newProjectId.equals(projectId)); // assumption check
		projectEntity = new Project();
		projectEntity.setId(newProjectId);
		projectBundle = new EntityBundle(projectEntity, null, null, null, null, null, null, null);
		EntityHeader newProjectHeader = new EntityHeader();
		newProjectHeader.setId(newProjectId);
		pageTop.configure(projectBundle, entityVersion, newProjectHeader, EntityArea.WIKI, null);
		assertFalse(pageTop.isPlaceChangeForArea(EntityArea.FILES));
		assertFalse(pageTop.isPlaceChangeForArea(EntityArea.WIKI));
		// click files to test
		pageTop.gotoProjectArea(EntityArea.FILES);
		capture = ArgumentCaptor.forClass(Synapse.class);
		verify(mockPlaceChanger).goTo(capture.capture());
		gotoPlace = capture.getValue();
		assertEquals(EntityArea.FILES, gotoPlace.getArea());
		assertEquals(newProjectId, gotoPlace.getEntityId());
		assertNull(gotoPlace.getVersionNumber());			
		
		// visit wiki subpage and assure switch to default files view and back is not place change
		pageTop.configure(projectBundle, entityVersion, newProjectHeader, EntityArea.WIKI, wikiSubpage);
		assertFalse(pageTop.isPlaceChangeForArea(EntityArea.FILES));
		assertFalse(pageTop.isPlaceChangeForArea(EntityArea.WIKI));
		

	}
}
