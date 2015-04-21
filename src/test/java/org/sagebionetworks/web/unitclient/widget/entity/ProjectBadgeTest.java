package org.sagebionetworks.web.unitclient.widget.entity;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

import java.util.Date;
import java.util.Map;

import static junit.framework.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.sagebionetworks.repo.model.Project;
import org.sagebionetworks.repo.model.ProjectHeader;
import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.schema.adapter.AdapterFactory;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.schema.adapter.org.json.AdapterFactoryImpl;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.PlaceChanger;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.cache.ClientCache;
import org.sagebionetworks.web.client.place.Synapse;
import org.sagebionetworks.web.client.widget.entity.EntityIconsCache;
import org.sagebionetworks.web.client.widget.entity.FavoriteWidget;
import org.sagebionetworks.web.client.widget.entity.ProjectBadge;
import org.sagebionetworks.web.client.widget.entity.ProjectBadgeView;
import org.sagebionetworks.web.shared.KeyValueDisplay;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;

public class ProjectBadgeTest {

	SynapseClientAsync mockSynapseClient;
	GlobalApplicationState mockGlobalApplicationState;
	PlaceChanger mockPlaceChanger;
	EntityIconsCache mockEntityIconsCache;
	AdapterFactory adapterFactory = new AdapterFactoryImpl();
	ClientCache mockClientCache;
	AsyncCallback<KeyValueDisplay<String>> getInfoCallback;
	ProjectBadgeView mockView;
	String entityId = "syn123";
	UserProfile userProfile;
	ProjectBadge widget;
	FavoriteWidget mockFavoriteWidget;

	@Before
	public void before() throws JSONObjectAdapterException {
		mockGlobalApplicationState = mock(GlobalApplicationState.class);
		mockSynapseClient = mock(SynapseClientAsync.class);
		mockView = mock(ProjectBadgeView.class);
		mockClientCache = mock(ClientCache.class);
		mockEntityIconsCache = mock(EntityIconsCache.class);
		getInfoCallback = mock(AsyncCallback.class);
		mockPlaceChanger = mock(PlaceChanger.class);
		mockFavoriteWidget = mock(FavoriteWidget.class);
		when(mockGlobalApplicationState.getPlaceChanger()).thenReturn(mockPlaceChanger);
		//by default, the view is attached
		when(mockView.isAttached()).thenReturn(true);
		widget = new ProjectBadge(mockView, mockSynapseClient, adapterFactory, mockGlobalApplicationState, mockClientCache, mockFavoriteWidget);
		
		//set up user profile
		userProfile =  new UserProfile();
		userProfile.setOwnerId("4444");
		userProfile.setUserName("Bilbo");
		AsyncMockStubber.callSuccessWith(userProfile).when(mockSynapseClient).getUserProfile(anyString(), any(AsyncCallback.class));
	}
	
	private void setupEntity(Project entity, Date lastActivityDate) throws JSONObjectAdapterException {
		AsyncMockStubber.callSuccessWith(entity).when(mockSynapseClient).getProject(anyString(), any(AsyncCallback.class));
		ProjectHeader header = new ProjectHeader();
		header.setId(entity.getId());
		header.setName(entity.getName());
		header.setLastActivity(lastActivityDate);
		widget.configure(header);
	}
	
	@Test
	public void testConfigure() throws Exception {
		ProjectHeader header = new ProjectHeader();
		String id = "syn37373";
		String name = "a name";
		Date lastActivity = new Date();
		header.setId(id);
		header.setName(name);
		header.setLastActivity(lastActivity);
		
		
		widget.configure(header);
		verify(mockView).setProject(eq(name), anyString());
		verify(mockView).setLastActivityVisible(true);
		verify(mockView).setLastActivityText(anyString());
		verify(mockView).setFavoritesWidget(any(Widget.class));
		verify(mockFavoriteWidget).asWidget();
	}
	
	@Test
	public void testConfigureNoActivityDate() throws Exception {
		ProjectHeader header = new ProjectHeader();
		String id = "syn37373";
		String name = "a name";
		header.setId(id);
		header.setName(name);
		widget.configure(header);
		verify(mockView).setProject(eq(name), anyString());
		verify(mockView).setLastActivityVisible(false);
		verify(mockView, never()).setLastActivityText(anyString());
	}

	@Test
	public void testGetInfoHappyCase() throws Exception {
		String entityId = "syn12345";
		Project testProject = new Project();
		testProject.setModifiedBy("4444");
		//note: can't test modified on because it format it using the gwt DateUtils (calls GWT.create())
		testProject.setId(entityId);
		setupEntity(testProject, null);
		widget.getInfo(getInfoCallback);
		verify(getInfoCallback).onSuccess(any(KeyValueDisplay.class));
	}
	
	@Test
	public void testGetInfoNotAttached() throws Exception {
		//same as happy case, but now the view reports that it is not attached
		when(mockView.isAttached()).thenReturn(false);
		String entityId = "syn12345";
		Project testProject = new Project();
		testProject.setModifiedBy("4444");
		testProject.setId(entityId);
		setupEntity(testProject, null);
		widget.getInfo(getInfoCallback);
		verify(getInfoCallback, never()).onSuccess(any(KeyValueDisplay.class));
	}
	
	@Test
	public void testprofileToKeyValueDisplay() {
		ProjectHeader header = new ProjectHeader();
		String id = "syn37373";
		String name = "a name";
		header.setId(id);
		header.setName(name);
		header.setModifiedBy(Long.valueOf(userProfile.getOwnerId()));
		widget.configure(header);
		//note: can't test modified on because it format it using the gwt DateUtils (calls GWT.create())
			
		// getMap() is directly called when used, so it's tested directly 
		Map<String,String> tooltipMap = widget.profileToKeyValueDisplay(userProfile, "Bilbo").getMap();
		assert(3 == tooltipMap.size());
		assert(tooltipMap.get("ID").equals(header.getId()));
		assert(tooltipMap.get("Modified By").equals(Long.valueOf(userProfile.getOwnerId())));
	}

	@Test
	public void testGetInfoProfileFailure() throws Exception {
		String entityId = "syn12345";
		Project testProject = new Project();
		testProject.setModifiedBy("4444");
		testProject.setId(entityId);
		setupEntity(testProject, null);
		Exception ex = new Exception("unhandled get profile error");
		AsyncMockStubber.callFailureWith(ex).when(mockSynapseClient).getUserProfile(anyString(), any(AsyncCallback.class));
		
		widget.getInfo(getInfoCallback);
		verify(getInfoCallback).onFailure(eq(ex));
	}
	
	@Test
	public void testEntityClicked() throws Exception {
		//check the passthrough
		String entityId = "syn12345";
		Project testProject = new Project();
		testProject.setModifiedBy("4444");
		testProject.setId(entityId);
		String projectName = "rosebud";
		testProject.setName(projectName);
		setupEntity(testProject, null);
		ArgumentCaptor<String> hrefCaptor = ArgumentCaptor.forClass(String.class);
		verify(mockView).setProject(eq(projectName), hrefCaptor.capture());
		String href = hrefCaptor.getValue();
		assertTrue(href.contains("#!Synapse:"));
		assertTrue(href.contains(entityId));
	}
}
