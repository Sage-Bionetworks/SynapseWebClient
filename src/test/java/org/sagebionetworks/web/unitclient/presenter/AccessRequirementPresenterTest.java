package org.sagebionetworks.web.unitclient.presenter;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import java.util.Collections;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.sagebionetworks.repo.model.AccessRequirement;
import org.sagebionetworks.repo.model.RestrictableObjectDescriptor;
import org.sagebionetworks.repo.model.RestrictableObjectType;
import org.sagebionetworks.repo.model.dataaccess.AccessRequirementPermissions;
import org.sagebionetworks.repo.model.dataaccess.Submission;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.PlaceChanger;
import org.sagebionetworks.web.client.SynapseJavascriptClient;
import org.sagebionetworks.web.client.place.AccessRequirementPlace;
import org.sagebionetworks.web.client.place.AccessRequirementsPlace;
import org.sagebionetworks.web.client.place.DataAccessManagementPlace;
import org.sagebionetworks.web.client.presenter.AccessRequirementPresenter;
import org.sagebionetworks.web.client.view.DivView;
import org.sagebionetworks.web.client.view.PlaceView;
import org.sagebionetworks.web.client.widget.accessrequirements.AccessRequirementWidget;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;

@RunWith(MockitoJUnitRunner.Silent.class)
public class AccessRequirementPresenterTest {

  AccessRequirementPresenter presenter;

  @Mock
  GlobalApplicationState mockGlobalApplicationState;

  @Mock
  PlaceChanger mockPlaceChanger;

  @Mock
  AccessRequirementPermissions mockPermissions;

  @Mock
  Submission mockSubmission;

  @Mock
  PlaceView mockView;

  @Mock
  AccessRequirementWidget mockArWidget;

  @Mock
  DivView mockArDiv;

  @Mock
  SynapseJavascriptClient mockJsClient;

  @Mock
  SynapseAlert mockSynAlert;

  @Mock
  AccessRequirementPlace mockPlace;

  @Mock
  AccessRequirement mockAR;

  @Captor
  ArgumentCaptor<RestrictableObjectDescriptor> subjectCaptor;

  Exception caught = new Exception("this is an exception");

  public static final String AR_ID = "1111112";
  public static final String ENTITY_ID = "syn239834";
  public static final String THREAD_ID = "99";

  @Mock
  RestrictableObjectDescriptor mockSubject;

  @Before
  public void setup() {
    presenter =
      new AccessRequirementPresenter(
        mockView,
        mockArWidget,
        mockArDiv,
        mockJsClient,
        mockSynAlert,
        mockGlobalApplicationState
      );
    when(mockPlace.getParam(AccessRequirementPlace.AR_ID_PARAM))
      .thenReturn(AR_ID);
    when(mockAR.getSubjectIds())
      .thenReturn(Collections.singletonList(mockSubject));
    AsyncMockStubber
      .callSuccessWith(mockAR)
      .when(mockJsClient)
      .getAccessRequirement(anyString(), any(AsyncCallback.class));
    when(mockGlobalApplicationState.getPlaceChanger())
      .thenReturn(mockPlaceChanger);
  }

  @Test
  public void testConstruction() {
    verify(mockArDiv).add(any());
    verify(mockView).add(any());
  }

  @Test
  public void testParamsProvided() {
    when(mockPlace.getParam(AccessRequirementsPlace.ID_PARAM))
      .thenReturn(ENTITY_ID);
    when(mockPlace.getParam(AccessRequirementsPlace.TYPE_PARAM))
      .thenReturn(RestrictableObjectType.ENTITY.toString());

    presenter.setPlace(mockPlace);

    verify(mockArWidget).configure(eq(AR_ID), subjectCaptor.capture());
    RestrictableObjectDescriptor subject = subjectCaptor.getValue();
    assertEquals(ENTITY_ID, subject.getId());
    assertEquals(RestrictableObjectType.ENTITY, subject.getType());
  }

  @Test
  public void testGetAR() {
    presenter.setPlace(mockPlace);

    verify(mockArWidget).configure(AR_ID, mockSubject);
  }

