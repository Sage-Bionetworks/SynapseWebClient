package org.sagebionetworks.web.unitclient.widget.docker;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.repo.model.EntityChildrenRequest;
import org.sagebionetworks.repo.model.EntityChildrenResponse;
import org.sagebionetworks.repo.model.EntityHeader;
import org.sagebionetworks.repo.model.EntityType;
import org.sagebionetworks.repo.model.Project;
import org.sagebionetworks.repo.model.docker.DockerRepository;
import org.sagebionetworks.repo.model.entity.Direction;
import org.sagebionetworks.repo.model.entity.SortBy;
import org.sagebionetworks.repo.model.entitybundle.v2.EntityBundle;
import org.sagebionetworks.web.client.SynapseJavascriptClient;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.LoadMoreWidgetContainer;
import org.sagebionetworks.web.client.widget.docker.DockerRepoListWidget;
import org.sagebionetworks.web.client.widget.docker.DockerRepoListWidgetView;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.client.widget.pagination.countbased.BasicPaginationWidget;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;
import com.google.gwt.http.client.Request;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;

public class DockerRepoListWidgetTest {
	@Mock
	private DockerRepoListWidgetView mockView;
	@Mock
	private BasicPaginationWidget mockPaginationWidget;
	@Mock
	private Project mockProject;
	@Mock
	private SynapseAlert mockSynAlert;
	@Mock
	private LoadMoreWidgetContainer mockMembersContainer;
	@Mock
	EntityChildrenResponse mockResults;
	@Mock
	SynapseJavascriptClient mockSynapseJavascriptClient;
	@Captor
	ArgumentCaptor<CallbackP<String>> callbackPCaptor;
	@Mock
	CallbackP<String> mockCallbackP;
	@Mock
	Request mockRequest;
	List<EntityHeader> searchResults;

