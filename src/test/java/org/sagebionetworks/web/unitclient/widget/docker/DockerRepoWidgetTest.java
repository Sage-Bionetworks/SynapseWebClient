package org.sagebionetworks.web.unitclient.widget.docker;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;
import static org.sagebionetworks.web.client.widget.docker.DockerRepoWidget.*;

import java.util.Date;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.repo.model.EntityBundle;
import org.sagebionetworks.repo.model.auth.UserEntityPermissions;
import org.sagebionetworks.repo.model.docker.DockerRepository;
import org.sagebionetworks.web.client.events.EntityUpdatedHandler;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.docker.DockerCommitListWidget;
import org.sagebionetworks.web.client.widget.docker.DockerRepoWidget;
import org.sagebionetworks.web.client.widget.docker.DockerRepoWidgetView;
import org.sagebionetworks.web.client.widget.entity.EntityMetadata;
import org.sagebionetworks.web.client.widget.entity.ModifiedCreatedByWidget;
import org.sagebionetworks.web.client.widget.entity.WikiPageWidget;
import org.sagebionetworks.web.client.widget.entity.controller.EntityActionController;
import org.sagebionetworks.web.client.widget.entity.controller.PreflightController;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.client.widget.entity.file.BasicTitleBar;
import org.sagebionetworks.web.client.widget.entity.file.DockerTitleBar;
import org.sagebionetworks.web.client.widget.entity.menu.v2.Action;
import org.sagebionetworks.web.client.widget.entity.menu.v2.ActionMenuWidget;
import org.sagebionetworks.web.client.widget.entity.menu.v2.ActionMenuWidget.ActionListener;
import org.sagebionetworks.web.client.widget.provenance.ProvenanceWidget;
import org.sagebionetworks.web.shared.WikiPageKey;

import com.google.gwt.user.client.ui.Widget;

public class DockerRepoWidgetTest {
	@Mock
	private PreflightController mockPreflightController;
	@Mock
	private DockerRepoWidgetView mockView;
	@Mock
	private SynapseAlert mockSynAlert;
	@Mock
	private WikiPageWidget mockWikiPageWidget;
	@Mock
	private ProvenanceWidget mockProvWidget;
	@Mock
	private EntityBundle mockEntityBundle;
	@Mock
	private EntityUpdatedHandler mockHandler;
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
	ActionMenuWidget mockActionMenu;
	@Mock
	EntityActionController mockController;
	@Mock
	DockerCommitListWidget mockDockerCommitListWidget;

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
		dockerRepoWidget = new DockerRepoWidget(mockPreflightController,
				mockView, mockSynAlert, mockWikiPageWidget, mockProvWidget,
				mockActionMenu, mockDockerTitleBar, mockMetadata,
				mockModifiedCreatedBy, mockController, mockDockerCommitListWidget);
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
	}

	@Test
	public void testConstruction() {
		verify(mockView).setPresenter(dockerRepoWidget);
		verify(mockView).setSynapseAlert(any(Widget.class));
		verify(mockView).setWikiPage(any(Widget.class));
		verify(mockView).setProvenance(any(Widget.class));
		verify(mockView).setEntityMetadata(any(Widget.class));
		verify(mockView).setModifiedCreatedBy(any(Widget.class));
		verify(mockView).setTitlebar(any(Widget.class));
		verify(mockView).setActionMenu(any(Widget.class));
		verify(mockActionMenu).addControllerWidget(any(Widget.class));
		verify(mockView).setDockerCommitListWidget(any(Widget.class));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testConfigure() {
		dockerRepoWidget.configure(mockEntityBundle, mockHandler);
		verify(mockWikiPageWidget).configure(any(WikiPageKey.class), eq(canEdit), any(WikiPageWidget.Callback.class), eq(false));
		verify(mockWikiPageWidget).setWikiReloadHandler(any(CallbackP.class));
		verify(mockProvWidget).configure(any(Map.class));
		verify(mockView).setDockerPullCommand(DOCKER_PULL_COMMAND + repoName);
		verify(mockMetadata).setEntityUpdatedHandler(mockHandler);
		verify(mockMetadata).setEntityBundle(mockEntityBundle, null);
		verify(mockDockerTitleBar).configure(mockEntity);
		verify(mockModifiedCreatedBy).configure(createdOn, createdBy, modifiedOn, modifiedBy);
		verify(mockActionMenu).addActionListener(eq(Action.TOGGLE_ANNOTATIONS), any(ActionListener.class));
		verify(mockController).configure(mockActionMenu, mockEntityBundle, true, rootWikiId, mockHandler);
		verify(mockActionMenu).setActionVisible(Action.ADD_COMMIT, canEdit);
		verify(mockActionMenu).setActionVisible(Action.EDIT_WIKI_PAGE, canEdit);
		verify(mockActionMenu).setActionVisible(Action.EDIT_PROVENANCE, canEdit);
		verify(mockActionMenu).setActionVisible(Action.CHANGE_ENTITY_NAME, false);
		verify(mockActionMenu).setActionVisible(Action.MOVE_ENTITY, false);
		verify(mockActionMenu).setActionVisible(Action.DELETE_ENTITY, false);
		verify(mockActionMenu).setActionListener(eq(Action.ADD_COMMIT), any(ActionListener.class));
		verify(mockDockerCommitListWidget).configure(entityId, false);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testConfigureCannotEdit() {
		canEdit = false;
		when(mockPermissions.getCanCertifiedUserEdit()).thenReturn(canEdit);
		dockerRepoWidget.configure(mockEntityBundle, mockHandler);
		verify(mockWikiPageWidget).configure(any(WikiPageKey.class), eq(canEdit), any(WikiPageWidget.Callback.class), eq(false));
		verify(mockWikiPageWidget).setWikiReloadHandler(any(CallbackP.class));
		verify(mockProvWidget).configure(any(Map.class));
		verify(mockView).setDockerPullCommand(DOCKER_PULL_COMMAND + repoName);
		verify(mockMetadata).setEntityUpdatedHandler(mockHandler);
		verify(mockMetadata).setEntityBundle(mockEntityBundle, null);
		verify(mockDockerTitleBar).configure(mockEntity);
		verify(mockModifiedCreatedBy).configure(createdOn, createdBy, modifiedOn, modifiedBy);
		verify(mockActionMenu).addActionListener(eq(Action.TOGGLE_ANNOTATIONS), any(ActionListener.class));
		verify(mockController).configure(mockActionMenu, mockEntityBundle, true, rootWikiId, mockHandler);
		verify(mockActionMenu).setActionVisible(Action.ADD_COMMIT, false);
		verify(mockActionMenu).setActionVisible(Action.EDIT_WIKI_PAGE, false);
		verify(mockActionMenu).setActionVisible(Action.EDIT_PROVENANCE, false);
		verify(mockActionMenu).setActionVisible(Action.CHANGE_ENTITY_NAME, false);
		verify(mockActionMenu).setActionVisible(Action.MOVE_ENTITY, false);
		verify(mockActionMenu).setActionVisible(Action.DELETE_ENTITY, false);
		verify(mockActionMenu).setActionListener(eq(Action.ADD_COMMIT), any(ActionListener.class));
	}
}
