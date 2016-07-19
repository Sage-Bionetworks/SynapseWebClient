package org.sagebionetworks.web.unitclient.widget.docker;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;
import static org.sagebionetworks.web.client.widget.docker.DockerRepoListWidget.*;

import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.repo.model.EntityBundle;
import org.sagebionetworks.repo.model.EntityType;
import org.sagebionetworks.repo.model.Project;
import org.sagebionetworks.repo.model.docker.DockerRepository;
import org.sagebionetworks.repo.model.entity.query.Condition;
import org.sagebionetworks.repo.model.entity.query.EntityFieldName;
import org.sagebionetworks.repo.model.entity.query.EntityQuery;
import org.sagebionetworks.repo.model.entity.query.EntityQueryResult;
import org.sagebionetworks.repo.model.entity.query.EntityQueryResults;
import org.sagebionetworks.repo.model.entity.query.EntityQueryUtils;
import org.sagebionetworks.repo.model.entity.query.Operator;
import org.sagebionetworks.repo.model.entity.query.SortDirection;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.docker.DockerRepoListWidget;
import org.sagebionetworks.web.client.widget.docker.DockerRepoListWidgetView;
import org.sagebionetworks.web.client.widget.docker.modal.AddExternalRepoModal;
import org.sagebionetworks.web.client.widget.entity.controller.PreflightController;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.client.widget.pagination.PaginationWidget;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;

public class DockerRepoListWidgetTest {
	@Mock
	private PreflightController mockPreflightController;
	@Mock
	private DockerRepoListWidgetView mockView;
	@Mock
	private PaginationWidget mockPaginationWidget;
	@Mock
	private AddExternalRepoModal mockAddExternalRepoModal;
	@Mock
	private SynapseClientAsync mockSynapseClient;
	@Mock
	private EntityBundle mockProjectBundle;
	@Mock
	private Project mockProject;
	@Mock
	private EntityQueryResults mockEntityQueryResults;
	@Mock
	private SynapseAlert mockSynAlert;

