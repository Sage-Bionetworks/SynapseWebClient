package org.sagebionetworks.web.unitclient.widget.entity.tabs;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.repo.model.EntityBundle;
import org.sagebionetworks.repo.model.Project;
import org.sagebionetworks.repo.model.auth.UserEntityPermissions;
import org.sagebionetworks.web.client.events.EntityUpdatedHandler;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.breadcrumb.Breadcrumb;
import org.sagebionetworks.web.client.widget.docker.DockerRepoListWidget;
import org.sagebionetworks.web.client.widget.docker.DockerRepoWidget;
import org.sagebionetworks.web.client.widget.entity.EntityMetadata;
import org.sagebionetworks.web.client.widget.entity.ModifiedCreatedByWidget;
import org.sagebionetworks.web.client.widget.entity.controller.StuAlert;
import org.sagebionetworks.web.client.widget.entity.file.BasicTitleBar;
import org.sagebionetworks.web.client.widget.entity.tabs.DockerTab;
import org.sagebionetworks.web.client.widget.entity.tabs.DockerTabView;
import org.sagebionetworks.web.client.widget.entity.tabs.Tab;

import com.google.gwt.user.client.ui.Widget;

public class DockerTabTest {
	@Mock
	Tab mockTab;
	@Mock
	DockerTabView mockView;
	@Mock
	BasicTitleBar mockDockerTitleBar;
	@Mock
	DockerRepoListWidget mockDockerRepoListWidget;
	@Mock
	DockerRepoWidget mockDockerRepoWidget;
	@Mock
	Breadcrumb mockBreadcrumb;
	@Mock
	EntityMetadata mockMetadata;
	@Mock
	ModifiedCreatedByWidget mockModifiedCreatedBy;
	@Mock
	StuAlert mockSynAlert;

	@Mock
	CallbackP<Tab> mockOnClickCallback;
	@Mock
	UserEntityPermissions mockPermissions;
	@Mock
	EntityBundle mockProjectEntityBundle;
	@Mock
	EntityUpdatedHandler mockEntityUpdatedHandler;
/*	@Mock
	DockerRepository mockTableEntity;*/
	@Mock
	Project mockProjectEntity;

	DockerTab tab;

	String projectEntityId = "syn123";
	String projectName = "test project";
	String dockerRepoEntityId = "syn170";
	String dockerRepoName = "test repository";

	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
		tab = new DockerTab(mockView, mockTab, mockDockerTitleBar, mockDockerRepoListWidget,
				mockDockerRepoWidget, mockBreadcrumb, mockMetadata, mockModifiedCreatedBy,
				mockSynAlert);
		when(mockProjectEntityBundle.getPermissions()).thenReturn(mockPermissions);
	}

	@Test
	public void testConstruction() {
		verify(mockView).setPresenter(tab);
		verify(mockView).setBreadcrumb(any(Widget.class));
		verify(mockView).setDockerRepoList(any(Widget.class));
		verify(mockView).setDockerRepoWidget(any(Widget.class));
		verify(mockView).setEntityMetadata(any(Widget.class));
		verify(mockView).setModifiedCreatedBy(any(Widget.class));
		verify(mockView).setSynapseAlert(any(Widget.class));
		verify(mockView).setTitlebar(any(Widget.class));
		verify(mockTab).configure(anyString(), any(Widget.class));
	}

	@Test
	public void testAsTab() {
		assertEquals(mockTab, tab.asTab());
	}

	@Test
	public void testSetTabClickedCallback() {
		tab.setTabClickedCallback(mockOnClickCallback);
		verify(mockTab).addTabClickedCallback(mockOnClickCallback);
	}

	@Test
	public void testConfigureWithProject() {
		String areaToken = null;

		boolean canCertifiedUserEdit = true;
		boolean isCertifiedUser = false;
		when(mockPermissions.getCanCertifiedUserEdit()).thenReturn(canCertifiedUserEdit);
		when(mockPermissions.getIsCertifiedUser()).thenReturn(isCertifiedUser);
		tab.setProject(projectEntityId, mockProjectEntityBundle, null);
		tab.configure(mockProjectEntity, mockEntityUpdatedHandler, areaToken);
		verify(mockMetadata).setEntityUpdatedHandler(mockEntityUpdatedHandler);
		verify(mockSynAlert).clear();
	}

	
}
