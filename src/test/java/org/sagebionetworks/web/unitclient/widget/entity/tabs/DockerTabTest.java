package org.sagebionetworks.web.unitclient.widget.entity.tabs;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.repo.model.EntityHeader;
import org.sagebionetworks.repo.model.EntityPath;
import org.sagebionetworks.repo.model.Project;
import org.sagebionetworks.repo.model.auth.UserEntityPermissions;
import org.sagebionetworks.repo.model.docker.DockerRepository;
import org.sagebionetworks.repo.model.entitybundle.v2.EntityBundle;
import org.sagebionetworks.web.client.EntityTypeUtils;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.place.Synapse;
import org.sagebionetworks.web.client.place.Synapse.EntityArea;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.breadcrumb.Breadcrumb;
import org.sagebionetworks.web.client.widget.breadcrumb.LinkData;
import org.sagebionetworks.web.client.widget.docker.DockerRepoListWidget;
import org.sagebionetworks.web.client.widget.docker.DockerRepoWidget;
import org.sagebionetworks.web.client.widget.entity.controller.StuAlert;
import org.sagebionetworks.web.client.widget.entity.menu.v2.ActionMenuWidget;
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
	DockerRepoListWidget mockDockerRepoListWidget;
	@Mock
	DockerRepoWidget mockDockerRepoWidget;
	@Mock
	Breadcrumb mockBreadcrumb;
	@Mock
	StuAlert mockSynAlert;
	@Mock
	PortalGinInjector mockGinInjector;

	@Mock
	CallbackP<Tab> mockOnClickCallback;
	@Mock
	CallbackP<String> mockUpdateEntityCallback;
	@Mock
	UserEntityPermissions mockPermissions;
	@Mock
	EntityBundle mockProjectEntityBundle;
	@Mock
	DockerRepository mockDockerRepoEntity;
	@Mock
	Project mockProjectEntity;
	@Mock
	Throwable mockProjectBundleLoadError;
	@Mock
	EntityBundle mockDockerRepoEntityBundle;
	@Mock
	EntityPath path;
	@Captor
	ArgumentCaptor<CallbackP> callbackPCaptor;
	@Mock
	CallbackP<String> mockEntitySelectedCallback;
	@Mock
	ActionMenuWidget mockActionMenuWidget;
	DockerTab tab;

	String projectEntityId = "syn123";
	String projectName = "test project";
	String dockerRepoEntityId = "syn170";
	String dockerRepoName = "test repository";

	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
		tab = new DockerTab(mockTab, mockGinInjector);

		when(mockGinInjector.getDockerTabView()).thenReturn(mockView);
		when(mockGinInjector.getDockerRepoListWidget()).thenReturn(mockDockerRepoListWidget);
		when(mockGinInjector.getBreadcrumb()).thenReturn(mockBreadcrumb);
		when(mockGinInjector.getStuAlert()).thenReturn(mockSynAlert);

		when(mockProjectEntityBundle.getEntity()).thenReturn(mockProjectEntity);
		when(mockProjectEntityBundle.getPermissions()).thenReturn(mockPermissions);
		when(mockProjectEntity.getName()).thenReturn(projectName);
		when(mockProjectEntity.getId()).thenReturn(projectEntityId);
		when(mockDockerRepoEntity.getId()).thenReturn(dockerRepoEntityId);
		when(mockDockerRepoEntity.getName()).thenReturn(dockerRepoName);
		when(mockDockerRepoEntityBundle.getEntity()).thenReturn(mockDockerRepoEntity);
		when(mockDockerRepoEntity.getRepositoryName()).thenReturn(dockerRepoName);
		when(mockGinInjector.createNewDockerRepoWidget()).thenReturn(mockDockerRepoWidget);

		List<EntityHeader> pathHeaders = new ArrayList<EntityHeader>();

		EntityHeader rootHeader = new EntityHeader();
		rootHeader.setId("1");
		rootHeader.setName("root");
		pathHeaders.add(rootHeader);

		EntityHeader projHeader = new EntityHeader();
		projHeader.setId("123");
		projHeader.setName(projectName);
		pathHeaders.add(projHeader);

		EntityHeader dsHeader = new EntityHeader();
		dsHeader.setId("170");
		dsHeader.setName("syn170");
		pathHeaders.add(dsHeader);

		path = new EntityPath();
		path.setPath(pathHeaders);
		when(mockDockerRepoEntityBundle.getPath()).thenReturn(path);
		tab.lazyInject();
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testConstruction() {
		verify(mockView).setPresenter(tab);
		verify(mockView).setBreadcrumb(any(Widget.class));
		verify(mockView).setDockerRepoList(any(Widget.class));
		verify(mockView).setSynapseAlert(any(Widget.class));
		verify(mockTab).configure(anyString(), anyString(), anyString());
		verify(mockBreadcrumb).setLinkClickedHandler(callbackPCaptor.capture());
		verify(mockTab).setContent(any(Widget.class));

		// test click on breadcrumb
		String newEntityId = "syn9898989822";
		tab.setEntitySelectedCallback(mockEntitySelectedCallback);
		callbackPCaptor.getValue().invoke(new Synapse(newEntityId));
		verify(mockEntitySelectedCallback).invoke(newEntityId);
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
		tab.setEntitySelectedCallback(mockUpdateEntityCallback);
		tab.setProject(projectEntityId, mockProjectEntityBundle, null);
		tab.configure(mockProjectEntityBundle, areaToken, mockActionMenuWidget);
		verify(mockSynAlert, atLeastOnce()).clear();
		verify(mockTab).setEntityNameAndPlace(eq(projectName), any(Synapse.class));
		verify(mockView, times(2)).setBreadcrumbVisible(false);
		verify(mockView).setDockerRepoListVisible(true);
		verify(mockView, times(2)).clearDockerRepoWidget();
		verify(mockUpdateEntityCallback, never()).invoke(anyString());
		verify(mockDockerRepoListWidget).configure(projectEntityId);
	}

	@Test
	public void testConfigureWithProjectFailure() {
		String areaToken = null;
		mockProjectEntityBundle = null;
		tab.setEntitySelectedCallback(mockUpdateEntityCallback);
		tab.setProject(projectEntityId, mockProjectEntityBundle, mockProjectBundleLoadError);
		tab.configure(mockProjectEntityBundle, areaToken, mockActionMenuWidget);
		verify(mockSynAlert, atLeastOnce()).clear();
		verify(mockTab).setEntityNameAndPlace(eq(projectEntityId), any(Synapse.class));
		verify(mockDockerRepoListWidget, never()).configure(projectEntityId);
		verify(mockSynAlert).handleException(mockProjectBundleLoadError);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testConfigureWithDockerRepoEntity() {
		String areaToken = null;
		tab.setEntitySelectedCallback(mockUpdateEntityCallback);
		tab.setProject(projectEntityId, mockProjectEntityBundle, mockProjectBundleLoadError);
		tab.configure(mockDockerRepoEntityBundle, areaToken, mockActionMenuWidget);
		verify(mockTab).setEntityNameAndPlace(eq(dockerRepoName), any(Synapse.class));
		verify(mockTab).showTab();
		verify(mockView).setBreadcrumbVisible(true);
		verify(mockView, times(2)).setDockerRepoListVisible(false);
		verify(mockView).setDockerRepoUIVisible(true);
		verify(mockView, atLeastOnce()).clearDockerRepoWidget();
		ArgumentCaptor<List> listCaptor = ArgumentCaptor.forClass(List.class);
		verify(mockBreadcrumb).configure(listCaptor.capture(), eq(dockerRepoName));
		List<LinkData> list = listCaptor.getValue();
		assertNotNull(list);
		assertEquals(1, list.size());
		assertEquals(list.get(0).getPlace(), new Synapse(projectEntityId, null, EntityArea.DOCKER, null));
		assertEquals(list.get(0).getText(), DockerTab.DOCKER);
		assertEquals(list.get(0).getIconType(), EntityTypeUtils.getIconTypeForEntityClassName(DockerRepository.class.getName()));
		verify(mockGinInjector).createNewDockerRepoWidget();
		verify(mockView).setDockerRepoWidget(any(Widget.class));
		verify(mockDockerRepoListWidget, never()).configure(projectEntityId);
	}

	@Test
	public void testShowError() {
		Throwable error = new Throwable();
		tab.showError(error);
		verify(mockSynAlert).clear();
		verify(mockSynAlert).handleException(error);
	}

	@Test
	public void testResetView() {
		tab.resetView();
		verify(mockView).setBreadcrumbVisible(false);
		verify(mockView).setDockerRepoListVisible(false);
		verify(mockView).setDockerRepoUIVisible(false);
		verify(mockView).clearDockerRepoWidget();
		verify(mockSynAlert, never()).handleException(any(Throwable.class));
	}
}
