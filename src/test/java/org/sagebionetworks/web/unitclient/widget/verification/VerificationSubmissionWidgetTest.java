package org.sagebionetworks.web.unitclient.widget.verification;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.gwt.user.client.rpc.AsyncCallback;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.repo.model.file.FileHandleAssociation;
import org.sagebionetworks.repo.model.verification.AttachmentMetadata;
import org.sagebionetworks.repo.model.verification.VerificationState;
import org.sagebionetworks.repo.model.verification.VerificationStateEnum;
import org.sagebionetworks.repo.model.verification.VerificationSubmission;
import org.sagebionetworks.web.client.GWTWrapper;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.SynapseProperties;
import org.sagebionetworks.web.client.UserProfileClientAsync;
import org.sagebionetworks.web.client.context.SynapseReactClientFullContextPropsProvider;
import org.sagebionetworks.web.client.jsinterop.RejectProfileValidationRequestModalProps;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.entity.PromptForValuesModalView;
import org.sagebionetworks.web.client.widget.entity.act.RejectReasonWidget;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.client.widget.upload.FileHandleList;
import org.sagebionetworks.web.client.widget.verification.VerificationSubmissionModalViewImpl;
import org.sagebionetworks.web.client.widget.verification.VerificationSubmissionRowViewImpl;
import org.sagebionetworks.web.client.widget.verification.VerificationSubmissionWidget;
import org.sagebionetworks.web.shared.WikiPageKey;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;

public class VerificationSubmissionWidgetTest {

  @Mock
  VerificationSubmissionModalViewImpl mockView;

  @Mock
  VerificationSubmissionRowViewImpl mockRowView;

  @Mock
  UserProfileClientAsync mockUserProfileClient;

  @Mock
  PortalGinInjector mockGinInjector;

  @Mock
  SynapseAlert mockSynapseAlert;

  @Mock
  FileHandleList mockFileHandleList;

  @Mock
  RejectReasonWidget mockPromptModalWidget;

  @Mock
  GlobalApplicationState mockGlobalApplicationState;

  @Mock
  VerificationSubmission mockSubmission;

  @Mock
  UserProfile mockProfile;

  @Mock
  GWTWrapper mockGWT;

  @Mock
  SynapseReactClientFullContextPropsProvider mockPropsProvider;

  @Mock
  SynapseProperties mockSynapseProperties;

  @Mock
  HashMap<String, WikiPageKey> mockWikiPageMap;

  @Mock
  PromptForValuesModalView mockPromptForValuesModal;

  @Captor
  ArgumentCaptor<CallbackP<String>> promptModalPresenterCaptor;

  @Captor
  ArgumentCaptor<
    RejectProfileValidationRequestModalProps.Callback
  > onRejectSuccessCaptor;

  CallbackP<String> confirmRejectionCallback;

  @Captor
  ArgumentCaptor<VerificationState> verificationStateCaptor;

  VerificationSubmissionWidget widget;
  String fileUrl = "https://s3/file.txt";
  String submissionId = "5432";
  String submissionFirstName = "Dubois";
  String submissionLastName = "Harris";
  String submissionLocation = "Cloud Arc";
  String submissionCompany = "Various";
  String submissionOrcId = "http://orcid.org/983";
  String hostPageURL = "https://www.synapse.org/";
  List<String> submissionEmails = Collections.singletonList(
    "doc@spacemail.org"
  );
  List<AttachmentMetadata> submissionAttachments;
  List<String> fileHandleIds;

