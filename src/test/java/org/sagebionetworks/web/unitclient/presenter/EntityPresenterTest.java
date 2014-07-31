package org.sagebionetworks.web.unitclient.presenter;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.repo.model.EntityHeader;
import org.sagebionetworks.repo.model.EntityPath;
import org.sagebionetworks.repo.model.FileEntity;
import org.sagebionetworks.repo.model.ObjectType;
import org.sagebionetworks.repo.model.Project;
import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.repo.model.UserSessionData;
import org.sagebionetworks.schema.adapter.AdapterFactory;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.schema.adapter.org.json.AdapterFactoryImpl;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.PlaceChanger;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.cookie.CookieProvider;
import org.sagebionetworks.web.client.model.EntityBundle;
import org.sagebionetworks.web.client.place.Synapse;
import org.sagebionetworks.web.client.place.Wiki;
import org.sagebionetworks.web.client.presenter.EntityPresenter;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.transform.NodeModelCreator;
import org.sagebionetworks.web.client.view.EntityView;
import org.sagebionetworks.web.shared.EntityBundleTransport;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;

public class EntityPresenterTest {
	
	EntityPresenter entityPresenter;
	EntityView mockView;
	GlobalApplicationState mockGlobalApplicationState;
	AuthenticationController mockAuthenticationController;
	SynapseClientAsync mockSynapseClient;
	NodeModelCreator mockNodeModelCreator;
	CookieProvider mockCookies;
	PlaceChanger mockPlaceChanger;
	AdapterFactory adapterFactory = new AdapterFactoryImpl();
	String EntityId = "1";
	Synapse place = new Synapse("Synapse:"+ EntityId);
	Entity EntityModel1;
	EntityBundle eb;
	EntityBundleTransport ebt;
	String entityId = "syn43344";
	Synapse.EntityArea area = Synapse.EntityArea.FILES;
	String areaToken = null;
	
	@Before
	public void setup() throws Exception{
		mockView = mock(EntityView.class);
		mockGlobalApplicationState = mock(GlobalApplicationState.class);
		mockPlaceChanger = mock(PlaceChanger.class);
		when(mockGlobalApplicationState.getPlaceChanger()).thenReturn(mockPlaceChanger);
		mockAuthenticationController = mock(AuthenticationController.class);
		mockSynapseClient = mock(SynapseClientAsync.class);
		mockNodeModelCreator = mock(NodeModelCreator.class);
		mockCookies = mock(CookieProvider.class);
		entityPresenter = new EntityPresenter(mockView, mockGlobalApplicationState, mockAuthenticationController, mockSynapseClient, mockNodeModelCreator, adapterFactory, mockCookies);
		ebt = new EntityBundleTransport();
		ebt.setIsWikiBasedEntity(false);
		Entity testEntity = new Project();
		eb = new EntityBundle(testEntity, null, null, null, null, null, null, null);
		EntityPath path = new EntityPath();
		path.setPath(new ArrayList<EntityHeader>());
		eb.setPath(path);
		AsyncMockStubber.callSuccessWith(ebt).when(mockSynapseClient).getEntityBundle(anyString(), anyInt(), any(AsyncCallback.class));
		AsyncMockStubber.callSuccessWith(ebt).when(mockSynapseClient).getEntityBundleForVersion(anyString(), anyLong(), anyInt(), any(AsyncCallback.class));
		when(mockNodeModelCreator.createEntityBundle(eq(ebt))).thenReturn(eb);
		verify(mockView).setPresenter(entityPresenter);
	}	
	
	@Test
	public void testSetPlace() {
		Synapse place = Mockito.mock(Synapse.class);
		entityPresenter.setPlace(place);
		verify(mockView, times(2)).setPresenter(entityPresenter);
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
	public void testGetEntityBundle() {
		Long version = null;
		Synapse place = new Synapse(entityId, version, area, areaToken);
		entityPresenter.setPlace(place);
		//verify synapse client call
		verify(mockSynapseClient).getEntityBundle(eq(entityId), anyInt(), any(AsyncCallback.class));
		verify(mockView).setEntityBundle(eq(eb), eq(version), any(EntityHeader.class), eq(area), eq(areaToken));
	}
	
	@Test
	public void testGetEntityBundleForVersion() {
		Long version = 42L;
		Synapse place = new Synapse(entityId, version, area, areaToken);
		entityPresenter.setPlace(place);
		//verify synapse client call
		verify(mockSynapseClient).getEntityBundleForVersion(eq(entityId), eq(version), anyInt(), any(AsyncCallback.class));
		verify(mockView).setEntityBundle(eq(eb), eq(version), any(EntityHeader.class), eq(area), eq(areaToken));
	}
	
	@Test
	public void testWikiBasedEntity() {
		ebt.setIsWikiBasedEntity(true);
		Long version = null;
		Synapse place = new Synapse(entityId, version, area, areaToken);
		entityPresenter.setPlace(place);
		//verify synapse client call
		verify(mockSynapseClient).getEntityBundle(eq(entityId), anyInt(), any(AsyncCallback.class));
		//redirects to the wiki place
		verify(mockPlaceChanger).goTo(any(Wiki.class));
		//view's setEntityBundle is never called
		verify(mockView, never()).setEntityBundle(eq(eb), eq(version), any(EntityHeader.class), eq(area), eq(areaToken));
	}
	
	@Test
	public void testWikiBasedEntityInTestWebsite() {
		//will show full project page for wiki based entities when in alpha mode
		ebt.setIsWikiBasedEntity(true);
		when(mockCookies.getCookie(eq(DisplayUtils.SYNAPSE_TEST_WEBSITE_COOKIE_KEY))).thenReturn("true");
		Long version = null;
		Synapse place = new Synapse(entityId, version, area, areaToken);
		entityPresenter.setPlace(place);
		//verify synapse client call
		verify(mockSynapseClient).getEntityBundle(eq(entityId), anyInt(), any(AsyncCallback.class));
		verify(mockView).setEntityBundle(eq(eb), eq(version), any(EntityHeader.class), eq(area), eq(areaToken));
	}
	
}
