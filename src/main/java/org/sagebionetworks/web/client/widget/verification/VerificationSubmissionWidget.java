package org.sagebionetworks.web.client.widget.verification;

import static org.sagebionetworks.web.client.ServiceEntryPointUtils.fixServiceEntryPoint;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.repo.model.file.FileHandleAssociateType;
import org.sagebionetworks.repo.model.file.FileHandleAssociation;
import org.sagebionetworks.repo.model.verification.AttachmentMetadata;
import org.sagebionetworks.repo.model.verification.VerificationState;
import org.sagebionetworks.repo.model.verification.VerificationStateEnum;
import org.sagebionetworks.repo.model.verification.VerificationSubmission;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.GWTWrapper;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.UserProfileClientAsync;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.entity.PromptForValuesModalView;
import org.sagebionetworks.web.client.widget.entity.act.RejectReasonWidget;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.client.widget.upload.FileHandleList;

public class VerificationSubmissionWidget
  implements VerificationSubmissionWidgetView.Presenter, IsWidget {

  private static final String ACT_NOTES_PROMPT = "ACT notes (optional)";
  public static final String FILL_IN_PROFILE_FIELDS_MESSAGE =
    "Please edit your profile to fill in your first name, last name, affiliation, and city/country before requesting profile validation.";
  private UserProfileClientAsync userProfileClient;
  private SynapseAlert synAlert;
  private FileHandleList fileHandleList;
  private UserProfile profile;
  private VerificationSubmission submission;
  private String orcId;
  private List<AttachmentMetadata> existingAttachments;
  private VerificationSubmissionWidgetView view;
  private GlobalApplicationState globalAppState;
  private PortalGinInjector ginInjector;
  private GWTWrapper gwt;
  // this could be Reject or Suspend. We store this state while the reason is being collected from the
  // ACT user
  private VerificationStateEnum actRejectState;
  private boolean isACTMember;
  private boolean isNewSubmission;
  private boolean isModal;
  private Callback resubmitCallback;

  @Inject
  public VerificationSubmissionWidget(
    PortalGinInjector ginInjector,
    UserProfileClientAsync userProfileClient,
    SynapseAlert synAlert,
    FileHandleList fileHandleList,
    GlobalApplicationState globalAppState,
    GWTWrapper gwt
  ) {
    this.ginInjector = ginInjector;
    this.userProfileClient = userProfileClient;
    fixServiceEntryPoint(userProfileClient);
    this.synAlert = synAlert;
    this.fileHandleList = fileHandleList;
    this.globalAppState = globalAppState;
    this.gwt = gwt;
  }

  public void initView(boolean isModal) {
    if (isModal) {
      view = ginInjector.getVerificationSubmissionModalViewImpl();
    } else {
      view = ginInjector.getVerificationSubmissionRowViewImpl();
    }
    view.setFileHandleList(fileHandleList.asWidget());
    view.setSynAlert(synAlert.asWidget());
    view.setPresenter(this);
  }

  /**
   * Configuration used to view an existing verification submission
   *
   * @param verificationSubmission
   * @param isACTMember
   * @param isModal
   * @return
   */
  public VerificationSubmissionWidget configure(
    VerificationSubmission verificationSubmission,
    boolean isACTMember,
    boolean isModal
  ) {
    isNewSubmission = false;
    this.submission = verificationSubmission;
    this.isACTMember = isACTMember;
    this.existingAttachments = verificationSubmission.getAttachments();
    this.orcId = null;
    this.profile = null;
    this.isModal = isModal;
    initView(isModal);
    view.setProfileLink(
      verificationSubmission.getCreatedBy(),
      "#!Profile:" + verificationSubmission.getCreatedBy()
    );
    return this;
  }

  @Override
  public void showSubmissionInModal() {
    VerificationSubmissionWidget widget = ginInjector.getVerificationSubmissionWidget();
    boolean isModal = true;
    if (isNewSubmission) {
      widget.configure(profile, orcId, isModal, existingAttachments);
    } else {
      widget.configure(submission, isACTMember, isModal);
    }
    widget.show();
  }

  public VerificationSubmissionWidget setResubmitCallback(Callback c) {
    this.resubmitCallback = c;
    return this;
  }

  /**
   * Configuration used to create a new verification submission.
   *
   * @param userProfile
   * @param orcId
   * @param isModal
   * @return
   */
  public VerificationSubmissionWidget configure(
    UserProfile userProfile,
    String orcId,
    boolean isModal,
    List<AttachmentMetadata> existingAttachments
  ) {
    isNewSubmission = true;
    this.profile = userProfile;
    this.isACTMember = false;
    this.orcId = orcId;
    this.submission = null;
    this.existingAttachments = existingAttachments;
    this.isModal = isModal;
    initView(isModal);
    return this;
  }

  public void show() {
    view.clear();
    synAlert.clear();
    if (isNewSubmission) {
      showNewVerificationSubmission();
    } else {
      showExistingVerificationSubmission();
    }
  }

  public void showNewVerificationSubmission() {
    if (isPreconditionsForNewSubmissionMet(profile, orcId)) {
      // show wiki on validation process
      view.setCancelButtonVisible(true);
      view.setSubmitButtonVisible(true);
      fileHandleList
        .configure()
        .setUploadButtonText("Upload...")
        .setCanDelete(true)
        .setCanUpload(true);
      view.setFirstName(profile.getFirstName());
      view.setLastName(profile.getLastName());
      view.setLocation(profile.getLocation());
      view.setOrganization(profile.getCompany());
      view.setOrcID(orcId);
      view.setEmails(profile.getEmails());
      view.setTitle("Profile Validation");
      view.setProfileFieldsEditable(true);
      view.setACTStateHistoryVisible(false);
      view.setUploadedFilesUIVisible(true);
      initAttachments();
      view.show();
    }
  }

  public void showExistingVerificationSubmission() {
    // view an existing verification submission
    view.setFirstName(submission.getFirstName());
    view.setLastName(submission.getLastName());
    view.setLocation(submission.getLocation());
    view.setOrganization(submission.getCompany());
    view.setOrcID(submission.getOrcid());
    view.setEmails(submission.getEmails());
    view.setProfileFieldsEditable(false);
    // marking own submission as rejected currently forbidden, and we don't want to actually delete the
    // submission
    view.setDeleteButtonVisible(false);

    VerificationState currentState = submission
      .getStateHistory()
      .get(submission.getStateHistory().size() - 1);
    view.setState(currentState.getState());
    view.setACTStateHistoryVisible(isACTMember);
    if (isACTMember) {
      view.setACTStateHistory(submission.getStateHistory());
    }
    view.setShowSubmissionInModalButtonVisible(isACTMember && !isModal);
    // only show the uploaded files if in submitted state
    view.setUploadedFilesUIVisible(
      VerificationStateEnum.SUBMITTED.equals(currentState.getState())
    );

    if (VerificationStateEnum.SUBMITTED.equals(currentState.getState())) {
      // pending
      view.setApproveButtonVisible(isACTMember);
      view.setRejectButtonVisible(isACTMember);
      view.setTitle("Profile Validation Pending");
      view.setCloseButtonVisible(true);
    } else if (VerificationStateEnum.APPROVED.equals(currentState.getState())) {
      // approved
      view.setOKButtonVisible(true);
      view.setSuspendButtonVisible(isACTMember);
      view.setTitle("Profile Validated");
    } else if (
      VerificationStateEnum.SUSPENDED.equals(currentState.getState()) ||
      VerificationStateEnum.REJECTED.equals(currentState.getState())
    ) {
      view.setTitle("Profile Validation Suspended");
      view.setSuspendedReason(currentState.getReason());
      view.setSuspendedAlertVisible(true);
      view.setResubmitButtonVisible(!isACTMember);
      view.setCloseButtonVisible(true);
    }
    fileHandleList.configure().setCanDelete(false).setCanUpload(false);
    initAttachments();
    view.show();
  }

  public void initAttachments() {
    if (submission == null) {
      for (AttachmentMetadata metadata : existingAttachments) {
        fileHandleList.addFileLink(metadata.getFileName(), metadata.getId());
      }
    } else {
      for (AttachmentMetadata metadata : existingAttachments) {
        FileHandleAssociation fha = new FileHandleAssociation();
        fha.setAssociateObjectId(submission.getId());
        fha.setAssociateObjectType(
          FileHandleAssociateType.VerificationSubmission
        );
        fha.setFileHandleId(metadata.getId());
        fileHandleList.addFileLink(fha);
      }
    }
    fileHandleList.refreshLinkUI();
  }

  public boolean isPreconditionsForNewSubmissionMet(
    UserProfile profile,
    String orcId
  ) {
    // new submission. make sure orcid is set and profile is populated.
    if (!DisplayUtils.isDefined(orcId)) {
      view.showErrorMessage(
        "Please link your ORCID before requesting profile validation."
      );
      return false;
    }
    if (
      !DisplayUtils.isDefined(profile.getFirstName()) ||
      !DisplayUtils.isDefined(profile.getLastName()) ||
      !DisplayUtils.isDefined(profile.getCompany()) ||
      !DisplayUtils.isDefined(profile.getLocation())
    ) {
      view.showErrorMessage(FILL_IN_PROFILE_FIELDS_MESSAGE);
      return false;
    }
    return true;
  }

  public boolean isPreconditionsForSubmissionMet() {
    if (
      !DisplayUtils.isDefined(view.getFirstName()) ||
      !DisplayUtils.isDefined(view.getLastName()) ||
      !DisplayUtils.isDefined(view.getOrganization()) ||
      !DisplayUtils.isDefined(view.getLocation())
    ) {
      return false;
    }
    return true;
  }

  private String getConfirmationDialogTitle(VerificationStateEnum newState) {
    String verb = "";
    switch (newState) {
      case SUSPENDED:
        verb = "Suspend";
        break;
      case REJECTED:
        verb = "Reject";
        break;
      case APPROVED:
        verb = "Approve";
        break;
      case SUBMITTED:
      default:
      // should be unreachable
    }
    return (
      "Are you sure you want to " +
      verb +
      " this User Profile Validation Submission?"
    );
  }

  @Override
  public void approveVerification() {
    PromptForValuesModalView promptForValuesModal = ginInjector.getPromptForValuesModal();
    view.setPromptModal(promptForValuesModal.asWidget());
    promptForValuesModal.configureAndShow(
      getConfirmationDialogTitle(VerificationStateEnum.APPROVED),
      ACT_NOTES_PROMPT,
      "",
      actNotes -> {
        promptForValuesModal.hide();
        updateVerificationState(VerificationStateEnum.APPROVED, null, actNotes);
      }
    );
  }

  public void updateVerificationState(
    VerificationStateEnum state,
    String reason,
    String notes
  ) {
    long verificationId = Long.parseLong(submission.getId());
    VerificationState newState = new VerificationState();
    newState.setState(state);
    newState.setReason(reason);
    newState.setNotes(notes);
    userProfileClient.updateVerificationState(
      verificationId,
      newState,
      gwt.getHostPageBaseURL(),
      new AsyncCallback<Void>() {
        @Override
        public void onSuccess(Void result) {
          handleSuccess("Submission state has been updated.");
        }

        @Override
        public void onFailure(Throwable caught) {
          synAlert.handleException(caught);
        }
      }
    );
  }

  public void handleSuccess(String message) {
    view.showInfo(message);
    view.hide();
    globalAppState.refreshPage();
  }

  @Override
  public void rejectVerification() {
    // get reason, and update state
    actRejectState = VerificationStateEnum.REJECTED;
    rejectSuspendVerification();
  }

  @Override
  public void submitVerification() {
    // create a new verification submission
    synAlert.clear();
    VerificationSubmission sub = new VerificationSubmission();
    List<AttachmentMetadata> attachments = new ArrayList<AttachmentMetadata>();
    for (String fileHandleId : fileHandleList.getFileHandleIds()) {
      AttachmentMetadata meta = new AttachmentMetadata();
      meta.setId(fileHandleId);
      attachments.add(meta);
    }
    if (attachments.size() < 2) {
      synAlert.showError(
        "Please upload your signed and initialed oath AND your documentation, then re-submit."
      );
      return;
    }
    if (!isPreconditionsForSubmissionMet()) {
      synAlert.showError(FILL_IN_PROFILE_FIELDS_MESSAGE);
      return;
    }
    sub.setAttachments(attachments);
    sub.setCompany(view.getOrganization());
    sub.setEmails(profile.getEmails());
    sub.setFirstName(view.getFirstName());
    sub.setLastName(view.getLastName());
    sub.setLocation(view.getLocation());
    sub.setOrcid(orcId);
    userProfileClient.createVerificationSubmission(
      sub,
      gwt.getHostPageBaseURL(),
      new AsyncCallback<VerificationSubmission>() {
        @Override
        public void onSuccess(VerificationSubmission result) {
          handleSuccess("Successfully submitted profile for validation.");
        }

        @Override
        public void onFailure(Throwable caught) {
          synAlert.handleException(caught);
        }
      }
    );
  }

  @Override
  public void suspendVerification() {
    actRejectState = VerificationStateEnum.SUSPENDED;
    rejectSuspendVerification();
  }

  private void rejectSuspendVerification() {
    RejectReasonWidget promptModal = ginInjector.getRejectReasonWidget();
    view.setPromptModal(promptModal.asWidget());
    promptModal.show(rejectionReason -> {
      PromptForValuesModalView promptForValuesModal = ginInjector.getPromptForValuesModal();
      view.setPromptModal(promptForValuesModal.asWidget());
      promptForValuesModal.configureAndShow(
        getConfirmationDialogTitle(actRejectState),
        ACT_NOTES_PROMPT,
        "",
        actNotes -> {
          promptForValuesModal.hide();
          updateVerificationState(actRejectState, rejectionReason, actNotes);
        }
      );
    });
  }

  @Override
  public void deleteVerification() {
    // TODO: allow user to rescind own submission
    // long verificationId = Long.parseLong(submission.getId());
    // userProfileClient.deleteVerificationSubmission(verificationId,new AsyncCallback<Void>() {
    // @Override
    // public void onSuccess(Void result) {
    // handleSuccess("Submission state has been deleted.");
    // }
    // @Override
    // public void onFailure(Throwable caught) {
    // synAlert.handleException(caught);
    // }
    // });
  }

  @Override
  public Widget asWidget() {
    return view.asWidget();
  }

  public boolean isNewSubmission() {
    return isNewSubmission;
  }

  @Override
  public void recreateVerification() {
    if (resubmitCallback != null) {
      view.hide();
      resubmitCallback.invoke();
    }
  }
}
