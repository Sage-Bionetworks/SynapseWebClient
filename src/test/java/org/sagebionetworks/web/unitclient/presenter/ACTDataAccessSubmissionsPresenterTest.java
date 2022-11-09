package org.sagebionetworks.web.unitclient.presenter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.sagebionetworks.web.client.place.ACTDataAccessSubmissionsPlace.ACCESSOR_ID_PARAM;
import static org.sagebionetworks.web.client.place.ACTDataAccessSubmissionsPlace.ACCESS_REQUIREMENT_ID_PARAM;
import static org.sagebionetworks.web.client.presenter.ACTDataAccessSubmissionsPresenter.SHOW_AR_TEXT;

import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.DateTimeFormat.PredefinedFormat;
import com.google.gwt.place.shared.Place;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.IsWidget;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.repo.model.ManagedACTAccessRequirement;
import org.sagebionetworks.repo.model.RestrictableObjectDescriptor;
import org.sagebionetworks.repo.model.UserGroupHeader;
import org.sagebionetworks.repo.model.dataaccess.AccessorGroupRequest;
import org.sagebionetworks.repo.model.dataaccess.Submission;
import org.sagebionetworks.repo.model.dataaccess.SubmissionOrder;
import org.sagebionetworks.repo.model.dataaccess.SubmissionPage;
import org.sagebionetworks.repo.model.dataaccess.SubmissionState;
import org.sagebionetworks.repo.model.file.FileHandleAssociateType;
import org.sagebionetworks.repo.model.file.FileHandleAssociation;
import org.sagebionetworks.repo.model.principal.TypeFilter;
import org.sagebionetworks.web.client.GWTWrapper;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.SynapseJavascriptClient;
import org.sagebionetworks.web.client.place.ACTDataAccessSubmissionsPlace;
import org.sagebionetworks.web.client.presenter.ACTDataAccessSubmissionsPresenter;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.view.ACTDataAccessSubmissionsView;
import org.sagebionetworks.web.client.widget.Button;
import org.sagebionetworks.web.client.widget.FileHandleWidget;
import org.sagebionetworks.web.client.widget.LoadMoreWidgetContainer;
import org.sagebionetworks.web.client.widget.accessrequirements.ManagedACTAccessRequirementWidget;
import org.sagebionetworks.web.client.widget.accessrequirements.SubjectsWidget;
import org.sagebionetworks.web.client.widget.accessrequirements.submission.ACTDataAccessSubmissionWidget;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.client.widget.search.SynapseSuggestBox;
import org.sagebionetworks.web.client.widget.search.UserGroupSuggestion;
import org.sagebionetworks.web.client.widget.search.UserGroupSuggestionProvider;
import org.sagebionetworks.web.client.widget.user.UserBadge;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;

public class ACTDataAccessSubmissionsPresenterTest {

  ACTDataAccessSubmissionsPresenter presenter;

  @Mock
  ACTDataAccessSubmissionsPlace mockPlace;

  @Mock
  ACTDataAccessSubmissionsView mockView;

  @Mock
  SynapseAlert mockSynAlert;

  @Mock
  PortalGinInjector mockGinInjector;

  @Mock
  LoadMoreWidgetContainer mockLoadMoreContainer;

  @Mock
  ManagedACTAccessRequirementWidget mockACTAccessRequirementWidget;

  @Mock
  Button mockButton;

  @Mock
  FileHandleWidget mockDucTemplateFileHandleWidget;

  @Mock
  SynapseJavascriptClient mockJsClient;

  @Mock
  ManagedACTAccessRequirement mockACTAccessRequirement;

  @Mock
  SubmissionPage mockDataAccessSubmissionPage;

  @Mock
  Submission mockDataAccessSubmission;

  @Captor
  ArgumentCaptor<FileHandleAssociation> fhaCaptor;

  @Mock
  ACTDataAccessSubmissionWidget mockACTDataAccessSubmissionWidget;

  @Mock
  SubjectsWidget mockSubjectsWidget;

  @Mock
  List<RestrictableObjectDescriptor> mockSubjects;

  @Mock
  GWTWrapper mockGWT;

  @Mock
  DateTimeFormat mockDateTimeFormat;

  @Mock
  SynapseSuggestBox mockAccessorSuggestWidget;

  @Mock
  UserGroupSuggestionProvider mockUserGroupSuggestionProvider;

  @Mock
  UserBadge mockSelectedAccessorUserBadge;

  @Mock
  UserGroupSuggestion mockUserGroupSuggestion;

  @Mock
  UserGroupHeader mockUserGroupHeader;

  @Captor
  ArgumentCaptor<Place> placeCaptor;

  public static final String FILE_HANDLE_ID = "9999";
  public static final String AR_ID = "76555";
  public static final String ACCESSOR_ID = "9999999";
  public static final String NEXT_PAGE_TOKEN = "abc678";
  public static final Long EXPIRATION_PERIOD = 0L;
  public static final String USER_ID_SELECTED = "42";

