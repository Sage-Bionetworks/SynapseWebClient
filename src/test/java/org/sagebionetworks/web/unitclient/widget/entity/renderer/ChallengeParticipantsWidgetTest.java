package org.sagebionetworks.web.unitclient.widget.entity.renderer;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.gwt.user.client.rpc.AsyncCallback;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.sagebionetworks.repo.model.ObjectType;
import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.client.ChallengeClientAsync;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.client.widget.entity.renderer.ChallengeParticipantsWidget;
import org.sagebionetworks.web.client.widget.entity.renderer.UserListRowWidget;
import org.sagebionetworks.web.client.widget.entity.renderer.UserListView;
import org.sagebionetworks.web.client.widget.pagination.BasicPaginationWidget;
import org.sagebionetworks.web.shared.UserProfilePagedResults;
import org.sagebionetworks.web.shared.WidgetConstants;
import org.sagebionetworks.web.shared.WikiPageKey;
import org.sagebionetworks.web.shared.exceptions.RestServiceException;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;

@RunWith(MockitoJUnitRunner.class)
public class ChallengeParticipantsWidgetTest {

  @Mock
  UserListView mockView;

  @Mock
  BasicPaginationWidget mockPaginationWidget;

  @Mock
  ChallengeClientAsync mockChallengeClient;

  ChallengeParticipantsWidget widget;
  Map<String, String> descriptor;
  public static final String CHALLENGE_ID = "55555";
  String entityId = "syn22";
  UserProfile testProfile;

  @Mock
  SynapseAlert mockSynAlert;

  @Mock
  PortalGinInjector mockGinInjector;

  @Mock
  UserListRowWidget mockRow;

  @Before
  public void before() throws RestServiceException, JSONObjectAdapterException {
    widget =
      new ChallengeParticipantsWidget(
        mockView,
        mockPaginationWidget,
        mockChallengeClient,
        mockSynAlert,
        mockGinInjector
      );
    descriptor = new HashMap<String, String>();
    descriptor.put(WidgetConstants.CHALLENGE_ID_KEY, CHALLENGE_ID);
    descriptor.put(
      WidgetConstants.IS_IN_CHALLENGE_TEAM_KEY,
      Boolean.toString(false)
    );
    when(mockGinInjector.getUserListRowWidget()).thenReturn(mockRow);
    AsyncMockStubber
      .callSuccessWith(getTestUserProfilePagedResults())
      .when(mockChallengeClient)
      .getChallengeParticipants(
        anyBoolean(),
        anyString(),
        anyInt(),
        anyInt(),
        any(AsyncCallback.class)
      );
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
    widget.configure(
      new WikiPageKey(entityId, ObjectType.ENTITY.toString(), null),
      descriptor,
      null,
      null
    );

    verify(mockSynAlert).clear();
    verify(mockView).setLoadingVisible(true);
    verify(mockView).clearRows();
    verify(mockChallengeClient)
      .getChallengeParticipants(
        anyBoolean(),
        anyString(),
        anyInt(),
        anyInt(),
        any(AsyncCallback.class)
      );
    verify(mockView).setLoadingVisible(false);
    verify(mockPaginationWidget)
      .configure(anyLong(), anyLong(), anyLong(), eq(widget));

    verify(mockView).addRow(mockRow);
    verify(mockRow).configure(testProfile);
  }

  @Test
  public void testHappyCaseNoParticipants() throws Exception {
    AsyncMockStubber
      .callSuccessWith(getEmptyUserProfilePagedResults())
      .when(mockChallengeClient)
      .getChallengeParticipants(
        anyBoolean(),
        anyString(),
        anyInt(),
        anyInt(),
        any(AsyncCallback.class)
      );
    widget.configure(
      new WikiPageKey(entityId, ObjectType.ENTITY.toString(), null),
      descriptor,
      null,
      null
    );

    verify(mockSynAlert).clear();
    verify(mockView).setLoadingVisible(true);
    verify(mockView).clearRows();
    verify(mockChallengeClient)
      .getChallengeParticipants(
        anyBoolean(),
        anyString(),
        anyInt(),
        anyInt(),
        any(AsyncCallback.class)
      );
    verify(mockView).setLoadingVisible(false);
    verify(mockSynAlert)
      .showError(
        ChallengeParticipantsWidget.NO_CHALLENGE_PARTICIPANTS_FOUND_MESSAGE
      );
  }

  @Test
  public void testGetChallengeTeamsFailure() throws Exception {
    Exception error = new Exception("unhandled");
    AsyncMockStubber
      .callFailureWith(error)
      .when(mockChallengeClient)
      .getChallengeParticipants(
        anyBoolean(),
        anyString(),
        anyInt(),
        anyInt(),
        any(AsyncCallback.class)
      );
    widget.configure(
      new WikiPageKey(entityId, ObjectType.ENTITY.toString(), null),
      descriptor,
      null,
      null
    );

    verify(mockSynAlert).clear();
    verify(mockView).setLoadingVisible(true);
    verify(mockView).clearRows();
    verify(mockChallengeClient)
      .getChallengeParticipants(
        anyBoolean(),
        anyString(),
        anyInt(),
        anyInt(),
        any(AsyncCallback.class)
      );
    verify(mockView).setLoadingVisible(false);
    verify(mockSynAlert).handleException(error);
  }

  @Test
  public void testAsWidget() {
    widget.asWidget();
    verify(mockView).asWidget();
  }
}
