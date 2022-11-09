package org.sagebionetworks.web.unitclient.widget.accessrequirements.createaccessrequirement;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.sagebionetworks.repo.model.ACCESS_TYPE.REVIEW_SUBMISSIONS;
import static org.sagebionetworks.web.client.utils.FutureUtils.getDoneFuture;
import static org.sagebionetworks.web.client.utils.FutureUtils.getFailedFuture;

import java.util.HashSet;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;
import org.sagebionetworks.repo.model.AccessControlList;
import org.sagebionetworks.repo.model.ManagedACTAccessRequirement;
import org.sagebionetworks.repo.model.ResourceAccess;
import org.sagebionetworks.repo.model.UserGroupHeader;
import org.sagebionetworks.repo.model.principal.TypeFilter;
import org.sagebionetworks.web.client.SynapseJavascriptClient;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.accessrequirements.createaccessrequirement.CreateManagedACTAccessRequirementStep3;
import org.sagebionetworks.web.client.widget.accessrequirements.createaccessrequirement.CreateManagedACTAccessRequirementStep3View;
import org.sagebionetworks.web.client.widget.search.SynapseSuggestBox;
import org.sagebionetworks.web.client.widget.search.UserGroupSuggestion;
import org.sagebionetworks.web.client.widget.search.UserGroupSuggestionProvider;
import org.sagebionetworks.web.client.widget.table.modal.wizard.ModalPage.ModalPresenter;
import org.sagebionetworks.web.client.widget.team.UserTeamBadge;
import org.sagebionetworks.web.shared.exceptions.NotFoundException;

@RunWith(MockitoJUnitRunner.class)
public class CreateManagedACTAccessRequirementStep3Test {

  CreateManagedACTAccessRequirementStep3 widget;

  @Mock
  ModalPresenter mockModalPresenter;

  @Mock
  CreateManagedACTAccessRequirementStep3View mockView;

  @Mock
  SynapseJavascriptClient mockJsClient;

  @Mock
  SynapseSuggestBox mockSuggestBox;

  @Mock
  UserGroupSuggestionProvider mockSuggestionProvider;

  @Mock
  UserTeamBadge mockUserTeamBadge;

  @Mock
  ManagedACTAccessRequirement mockACTAccessRequirement;

  @Mock
  AccessControlList mockACL;

  @Captor
  ArgumentCaptor<CallbackP<UserGroupSuggestion>> userSuggestionCallbackCaptor;

  @Captor
  ArgumentCaptor<AccessControlList> aclCaptor;

  @Captor
  ArgumentCaptor<Set<ResourceAccess>> resourceAccessSetCaptor;

  @Mock
  ResourceAccess mockResourceAccess;

  @Mock
  ResourceAccess mockResourceAccess2;

  @Mock
  UserGroupSuggestion mockUserGroupSuggestion;

  @Mock
  UserGroupHeader mockUserGroupHeader;