  @Before
  public void setup() {
    MockitoAnnotations.initMocks(this);
    when(mockGWT.getDateTimeFormat(any(PredefinedFormat.class)))
      .thenReturn(mockDateTimeFormat);
    presenter =
      new ACTDataAccessSubmissionsPresenter(
        mockView,
        mockSynAlert,
        mockGinInjector,
        mockLoadMoreContainer,
        mockACTAccessRequirementWidget,
        mockButton,
        mockDucTemplateFileHandleWidget,
        mockJsClient,
        mockSubjectsWidget,
        mockGWT,
        mockAccessorSuggestWidget,
        mockUserGroupSuggestionProvider,
        mockSelectedAccessorUserBadge
      );
    AsyncMockStubber
      .callSuccessWith(mockACTAccessRequirement)
      .when(mockJsClient)
      .getAccessRequirement(anyString(), any(AsyncCallback.class));
    AsyncMockStubber
      .callSuccessWith(mockDataAccessSubmissionPage)
      .when(mockJsClient)
      .getDataAccessSubmissions(
        anyString(),
        anyString(),
        anyString(),
        any(SubmissionState.class),
        any(SubmissionOrder.class),
        anyBoolean(),
        any(AsyncCallback.class)
      );
    when(mockDataAccessSubmissionPage.getResults())
      .thenReturn(Collections.singletonList(mockDataAccessSubmission));
    when(mockDataAccessSubmissionPage.getNextPageToken())
      .thenReturn(NEXT_PAGE_TOKEN);
    when(mockACTAccessRequirement.getDucTemplateFileHandleId())
      .thenReturn(FILE_HANDLE_ID);
    when(mockACTAccessRequirement.getId()).thenReturn(Long.parseLong(AR_ID));
    when(mockGinInjector.getACTDataAccessSubmissionWidget())
      .thenReturn(mockACTDataAccessSubmissionWidget);
    when(mockACTAccessRequirement.getSubjectIds()).thenReturn(mockSubjects);

    when(mockACTAccessRequirement.getDucTemplateFileHandleId())
      .thenReturn(FILE_HANDLE_ID);
    when(mockACTAccessRequirement.getAreOtherAttachmentsRequired())
      .thenReturn(true);

    when(mockACTAccessRequirement.getExpirationPeriod())
      .thenReturn(EXPIRATION_PERIOD);
    when(mockACTAccessRequirement.getIsCertifiedUserRequired())
      .thenReturn(true);
    when(mockACTAccessRequirement.getIsDUCRequired()).thenReturn(false);
    when(mockACTAccessRequirement.getIsIDURequired()).thenReturn(true);
    when(mockACTAccessRequirement.getIsIDUPublic()).thenReturn(true);
    when(mockACTAccessRequirement.getIsIRBApprovalRequired()).thenReturn(false);
    when(mockACTAccessRequirement.getIsValidatedProfileRequired())
      .thenReturn(true);
    when(mockUserGroupSuggestion.getHeader()).thenReturn(mockUserGroupHeader);
    when(mockUserGroupHeader.getOwnerId()).thenReturn(USER_ID_SELECTED);
  }

  @Test
  public void testConstruction() {
    verify(mockView).setStates(anyList());
    verify(mockButton).setText(SHOW_AR_TEXT);
    verify(mockView).setAccessRequirementUIVisible(false);
    verify(mockView).setSynAlert(any(IsWidget.class));
    verify(mockView).setAccessRequirementWidget(any(IsWidget.class));
    verify(mockView).setLoadMoreContainer(any(IsWidget.class));
    verify(mockView).setShowHideButton(any(IsWidget.class));
    verify(mockView).setPresenter(presenter);
    verify(mockLoadMoreContainer).configure(any(Callback.class));
    verify(mockButton).addClickHandler(any(ClickHandler.class));
    verify(mockAccessorSuggestWidget)
      .setSuggestionProvider(mockUserGroupSuggestionProvider);
    verify(mockAccessorSuggestWidget).setTypeFilter(TypeFilter.USERS_ONLY);
  }