  @Before
  public void before() {
    MockitoAnnotations.initMocks(this);
    when(mockGinInjector.getVerificationSubmissionModalViewImpl())
      .thenReturn(mockView);
    when(mockGinInjector.getVerificationSubmissionRowViewImpl())
      .thenReturn(mockRowView);
    when(mockGinInjector.getRejectReasonWidget())
      .thenReturn(mockPromptModalWidget);
    when(mockGinInjector.getPromptForValuesModal())
      .thenReturn(mockPromptForValuesModal);
    widget =
      new VerificationSubmissionWidget(
        mockGinInjector,
        mockUserProfileClient,
        mockSynapseAlert,
        mockFileHandleList,
        mockGlobalApplicationState,
        mockGWT,
        mockPropsProvider,
        mockSynapseProperties
      );

    when(mockGWT.getHostPageBaseURL()).thenReturn(hostPageURL);
    when(mockSubmission.getId()).thenReturn(submissionId);
    when(mockSubmission.getFirstName()).thenReturn(submissionFirstName);
    when(mockSubmission.getLastName()).thenReturn(submissionLastName);
    when(mockSubmission.getLocation()).thenReturn(submissionLocation);
    when(mockSubmission.getCompany()).thenReturn(submissionCompany);
    when(mockSubmission.getEmails()).thenReturn(submissionEmails);
    when(mockSubmission.getOrcid()).thenReturn(submissionOrcId);
    AttachmentMetadata meta = new AttachmentMetadata();
    meta.setId("12836");
    meta.setFileName("abc.txt");
    submissionAttachments = new ArrayList<AttachmentMetadata>();
    submissionAttachments.add(meta);
    meta = new AttachmentMetadata();
    meta.setId("789");
    meta.setFileName("def.txt");
    submissionAttachments.add(meta);
    when(mockSubmission.getAttachments()).thenReturn(submissionAttachments);

    when(mockFileHandleList.configure()).thenReturn(mockFileHandleList);
    when(mockFileHandleList.setUploadButtonText(anyString()))
      .thenReturn(mockFileHandleList);
    when(mockFileHandleList.setCanDelete(anyBoolean()))
      .thenReturn(mockFileHandleList);
    when(mockFileHandleList.setCanUpload(anyBoolean()))
      .thenReturn(mockFileHandleList);

    AsyncMockStubber
      .callSuccessWith(null)
      .when(mockUserProfileClient)
      .updateVerificationState(anyLong(), any(), any(), any());
    AsyncMockStubber
      .callSuccessWith(null)
      .when(mockUserProfileClient)
      .createVerificationSubmission(any(), any(), any());
    fileHandleIds = new ArrayList<String>();
    when(mockFileHandleList.getFileHandleIds()).thenReturn(fileHandleIds);

    when(mockView.getOrganization()).thenReturn(submissionCompany);
    when(mockView.getFirstName()).thenReturn(submissionFirstName);
    when(mockView.getLastName()).thenReturn(submissionLastName);
    when(mockView.getLocation()).thenReturn(submissionLocation);
  }

  private void configureWithMockSubmission() {
    boolean isACTMember = false;
    boolean isModal = true;
    widget.configure(mockSubmission, isACTMember, isModal);
  }

  private void configureWithMockProfile() {
    String orcId = "http://orcid.org/123";
    UserProfile profile = getPopulatedProfile();
    boolean isModal = true;
    widget.configure(profile, orcId, isModal, submissionAttachments);
  }

  private void submitVerificationWithMockProfile() {
    configureWithMockProfile();
    fileHandleIds.add("1");
    fileHandleIds.add("2");
    widget.submitVerification();
  }

  @Test
  public void testConfigureEditAsModal() {
    configureWithMockSubmission();

    assertFalse(widget.isNewSubmission());
    verify(mockGinInjector).getVerificationSubmissionModalViewImpl();
    verify(mockView).setFileHandleList(any());
    verify(mockView).setSynAlert(any());
    verify(mockView).setPresenter(widget);

    widget.asWidget();
    verify(mockView).asWidget();
  }

  @Test
  public void testConfigureNewAsModal() {
    boolean isModal = true;
    String orcId = "http://orcid.org/123";
    widget.configure(mockProfile, orcId, isModal, submissionAttachments);

    assertTrue(widget.isNewSubmission());
    verify(mockGinInjector).getVerificationSubmissionModalViewImpl();
    verify(mockView).setFileHandleList(any());
    verify(mockView).setSynAlert(any());
    verify(mockView).setPresenter(widget);
  }

  @Test
  public void testConfigureAsTableRow() {
    boolean isACTMember = true;
    boolean isModal = false;
    widget.configure(mockSubmission, isACTMember, isModal);
    verify(mockGinInjector).getVerificationSubmissionRowViewImpl();
    verify(mockRowView).setFileHandleList(any());
    verify(mockRowView).setSynAlert(any());
    verify(mockRowView).setPresenter(widget);

    widget.asWidget();
    verify(mockRowView).asWidget();
  }

  private UserProfile getPopulatedProfile() {
    UserProfile profile = new UserProfile();
    profile.setFirstName("Bruce");
    profile.setLastName("Banner");
    profile.setCompany("U.S. Government");
    profile.setLocation("Unknown");
    profile.setEmails(Collections.singletonList("bruce@gov.org"));
    return profile;
  }

