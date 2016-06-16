package org.sagebionetworks.web.unitclient.widget.docker;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;
import static org.sagebionetworks.web.client.widget.docker.DockerRepoWidget.*;

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
import org.sagebionetworks.web.client.widget.docker.DockerRepoWidget;
import org.sagebionetworks.web.client.widget.docker.DockerRepoWidgetView;
import org.sagebionetworks.web.client.widget.entity.WikiPageWidget;
import org.sagebionetworks.web.client.widget.entity.controller.PreflightController;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
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

	DockerRepoWidget dockerRepoWidget;
	String entityId = "syn123";
	String entityName = "dockerRepoName";
	Boolean canEdit = true;

	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
		dockerRepoWidget = new DockerRepoWidget(mockPreflightController,
				mockView, mockSynAlert, mockWikiPageWidget, mockProvWidget);
		when(mockEntity.getId()).thenReturn(entityId);
		when(mockEntity.getName()).thenReturn(entityName);
		when(mockEntityBundle.getEntity()).thenReturn(mockEntity);
		when(mockPermissions.getCanCertifiedUserEdit()).thenReturn(canEdit);
		when(mockEntityBundle.getPermissions()).thenReturn(mockPermissions);
	}

	@Test
	public void testConstruction() {
		verify(mockView).setPresenter(dockerRepoWidget);
		verify(mockView).setSynapseAlert(any(Widget.class));
		verify(mockView).setWikiPage(any(Widget.class));
		verify(mockView).setProvenance(any(Widget.class));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testConfigure() {
		dockerRepoWidget.configure(mockEntityBundle, mockHandler);
		verify(mockWikiPageWidget).configure(any(WikiPageKey.class), eq(canEdit), any(WikiPageWidget.Callback.class), eq(false));
		verify(mockWikiPageWidget).setWikiReloadHandler(any(CallbackP.class));
		verify(mockProvWidget).configure(any(Map.class));
		verify(mockView).setDockerPullCommand(DOCKER_PULL_COMMAND + entityName);
	}
}
