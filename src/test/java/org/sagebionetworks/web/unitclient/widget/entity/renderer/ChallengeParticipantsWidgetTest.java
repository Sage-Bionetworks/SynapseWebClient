package org.sagebionetworks.web.unitclient.widget.entity.renderer;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
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
import org.sagebionetworks.repo.model.ObjectType;
import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.client.ChallengeClientAsync;
import org.sagebionetworks.web.client.widget.entity.renderer.ChallengeParticipantsWidget;
import org.sagebionetworks.web.client.widget.entity.renderer.UserListView;
import org.sagebionetworks.web.client.widget.pagination.countbased.BasicPaginationWidget;
import org.sagebionetworks.web.shared.UserProfilePagedResults;
import org.sagebionetworks.web.shared.WidgetConstants;
import org.sagebionetworks.web.shared.WikiPageKey;
import org.sagebionetworks.web.shared.exceptions.RestServiceException;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class ChallengeParticipantsWidgetTest {

	UserListView mockView;
	BasicPaginationWidget mockPaginationWidget;
	ChallengeClientAsync mockChallengeClient;
	ChallengeParticipantsWidget widget;
	Map<String, String> descriptor;
	public static final String CHALLENGE_ID = "55555";
	String entityId = "syn22";
	UserProfile testProfile;

	@Before
	public void before() throws RestServiceException, JSONObjectAdapterException {
		mockView = mock(UserListView.class);
		mockPaginationWidget = mock(BasicPaginationWidget.class);
		mockChallengeClient = mock(ChallengeClientAsync.class);
		widget = new ChallengeParticipantsWidget(mockView, mockPaginationWidget, mockChallengeClient);
		verify(mockView).setPresenter(widget);
		descriptor = new HashMap<String, String>();
		descriptor.put(WidgetConstants.CHALLENGE_ID_KEY, CHALLENGE_ID);
		descriptor.put(WidgetConstants.IS_IN_CHALLENGE_TEAM_KEY, Boolean.toString(false));

		AsyncMockStubber.callSuccessWith(getTestUserProfilePagedResults()).when(mockChallengeClient).getChallengeParticipants(anyBoolean(), anyString(), anyInt(), anyInt(), any(AsyncCallback.class));
	}

	public UserProfilePagedResults getTestUserProfilePagedResults() {
		UserProfilePagedResults results = new UserProfilePagedResults();
		testProfile = new UserProfile();
		testProfile.setOwnerId("9837");
		results.setResults(Collections.singletonList(testProfile));
		results.setTotalNumberOfResults(1L);
		return results;
	}

	public UserProfilePagedResults getEmptyUserProfilePagedResults() {
		UserProfilePagedResults results = new UserProfilePagedResults();
		List<UserProfile> emptyList = Collections.emptyList();
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
		verify(mockChallengeClient).getChallengeParticipants(anyBoolean(), anyString(), anyInt(), anyInt(), any(AsyncCallback.class));
		verify(mockView).hideLoading();
		verify(mockPaginationWidget).configure(anyLong(), anyLong(), anyLong(), eq(widget));

		verify(mockView).addUser(testProfile);
	}


	@Test
	public void testHappyCaseNoParticipants() throws Exception {
		AsyncMockStubber.callSuccessWith(getEmptyUserProfilePagedResults()).when(mockChallengeClient).getChallengeParticipants(anyBoolean(), anyString(), anyInt(), anyInt(), any(AsyncCallback.class));
		widget.configure(new WikiPageKey(entityId, ObjectType.ENTITY.toString(), null), descriptor, null, null);

		verify(mockView).hideErrors();
		verify(mockView).showLoading();
		verify(mockView).clearUsers();
		verify(mockChallengeClient).getChallengeParticipants(anyBoolean(), anyString(), anyInt(), anyInt(), any(AsyncCallback.class));
		verify(mockView).hideLoading();
		verify(mockView).showNoUsers();
	}

	@Test
	public void testGetChallengeTeamsFailure() throws Exception {
		AsyncMockStubber.callFailureWith(new Exception("unhandled")).when(mockChallengeClient).getChallengeParticipants(anyBoolean(), anyString(), anyInt(), anyInt(), any(AsyncCallback.class));
		widget.configure(new WikiPageKey(entityId, ObjectType.ENTITY.toString(), null), descriptor, null, null);

		verify(mockView).hideErrors();
		verify(mockView).showLoading();
		verify(mockView).clearUsers();
		verify(mockChallengeClient).getChallengeParticipants(anyBoolean(), anyString(), anyInt(), anyInt(), any(AsyncCallback.class));
		verify(mockView).hideLoading();
		verify(mockView).showErrorMessage(anyString());
	}

	@Test
	public void testAsWidget() {
		widget.asWidget();
		verify(mockView).asWidget();
	}
}


