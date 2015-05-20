package org.sagebionetworks.web.unitclient.presenter;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.sagebionetworks.repo.model.Annotations;
import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.repo.model.Project;
import org.sagebionetworks.schema.adapter.AdapterFactory;
import org.sagebionetworks.schema.adapter.JSONObjectAdapter;
import org.sagebionetworks.schema.adapter.org.json.AdapterFactoryImpl;
import org.sagebionetworks.schema.adapter.org.json.JSONObjectAdapterImpl;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.PlaceChanger;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.place.ProjectsHome;
import org.sagebionetworks.web.client.place.Synapse;
import org.sagebionetworks.web.client.presenter.ProjectsHomePresenter;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.view.ProjectsHomeView;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.shared.exceptions.ConflictException;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;

import com.google.gwt.place.shared.Place;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class ProjectsHomePresenterTest {

	ProjectsHomePresenter projectsHomePresenter;
	ProjectsHomeView mockView;
	AuthenticationController mockAuthenticationController;
	GlobalApplicationState mockGlobalApplicationState;
	JSONObjectAdapter jsonObjectAdapter = new JSONObjectAdapterImpl();
	SynapseClientAsync mockSynapseClient;
	PlaceChanger mockPlaceChanger;
	SynapseAlert mockSynAlert;
	AdapterFactory adapterFactory = new AdapterFactoryImpl();	
	
	@Before
	public void setup(){
		mockView = mock(ProjectsHomeView.class);
		mockAuthenticationController = mock(AuthenticationController.class);
		mockGlobalApplicationState = mock(GlobalApplicationState.class);
		mockPlaceChanger = mock(PlaceChanger.class);
		mockSynAlert = mock(SynapseAlert.class);
		when(mockGlobalApplicationState.getPlaceChanger()).thenReturn(mockPlaceChanger);
		mockSynapseClient = mock(SynapseClientAsync.class);	
		projectsHomePresenter = new ProjectsHomePresenter(mockView,
				mockGlobalApplicationState, 
				mockAuthenticationController,
				mockSynapseClient, 
				adapterFactory, mockSynAlert);
		verify(mockView).setPresenter(projectsHomePresenter);
	}	
	
	@Test
	public void testSetPlace() {
		ProjectsHome place = Mockito.mock(ProjectsHome.class);
		projectsHomePresenter.setPlace(place);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testCreateProject() throws Exception {
		String newId = "syn123";
		String name = "name";
		
		AsyncMockStubber.callSuccessWith(newId).when(mockSynapseClient).createOrUpdateEntity(any(Entity.class), any(Annotations.class), eq(true), any(AsyncCallback.class));
		
		projectsHomePresenter.createProject(name);
		
		ArgumentCaptor<Entity> arg = ArgumentCaptor.forClass(Entity.class);
		verify(mockSynapseClient).createOrUpdateEntity(arg.capture(), any(Annotations.class), eq(true), any(AsyncCallback.class));
		Project proj = (Project) arg.getValue();
		assertEquals(name, proj.getName());
		
		verify(mockView).showInfo(anyString(), anyString());
		
		ArgumentCaptor<Place> arg2 = ArgumentCaptor.forClass(Place.class);
		verify(mockPlaceChanger).goTo(arg2.capture());
		Synapse place = (Synapse)arg2.getValue();
		assertEquals(newId, place.getEntityId());
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testCreateProjectFailConflict() throws Exception {
		String newId = "syn123";
		String name = "name";
		
		ConflictException ex = new ConflictException();
		AsyncMockStubber.callFailureWith(ex).when(mockSynapseClient).createOrUpdateEntity(any(Entity.class), any(Annotations.class), eq(true), any(AsyncCallback.class));
		
		projectsHomePresenter.createProject(name);

		verify(mockView).showErrorMessage(DisplayConstants.WARNING_PROJECT_NAME_EXISTS);
	}

}