  @Test
  public void testGetARFailure() {
    AsyncMockStubber
      .callFailureWith(caught)
      .when(mockJsClient)
      .getAccessRequirement(anyString(), any(AsyncCallback.class));
    presenter.setPlace(mockPlace);
    verify(mockSynAlert).clear();
    verify(mockSynAlert).handleException(caught);
  }

  @Test
  public void testThreadIdRedirects() {
    when(mockPlace.getParam(AccessRequirementPlace.THREAD_ID_PARAM))
      .thenReturn(THREAD_ID);
    when(mockPermissions.getCanReviewSubmissions()).thenReturn(true);
    when(mockSubmission.getId()).thenReturn("12345");
    AsyncMockStubber
      .callSuccessWith(mockPermissions)
      .when(mockJsClient)
      .getAccessRequirementPermissions(anyString(), any(AsyncCallback.class));
    AsyncMockStubber
      .callSuccessWith(mockSubmission)
      .when(mockJsClient)
      .getSubmissionForThread(anyString(), any(AsyncCallback.class));
    presenter.setPlace(mockPlace);

    verify(mockPlaceChanger).goTo(any(DataAccessManagementPlace.class));
  }

  @Test
  public void testThreadIdNoPermission() {
    when(mockPlace.getParam(AccessRequirementPlace.THREAD_ID_PARAM))
      .thenReturn(THREAD_ID);
    when(mockPermissions.getCanReviewSubmissions()).thenReturn(false);
    AsyncMockStubber
      .callSuccessWith(mockPermissions)
      .when(mockJsClient)
      .getAccessRequirementPermissions(anyString(), any(AsyncCallback.class));

    presenter.setPlace(mockPlace);

    verify(mockJsClient, never())
      .getSubmissionForThread(anyString(), any(AsyncCallback.class));
  }

  @Test
  public void testThreadIdPermissionsFailure() {
    when(mockPlace.getParam(AccessRequirementPlace.THREAD_ID_PARAM))
      .thenReturn(THREAD_ID);

    AsyncMockStubber
      .callFailureWith(caught)
      .when(mockJsClient)
      .getAccessRequirementPermissions(anyString(), any(AsyncCallback.class));

    presenter.setPlace(mockPlace);

    verify(mockSynAlert).handleException(caught);
  }

  @Test
  public void testThreadIdSubmissionFailure() {
    when(mockPlace.getParam(AccessRequirementPlace.THREAD_ID_PARAM))
      .thenReturn(THREAD_ID);
    when(mockPermissions.getCanReviewSubmissions()).thenReturn(true);

    AsyncMockStubber
      .callSuccessWith(mockPermissions)
      .when(mockJsClient)
      .getAccessRequirementPermissions(anyString(), any(AsyncCallback.class));
    AsyncMockStubber
      .callFailureWith(caught)
      .when(mockJsClient)
      .getSubmissionForThread(anyString(), any(AsyncCallback.class));

    presenter.setPlace(mockPlace);

    verify(mockSynAlert).handleException(caught);
  }

  @Test
  public void testThreadIdRedirectsWithIdAndTypeParams() {
    when(mockPlace.getParam(AccessRequirementPlace.THREAD_ID_PARAM))
      .thenReturn(THREAD_ID);
    when(mockPlace.getParam(AccessRequirementsPlace.ID_PARAM))
      .thenReturn(ENTITY_ID);
    when(mockPlace.getParam(AccessRequirementsPlace.TYPE_PARAM))
      .thenReturn(RestrictableObjectType.ENTITY.toString());
    when(mockPermissions.getCanReviewSubmissions()).thenReturn(true);
    when(mockSubmission.getId()).thenReturn("123");
    AsyncMockStubber
      .callSuccessWith(mockPermissions)
      .when(mockJsClient)
      .getAccessRequirementPermissions(anyString(), any(AsyncCallback.class));
    AsyncMockStubber
      .callSuccessWith(mockSubmission)
      .when(mockJsClient)
      .getSubmissionForThread(anyString(), any(AsyncCallback.class));

    presenter.setPlace(mockPlace);

    verify(mockPlaceChanger).goTo(any(DataAccessManagementPlace.class));
  }
}
