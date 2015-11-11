package org.sagebionetworks.web.client.widget.verification;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.sagebionetworks.repo.model.UserBundle;
import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.repo.model.file.FileHandleAssociateType;
import org.sagebionetworks.repo.model.file.FileHandleAssociation;
import org.sagebionetworks.repo.model.verification.AttachmentMetadata;
import org.sagebionetworks.repo.model.verification.VerificationState;
import org.sagebionetworks.repo.model.verification.VerificationStateEnum;
import org.sagebionetworks.repo.model.verification.VerificationSubmission;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.UserProfileClientAsync;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.entity.MarkdownWidget;
import org.sagebionetworks.web.client.widget.entity.PromptModalView;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.client.widget.upload.FileHandleList;
import org.sagebionetworks.web.shared.WebConstants;
import org.sagebionetworks.web.shared.WikiPageKey;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;

public class VerificationSubmissionModal implements VerificationSubmissionModalView.Presenter{
	private UserProfileClientAsync userProfileClient;
	private SynapseClientAsync synapseClient;
	private MarkdownWidget helpWikiPage;
	private SynapseAlert synAlert;
	private FileHandleList fileHandleList;
	private static WikiPageKey validationPageKey;
	private UserBundle userBundle;
	private VerificationSubmissionModalView view;
	private SynapseJSNIUtils jsniUtils;
	private PromptModalView promptModal;
	
	CallbackP<String> fileHandleClickedCallback;
	CallbackP<String> rawFileHandleClickedCallback;
	//this could be Reject or Suspend.  We store this state while the reason is being collected from the ACT user
	private VerificationStateEnum actRejectState;
	
	@Inject
	public VerificationSubmissionModal(
			VerificationSubmissionModalView view,
			UserProfileClientAsync userProfileClient,
			MarkdownWidget helpWikiPage,
			SynapseClientAsync synapseClient,
			SynapseAlert synAlert,
			FileHandleList fileHandleList,
			SynapseJSNIUtils jsniUtils,
			PromptModalView promptModalView
			) {
		this.view = view;
		this.userProfileClient = userProfileClient;
		this.helpWikiPage = helpWikiPage;
		this.synapseClient = synapseClient;
		this.synAlert = synAlert;
		this.fileHandleList = fileHandleList;
		this.jsniUtils = jsniUtils;
		this.promptModal = promptModalView;
		promptModal.configure("", "Reason", "OK", "");
		promptModal.setPresenter(new PromptModalView.Presenter() {
			@Override
			public void onPrimary() {
				updateVerificationState(actRejectState, promptModal.getName());
			}
		});
		view.setFileHandleList(fileHandleList.asWidget());
		view.setWikiPage(helpWikiPage.asWidget());
		view.setPromptModal(promptModal.asWidget());
		fileHandleClickedCallback = new CallbackP<String>(){
			@Override
			public void invoke(String fileHandleId) {
				getVerificationSubmissionHandleUrlAndOpen(fileHandleId);
			}
		};
		
		rawFileHandleClickedCallback = new CallbackP<String>(){
			@Override
			public void invoke(String fileHandleId) {
				getRawFileHandleUrlAndOpen(fileHandleId);
			}
		};
	}
	
	public VerificationSubmissionModal configure(UserBundle userBundle) {
		this.userBundle = userBundle;
		return this;
	}
	
	public void getVerificationSubmissionHandleUrlAndOpen(String fileHandleId) {
		FileHandleAssociation fha = new FileHandleAssociation();
		fha.setAssociateObjectId(userBundle.getVerificationSubmission().getId());
		fha.setAssociateObjectType(FileHandleAssociateType.VerificationSubmission);
		fha.setFileHandleId(fileHandleId);
		userProfileClient.getFileURL(fha, new AsyncCallback<String>() {
			@Override
			public void onSuccess(String resultUrl) {
				view.openWindow(resultUrl);
			}
			@Override
			public void onFailure(Throwable caught) {
				synAlert.handleException(caught);
			}
		});
	}
	
	public void getRawFileHandleUrlAndOpen(String fileHandleId) {
		String url = jsniUtils.getBaseFileHandleUrl() + "?rawFileHandleId=" + fileHandleId;
		view.openWindow(url);
	}
	
