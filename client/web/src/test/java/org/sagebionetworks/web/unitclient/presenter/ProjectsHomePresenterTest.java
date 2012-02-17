package org.sagebionetworks.web.unitclient.presenter;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.sagebionetworks.schema.adapter.JSONObjectAdapter;
import org.sagebionetworks.schema.adapter.org.json.JSONObjectAdapterImpl;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.place.ProjectsHome;
import org.sagebionetworks.web.client.presenter.ProjectsHomePresenter;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.services.NodeServiceAsync;
import org.sagebionetworks.web.client.transform.NodeModelCreator;
import org.sagebionetworks.web.client.view.ProjectsHomeView;

public class ProjectsHomePresenterTest {

	ProjectsHomePresenter projectsHomePresenter;
	ProjectsHomeView mockView;
	GlobalApplicationState mockGlobalApplicationState;
	NodeServiceAsync mockNodeService;
	JSONObjectAdapter jsonObjectAdapter;
	NodeModelCreator mockNodeModelCreator;
	AuthenticationController mockAuthenticationController;

	@Before
	public void setup() {
		mockView = mock(ProjectsHomeView.class);
		mockGlobalApplicationState = mock(GlobalApplicationState.class);
		mockNodeService = mock(NodeServiceAsync.class);
		jsonObjectAdapter = new JSONObjectAdapterImpl();
		mockNodeModelCreator = mock(NodeModelCreator.class);
		mockAuthenticationController = mock(AuthenticationController.class);

		projectsHomePresenter = new ProjectsHomePresenter(mockView,
				mockGlobalApplicationState, mockNodeService, jsonObjectAdapter,
				mockNodeModelCreator, mockAuthenticationController);

		verify(mockView).setPresenter(projectsHomePresenter);
	}

	@Test
	public void testSetPlace() {
		ProjectsHome place = Mockito.mock(ProjectsHome.class);
		projectsHomePresenter.setPlace(place);
	}
}