	DockerRepoListWidget dockerRepoListWidget;
	String projectId;

	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
		dockerRepoListWidget = new DockerRepoListWidget(mockView, mockSynapseClient,
				mockPaginationWidget, mockAddExternalRepoModal, mockPreflightController,
				mockSynAlert);
		projectId = "syn123";
		when(mockProjectBundle.getEntity()).thenReturn(mockProject);
		when(mockProject.getId()).thenReturn(projectId);
	}

	@Test
	public void testConstruction() {
		verify(mockView).setPresenter(dockerRepoListWidget);
		verify(mockView).addExternalRepoModal(any(Widget.class));
		verify(mockView).addPaginationWidget(any(PaginationWidget.class));
		verify(mockView).setSynAlert(any(Widget.class));
	}

	@Test
	public void testAsWidget() {
		dockerRepoListWidget.asWidget();
		verify(mockView).asWidget();
	}

	@Test
	public void testOnClickAddExternalRepo() {
		dockerRepoListWidget.configure(mockProjectBundle);
		dockerRepoListWidget.onClickAddExternalRepo();
		verify(mockPreflightController).checkCreateEntity(eq(mockProjectBundle), eq(DockerRepository.class.getName()), any(Callback.class));
	}

	@Test
	public void testOnClickAddExternalRepoPreflightFailed(){
		AsyncMockStubber.callNoInvovke().when(mockPreflightController).checkCreateEntity(eq(mockProjectBundle), eq(DockerRepository.class.getName()), any(Callback.class));
		dockerRepoListWidget.configure(mockProjectBundle);
		dockerRepoListWidget.onClickAddExternalRepo();
		verify(mockAddExternalRepoModal, never()).show();
	}

	@Test
	public void testOnClickAddExternalRepoPreflightPassed(){
		AsyncMockStubber.callWithInvoke().when(mockPreflightController).checkCreateEntity(eq(mockProjectBundle), eq(DockerRepository.class.getName()), any(Callback.class));
		dockerRepoListWidget.configure(mockProjectBundle);
		dockerRepoListWidget.onClickAddExternalRepo();
		verify(mockAddExternalRepoModal).show();
	}

	@Test
	public void testCreateDockerRepoEntityQuery() {
		EntityQuery query = dockerRepoListWidget.createDockerRepoEntityQuery(projectId);
		Condition projectCondition = EntityQueryUtils.buildCondition(EntityFieldName.parentId, Operator.EQUALS, projectId);
		Condition typeCondition = EntityQueryUtils.buildCondition(EntityFieldName.nodeType, Operator.IN, EntityType.dockerrepo.name());
		assertTrue(query.getConditions().containsAll(Arrays.asList(projectCondition, typeCondition)));
		assertEquals(PAGE_SIZE, query.getLimit());
		assertEquals(OFFSET_ZERO, query.getOffset());
		assertEquals(EntityFieldName.createdOn.name(), query.getSort().getColumnName());
		assertEquals(SortDirection.DESC, query.getSort().getDirection());
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testConfigurationSuccess() {
		Long count = 2L;
		when(mockEntityQueryResults.getTotalEntityCount()).thenReturn(count);
		List<EntityQueryResult> list = Arrays.asList(new EntityQueryResult(), new EntityQueryResult());
		when(mockEntityQueryResults.getEntities()).thenReturn(list);
		AsyncMockStubber.callSuccessWith(mockEntityQueryResults)
			.when(mockSynapseClient).executeEntityQuery(any(EntityQuery.class), any(AsyncCallback.class));
		dockerRepoListWidget.configure(mockProjectBundle);
		verify(mockAddExternalRepoModal).configuration(eq(projectId), any(Callback.class));
		verify(mockSynapseClient).executeEntityQuery(any(EntityQuery.class), any(AsyncCallback.class));
		verify(mockPaginationWidget).configure(PAGE_SIZE, OFFSET_ZERO, count, dockerRepoListWidget);
		verify(mockView).showPaginationVisible(false);
		verify(mockView).clear();
		verify(mockView).addRepos(list);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testConfigurationSuccessOverOnePage() {
		Long count = 12L;
		when(mockEntityQueryResults.getTotalEntityCount()).thenReturn(count);
		AsyncMockStubber.callSuccessWith(mockEntityQueryResults)
			.when(mockSynapseClient).executeEntityQuery(any(EntityQuery.class), any(AsyncCallback.class));
		dockerRepoListWidget.configure(mockProjectBundle);
		verify(mockAddExternalRepoModal).configuration(eq(projectId), any(Callback.class));
		verify(mockSynapseClient).executeEntityQuery(any(EntityQuery.class), any(AsyncCallback.class));
		verify(mockPaginationWidget).configure(PAGE_SIZE, OFFSET_ZERO, count, dockerRepoListWidget);
		verify(mockView).showPaginationVisible(true);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testConfigurationFailure() {
		Long count = 2L;
		when(mockEntityQueryResults.getTotalEntityCount()).thenReturn(count);
		List<EntityQueryResult> list = Arrays.asList(new EntityQueryResult(), new EntityQueryResult());
		when(mockEntityQueryResults.getEntities()).thenReturn(list);
		Throwable error = new Throwable();
		AsyncMockStubber.callFailureWith(error)
			.when(mockSynapseClient).executeEntityQuery(any(EntityQuery.class), any(AsyncCallback.class));
		dockerRepoListWidget.configure(mockProjectBundle);
		verify(mockAddExternalRepoModal).configuration(eq(projectId), any(Callback.class));
		verify(mockSynapseClient).executeEntityQuery(any(EntityQuery.class), any(AsyncCallback.class));
		verify(mockPaginationWidget, never()).configure(PAGE_SIZE, OFFSET_ZERO, count, dockerRepoListWidget);
		verify(mockView, never()).showPaginationVisible(false);
		verify(mockView, never()).clear();
		verify(mockView, never()).addRepos(list);
		verify(mockSynAlert).handleException(error);
	}
}
