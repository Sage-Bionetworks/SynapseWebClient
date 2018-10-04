package org.sagebionetworks.web.unitclient.widget.docker;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.sagebionetworks.web.client.widget.docker.DockerRepoWidget.DOCKER_PULL_COMMAND;

import java.util.Date;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.repo.model.EntityBundle;
import org.sagebionetworks.repo.model.auth.UserEntityPermissions;
import org.sagebionetworks.repo.model.docker.DockerRepository;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.cookie.CookieProvider;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.docker.DockerCommitListWidget;
import org.sagebionetworks.web.client.widget.docker.DockerRepoWidget;
import org.sagebionetworks.web.client.widget.docker.DockerRepoWidgetView;
import org.sagebionetworks.web.client.widget.entity.EntityMetadata;
import org.sagebionetworks.web.client.widget.entity.ModifiedCreatedByWidget;
import org.sagebionetworks.web.client.widget.entity.WikiPageWidget;
import org.sagebionetworks.web.client.widget.entity.file.DockerTitleBar;
import org.sagebionetworks.web.client.widget.entity.menu.v2.ActionMenuWidget;
import org.sagebionetworks.web.client.widget.provenance.ProvenanceWidget;
import org.sagebionetworks.web.shared.WikiPageKey;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.Widget;

public class DockerRepoWidgetTest {
	@Mock
	private DockerRepoWidgetView mockView;
	@Mock
	private WikiPageWidget mockWikiPageWidget;
	@Mock
	private ProvenanceWidget mockProvWidget;
	@Mock
	private EntityBundle mockEntityBundle;
	@Mock
	private DockerRepository mockEntity;
	@Mock
	private UserEntityPermissions mockPermissions;
	@Mock
	DockerTitleBar mockDockerTitleBar;
	@Mock
	EntityMetadata mockMetadata;
	@Mock
	ModifiedCreatedByWidget mockModifiedCreatedBy;
	@Mock
	DockerCommitListWidget mockDockerCommitListWidget;
	@Mock
	CookieProvider mockCookieProvider;
	@Mock
	ActionMenuWidget mockActionWidget;
	@Mock
	EventBus mockEventBus;

	DockerRepoWidget dockerRepoWidget;
	String entityId = "syn123";
	String repoName = "dockerRepoName";
	Boolean canEdit = true;
	private Date createdOn = new Date();
	private String createdBy = "999";
	private Date modifiedOn = new Date();
	private String modifiedBy = "555";
	private String rootWikiId = "678";

	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
		dockerRepoWidget = new DockerRepoWidget(
				mockView, mockWikiPageWidget, mockProvWidget,
				mockDockerTitleBar, mockMetadata,
				mockModifiedCreatedBy, mockDockerCommitListWidget,
				mockCookieProvider,
				mockEventBus);
		when(mockEntity.getId()).thenReturn(entityId);
		when(mockEntity.getRepositoryName()).thenReturn(repoName);
		when(mockEntityBundle.getEntity()).thenReturn(mockEntity);
		when(mockPermissions.getCanCertifiedUserEdit()).thenReturn(canEdit);
		when(mockEntityBundle.getPermissions()).thenReturn(mockPermissions);
		when(mockEntity.getCreatedBy()).thenReturn(createdBy);
		when(mockEntity.getCreatedOn()).thenReturn(createdOn);
		when(mockEntity.getModifiedBy()).thenReturn(modifiedBy);
		when(mockEntity.getModifiedOn()).thenReturn(modifiedOn);
		when(mockEntityBundle.getRootWikiId()).thenReturn(rootWikiId);
		when(mockCookieProvider.getCookie(DisplayUtils.SYNAPSE_TEST_WEBSITE_COOKIE_KEY)).thenReturn(null);
	}

	@Test
	public void testConstruction() {
		verify(mockView).setWikiPage(any(Widget.class));
		verify(mockView).setProvenance(any(Widget.class));
		verify(mockView).setEntityMetadata(any(Widget.class));
		verify(mockView).setModifiedCreatedBy(any(Widget.class));
		verify(mockView).setTitlebar(any(Widget.class));
		verify(mockView).setDockerCommitListWidget(any(Widget.class));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testConfigure() {
		dockerRepoWidget.configure(mockEntityBundle, mockActionWidget);
		verify(mockWikiPageWidget).configure(any(WikiPageKey.class), eq(canEdit), any(WikiPageWidget.Callback.class));
		verify(mockWikiPageWidget).setWikiReloadHandler(any(CallbackP.class));
		verify(mockProvWidget).configure(any(Map.class));
		verify(mockView).setDockerPullCommand(DOCKER_PULL_COMMAND + repoName);
		verify(mockMetadata).configure(mockEntityBundle, null, mockActionWidget);
		verify(mockDockerTitleBar).configure(mockEntity);
		verify(mockModifiedCreatedBy).configure(createdOn, createdBy, modifiedOn, modifiedBy);
		verify(mockDockerCommitListWidget).configure(entityId, false);
		verify(mockView).setProvenanceWidgetVisible(false);
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testConfigureCannotEdit() {
		canEdit = false;
		when(mockPermissions.getCanCertifiedUserEdit()).thenReturn(canEdit);
		dockerRepoWidget.configure(mockEntityBundle, mockActionWidget);
		verify(mockWikiPageWidget).configure(any(WikiPageKey.class), eq(canEdit), any(WikiPageWidget.Callback.class));
		verify(mockWikiPageWidget).setWikiReloadHandler(any(CallbackP.class));
		verify(mockProvWidget).configure(any(Map.class));
		verify(mockView).setDockerPullCommand(DOCKER_PULL_COMMAND + repoName);
		verify(mockMetadata).configure(mockEntityBundle, null, mockActionWidget);
		verify(mockDockerTitleBar).configure(mockEntity);
		verify(mockModifiedCreatedBy).configure(createdOn, createdBy, modifiedOn, modifiedBy);
	}
}