	DockerRepoListWidget dockerRepoListWidget;
	String projectId;

	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
		dockerRepoListWidget = new DockerRepoListWidget(mockView, mockMembersContainer, mockSynAlert, mockSynapseJavascriptClient);
		projectId = "syn123";
		when(mockProject.getId()).thenReturn(projectId);
		AsyncMockStubber.callSuccessWith(mockResults).when(mockSynapseJavascriptClient).getEntityChildren(any(EntityChildrenRequest.class), any(AsyncCallback.class));
		searchResults = new ArrayList<EntityHeader>();
		when(mockResults.getPage()).thenReturn(searchResults);
	}

	@Test
	public void testConstruction() {
		verify(mockView).setMembersContainer(mockMembersContainer);
		verify(mockView).setSynAlert(any(Widget.class));
	}

	@Test
	public void testAsWidget() {
		dockerRepoListWidget.asWidget();
		verify(mockView).asWidget();
	}

	@Test
	public void testCreateDockerRepoEntityQuery() {
		EntityChildrenRequest query = dockerRepoListWidget.createDockerRepoEntityQuery(projectId);
		assertEquals(projectId, query.getParentId());
		assertEquals(Collections.singletonList(EntityType.dockerrepo), query.getIncludeTypes());
		assertEquals(SortBy.CREATED_ON, query.getSortBy());
		assertEquals(Direction.DESC, query.getSortDirection());
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testConfigurationSuccess() {
		String id1 = "syn1", id2 = "syn2";
		EntityHeader header1 = new EntityHeader();
		header1.setId(id1);
		EntityHeader header2 = new EntityHeader();
		header2.setId(id2);
		searchResults.add(header1);
		searchResults.add(header2);
		DockerRepository repo1 = new DockerRepository();
		EntityBundle bundle1 = new EntityBundle();
		bundle1.setEntity(repo1);
		DockerRepository repo2 = new DockerRepository();
		EntityBundle bundle2 = new EntityBundle();
		bundle2.setEntity(repo2);
		AsyncMockStubber.callSuccessWith(bundle1, bundle2).when(mockSynapseJavascriptClient).getEntityBundleFromCache(anyString(), any(AsyncCallback.class));
		dockerRepoListWidget.configure(projectId);
		verify(mockSynapseJavascriptClient).getEntityChildren(any(EntityChildrenRequest.class), any(AsyncCallback.class));
		verify(mockView).clear();
		verify(mockView).addRepo(header1);
		verify(mockView).addRepo(header2);
		verify(mockSynapseJavascriptClient).getEntityBundleFromCache(eq(id1), any(AsyncCallback.class));
		verify(mockSynapseJavascriptClient).getEntityBundleFromCache(eq(id2), any(AsyncCallback.class));

	}

	@SuppressWarnings("unchecked")
	@Test
	public void testConfigurationSuccessOverOnePage() {
		dockerRepoListWidget.configure(projectId);
		verify(mockSynapseJavascriptClient).getEntityChildren(any(EntityChildrenRequest.class), any(AsyncCallback.class));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testConfigurationQueryFailure() {
		Throwable error = new Throwable();
		AsyncMockStubber.callFailureWith(error).when(mockSynapseJavascriptClient).getEntityChildren(any(EntityChildrenRequest.class), any(AsyncCallback.class));
		dockerRepoListWidget.configure(projectId);
		verify(mockSynapseJavascriptClient).getEntityChildren(any(EntityChildrenRequest.class), any(AsyncCallback.class));
		verify(mockView).clear();
		verify(mockView, never()).addRepo(any(EntityHeader.class));
		verify(mockSynAlert).handleException(error);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testConfigurationFailToGetSecondDockerRepository() {
		String id1 = "syn1", id2 = "syn2";
		EntityHeader header1 = new EntityHeader();
		header1.setId(id1);
		EntityHeader header2 = new EntityHeader();
		header2.setId(id2);
		searchResults.add(header1);
		searchResults.add(header2);
		DockerRepository repo1 = new DockerRepository();
		EntityBundle bundle1 = new EntityBundle();
		bundle1.setEntity(repo1);
		AsyncMockStubber.callSuccessWith(bundle1).when(mockSynapseJavascriptClient).getEntityBundleFromCache(eq(id1), any(AsyncCallback.class));
		Throwable error = new Throwable();
		AsyncMockStubber.callFailureWith(error).when(mockSynapseJavascriptClient).getEntityBundleFromCache(eq(id2), any(AsyncCallback.class));
		dockerRepoListWidget.configure(projectId);
		verify(mockSynapseJavascriptClient).getEntityChildren(any(EntityChildrenRequest.class), any(AsyncCallback.class));
		verify(mockView).clear();
		verify(mockView).addRepo(header1);
		verify(mockView).addRepo(header2);
		verify(mockSynapseJavascriptClient).getEntityBundleFromCache(eq(id1), any(AsyncCallback.class));
		verify(mockSynapseJavascriptClient).getEntityBundleFromCache(eq(id2), any(AsyncCallback.class));
		verify(mockSynAlert).handleException(error);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testConfigurationFailToGetFirstDockerRepository() {
		String id1 = "syn1", id2 = "syn2";
		EntityHeader header1 = new EntityHeader();
		header1.setId(id1);
		EntityHeader header2 = new EntityHeader();
		header2.setId(id2);
		searchResults.add(header1);
		searchResults.add(header2);
		DockerRepository repo2 = new DockerRepository();
		EntityBundle bundle2 = new EntityBundle();
		bundle2.setEntity(repo2);

		AsyncMockStubber.callSuccessWith(bundle2).when(mockSynapseJavascriptClient).getEntityBundleFromCache(eq(id2), any(AsyncCallback.class));
		Throwable error = new Throwable();
		AsyncMockStubber.callFailureWith(error).when(mockSynapseJavascriptClient).getEntityBundleFromCache(eq(id1), any(AsyncCallback.class));
		dockerRepoListWidget.configure(projectId);
		verify(mockSynapseJavascriptClient).getEntityChildren(any(EntityChildrenRequest.class), any(AsyncCallback.class));
		verify(mockView).addRepo(header1);
		verify(mockSynapseJavascriptClient).getEntityBundleFromCache(eq(id1), any(AsyncCallback.class));
		verify(mockSynapseJavascriptClient).getEntityBundleFromCache(eq(id2), any(AsyncCallback.class));
		verify(mockSynAlert).handleException(error);
	}

	@Test
	public void testLoadMore() {
		when(mockResults.getNextPageToken()).thenReturn("not null");
		dockerRepoListWidget.configure(projectId);
		verify(mockView).setLoadingVisible(false);
		verify(mockSynapseJavascriptClient).getEntityChildren(any(EntityChildrenRequest.class), any(AsyncCallback.class));
		verify(mockMembersContainer).setIsMore(true);
		when(mockResults.getNextPageToken()).thenReturn(null);
		dockerRepoListWidget.loadMore();
		verify(mockRequest, never()).cancel();
		verify(mockSynapseJavascriptClient, times(2)).getEntityChildren(any(EntityChildrenRequest.class), any(AsyncCallback.class));
		verify(mockMembersContainer).setIsMore(false);
	}

	@Test
	public void testReconfigureCancels() {
		// Do not test async response, only test the Request
		reset(mockSynapseJavascriptClient);
		when(mockSynapseJavascriptClient.getEntityChildren(any(EntityChildrenRequest.class), any(AsyncCallback.class))).thenReturn(mockRequest);
		dockerRepoListWidget.configure(projectId);

		verify(mockSynapseJavascriptClient).getEntityChildren(any(EntityChildrenRequest.class), any(AsyncCallback.class));
		verify(mockRequest, never()).cancel();

		dockerRepoListWidget.configure(projectId);

		verify(mockRequest).cancel();
		verify(mockSynapseJavascriptClient, times(2)).getEntityChildren(any(EntityChildrenRequest.class), any(AsyncCallback.class));
	}

	@Test
	public void testEntityClickHandler() {
		String entityId = "syn9992";
		dockerRepoListWidget.setEntityClickedHandler(mockCallbackP);
		verify(mockView).setEntityClickedHandler(callbackPCaptor.capture());
		verify(mockView, never()).setLoadingVisible(true);

		// simulate click
		CallbackP<String> callbackP = callbackPCaptor.getValue();
		callbackP.invoke(entityId);

		verify(mockView).clear();
		verify(mockView).setLoadingVisible(true);
		verify(mockCallbackP).invoke(entityId);
	}
}
