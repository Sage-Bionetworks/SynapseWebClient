package org.sagebionetworks.web.unitclient.widget.accessrequirements.createaccessrequirement;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.sagebionetworks.web.client.widget.accessrequirements.createaccessrequirement.CreateAccessRequirementStep1.EMPTY_SUBJECT_LIST_ERROR_MESSAGE;

import com.google.gwt.user.client.rpc.AsyncCallback;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.repo.model.ACCESS_TYPE;
import org.sagebionetworks.repo.model.ACTAccessRequirement;
import org.sagebionetworks.repo.model.AccessRequirement;
import org.sagebionetworks.repo.model.ManagedACTAccessRequirement;
import org.sagebionetworks.repo.model.RestrictableObjectDescriptor;
import org.sagebionetworks.repo.model.RestrictableObjectType;
import org.sagebionetworks.repo.model.SelfSignAccessRequirement;
import org.sagebionetworks.repo.model.TermsOfUseAccessRequirement;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.accessrequirements.EntitySubjectsWidget;
import org.sagebionetworks.web.client.widget.accessrequirements.TeamSubjectsWidget;
import org.sagebionetworks.web.client.widget.accessrequirements.createaccessrequirement.CreateAccessRequirementStep1;
import org.sagebionetworks.web.client.widget.accessrequirements.createaccessrequirement.CreateAccessRequirementStep1View;
import org.sagebionetworks.web.client.widget.accessrequirements.createaccessrequirement.CreateBasicAccessRequirementStep2;
import org.sagebionetworks.web.client.widget.accessrequirements.createaccessrequirement.CreateManagedACTAccessRequirementStep2;
import org.sagebionetworks.web.client.widget.table.modal.wizard.ModalPage.ModalPresenter;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;

public class CreateAccessRequirementStep1Test {

  CreateAccessRequirementStep1 widget;

  @Mock
  CreateAccessRequirementStep1View mockView;

  @Mock
  CreateManagedACTAccessRequirementStep2 mockActStep2;

  @Mock
  CreateBasicAccessRequirementStep2 mockTouStep2;

  @Mock
  SynapseClientAsync mockSynapseClient;

  @Mock
  ModalPresenter mockModalPresenter;

  @Mock
  ManagedACTAccessRequirement mockACTAccessRequirement;

  @Mock
  TermsOfUseAccessRequirement mockTermsOfUseAccessRequirement;

  @Mock
  RestrictableObjectDescriptor mockEntityRestrictableObjectDescriptor;

  @Mock
  RestrictableObjectDescriptor mockTeamRestrictableObjectDescriptor;

  @Mock
  TeamSubjectsWidget mockTeamSubjectsWidget;

  @Mock
  EntitySubjectsWidget mockEntitySubjectsWidget;

  @Captor
  ArgumentCaptor<AccessRequirement> arCaptor;

  @Captor
  ArgumentCaptor<List> listCaptor;

  @Captor
  ArgumentCaptor<CallbackP<List<RestrictableObjectDescriptor>>> callbackPCaptor;

  public static final String VIEW_TEAM_ID1 = "5678";
  public static final String VIEW_TEAM_ID2 = "8765";
  public static final String VIEW_TEAM_IDS =
    VIEW_TEAM_ID1 + ", " + VIEW_TEAM_ID2;

  public static final String VIEW_ENTITY_ID1 = "syn97";
  public static final String VIEW_ENTITY_ID2 = "syn79";
  public static final String VIEW_ENTITY_IDS =
    VIEW_ENTITY_ID1 + ", " + VIEW_ENTITY_ID2;

