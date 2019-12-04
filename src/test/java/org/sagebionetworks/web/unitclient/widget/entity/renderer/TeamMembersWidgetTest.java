package org.sagebionetworks.web.unitclient.widget.entity.renderer;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.repo.model.ObjectType;
import org.sagebionetworks.repo.model.TeamMemberTypeFilterOptions;
import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.SynapseJavascriptClient;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.client.widget.entity.renderer.TeamMemberRowWidget;
import org.sagebionetworks.web.client.widget.entity.renderer.TeamMembersWidget;
import org.sagebionetworks.web.client.widget.entity.renderer.TeamMembersWidgetView;
import org.sagebionetworks.web.client.widget.pagination.BasicPaginationWidget;
import org.sagebionetworks.web.shared.TeamMemberBundle;
import org.sagebionetworks.web.shared.TeamMemberPagedResults;
import org.sagebionetworks.web.shared.WidgetConstants;
import org.sagebionetworks.web.shared.WikiPageKey;
import org.sagebionetworks.web.shared.exceptions.RestServiceException;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class TeamMembersWidgetTest {

	@Mock
	TeamMembersWidgetView mockView;
	@Mock
	BasicPaginationWidget mockPaginationWidget;
	@Mock
	SynapseJavascriptClient mockJsClient;
	@Mock
	SynapseAlert mockSynAlert;
	@Mock
	PortalGinInjector mockGinInjector;
	@Mock
	TeamMemberRowWidget mockRow;

	TeamMembersWidget widget;
	Map<String, String> descriptor;
	public static final String TEAM_ID = "121111";
	String entityId = "syn22";
	UserProfile testProfile;

	@Before
	public void before() throws RestServiceException, JSONObjectAdapterException {
		MockitoAnnotations.initMocks(this);
		widget = new TeamMembersWidget(mockView, mockPaginationWidget, mockJsClient, mockSynAlert, mockGinInjector);
		descriptor = new HashMap<String, String>();
		descriptor.put(WidgetConstants.TEAM_ID_KEY, TEAM_ID);
		when(mockGinInjector.getTeamMemberRowWidget()).thenReturn(mockRow);
		AsyncMockStubber.callSuccessWith(getTestUserProfilePagedResults()).when(mockJsClient).getTeamMembers(anyString(), anyString(), any(TeamMemberTypeFilterOptions.class), anyInt(), anyInt(), any(AsyncCallback.class));
	}

	public TeamMemberPagedResults getTestUserProfilePagedResults() {
		TeamMemberPagedResults results = new TeamMemberPagedResults();
		testProfile = new UserProfile();
		testProfile.setOwnerId("9837");
		TeamMemberBundle bundle = new TeamMemberBundle(testProfile, false, TEAM_ID);
		results.setResults(Collections.singletonList(bundle));
		results.setTotalNumberOfResults(1L);
		return results;
	}

	public TeamMemberPagedResults getEmptyUserProfilePagedResults() {
		TeamMemberPagedResults results = new TeamMemberPagedResults();
		List<TeamMemberBundle> emptyList = Collections.emptyList();
		results.setResults(emptyList);
		results.setTotalNumberOfResults(0L);
		return results;
	}


	@Test
	public void testHappyCaseConfigure() throws Exception {
		widget.configure(new WikiPageKey(entityId, ObjectType.ENTITY.toString(), null), descriptor, null, null);

		verify(mockSynAlert).clear();
		verify(mockView).setLoadingVisible(true);
		verify(mockView).clearRows();
		verify(mockJsClient).getTeamMembers(anyString(), anyString(), eq(TeamMemberTypeFilterOptions.ALL), anyInt(), anyInt(), any(AsyncCallback.class));
		verify(mockView).setLoadingVisible(false);
		verify(mockPaginationWidget).configure(anyLong(), anyLong(), anyLong(), eq(widget));

		verify(mockView).addRow(mockRow);
		verify(mockRow).configure(testProfile);
	}


	@Test
	public void testHappyCaseNoParticipants() throws Exception {
		AsyncMockStubber.callSuccessWith(getEmptyUserProfilePagedResults()).when(mockJsClient).getTeamMembers(anyString(), anyString(), any(TeamMemberTypeFilterOptions.class), anyInt(), anyInt(), any(AsyncCallback.class));
		widget.configure(new WikiPageKey(entityId, ObjectType.ENTITY.toString(), null), descriptor, null, null);

		verify(mockView).clearRows();
		verify(mockJsClient).getTeamMembers(anyString(), anyString(), eq(TeamMemberTypeFilterOptions.ALL), anyInt(), anyInt(), any(AsyncCallback.class));
		verify(mockView, never()).addRow(mockRow);
	}

	@Test
	public void testGetTeamMembersFailure() throws Exception {
		Exception ex = new Exception("unhandled");
		AsyncMockStubber.callFailureWith(ex).when(mockJsClient).getTeamMembers(anyString(), anyString(), any(TeamMemberTypeFilterOptions.class), anyInt(), anyInt(), any(AsyncCallback.class));
		widget.configure(new WikiPageKey(entityId, ObjectType.ENTITY.toString(), null), descriptor, null, null);
		verify(mockSynAlert).clear();
		verify(mockView).setLoadingVisible(true);
		verify(mockView).clearRows();
		verify(mockJsClient).getTeamMembers(anyString(), anyString(), eq(TeamMemberTypeFilterOptions.ALL), anyInt(), anyInt(), any(AsyncCallback.class));
		verify(mockView).setLoadingVisible(false);

		verify(mockSynAlert).handleException(ex);
	}

	@Test
	public void testAsWidget() {
		widget.asWidget();
		verify(mockView).asWidget();
	}
}


