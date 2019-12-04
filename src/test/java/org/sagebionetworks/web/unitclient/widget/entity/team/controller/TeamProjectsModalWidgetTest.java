package org.sagebionetworks.web.unitclient.widget.entity.team.controller;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.util.ArrayList;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.sagebionetworks.repo.model.ProjectHeader;
import org.sagebionetworks.repo.model.ProjectHeaderList;
import org.sagebionetworks.repo.model.ProjectListSortColumn;
import org.sagebionetworks.repo.model.Team;
import org.sagebionetworks.repo.model.entity.query.SortDirection;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.SynapseJavascriptClient;
import org.sagebionetworks.web.client.presenter.ProfilePresenter;
import org.sagebionetworks.web.client.widget.LoadMoreWidgetContainer;
import org.sagebionetworks.web.client.widget.entity.ProjectBadge;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.client.widget.team.controller.TeamProjectsModalWidget;
import org.sagebionetworks.web.client.widget.team.controller.TeamProjectsModalWidgetView;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;

@RunWith(MockitoJUnitRunner.class)
public class TeamProjectsModalWidgetTest {

	TeamProjectsModalWidget presenter;
	@Mock
	PortalGinInjector mockGinInjector;
	@Mock
	TeamProjectsModalWidgetView mockView;
	@Mock
	SynapseAlert mockSynAlert;
	@Mock
	SynapseJavascriptClient mockJsClient;
	@Mock
	Team mockTeam;
	@Mock
	ProjectBadge mockProjectBadge;
	@Mock
	LoadMoreWidgetContainer mockLoadMoreWidgetContainer;
	@Mock
	ProjectHeaderList mockProjectHeaderList;
	ArrayList<ProjectHeader> myProjects;

	String teamId = "teamId";
	String teamName = "Team A";
	Exception caught = new Exception("this is an exception");

	@Before
	public void setup() {
		when(mockTeam.getId()).thenReturn(teamId);
		when(mockTeam.getName()).thenReturn(teamName);
		when(mockGinInjector.getProjectBadgeWidget()).thenReturn(mockProjectBadge);
		when(mockGinInjector.getLoadMoreProjectsWidgetContainer()).thenReturn(mockLoadMoreWidgetContainer);
		presenter = new TeamProjectsModalWidget(mockSynAlert, mockJsClient, mockGinInjector, mockView);

		ProjectHeader projectHeader1 = new ProjectHeader();
		projectHeader1.setId("syn1");
		ProjectHeader projectHeader2 = new ProjectHeader();
		projectHeader2.setId("syn2");

		myProjects = new ArrayList<ProjectHeader>();
		myProjects.add(projectHeader1);
		myProjects.add(projectHeader2);
		when(mockProjectHeaderList.getResults()).thenReturn(myProjects);
		AsyncMockStubber.callSuccessWith(mockProjectHeaderList).when(mockJsClient).getProjectsForTeam(anyString(), anyInt(), anyString(), any(ProjectListSortColumn.class), any(SortDirection.class), any(AsyncCallback.class));
	}

	@Test
	public void testConstruction() {
		verify(mockView).setPresenter(presenter);
		verify(mockView).setSynAlertWidget(mockSynAlert.asWidget());
	}

	@Test
	public void testConfigureAndShow() {
		presenter.configureAndShow(mockTeam);

		verify(mockView).setTitle(teamName + " Projects");
		verify(mockView).show();
		// ask for the projects
		String expectedNextPageToken = null;
		verify(mockJsClient).getProjectsForTeam(eq(teamId), eq(ProfilePresenter.PROJECT_PAGE_SIZE), eq(expectedNextPageToken), eq(ProjectListSortColumn.LAST_ACTIVITY), eq(SortDirection.DESC), any(AsyncCallback.class));
		// added the 2 project badges
		verify(mockGinInjector, times(2)).getProjectBadgeWidget();
		verify(mockLoadMoreWidgetContainer, times(2)).add(any(Widget.class));
	}

	@Test
	public void testSort() {
		presenter.configureAndShow(mockTeam);

		// simulate clicking sort on LAST_ACTIVITY
		presenter.sort(ProjectListSortColumn.LAST_ACTIVITY);
		verify(mockJsClient).getProjectsForTeam(eq(teamId), eq(ProfilePresenter.PROJECT_PAGE_SIZE), eq(null), eq(ProjectListSortColumn.LAST_ACTIVITY), eq(SortDirection.ASC), any(AsyncCallback.class));

		// simulate clicking sort on PROJECT_NAME
		presenter.sort(ProjectListSortColumn.PROJECT_NAME);
		verify(mockJsClient).getProjectsForTeam(eq(teamId), eq(ProfilePresenter.PROJECT_PAGE_SIZE), eq(null), eq(ProjectListSortColumn.PROJECT_NAME), eq(SortDirection.DESC), any(AsyncCallback.class));

		// simulate clicking sort on PROJECT_NAME again
		presenter.sort(ProjectListSortColumn.PROJECT_NAME);
		verify(mockJsClient).getProjectsForTeam(eq(teamId), eq(ProfilePresenter.PROJECT_PAGE_SIZE), eq(null), eq(ProjectListSortColumn.PROJECT_NAME), eq(SortDirection.ASC), any(AsyncCallback.class));
	}
}