  @Test
  public void testIsPreconditionsForNewSubmissionMet() {
    configureWithMockSubmission();
    String orcId = "http://orcid.org/123";
    UserProfile profile = getPopulatedProfile();

    assertTrue(widget.isPreconditionsForNewSubmissionMet(profile, orcId));
    assertFalse(widget.isPreconditionsForNewSubmissionMet(profile, ""));
    profile.setFirstName("");
    assertFalse(widget.isPreconditionsForNewSubmissionMet(profile, orcId));
    profile = getPopulatedProfile();
    profile.setLastName("");
    assertFalse(widget.isPreconditionsForNewSubmissionMet(profile, orcId));
    profile = getPopulatedProfile();
    profile.setCompany("");
    assertFalse(widget.isPreconditionsForNewSubmissionMet(profile, orcId));
    profile = getPopulatedProfile();
    profile.setLocation("");
    assertFalse(widget.isPreconditionsForNewSubmissionMet(profile, orcId));
  }

  @Test
  public void testShowNewVerificationSubmission() {
    String orcId = "http://orcid.org/123";
    UserProfile profile = getPopulatedProfile();
    boolean isModal = true;
    widget.configure(profile, orcId, isModal, submissionAttachments);
    widget.show();
    verify(mockView).clear();
    verify(mockView).setCancelButtonVisible(true);
    verify(mockView).setSubmitButtonVisible(true);
    verify(mockFileHandleList).configure();
    verify(mockFileHandleList).setUploadButtonText(anyString());
    verify(mockFileHandleList).setCanDelete(true);
    verify(mockFileHandleList).setCanUpload(true);

    verify(mockView).setFirstName(profile.getFirstName());
    verify(mockView).setLastName(profile.getLastName());
    verify(mockView).setLocation(profile.getLocation());
    verify(mockView).setOrganization(profile.getCompany());
    verify(mockView).setOrcID(orcId);
    verify(mockView).setEmails(profile.getEmails());
    verify(mockFileHandleList, times(submissionAttachments.size()))
      .addFileLink(anyString(), anyString());
    verify(mockFileHandleList).refreshLinkUI();
    verify(mockView).setACTStateHistoryVisible(false);
    verify(mockView).setUploadedFilesUIVisible(true);
    verify(mockView).show();
  }

  private void setCurrentMockState(VerificationStateEnum state, String reason) {
    VerificationState currentState = new VerificationState();
    currentState.setState(state);
    currentState.setReason(reason);
    when(mockSubmission.getStateHistory())
      .thenReturn(Collections.singletonList(currentState));
  }

  @Test
  public void testShowExistingSubmittedVerificationSubmission() {
    configureWithMockSubmission();
    setCurrentMockState(VerificationStateEnum.SUBMITTED, null);

    widget.show();
    verify(mockView).setFirstName(submissionFirstName);
    verify(mockView).setLastName(submissionLastName);
    verify(mockView).setLocation(submissionLocation);
    verify(mockView).setOrganization(submissionCompany);
    verify(mockView).setOrcID(submissionOrcId);
    verify(mockView).setEmails(submissionEmails);

    verify(mockView).setCloseButtonVisible(true);

    // in the configureWithMockSubmission, the current user is not a member of the ACT
    verify(mockView).setACTStateHistoryVisible(false);
    verify(mockView, never()).setACTStateHistory(anyList());
    verify(mockView).setShowSubmissionInModalButtonVisible(false);
    // is in Submitted state, so show the file list
    verify(mockView).setUploadedFilesUIVisible(true);
    verify(mockView).setApproveButtonVisible(false);
    verify(mockView).setRejectButtonVisible(false);

    verify(mockFileHandleList).configure();
    verify(mockFileHandleList).setCanDelete(false);
    verify(mockFileHandleList).setCanUpload(false);
    verify(mockFileHandleList, times(submissionAttachments.size()))
      .addFileLink(any(FileHandleAssociation.class));
    verify(mockFileHandleList).refreshLinkUI();
    verify(mockView).setState(VerificationStateEnum.SUBMITTED);
    verify(mockView).show();
  }

  @Test
  public void testShowExistingSubmittedVerificationSubmissionAsACTAsModal() {
    boolean isACTMember = true;
    boolean isModal = true;
    widget.configure(mockSubmission, isACTMember, isModal);
    setCurrentMockState(VerificationStateEnum.SUBMITTED, null);
    widget.show();
    verify(mockView).setApproveButtonVisible(true);
    verify(mockView).setRejectButtonVisible(true);
    verify(mockView).setACTStateHistoryVisible(true);
    verify(mockView).setACTStateHistory(anyList());
    verify(mockView).setShowSubmissionInModalButtonVisible(false);
    verify(mockView).setUploadedFilesUIVisible(true);
  }

