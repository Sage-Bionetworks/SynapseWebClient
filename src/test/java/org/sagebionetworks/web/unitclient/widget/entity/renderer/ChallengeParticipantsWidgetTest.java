package org.sagebionetworks.web.unitclient.widget.entity.renderer;

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.gwtbootstrap3.extras.bootbox.client.callback.ConfirmCallback;
import org.junit.Before;
import org.junit.Test;
import org.sagebionetworks.evaluation.model.Evaluation;
import org.sagebionetworks.repo.model.AutoGenFactory;
import org.sagebionetworks.repo.model.ChallengeTeam;
import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.repo.model.ObjectType;
import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.schema.adapter.AdapterFactory;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.schema.adapter.org.json.AdapterFactoryImpl;
import org.sagebionetworks.web.client.ChallengeClientAsync;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.PlaceChanger;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.transform.NodeModelCreator;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.entity.EditRegisteredTeamDialog;
import org.sagebionetworks.web.client.widget.entity.EvaluationSubmitter;
import org.sagebionetworks.web.client.widget.entity.RegisterTeamDialog;
import org.sagebionetworks.web.client.widget.entity.renderer.ChallengeParticipantsView;
import org.sagebionetworks.web.client.widget.entity.renderer.ChallengeParticipantsWidget;
import org.sagebionetworks.web.client.widget.entity.renderer.ChallengeTeamsView;
import org.sagebionetworks.web.client.widget.entity.renderer.ChallengeTeamsWidget;
import org.sagebionetworks.web.client.widget.entity.renderer.RegisterChallengeTeamWidget;
import org.sagebionetworks.web.client.widget.entity.renderer.SingleButtonView;
import org.sagebionetworks.web.client.widget.entity.renderer.SubmitToEvaluationWidget;
import org.sagebionetworks.web.client.widget.entity.renderer.SubmitToEvaluationWidgetView;
import org.sagebionetworks.web.client.widget.pagination.DetailedPaginationWidget;
import org.sagebionetworks.web.shared.ChallengeTeamBundle;
import org.sagebionetworks.web.shared.ChallengeTeamPagedResults;
import org.sagebionetworks.web.shared.PaginatedResults;
import org.sagebionetworks.web.shared.UserProfilePagedResults;
import org.sagebionetworks.web.shared.WidgetConstants;
import org.sagebionetworks.web.shared.WikiPageKey;
import org.sagebionetworks.web.shared.exceptions.RestServiceException;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;

public class ChallengeParticipantsWidgetTest {
	
	ChallengeParticipantsView mockView;
	DetailedPaginationWidget mockPaginationWidget;
	ChallengeClientAsync mockChallengeClient;
	ChallengeParticipantsWidget widget;
	Map<String, String> descriptor;
	public static final String CHALLENGE_ID = "55555";
	String entityId = "syn22";
	UserProfile testProfile;
	
	@Before
	public void before() throws RestServiceException, JSONObjectAdapterException {
		mockView = mock(ChallengeParticipantsView.class);
		mockPaginationWidget = mock(DetailedPaginationWidget.class);
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
		verify(mockView).clearParticipants();
		verify(mockChallengeClient).getChallengeParticipants(anyBoolean(), anyString(), anyInt(), anyInt(), any(AsyncCallback.class));
		verify(mockView).hideLoading();
		verify(mockPaginationWidget).configure(anyLong(), anyLong(), anyLong(), eq(widget));
		
		verify(mockView).addParticipant(testProfile);
	}
	

	@Test
	public void testHappyCaseNoParticipants() throws Exception {
		AsyncMockStubber.callSuccessWith(getEmptyUserProfilePagedResults()).when(mockChallengeClient).getChallengeParticipants(anyBoolean(), anyString(), anyInt(), anyInt(), any(AsyncCallback.class));
		widget.configure(new WikiPageKey(entityId, ObjectType.ENTITY.toString(), null), descriptor, null, null);
		
		verify(mockView).hideErrors();
		verify(mockView).showLoading();
		verify(mockView).clearParticipants();
		verify(mockChallengeClient).getChallengeParticipants(anyBoolean(), anyString(), anyInt(), anyInt(), any(AsyncCallback.class));
		verify(mockView).hideLoading();
		verify(mockView).showNoParticipants();
	}
	
	@Test
	public void testGetChallengeTeamsFailure() throws Exception {
		AsyncMockStubber.callFailureWith(new Exception("unhandled")).when(mockChallengeClient).getChallengeParticipants(anyBoolean(), anyString(), anyInt(), anyInt(), any(AsyncCallback.class));
		widget.configure(new WikiPageKey(entityId, ObjectType.ENTITY.toString(), null), descriptor, null, null);
		
		verify(mockView).hideErrors();
		verify(mockView).showLoading();
		verify(mockView).clearParticipants();
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