  public static final String ROD_ENTITY_ID = "syn97992";
  public static final String ROD_TEAM_ID = "87654";
  public static final String NAME = "A Dataset";
  public static final String MODIFIED_NAME = "An Interesting Dataset";

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);
    widget =
      new CreateAccessRequirementStep1(
        mockView,
        mockActStep2,
        mockTouStep2,
        mockSynapseClient,
        mockTeamSubjectsWidget,
        mockEntitySubjectsWidget
      );
    widget.setModalPresenter(mockModalPresenter);
    when(mockView.getTeamIds()).thenReturn(VIEW_TEAM_IDS);
    when(mockEntityRestrictableObjectDescriptor.getType())
      .thenReturn(RestrictableObjectType.ENTITY);
    when(mockEntityRestrictableObjectDescriptor.getId())
      .thenReturn(ROD_ENTITY_ID);
    when(mockTeamRestrictableObjectDescriptor.getType())
      .thenReturn(RestrictableObjectType.TEAM);
    when(mockTeamRestrictableObjectDescriptor.getId()).thenReturn(ROD_TEAM_ID);
    AsyncMockStubber
      .callSuccessWith(mockACTAccessRequirement)
      .when(mockSynapseClient)
      .createOrUpdateAccessRequirement(
        any(AccessRequirement.class),
        any(AsyncCallback.class)
      );

    when(mockView.isACTAccessRequirementType()).thenReturn(true);
    when(mockACTAccessRequirement.getSubjectIds())
      .thenReturn(new ArrayList<RestrictableObjectDescriptor>());
    when(mockTermsOfUseAccessRequirement.getSubjectIds())
      .thenReturn(new ArrayList<RestrictableObjectDescriptor>());
    when(mockEntitySubjectsWidget.isEntityIDsTextboxEmpty()).thenReturn(true);
  }

  @Test
  public void testConfigureWithEntityRod() {
    widget.configure(mockEntityRestrictableObjectDescriptor);
    verify(mockTeamSubjectsWidget).configure(listCaptor.capture());
    assertEquals(
      mockEntityRestrictableObjectDescriptor,
      listCaptor.getValue().get(0)
    );
    // go to the next page
    widget.onPrimary();
    verify(mockSynapseClient)
      .createOrUpdateAccessRequirement(
        arCaptor.capture(),
        any(AsyncCallback.class)
      );
    AccessRequirement ar = arCaptor.getValue();
    // in setUp, we have the view tell us that ACT is selected.
    assertTrue(ar instanceof ACTAccessRequirement);
    assertEquals(ACCESS_TYPE.DOWNLOAD, ar.getAccessType());
    assertEquals(1, ar.getSubjectIds().size());
    assertEquals(
      mockEntityRestrictableObjectDescriptor,
      ar.getSubjectIds().get(0)
    );

    verify(mockActStep2).configure(mockACTAccessRequirement);
    verify(mockModalPresenter).setNextActivePage(mockActStep2);
  }

  @Test
  public void testConfigureWithTeamRod() {
    widget.configure(mockTeamRestrictableObjectDescriptor);
    verify(mockTeamSubjectsWidget).configure(listCaptor.capture());
    assertEquals(
      mockTeamRestrictableObjectDescriptor,
      listCaptor.getValue().get(0)
    );

    when(mockView.isACTAccessRequirementType()).thenReturn(false);
    AsyncMockStubber
      .callSuccessWith(mockTermsOfUseAccessRequirement)
      .when(mockSynapseClient)
      .createOrUpdateAccessRequirement(
        any(AccessRequirement.class),
        any(AsyncCallback.class)
      );

    // go to the next page
    widget.onPrimary();
    verify(mockSynapseClient)
      .createOrUpdateAccessRequirement(
        arCaptor.capture(),
        any(AsyncCallback.class)
      );
    AccessRequirement ar = arCaptor.getValue();
    // in here, we have the view tell us that TermsOfUse was selected (not ACT).
    assertTrue(ar instanceof SelfSignAccessRequirement);
    assertEquals(ACCESS_TYPE.PARTICIPATE, ar.getAccessType());
    assertEquals(1, ar.getSubjectIds().size());
    assertEquals(
      mockTeamRestrictableObjectDescriptor,
      ar.getSubjectIds().get(0)
    );

    verify(mockTouStep2).configure(mockTermsOfUseAccessRequirement);
    verify(mockModalPresenter).setNextActivePage(mockTouStep2);
  }

  @Test
  public void testConfigureWithEmptySubjectList() {
    widget.configure(mockACTAccessRequirement);
    widget.onPrimary();

    verify(mockModalPresenter)
      .setErrorMessage(EMPTY_SUBJECT_LIST_ERROR_MESSAGE);
  }

  @Test
  public void testConfigureWithACTAccessRequirement() {
    when(mockACTAccessRequirement.getName()).thenReturn(NAME);
    when(mockACTAccessRequirement.getSubjectIds())
      .thenReturn(
        Collections.singletonList(mockEntityRestrictableObjectDescriptor)
      );

    widget.configure(mockACTAccessRequirement);

    when(mockView.getName()).thenReturn(MODIFIED_NAME);
    // on save, we should be updating the ar we passed in
    widget.onPrimary();
    verify(mockACTAccessRequirement).setAccessType(any(ACCESS_TYPE.class));
    verify(mockACTAccessRequirement).setSubjectIds(anyList());
    verify(mockACTAccessRequirement).setName(MODIFIED_NAME);
    verify(mockSynapseClient)
      .createOrUpdateAccessRequirement(
        eq(mockACTAccessRequirement),
        any(AsyncCallback.class)
      );
  }

  @Test
  public void testUnsavedEntityIDListChanges() {
    when(mockEntitySubjectsWidget.isEntityIDsTextboxEmpty()).thenReturn(false);
    when(mockACTAccessRequirement.getName()).thenReturn(NAME);
    when(mockACTAccessRequirement.getSubjectIds())
      .thenReturn(
        Collections.singletonList(mockEntityRestrictableObjectDescriptor)
      );

    widget.configure(mockACTAccessRequirement);

    widget.onPrimary();

    verify(mockSynapseClient, never())
      .createOrUpdateAccessRequirement(
        eq(mockACTAccessRequirement),
        any(AsyncCallback.class)
      );
    verify(mockModalPresenter)
      .setErrorMessage(
        CreateAccessRequirementStep1.UNSAVED_ENTITY_IDS_ERROR_MESSAGE
      );
  }

  @Test
  public void testConfigureWithToUAccessRequirement() {
    when(mockTermsOfUseAccessRequirement.getSubjectIds())
      .thenReturn(
        Collections.singletonList(mockEntityRestrictableObjectDescriptor)
      );
    widget.configure(mockTermsOfUseAccessRequirement);
    // on save, we should be updating the ar we passed in
    // also verify any errors are shown
    String error = "error occurred";
    AsyncMockStubber
      .callFailureWith(new Exception(error))
      .when(mockSynapseClient)
      .createOrUpdateAccessRequirement(
        any(AccessRequirement.class),
        any(AsyncCallback.class)
      );

    widget.onPrimary();
    verify(mockTermsOfUseAccessRequirement)
      .setAccessType(any(ACCESS_TYPE.class));
    verify(mockTermsOfUseAccessRequirement).setSubjectIds(anyList());
    verify(mockSynapseClient)
      .createOrUpdateAccessRequirement(
        eq(mockTermsOfUseAccessRequirement),
        any(AsyncCallback.class)
      );
    verify(mockModalPresenter).setErrorMessage(error);
  }

  @Test
  public void testAddEntities() {
    RestrictableObjectDescriptor rod = new RestrictableObjectDescriptor();
    rod.setId(ROD_ENTITY_ID);
    rod.setType(RestrictableObjectType.ENTITY);
    widget.configure(rod);
    verify(mockEntitySubjectsWidget)
      .configure(listCaptor.capture(), callbackPCaptor.capture());
    assertEquals(rod, listCaptor.getValue().get(0));

    // now add 1 new ID, 1 duplicate of the new ID, and 1 duplicate of the ID that the widget was
    // configured with.
    String newID = "syn9292929292";
    String entityIds = ROD_ENTITY_ID + "," + newID + "," + newID;
    CallbackP<List<RestrictableObjectDescriptor>> callbackP =
      callbackPCaptor.getValue();

    List<RestrictableObjectDescriptor> newSubjects = new ArrayList<>();
    newSubjects.add(mockEntityRestrictableObjectDescriptor);
    callbackP.invoke(newSubjects);

    assertEquals(newSubjects, widget.getSubjects());
  }

  @Test
  public void testAddTeams() {
    RestrictableObjectDescriptor rod = new RestrictableObjectDescriptor();
    rod.setId(ROD_TEAM_ID);
    rod.setType(RestrictableObjectType.TEAM);
    widget.configure(rod);
    verify(mockTeamSubjectsWidget).configure(listCaptor.capture());
    assertEquals(rod, listCaptor.getValue().get(0));

    // now add 1 new ID, 1 duplicate of the new ID, and 1 duplicate of the ID that the widget was
    // configured with.
    String newID = "9938383";
    String teamIds = ROD_TEAM_ID + "," + newID + "," + newID;
    when(mockView.getTeamIds()).thenReturn(teamIds);

    widget.onAddTeams();

    verify(mockTeamSubjectsWidget, times(2)).configure(listCaptor.capture());
    List<RestrictableObjectDescriptor> subjects = listCaptor.getValue();
    assertEquals(2, subjects.size());
    assertEquals(rod, subjects.get(0));
    assertEquals(newID, subjects.get(1).getId());
  }
}
