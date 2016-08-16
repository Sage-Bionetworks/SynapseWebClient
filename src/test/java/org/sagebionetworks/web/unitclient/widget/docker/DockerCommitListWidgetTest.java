package org.sagebionetworks.web.unitclient.widget.docker;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.client.DockerCommitSortBy;
import org.sagebionetworks.repo.model.docker.DockerCommit;
import org.sagebionetworks.web.client.DockerClientAsync;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.LoadMoreWidgetContainer;
import org.sagebionetworks.web.client.widget.docker.DockerCommitListWidget;
import org.sagebionetworks.web.client.widget.docker.DockerCommitListWidgetView;
import org.sagebionetworks.web.client.widget.docker.DockerCommitRowWidget;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.shared.PaginatedResults;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;

public class DockerCommitListWidgetTest {
	@Mock
	private DockerCommitListWidgetView mockView;
	@Mock
	private DockerClientAsync mockDockerClient;
	@Mock
	private SynapseAlert mockSynAlert;
	@Mock
	private LoadMoreWidgetContainer mockCommitsContainer;
	@Mock
	private PaginatedResults<DockerCommit> mockDockerCommitPage;
	@Mock
	private PortalGinInjector mockGinInjector;
	@Mock
	private DockerCommitRowWidget mockCommitRow;
	private DockerCommitListWidget dockerCommitListWidget;
	private String entityId;
	private List<DockerCommit> dockerCommitList;

	@Before
	public void before() {
		MockitoAnnotations.initMocks(this);
		dockerCommitListWidget = new DockerCommitListWidget(mockView, mockDockerClient, mockSynAlert, mockCommitsContainer, mockGinInjector);

		entityId = "syn123";
		dockerCommitList = new ArrayList<DockerCommit>();
	}

	@Test
	public void testConstructor() {
		verify(mockView).setPresenter(dockerCommitListWidget);
		verify(mockView).setCommitsContainer(mockCommitsContainer);
		verify(mockView).setSynAlert(any(Widget.class));
		ArgumentCaptor<Callback> captor = ArgumentCaptor.forClass(Callback.class);
		verify(mockCommitsContainer).configure(captor.capture());
		Callback callback = captor.getValue();
		callback.invoke();
		verify(mockSynAlert).clear();
		verify(mockDockerClient).getCommits(anyString(), anyLong(), anyLong(), any(DockerCommitSortBy.class), anyBoolean(), any(AsyncCallback.class));
	}

	@Test
	public void testConfigure() {
		dockerCommitListWidget.configure(entityId);
		verify(mockCommitsContainer).clear();
		verify(mockSynAlert).clear();
		verify(mockDockerClient).getCommits(eq(entityId), anyLong(), anyLong(), any(DockerCommitSortBy.class), anyBoolean(), any(AsyncCallback.class));
	}

	@Test
	public void testLoadMoreSuccess() {
		when(mockGinInjector.createNewDockerCommitRowWidget()).thenReturn(mockCommitRow);
		AsyncMockStubber.callSuccessWith(mockDockerCommitPage)
				.when(mockDockerClient).getCommits(anyString(),
						anyLong(), anyLong(), any(DockerCommitSortBy.class),
						anyBoolean(), any(AsyncCallback.class));
		when(mockDockerCommitPage.getTotalNumberOfResults()).thenReturn(1L);
		DockerCommit commit = new DockerCommit();
		dockerCommitList.add(commit);
		when(mockDockerCommitPage.getResults()).thenReturn(dockerCommitList);
		dockerCommitListWidget.configure(entityId);
		verify(mockCommitRow).configure(commit);
		verify(mockCommitsContainer).clear();
		verify(mockSynAlert).clear();
		verify(mockDockerClient).getCommits(eq(entityId),
				anyLong(), anyLong(), any(DockerCommitSortBy.class),
				anyBoolean(), any(AsyncCallback.class));
		verify(mockCommitsContainer).add(any(Widget.class));
		verify(mockCommitsContainer).setIsMore(false);
	}

	@Test
	public void testLoadMoreFailure() {
		Throwable exception = new Throwable();
		AsyncMockStubber.callFailureWith(exception)
				.when(mockDockerClient).getCommits(anyString(),
						anyLong(), anyLong(), any(DockerCommitSortBy.class),
						anyBoolean(), any(AsyncCallback.class));
		dockerCommitListWidget.configure(entityId);
		verify(mockCommitsContainer).clear();
		verify(mockSynAlert).clear();
		verify(mockDockerClient).getCommits(eq(entityId),
				anyLong(), anyLong(), any(DockerCommitSortBy.class),
				anyBoolean(), any(AsyncCallback.class));
		verify(mockCommitsContainer, never()).add(any(Widget.class));
		verify(mockSynAlert).handleException(exception);
		verify(mockCommitsContainer).setIsMore(false);
	}
}
