package org.sagebionetworks.web.unitclient.widget.entity;

import static junit.framework.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.sagebionetworks.repo.model.Project;
import org.sagebionetworks.repo.model.ProjectHeader;
import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.schema.adapter.AdapterFactory;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.schema.adapter.org.json.AdapterFactoryImpl;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.GWTWrapper;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.PlaceChanger;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.cache.ClientCache;
import org.sagebionetworks.web.client.widget.entity.EntityIconsCache;
import org.sagebionetworks.web.client.widget.entity.FavoriteWidget;
import org.sagebionetworks.web.client.widget.entity.ProjectBadge;
import org.sagebionetworks.web.client.widget.entity.ProjectBadgeView;
import org.sagebionetworks.web.client.widget.provenance.ProvViewUtil;
import org.sagebionetworks.web.shared.KeyValueDisplay;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;

import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.DateTimeFormat.PredefinedFormat;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;

public class ProjectBadgeTest {

	SynapseClientAsync mockSynapseClient;
	GlobalApplicationState mockGlobalApplicationState;
	PlaceChanger mockPlaceChanger;
	EntityIconsCache mockEntityIconsCache;
	AdapterFactory adapterFactory = new AdapterFactoryImpl();
	ClientCache mockClientCache;
	ProjectBadgeView mockView;
	String entityId = "syn123";
	UserProfile userProfile;
	ProjectBadge widget;
	FavoriteWidget mockFavoriteWidget;
	ProjectHeader mockProjectHeader;
	Date mockDate;
	DateTimeFormat mockDateTimeFormat;
	GWTWrapper mockGWT;

	@Before
	public void before() throws JSONObjectAdapterException {
		mockGlobalApplicationState = mock(GlobalApplicationState.class);
		mockSynapseClient = mock(SynapseClientAsync.class);
		mockView = mock(ProjectBadgeView.class);
		mockClientCache = mock(ClientCache.class);
		mockEntityIconsCache = mock(EntityIconsCache.class);
		mockPlaceChanger = mock(PlaceChanger.class);
		mockFavoriteWidget = mock(FavoriteWidget.class);
		mockProjectHeader = mock(ProjectHeader.class);
		mockDate = mock(Date.class);
		mockDateTimeFormat = mock(DateTimeFormat.class);
		mockGWT = mock(GWTWrapper.class);
		when(mockGWT.getDateTimeFormat(any(PredefinedFormat.class))).thenReturn(mockDateTimeFormat);
		when(mockDateTimeFormat.format(any(Date.class))).thenReturn("today");
		when(mockProjectHeader.getModifiedOn()).thenReturn(mockDate);
		when(mockDate.toString()).thenReturn("today");
		when(mockGlobalApplicationState.getPlaceChanger()).thenReturn(mockPlaceChanger);
		//by default, the view is attached
		when(mockView.isAttached()).thenReturn(true);
		widget = new ProjectBadge(mockView, mockSynapseClient, adapterFactory, mockGlobalApplicationState, mockClientCache, mockFavoriteWidget, mockGWT);
		
		//set up user profile
		userProfile =  new UserProfile();
		userProfile.setOwnerId("4444");
		userProfile.setUserName("Bilbo");
	}
	
	private void setupEntity(Project entity, Date lastActivityDate) throws JSONObjectAdapterException {
		AsyncMockStubber.callSuccessWith(entity).when(mockSynapseClient).getProject(anyString(), any(AsyncCallback.class));
		ProjectHeader header = new ProjectHeader();
		header.setId(entity.getId());
		header.setName(entity.getName());
		header.setLastActivity(lastActivityDate);
		widget.configure(header, null);
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
		
		
		widget.configure(header, null);
		verify(mockView).configure(eq(name), anyString(), anyString());
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
		widget.configure(header, null);
		verify(mockView).configure(eq(name), anyString(), anyString());
		verify(mockView).setLastActivityVisible(false);
		verify(mockView, never()).setLastActivityText(anyString());
	}
	
	@Test
	public void testGetProjectTooltipNoUserProfile() {
		ProjectHeader header = new ProjectHeader();
		String id = "syn37373";
		String name = "a name";
		header.setId(id);
		header.setName(name);
		header.setModifiedBy(Long.valueOf(userProfile.getOwnerId()));
		widget.configure(header, null);
		//note: can't test modified on because it format it using the gwt DateUtils (calls GWT.create())
		Map<String,String> map = new HashMap<String, String>();
		List<String> order = new ArrayList<String>();
		order.add("ID");
		map.put("ID", header.getId());
		String intended = ProvViewUtil.createEntityPopoverHtml(new KeyValueDisplay<String>(map, order)).asString();
		String tooltip = widget.getProjectTooltip();
		assertTrue(intended.equals(tooltip));
	}
	
	@Test
	public void testGetProjectTooltipComplete() {
		ProjectHeader header = new ProjectHeader();
		String id = "syn37373";
		String name = "a name";
		header.setId(id);
		header.setName(name);
		header.setModifiedBy(Long.valueOf(userProfile.getOwnerId()));
		header.setModifiedOn(mockDate);
		widget.configure(header, userProfile);
		Map<String,String> map = new HashMap<String, String>();
		List<String> order = new ArrayList<String>();
		order.add("ID");
		map.put("ID", header.getId());
		order.add("Modified By");
		map.put("Modified By", DisplayUtils.getDisplayName(userProfile));
		order.add("Modified On");
		map.put("Modified On", "today");
		String intended = ProvViewUtil.createEntityPopoverHtml(new KeyValueDisplay<String>(map, order)).asString();
		//note: can't test modified on because it format it using the gwt DateUtils (calls GWT.create())
		String tooltip = widget.getProjectTooltip();
		assertTrue(intended.equals(tooltip));
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
		verify(mockView).configure(eq(projectName), hrefCaptor.capture(), anyString());
		String href = hrefCaptor.getValue();
		assertTrue(href.contains("#!Synapse:"));
		assertTrue(href.contains(entityId));
	}
}