  @Test
  public void testShowExistingSubmittedVerificationSubmissionAsACTAsRow() {
    boolean isACTMember = true;
    boolean isModal = false;
    widget.configure(mockSubmission, isACTMember, isModal);
    setCurrentMockState(VerificationStateEnum.SUBMITTED, null);
    widget.show();
    verify(mockRowView).setApproveButtonVisible(true);
    verify(mockRowView).setRejectButtonVisible(true);
    verify(mockRowView).setACTStateHistoryVisible(true);
    verify(mockRowView).setACTStateHistory(anyList());
    verify(mockRowView).setShowSubmissionInModalButtonVisible(true);
    verify(mockRowView).setUploadedFilesUIVisible(true);
  }

  @Test
  public void testShowExistingApprovedVerificationSubmission() {
    configureWithMockSubmission();
    setCurrentMockState(VerificationStateEnum.APPROVED, null);
    widget.show();
    verify(mockView).setSuspendButtonVisible(false);
  }

  @Test
  public void testShowExistingApprovedVerificationSubmissionAsACT() {
    boolean isACTMember = true;
    boolean isModal = true;
    widget.configure(mockSubmission, isACTMember, isModal);
    setCurrentMockState(VerificationStateEnum.APPROVED, null);
    widget.show();
    verify(mockView).setSuspendButtonVisible(true);
  }

  @Test
  public void testShowExistingSuspendedVerificationSubmission() {
    configureWithMockSubmission();
    String reason = "a reason for the suspended validation";
    setCurrentMockState(VerificationStateEnum.SUSPENDED, reason);
    widget.show();
    verify(mockView).setSuspendedAlertVisible(true);
    verify(mockView).setSuspendedReason(reason);
    verify(mockView).setResubmitButtonVisible(true);
    verify(mockView).setCloseButtonVisible(true);
  }

  @Test
  public void testShowExistingRejectedVerificationSubmission() {
    configureWithMockSubmission();
    String reason = "a reason for the rejected submission";
    setCurrentMockState(VerificationStateEnum.REJECTED, reason);
    widget.show();
    verify(mockView).setSuspendedAlertVisible(true);
    verify(mockView).setSuspendedReason(reason);
    verify(mockView).setResubmitButtonVisible(true);
    verify(mockView).setCloseButtonVisible(true);
  }

  @Test
  public void testUpdateVerificationState() {
    configureWithMockSubmission();
    String reason = "suspending submission for this reason";
    String notes = "ACT internal notes";
    widget.updateVerificationState(
      VerificationStateEnum.SUSPENDED,
      reason,
      notes
    );
    verify(mockView).showInfo(anyString());
    verify(mockView).hide();
    verify(mockGlobalApplicationState).refreshPage();
  }

  @Test
  public void testUpdateVerificationStateFailure() {
    configureWithMockSubmission();
    String reason = "suspending submission for this reason";
    String notes = "ACT internal notes";
    Exception ex = new Exception("something went wrong");
    AsyncMockStubber
      .callFailureWith(ex)
      .when(mockUserProfileClient)
      .updateVerificationState(
        anyLong(),
        any(VerificationState.class),
        anyString(),
        any(AsyncCallback.class)
      );

    widget.updateVerificationState(
      VerificationStateEnum.SUSPENDED,
      reason,
      notes
    );
    verify(mockSynapseAlert).handleException(ex);
  }

  @Test
  public void testRejectVerification() {
    boolean isACTMember = true;
    boolean isModal = true;

    setCurrentMockState(VerificationStateEnum.SUBMITTED, null);
    widget.configure(mockSubmission, isACTMember, isModal);

    widget.rejectVerification();

    verify(mockView)
      .showRejectModal(
        eq(mockSubmission.getId()),
        eq(VerificationStateEnum.SUBMITTED),
        onRejectSuccessCaptor.capture()
      );

    // simulate save reject
    onRejectSuccessCaptor.getValue().run();

    verify(mockView).hide();
    verify(mockGlobalApplicationState).refreshPage();
  }

  @Test
  public void testApproveVerification() {
    boolean isACTMember = true;
    boolean isModal = true;
    widget.configure(mockSubmission, isACTMember, isModal);

    widget.approveVerification();

    String actNotes = "user is amazing";
    verify(mockPromptForValuesModal)
      .configureAndShow(
        anyString(),
        anyString(),
        anyString(),
        promptModalPresenterCaptor.capture()
      );
    confirmRejectionCallback = promptModalPresenterCaptor.getValue();
    confirmRejectionCallback.invoke(actNotes);

    verify(mockUserProfileClient)
      .updateVerificationState(
        anyLong(),
        verificationStateCaptor.capture(),
        anyString(),
        any(AsyncCallback.class)
      );
    assertEquals(
      VerificationStateEnum.APPROVED,
      verificationStateCaptor.getValue().getState()
    );
    assertEquals(actNotes, verificationStateCaptor.getValue().getNotes());
  }