  public static final Long AR_ID = 8765L;
  public static final Long RESOURCE_ACCESS_PRINCIPAL_ID1 = 102030L;
  public static final Long RESOURCE_ACCESS_PRINCIPAL_ID2 = 405060L;

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);
    widget =
      new CreateManagedACTAccessRequirementStep3(
        mockView,
        mockJsClient,
        mockSuggestBox,
        mockSuggestionProvider,
        mockUserTeamBadge
      );
    widget.setModalPresenter(mockModalPresenter);
    when(mockACTAccessRequirement.getId()).thenReturn(AR_ID);
    verify(mockSuggestBox)
      .addItemSelectedHandler(userSuggestionCallbackCaptor.capture());
    when(mockUserGroupSuggestion.getHeader()).thenReturn(mockUserGroupHeader);
  }

  private void setupSingleReviewerACL() {
    when(mockJsClient.getAccessRequirementACL(AR_ID.toString()))
      .thenReturn(getDoneFuture(mockACL));
    when(mockResourceAccess.getPrincipalId())
      .thenReturn(RESOURCE_ACCESS_PRINCIPAL_ID1);
    Set<ResourceAccess> originalResourceAccessSet = new HashSet<>();
    originalResourceAccessSet.add(mockResourceAccess);
    when(mockACL.getResourceAccess()).thenReturn(originalResourceAccessSet);
  }

  @Test
  public void testConstruction() {
    verify(mockView).setPresenter(widget);
    verify(mockView).setReviewerSearchBox(mockSuggestBox);
    verify(mockView).setReviewerBadge(mockUserTeamBadge);
    verify(mockSuggestBox).setSuggestionProvider(mockSuggestionProvider);
    verify(mockSuggestBox).setTypeFilter(TypeFilter.ALL);
  }

  @Test
  public void testCreateACL() {
    when(
      mockJsClient.createAccessRequirementACL(
        anyString(),
        any(AccessControlList.class)
      )
    )
      .thenReturn(getDoneFuture(mockACL));
    // original not found
    when(mockJsClient.getAccessRequirementACL(AR_ID.toString()))
      .thenReturn(getFailedFuture(new NotFoundException()));
    widget.configure(mockACTAccessRequirement);
    // user selects a reviewer
    when(mockUserGroupHeader.getOwnerId())
      .thenReturn(RESOURCE_ACCESS_PRINCIPAL_ID1.toString());
    userSuggestionCallbackCaptor.getValue().invoke(mockUserGroupSuggestion);

    // on save, an ACL should be created
    widget.onPrimary();

    verify(mockJsClient)
      .createAccessRequirementACL(eq(AR_ID.toString()), aclCaptor.capture());
    AccessControlList newAcl = aclCaptor.getValue();
    assertEquals(1, newAcl.getResourceAccess().size());
    ResourceAccess newRA = newAcl.getResourceAccess().iterator().next();
    assertEquals(RESOURCE_ACCESS_PRINCIPAL_ID1, newRA.getPrincipalId());
    assertEquals(1, newRA.getAccessType().size());
    assertEquals(REVIEW_SUBMISSIONS, newRA.getAccessType().iterator().next());
    verify(mockModalPresenter).onFinished();
  }

  @Test
  public void testUpdateACL() {
    when(
      mockJsClient.updateAccessRequirementACL(
        anyString(),
        any(AccessControlList.class)
      )
    )
      .thenReturn(getDoneFuture(mockACL));
    // original is set to RESOURCE_ACCESS_PRINCIPAL_ID1
    setupSingleReviewerACL();
    widget.configure(mockACTAccessRequirement);
    // user selects a different reviewer
    when(mockUserGroupHeader.getOwnerId())
      .thenReturn(RESOURCE_ACCESS_PRINCIPAL_ID2.toString());
    userSuggestionCallbackCaptor.getValue().invoke(mockUserGroupSuggestion);

    // on save, the ACL should be updated
    widget.onPrimary();

    verify(mockJsClient).updateAccessRequirementACL(AR_ID.toString(), mockACL);
    verify(mockACL).setResourceAccess(resourceAccessSetCaptor.capture());
    Set<ResourceAccess> newResourceAccessSet = resourceAccessSetCaptor.getValue();
    assertEquals(1, newResourceAccessSet.size());
    ResourceAccess newRA = newResourceAccessSet.iterator().next();
    assertEquals(RESOURCE_ACCESS_PRINCIPAL_ID2, newRA.getPrincipalId());
    assertEquals(1, newRA.getAccessType().size());
    assertEquals(REVIEW_SUBMISSIONS, newRA.getAccessType().iterator().next());
    verify(mockModalPresenter).onFinished();
  }

  @Test
  public void testDeleteACL() {
    when(mockJsClient.deleteAccessRequirementACL(anyString()))
      .thenReturn(getDoneFuture(null));
    // original is set to RESOURCE_ACCESS_PRINCIPAL_ID1
    setupSingleReviewerACL();
    widget.configure(mockACTAccessRequirement);
    // user removes the reviewer
    widget.onRemoveReviewer();

    // on save, the ACL should be deleted
    widget.onPrimary();

    verify(mockJsClient).deleteAccessRequirementACL(AR_ID.toString());
    verify(mockModalPresenter).onFinished();
  }

  @Test
  public void testUndefinedToUndefinedNoOp() {
    // original not found
    when(mockJsClient.getAccessRequirementACL(AR_ID.toString()))
      .thenReturn(getFailedFuture(new NotFoundException()));
    widget.configure(mockACTAccessRequirement);

    // on save, no-op
    widget.onPrimary();

    verify(mockJsClient).getAccessRequirementACL(AR_ID.toString());
    verifyNoMoreInteractions(mockJsClient);
    verify(mockModalPresenter).onFinished();
  }

  @Test
  public void testErrorGettingACL() {
    Exception ex = new Exception("an unexpected error occurred");
    when(mockJsClient.getAccessRequirementACL(AR_ID.toString()))
      .thenReturn(getFailedFuture(ex));

    widget.configure(mockACTAccessRequirement);

    verify(mockModalPresenter).setError(ex);
  }

  @Test
  public void testTooManyEntriesInACL() {
    when(mockJsClient.getAccessRequirementACL(AR_ID.toString()))
      .thenReturn(getDoneFuture(mockACL));
    when(mockResourceAccess.getPrincipalId())
      .thenReturn(RESOURCE_ACCESS_PRINCIPAL_ID1);
    when(mockResourceAccess2.getPrincipalId())
      .thenReturn(RESOURCE_ACCESS_PRINCIPAL_ID2);
    Set<ResourceAccess> originalResourceAccessSet = new HashSet<>();
    originalResourceAccessSet.add(mockResourceAccess);
    originalResourceAccessSet.add(mockResourceAccess2);
    when(mockACL.getResourceAccess()).thenReturn(originalResourceAccessSet);

    widget.configure(mockACTAccessRequirement);

    verify(mockModalPresenter)
      .setErrorMessage(
        CreateManagedACTAccessRequirementStep3.TOO_MANY_ACL_ENTRIES_ERROR
      );
  }
}
