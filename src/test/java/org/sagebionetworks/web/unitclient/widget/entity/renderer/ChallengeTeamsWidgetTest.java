package org.sagebionetworks.web.unitclient.widget.entity.renderer;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.sagebionetworks.repo.model.ChallengeTeam;
import org.sagebionetworks.repo.model.ObjectType;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.client.ChallengeClientAsync;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.entity.EditRegisteredTeamDialog;
import org.sagebionetworks.web.client.widget.entity.renderer.ChallengeTeamsView;
import org.sagebionetworks.web.client.widget.entity.renderer.ChallengeTeamsWidget;
import org.sagebionetworks.web.client.widget.pagination.countbased.BasicPaginationWidget;
import org.sagebionetworks.web.shared.ChallengeTeamBundle;
import org.sagebionetworks.web.shared.ChallengeTeamPagedResults;
import org.sagebionetworks.web.shared.WidgetConstants;
import org.sagebionetworks.web.shared.WikiPageKey;
import org.sagebionetworks.web.shared.exceptions.RestServiceException;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class ChallengeTeamsWidgetTest {

	ChallengeTeamsView mockView;
	EditRegisteredTeamDialog mockEditRegisterTeamDialog;
	BasicPaginationWidget mockPaginationWidget;
	ChallengeClientAsync mockChallengeClient;

	AuthenticationController mockAuthenticationController;

	ChallengeTeamsWidget widget;
	Map<String, String> descriptor;
	public static final String CHALLENGE_ID = "55555";
	String entityId = "syn22";
	ChallengeTeamBundle testTeamBundle;
	ChallengeTeam testChallengeTeam;
	public static final String TEST_TEAM_ID = "563";
	public static final String TEST_TEAM_RECRUITMENT_MESSAGE = "Let's join already!";
	boolean isAdminOfTestTeam;

	@Before
	public void before() throws RestServiceException, JSONObjectAdapterException {
		mockView = mock(ChallengeTeamsView.class);
		mockPaginationWidget = mock(BasicPaginationWidget.class);
		mockChallengeClient = mock(ChallengeClientAsync.class);
		mockEditRegisterTeamDialog = mock(EditRegisteredTeamDialog.class);
		mockAuthenticationController = mock(AuthenticationController.class);
		widget = new ChallengeTeamsWidget(mockView, mockEditRegisterTeamDialog, mockPaginationWidget, mockChallengeClient, mockAuthenticationController);
		verify(mockView).setPresenter(widget);
		descriptor = new HashMap<String, String>();
		when(mockAuthenticationController.isLoggedIn()).thenReturn(true);
		descriptor.put(WidgetConstants.CHALLENGE_ID_KEY, CHALLENGE_ID);
		AsyncMockStubber.callSuccessWith(getTestChallengeTeamPagedResults()).when(mockChallengeClient).getChallengeTeams(anyString(), anyString(), anyInt(), anyInt(), any(AsyncCallback.class));
	}

	public ChallengeTeamPagedResults getTestChallengeTeamPagedResults() {
		ChallengeTeamPagedResults results = new ChallengeTeamPagedResults();
		testTeamBundle = new ChallengeTeamBundle();
		isAdminOfTestTeam = true;
		testTeamBundle.setIsAdmin(isAdminOfTestTeam);
		testChallengeTeam = new ChallengeTeam();
		testChallengeTeam.setMessage(TEST_TEAM_RECRUITMENT_MESSAGE);
		testChallengeTeam.setTeamId(TEST_TEAM_ID);
		testTeamBundle.setChallengeTeam(testChallengeTeam);
		results.setResults(Collections.singletonList(testTeamBundle));
		results.setTotalNumberOfResults(1L);
		return results;
	}

	public ChallengeTeamPagedResults getEmptyTestChallengeTeamPagedResults() {
		ChallengeTeamPagedResults results = new ChallengeTeamPagedResults();
		List<ChallengeTeamBundle> emptyList = Collections.emptyList();
		results.setResults(emptyList);
		results.setTotalNumberOfResults(0L);
		return results;
	}


	@Test
	public void testHappyCaseConfigure() throws Exception {
		widget.configure(new WikiPageKey(entityId, ObjectType.ENTITY.toString(), null), descriptor, null, null);

		verify(mockView).hideErrors();
		verify(mockView).showLoading();
		verify(mockView).clearTeams();
		verify(mockChallengeClient).getChallengeTeams(anyString(), anyString(), anyInt(), anyInt(), any(AsyncCallback.class));
		verify(mockView).hideLoading();
		verify(mockPaginationWidget).configure(anyLong(), anyLong(), anyLong(), eq(widget));

		verify(mockView).addChallengeTeam(TEST_TEAM_ID, TEST_TEAM_RECRUITMENT_MESSAGE, isAdminOfTestTeam);

		// now edit test team
		widget.onEdit(TEST_TEAM_ID);
		verify(mockEditRegisterTeamDialog).configure(eq(testChallengeTeam), any(Callback.class));
	}


	@Test
	public void testHappyCaseNoTeams() throws Exception {
		AsyncMockStubber.callSuccessWith(getEmptyTestChallengeTeamPagedResults()).when(mockChallengeClient).getChallengeTeams(anyString(), anyString(), anyInt(), anyInt(), any(AsyncCallback.class));
		widget.configure(new WikiPageKey(entityId, ObjectType.ENTITY.toString(), null), descriptor, null, null);

		verify(mockView).hideErrors();
		verify(mockView).showLoading();
		verify(mockView).clearTeams();
		verify(mockChallengeClient).getChallengeTeams(anyString(), anyString(), anyInt(), anyInt(), any(AsyncCallback.class));
		verify(mockView).hideLoading();
		verify(mockView).showNoTeams();
	}

	@Test
	public void testGetChallengeTeamsFailure() throws Exception {
		AsyncMockStubber.callFailureWith(new Exception("unhandled")).when(mockChallengeClient).getChallengeTeams(anyString(), anyString(), anyInt(), anyInt(), any(AsyncCallback.class));
		widget.configure(new WikiPageKey(entityId, ObjectType.ENTITY.toString(), null), descriptor, null, null);

		verify(mockView).hideErrors();
		verify(mockView).showLoading();
		verify(mockView).clearTeams();
		verify(mockChallengeClient).getChallengeTeams(anyString(), anyString(), anyInt(), anyInt(), any(AsyncCallback.class));
		verify(mockView).hideLoading();
		verify(mockView).showErrorMessage(anyString());
	}

	@Test
	public void testAsWidget() {
		widget.asWidget();
		verify(mockView).asWidget();
	}
}