  @Test
  public void testLoadData() {
    when(mockPlace.getParam(ACCESS_REQUIREMENT_ID_PARAM)).thenReturn(AR_ID);
    when(mockPlace.getParam(ACCESSOR_ID_PARAM)).thenReturn(ACCESSOR_ID);

    presenter.setPlace(mockPlace);
    verify(mockJsClient)
      .getAccessRequirement(eq(AR_ID), any(AsyncCallback.class));

    // verify duc template file handle widget is configured properly (basd on act duc file handle id)
    verify(mockDucTemplateFileHandleWidget).configure(fhaCaptor.capture());
    FileHandleAssociation fha = fhaCaptor.getValue();
    assertEquals(
      FileHandleAssociateType.AccessRequirementAttachment,
      fha.getAssociateObjectType()
    );
    assertEquals(AR_ID, fha.getAssociateObjectId());
    assertEquals(FILE_HANDLE_ID, fha.getFileHandleId());
    verify(mockSubjectsWidget).configure(mockSubjects);
    verify(mockView).setAreOtherAttachmentsRequired(true);
    verify(mockView).setExpirationPeriod(EXPIRATION_PERIOD);
    verify(mockView).setIsCertifiedUserRequired(true);
    verify(mockView).setIsDUCRequired(false);
    verify(mockView).setIsIDURequired(true);
    verify(mockView).setIsIDUPublic(true);
    verify(mockView).setIsIRBApprovalRequired(false);
    verify(mockView).setIsValidatedProfileRequired(true);

    verify(mockACTAccessRequirementWidget)
      .setRequirement(eq(mockACTAccessRequirement), any(Callback.class));
    verify(mockLoadMoreContainer).setIsProcessing(true);
    verify(mockJsClient)
      .getDataAccessSubmissions(
        anyString(),
        eq(ACCESSOR_ID),
        eq((String) null),
        any(SubmissionState.class),
        any(SubmissionOrder.class),
        anyBoolean(),
        any(AsyncCallback.class)
      );

    // verify DataAccessSubmission widget is created/configured for the submission (based on the
    // mockACTAccessRequirement configuration)
    verify(mockGinInjector).getACTDataAccessSubmissionWidget();
    verify(mockACTDataAccessSubmissionWidget).setDucColumnVisible(false);
    verify(mockACTDataAccessSubmissionWidget).setIrbColumnVisible(false);
    verify(mockACTDataAccessSubmissionWidget)
      .setOtherAttachmentsColumnVisible(true);
    verify(mockLoadMoreContainer).setIsMore(true);
    verify(mockLoadMoreContainer).setIsProcessing(false);

    // verify final load of empty page
    when(mockDataAccessSubmissionPage.getResults())
      .thenReturn(Collections.EMPTY_LIST);
    when(mockDataAccessSubmissionPage.getNextPageToken()).thenReturn(null);
    presenter.loadMore();
    verify(mockJsClient)
      .getDataAccessSubmissions(
        anyString(),
        eq(ACCESSOR_ID),
        eq(NEXT_PAGE_TOKEN),
        any(SubmissionState.class),
        any(SubmissionOrder.class),
        anyBoolean(),
        any(AsyncCallback.class)
      );
    verify(mockLoadMoreContainer).setIsMore(false);
    verify(mockView).setProjectedExpirationDateVisible(false);
    verify(mockView, never()).setProjectedExpirationDateVisible(true);
  }

  @Test
  public void testProjectedExpiration() {
    String formattedDateTime = "In the future";
    when(mockDateTimeFormat.format(any(Date.class)))
      .thenReturn(formattedDateTime);
    when(mockPlace.getParam(ACCESS_REQUIREMENT_ID_PARAM)).thenReturn(AR_ID);

    Long expirationPeriod = 1111L;
    when(mockACTAccessRequirement.getExpirationPeriod())
      .thenReturn(expirationPeriod);
    presenter.setPlace(mockPlace);
    verify(mockView).setProjectedExpirationDateVisible(false);
    verify(mockView).setProjectedExpirationDateVisible(true);
    verify(mockView).setProjectedExpirationDate(formattedDateTime);
  }

  @Test
  public void testClearAccessorFilter() {
    when(mockPlace.getParam(ACCESS_REQUIREMENT_ID_PARAM)).thenReturn(AR_ID);
    when(mockPlace.getParam(ACCESSOR_ID_PARAM)).thenReturn(ACCESSOR_ID);
    presenter.setPlace(mockPlace);

    presenter.onClearAccessorFilter();

    verify(mockView).setSelectedAccessorUserBadgeVisible(false);
    verify(mockPlace).removeParam(ACCESSOR_ID_PARAM);
    verify(mockJsClient)
      .getDataAccessSubmissions(
        anyString(),
        eq((String) null),
        eq((String) null),
        any(SubmissionState.class),
        any(SubmissionOrder.class),
        anyBoolean(),
        any(AsyncCallback.class)
      );
  }

  @Test
  public void testOnAccessorSelected() {
    when(mockPlace.getParam(ACCESS_REQUIREMENT_ID_PARAM)).thenReturn(AR_ID);
    presenter.setPlace(mockPlace);

    presenter.onAccessorSelected(mockUserGroupSuggestion);

    verify(mockPlace).putParam(ACCESSOR_ID_PARAM, USER_ID_SELECTED);
    verify(mockSelectedAccessorUserBadge).configure(USER_ID_SELECTED);

    verify(mockJsClient)
      .getDataAccessSubmissions(
        anyString(),
        eq(USER_ID_SELECTED),
        eq((String) null),
        any(SubmissionState.class),
        any(SubmissionOrder.class),
        anyBoolean(),
        any(AsyncCallback.class)
      );
  }

  @Test
  public void testLoadDataFailure() {
    Exception ex = new Exception();
    AsyncMockStubber
      .callFailureWith(ex)
      .when(mockJsClient)
      .getDataAccessSubmissions(
        anyString(),
        anyString(),
        anyString(),
        any(SubmissionState.class),
        any(SubmissionOrder.class),
        anyBoolean(),
        any(AsyncCallback.class)
      );
    presenter.loadData();
    verify(mockSynAlert).handleException(ex);
    verify(mockLoadMoreContainer).setIsMore(false);
  }
}