  @Test
  public void testSubmitVerification() {
    String orcId = "http://orcid.org/123";
    UserProfile profile = getPopulatedProfile();
    boolean isModal = true;
    widget.configure(profile, orcId, isModal, submissionAttachments);

    // attach evidence
    fileHandleIds.add("999");
    // attach oath
    fileHandleIds.add("11");
    widget.submitVerification();
    verify(mockUserProfileClient)
      .createVerificationSubmission(
        any(VerificationSubmission.class),
        eq(hostPageURL),
        any(AsyncCallback.class)
      );

    verify(mockView).showInfo(anyString());
    verify(mockView).hide();
    verify(mockGlobalApplicationState).refreshPage();
  }

  @Test
  public void testSubmitVerificationNoEvidence() {
    String orcId = "http://orcid.org/123";
    UserProfile profile = getPopulatedProfile();
    boolean isModal = true;
    widget.configure(profile, orcId, isModal, submissionAttachments);

    // no evidence
    widget.submitVerification();
    verify(mockSynapseAlert).showError(anyString());
    verify(mockUserProfileClient, never())
      .createVerificationSubmission(
        any(VerificationSubmission.class),
        anyString(),
        any(AsyncCallback.class)
      );
  }

  @Test
  public void testSubmitVerificationSingleFile() {
    String orcId = "http://orcid.org/123";
    UserProfile profile = getPopulatedProfile();
    boolean isModal = true;
    widget.configure(profile, orcId, isModal, submissionAttachments);

    // attach evidence
    fileHandleIds.add("999");
    widget.submitVerification();

    // not enough evidence
    verify(mockSynapseAlert).showError(anyString());
    verify(mockUserProfileClient, never())
      .createVerificationSubmission(
        any(VerificationSubmission.class),
        anyString(),
        any(AsyncCallback.class)
      );
  }

  @Test
  public void testResubmitCallback() {
    configureWithMockSubmission();
    Callback callback = mock(Callback.class);
    widget.setResubmitCallback(callback);

    widget.recreateVerification();
    verify(mockView).hide();
    verify(callback).invoke();
  }

  @Test
  public void testSubmitNothingMissing() {
    submitVerificationWithMockProfile();
    verify(mockUserProfileClient)
      .createVerificationSubmission(
        any(VerificationSubmission.class),
        anyString(),
        any(AsyncCallback.class)
      );
  }

  @Test
  public void testSubmitMissingFirstName() {
    when(mockView.getFirstName()).thenReturn("");
    submitVerificationWithMockProfile();
    verify(mockUserProfileClient, never())
      .createVerificationSubmission(
        any(VerificationSubmission.class),
        anyString(),
        any(AsyncCallback.class)
      );
    verify(mockSynapseAlert)
      .showError(VerificationSubmissionWidget.FILL_IN_PROFILE_FIELDS_MESSAGE);
  }

  @Test
  public void testSubmitMissingLastName() {
    when(mockView.getLastName()).thenReturn("");
    submitVerificationWithMockProfile();
    verify(mockUserProfileClient, never())
      .createVerificationSubmission(
        any(VerificationSubmission.class),
        anyString(),
        any(AsyncCallback.class)
      );
    verify(mockSynapseAlert)
      .showError(VerificationSubmissionWidget.FILL_IN_PROFILE_FIELDS_MESSAGE);
  }

  @Test
  public void testSubmitMissingOrganization() {
    when(mockView.getOrganization()).thenReturn("");
    submitVerificationWithMockProfile();
    verify(mockUserProfileClient, never())
      .createVerificationSubmission(
        any(VerificationSubmission.class),
        anyString(),
        any(AsyncCallback.class)
      );
    verify(mockSynapseAlert)
      .showError(VerificationSubmissionWidget.FILL_IN_PROFILE_FIELDS_MESSAGE);
  }

  @Test
  public void testSubmitMissingLocation() {
    when(mockView.getLocation()).thenReturn("");
    submitVerificationWithMockProfile();
    verify(mockUserProfileClient, never())
      .createVerificationSubmission(
        any(VerificationSubmission.class),
        anyString(),
        any(AsyncCallback.class)
      );
    verify(mockSynapseAlert)
      .showError(VerificationSubmissionWidget.FILL_IN_PROFILE_FIELDS_MESSAGE);
  }
}
