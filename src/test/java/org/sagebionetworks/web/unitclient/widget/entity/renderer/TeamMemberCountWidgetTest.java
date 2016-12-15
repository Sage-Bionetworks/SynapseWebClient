package org.sagebionetworks.web.unitclient.widget.entity.renderer;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
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
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.client.widget.entity.renderer.TeamMemberCountView;
import org.sagebionetworks.web.client.widget.entity.renderer.TeamMemberCountWidget;
import org.sagebionetworks.web.shared.TeamMemberBundle;
import org.sagebionetworks.web.shared.TeamMemberPagedResults;
import org.sagebionetworks.web.shared.WidgetConstants;
import org.sagebionetworks.web.shared.WikiPageKey;
import org.sagebionetworks.web.shared.exceptions.RestServiceException;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;

import com.google.gwt.user.client.rpc.AsyncCallback;

public class TeamMemberCountWidgetTest {
	
	@Mock
	TeamMemberCountView mockView;
	@Mock
	SynapseClientAsync mockSynapseClient;
	@Mock
	SynapseAlert mockSynAlert;
	TeamMemberCountWidget widget;
	Map<String, String> descriptor;
	public static final String TEAM_ID = "121111";
	public static final Long TOTAL_COUNT = 44L;
	
	@Before
	public void before() throws RestServiceException, JSONObjectAdapterException {
		MockitoAnnotations.initMocks(this);
		mockView = mock(TeamMemberCountView.class);
		widget = new TeamMemberCountWidget(mockView, mockSynapseClient, mockSynAlert);
		descriptor = new HashMap<String, String>();
		descriptor.put(WidgetConstants.TEAM_ID_KEY, TEAM_ID);
		
		AsyncMockStubber.callSuccessWith(getTestUserProfilePagedResults()).when(mockSynapseClient).getTeamMembers(anyString(), anyString(), anyInt(), anyInt(), any(AsyncCallback.class));
	}

	public TeamMemberPagedResults getTestUserProfilePagedResults() {
		TeamMemberPagedResults results = new TeamMemberPagedResults();
		UserProfile testProfile = new UserProfile();
		testProfile.setOwnerId("9837");
		TeamMemberBundle bundle = new TeamMemberBundle(testProfile, false, TEAM_ID);
		results.setResults(Collections.singletonList(bundle));
		results.setTotalNumberOfResults(TOTAL_COUNT);
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
		widget.configure(new WikiPageKey("syn123", ObjectType.ENTITY.toString(), null), descriptor, null, null);
		verify(mockSynAlert).clear();
		verify(mockSynapseClient).getTeamMembers(anyString(), anyString(), anyInt(), anyInt(), any(AsyncCallback.class));
		verify(mockView).setCount(TOTAL_COUNT.toString());
	}
	

	@Test
	public void testHappyCaseNoParticipants() throws Exception {
		AsyncMockStubber.callSuccessWith(getEmptyUserProfilePagedResults()).when(mockSynapseClient).getTeamMembers(anyString(), anyString(), anyInt(), anyInt(), any(AsyncCallback.class));
		widget.configure(new WikiPageKey("syn123", ObjectType.ENTITY.toString(), null), descriptor, null, null);
		verify(mockSynAlert).clear();
		verify(mockSynapseClient).getTeamMembers(anyString(), anyString(), anyInt(), anyInt(), any(AsyncCallback.class));
		verify(mockView).setCount("0");
	}
	
	@Test
	public void testGetChallengeTeamsFailure() throws Exception {
		Exception ex = new Exception("unhandled");
		AsyncMockStubber.callFailureWith(ex).when(mockSynapseClient).getTeamMembers(anyString(), anyString(), anyInt(), anyInt(), any(AsyncCallback.class));
		widget.configure(new WikiPageKey("syn123", ObjectType.ENTITY.toString(), null), descriptor, null, null);
		verify(mockSynAlert).clear();
		verify(mockSynapseClient).getTeamMembers(anyString(), anyString(), anyInt(), anyInt(), any(AsyncCallback.class));
		verify(mockSynAlert).handleException(ex);
	}
	
	@Test
	public void testAsWidget() {
		widget.asWidget();
		verify(mockView).asWidget();
	}
}
