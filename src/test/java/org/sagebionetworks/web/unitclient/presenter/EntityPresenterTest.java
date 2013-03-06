package org.sagebionetworks.web.unitclient.presenter;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;

import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.repo.model.UserSessionData;
import org.sagebionetworks.schema.adapter.AdapterFactory;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.schema.adapter.org.json.AdapterFactoryImpl;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.place.Synapse;
import org.sagebionetworks.web.client.presenter.EntityPresenter;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.transform.NodeModelCreator;
import org.sagebionetworks.web.client.view.EntityView;
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
	AdapterFactory adapterFactory = new AdapterFactoryImpl();
	String EntityId = "1";
	Synapse place = new Synapse("Synapse:"+ EntityId);
	Entity EntityModel1;

	
	@Before
	public void setup(){
		mockView = mock(EntityView.class);
		mockGlobalApplicationState = mock(GlobalApplicationState.class);
		mockAuthenticationController = mock(AuthenticationController.class);
		mockSynapseClient = mock(SynapseClientAsync.class);
		mockNodeModelCreator = mock(NodeModelCreator.class);
		entityPresenter = new EntityPresenter(mockView, mockGlobalApplicationState, mockAuthenticationController, mockSynapseClient, mockNodeModelCreator, adapterFactory);

		verify(mockView).setPresenter(entityPresenter);
	}	
	
	@Test
	public void testSetPlace() {
		resetMocks();
		Synapse place = Mockito.mock(Synapse.class);
		entityPresenter.setPlace(place);
		
		verify(mockView).setPresenter(entityPresenter);
	}	
	
	@Test
	public void testStart() {
		resetMocks();
		entityPresenter = new EntityPresenter(mockView, mockGlobalApplicationState, mockAuthenticationController, mockSynapseClient, mockNodeModelCreator, adapterFactory);
		entityPresenter.setPlace(place);		
		AcceptsOneWidget panel = mock(AcceptsOneWidget.class);
		EventBus eventBus = mock(EventBus.class);		
		
		entityPresenter.start(panel, eventBus);		
		verify(panel).setWidget(mockView);		
	}
	
	private void resetMocks() {
		reset(mockView);
		reset(mockGlobalApplicationState);
	}
	
}
