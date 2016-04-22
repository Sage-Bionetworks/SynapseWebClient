package org.sagebionetworks.web.unitclient.widget.entity.renderer;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.repo.model.ObjectType;
import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.widget.entity.renderer.TeamMembersWidget;
import org.sagebionetworks.web.client.widget.entity.renderer.UserListView;
import org.sagebionetworks.web.client.widget.pagination.DetailedPaginationWidget;
import org.sagebionetworks.web.shared.TeamMemberBundle;
import org.sagebionetworks.web.shared.TeamMemberPagedResults;
import org.sagebionetworks.web.shared.WidgetConstants;
import org.sagebionetworks.web.shared.WikiPageKey;
import org.sagebionetworks.web.shared.exceptions.RestServiceException;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;

import com.google.gwt.user.client.rpc.AsyncCallback;

public class TeamMembersWidgetTest {
	
	@Mock
	UserListView mockView;
	@Mock
	DetailedPaginationWidget mockPaginationWidget;
	@Mock
	SynapseClientAsync mockSynapseClient;
	
	TeamMembersWidget widget;
	Map<String, String> descriptor;
	public static final String TEAM_ID = "121111";
	String entityId = "syn22";
	UserProfile testProfile;
	
	@Before
	public void before() throws RestServiceException, JSONObjectAdapterException {
		MockitoAnnotations.initMocks(this);
		mockView = mock(UserListView.class);
		mockPaginationWidget = mock(DetailedPaginationWidget.class);
		widget = new TeamMembersWidget(mockView, mockPaginationWidget, mockSynapseClient);
		verify(mockView).setPresenter(widget);
		descriptor = new HashMap<String, String>();
		descriptor.put(WidgetConstants.TEAM_ID_KEY, TEAM_ID);
		
		AsyncMockStubber.callSuccessWith(getTestUserProfilePagedResults()).when(mockSynapseClient).getTeamMembers(anyString(), anyString(), anyInt(), anyInt(), any(AsyncCallback.class));
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
		
		verify(mockView).hideErrors();
		verify(mockView).showLoading();
		verify(mockView).clearUsers();
		verify(mockSynapseClient).getTeamMembers(anyString(), anyString(), anyInt(), anyInt(), any(AsyncCallback.class));
		verify(mockView).hideLoading();
		verify(mockPaginationWidget).configure(anyLong(), anyLong(), anyLong(), eq(widget));
		
		verify(mockView).addUser(testProfile);
	}
	

	@Test
	public void testHappyCaseNoParticipants() throws Exception {
		AsyncMockStubber.callSuccessWith(getEmptyUserProfilePagedResults()).when(mockSynapseClient).getTeamMembers(anyString(), anyString(), anyInt(), anyInt(), any(AsyncCallback.class));
		widget.configure(new WikiPageKey(entityId, ObjectType.ENTITY.toString(), null), descriptor, null, null);
		
		verify(mockView).hideErrors();
		verify(mockView).showLoading();
		verify(mockView).clearUsers();
		verify(mockSynapseClient).getTeamMembers(anyString(), anyString(), anyInt(), anyInt(), any(AsyncCallback.class));
		verify(mockView).hideLoading();
		verify(mockView).showNoUsers();
	}
	
	@Test
	public void testGetChallengeTeamsFailure() throws Exception {
		AsyncMockStubber.callFailureWith(new Exception("unhandled")).when(mockSynapseClient).getTeamMembers(anyString(), anyString(), anyInt(), anyInt(), any(AsyncCallback.class));
		widget.configure(new WikiPageKey(entityId, ObjectType.ENTITY.toString(), null), descriptor, null, null);
		
		verify(mockView).hideErrors();
		verify(mockView).showLoading();
		verify(mockView).clearUsers();
		verify(mockSynapseClient).getTeamMembers(anyString(), anyString(), anyInt(), anyInt(), any(AsyncCallback.class));
		verify(mockView).hideLoading();
		verify(mockView).showErrorMessage(anyString());
	}
	
	@Test
	public void testAsWidget() {
		widget.asWidget();
		verify(mockView).asWidget();
	}
}