	public void show() {
		view.clear();
		if (isPreconditionsMet()) {
			if (userBundle.getVerificationSubmission() == null) {
				//show wiki on validation process
				view.setWikiPageVisible(true);
				loadWikiHelpContent();
				view.setCancelButtonVisible(true);
				view.setSubmitButtonVisible(true);
				fileHandleList.configure(rawFileHandleClickedCallback)
					.setUploadButtonText("Upload evidence...")
					.setCanDelete(true)
					.setCanUpload(true);
				UserProfile profile = userBundle.getUserProfile();
				view.setFirstName(profile.getFirstName());
				view.setLastName(profile.getLastName());
				view.setLocation(profile.getLocation());
				view.setOrganization(profile.getCompany());
				view.setOrcID(userBundle.getORCID());
				view.setEmails(profile.getEmails());
				view.setTitle("Profile Validation");
			} else {
				//view an existing verification submission
				VerificationSubmission submission = userBundle.getVerificationSubmission();
				boolean isACTMember = userBundle.getIsACTMember();
				view.setOKButtonVisible(true);
				VerificationState currentState = submission.getStateHistory().get(submission.getStateHistory().size()-1);
				if (VerificationStateEnum.SUBMITTED.equals(currentState.getState())) {
					//pending
					view.setApproveButtonVisible(isACTMember);
					view.setRejectButtonVisible(isACTMember);
					view.setTitle("Profile Validation");
				} else if (VerificationStateEnum.APPROVED.equals(currentState.getState())) {
					//approved
					view.setSuspendButtonVisible(isACTMember);
					view.setTitle("Validated");
				} else if (VerificationStateEnum.SUSPENDED.equals(currentState.getState()) || VerificationStateEnum.REJECTED.equals(currentState.getState())) {
					view.setTitle("Validation Suspended");
					view.setSuspendedReason(currentState.getReason());
				}
				if (isACTMember) {
					fileHandleList.configure(fileHandleClickedCallback)
						.setCanDelete(false)
						.setCanUpload(false);
					for (AttachmentMetadata metadata : submission.getAttachments()) {
						fileHandleList.addFileLink(metadata.getId(), metadata.getFileName());
					}
					
					view.setOrcID(userBundle.getORCID());
					view.setEmails(userBundle.getUserProfile().getEmails());
				}
			}
			fileHandleList.refreshLinkUI();
			view.show();
		}
	}
	
	public boolean isPreconditionsMet() {
		if (userBundle.getVerificationSubmission() == null) {
			//new submission.  make sure orc id is set and profile is populated.
			if (!DisplayUtils.isDefined(userBundle.getORCID())) {
				view.showErrorMessage("Please link your ORC ID before requesting profile validation.");
				return false;
			}
			UserProfile profile = userBundle.getUserProfile();
			if (!DisplayUtils.isDefined(profile.getFirstName()) || !DisplayUtils.isDefined(profile.getLastName())) {
				view.showErrorMessage("Please fill in your first and last name before requesting profile validation.");
				return false;
			}
			if (!DisplayUtils.isDefined(profile.getCompany())) {
				view.showErrorMessage("Please fill in your affiliation before requesting profile validation.");
				return false;
			}
			if (!DisplayUtils.isDefined(profile.getLocation())) {
				view.showErrorMessage("Please fill in your city and country before requesting profile validation.");
				return false;
			}
		}
		return true;
	}
	
	public void loadWikiHelpContent() {
		if (validationPageKey == null) {
			//get the wiki page key, then load the content
			synapseClient.getPageNameToWikiKeyMap(new AsyncCallback<HashMap<String,WikiPageKey>>() {
				@Override
				public void onSuccess(HashMap<String,WikiPageKey> result) {
					validationPageKey = result.get(WebConstants.VALIDATION);
					loadWikiHelpContent(validationPageKey);
				};
				@Override
				public void onFailure(Throwable caught) {
					synAlert.handleException(caught);
				}
			});
		} else {
			loadWikiHelpContent(validationPageKey);
		}
	}
	
	public void loadWikiHelpContent(WikiPageKey key) {
		helpWikiPage.loadMarkdownFromWikiPage(key, false);
		view.setWikiPageVisible(true);
	}
	
	@Override
	public void approveVerification() {
		updateVerificationState(VerificationStateEnum.APPROVED, null);
	}
	
	private void updateVerificationState(VerificationStateEnum state, String reason) {
		long verificationId = Long.parseLong(userBundle.getVerificationSubmission().getId());
		VerificationState newState = new VerificationState();
		newState.setState(state);
		newState.setReason(reason);
		userProfileClient.updateVerificationState(verificationId, newState, new AsyncCallback<Void>() {
			@Override
			public void onSuccess(Void result) {
				view.showInfo("Submission state has been updated.", "");
				view.hide();
			}
			@Override
			public void onFailure(Throwable caught) {
				synAlert.handleException(caught);
			}
		});

	}
	
	@Override
	public void rejectVerification() {
		//get reason, and update state
		promptModal.clear();
		actRejectState = VerificationStateEnum.REJECTED;
		promptModal.show();
	}
	@Override
	public void submitVerification() {
		//create a new verification submission
		UserProfile profile = userBundle.getUserProfile();
		VerificationSubmission sub = new VerificationSubmission();
		List<AttachmentMetadata> attachments = new ArrayList<AttachmentMetadata>();
		for (String fileHandleId : fileHandleList.getFileHandleIds()) {
			AttachmentMetadata meta = new AttachmentMetadata();
			meta.setId(fileHandleId);
		}
		sub.setAttachments(attachments);
		sub.setCompany(profile.getCompany());
		sub.setEmails(profile.getEmails());
		sub.setFirstName(profile.getFirstName());
		sub.setLastName(profile.getLastName());
		sub.setLocation(profile.getLocation());
		sub.setOrcid(userBundle.getORCID());
		userProfileClient.createVerificationSubmission(sub, new AsyncCallback<VerificationSubmission>() {
			@Override
			public void onSuccess(VerificationSubmission result) {
				//submitted, hide modal
				view.showInfo("Successfully submitted profile for validation.", "");
				view.hide();
			}
			@Override
			public void onFailure(Throwable caught) {
				synAlert.handleException(caught);
			}
		});
	}
	@Override
	public void suspendVerification() {
		promptModal.clear();
		actRejectState = VerificationStateEnum.SUSPENDED;
		promptModal.show();

	}
}
