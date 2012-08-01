package org.sagebionetworks.web.unitclient.presenter;

import static org.mockito.Mockito.*;

import org.junit.Before;
import org.junit.Test;
import org.sagebionetworks.repo.model.AutoGenFactory;
import org.sagebionetworks.schema.adapter.JSONObjectAdapter;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.place.ProjectsHome;
import org.sagebionetworks.web.client.presenter.ProjectsHomePresenter;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.services.NodeServiceAsync;
import org.sagebionetworks.web.client.transform.NodeModelCreator;
import org.sagebionetworks.web.client.view.ProjectsHomeView;

public class ProjectsHomePresenterTest {

	private ProjectsHome mockPlace;
	private ProjectsHomeView mockView;
	private GlobalApplicationState mockGlobalapplicationstate;
	private NodeServiceAsync mockNodeservice;
	private JSONObjectAdapter mockJsonobjectadapter;
	private NodeModelCreator mockNodemodelcreator;
	private AuthenticationController mockAuthenticationcontroller;
	private SynapseClientAsync mockSynapseclient;
	private AutoGenFactory mockEntityfactory;

	private ProjectsHomeView.Presenter presenter;

	@Before
	public void setUp() {
      mockPlace = mock(ProjectsHome.class);
      mockView = mock(ProjectsHomeView.class);
      mockGlobalapplicationstate = mock(GlobalApplicationState.class);
      mockNodeservice = mock(NodeServiceAsync.class);
      mockJsonobjectadapter = mock(JSONObjectAdapter.class);
      mockNodemodelcreator = mock(NodeModelCreator.class);
      mockAuthenticationcontroller = mock(AuthenticationController.class);
      mockSynapseclient = mock(SynapseClientAsync.class);
      mockEntityfactory = mock(AutoGenFactory.class);

		presenter = new ProjectsHomePresenter(mockView,
				mockGlobalapplicationstate, mockNodeservice,
				mockJsonobjectadapter, mockNodemodelcreator,
				mockAuthenticationcontroller, mockSynapseclient,
				mockEntityfactory);

	}

	@Test
	public void testCreateProjectEmpty() {
		presenter.createProject("");
		verify(mockView).showErrorMessage(DisplayConstants.WARNING_PROJECT_NAME_IS_EMPTY);
	}

	@Test
	public void testCreateProjectNull() {
		presenter.createProject(null);
		verify(mockView).showErrorMessage(DisplayConstants.WARNING_PROJECT_NAME_IS_EMPTY);
	}


}
