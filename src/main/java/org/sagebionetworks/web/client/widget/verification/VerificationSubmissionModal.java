package org.sagebionetworks.web.client.widget.verification;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.repo.model.file.FileHandleAssociateType;
import org.sagebionetworks.repo.model.file.FileHandleAssociation;
import org.sagebionetworks.repo.model.verification.AttachmentMetadata;
import org.sagebionetworks.repo.model.verification.VerificationState;
import org.sagebionetworks.repo.model.verification.VerificationStateEnum;
import org.sagebionetworks.repo.model.verification.VerificationSubmission;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.UserProfileClientAsync;
import org.sagebionetworks.web.client.cookie.CookieProvider;
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
	private UserProfile profile;
	private VerificationSubmission submission;
	private String orcId;
	private VerificationSubmissionModalView view;
	private SynapseJSNIUtils jsniUtils;
	private PromptModalView promptModal;
	private CookieProvider cookies;
	private GlobalApplicationState globalAppState;
	
	CallbackP<String> fileHandleClickedCallback;
	CallbackP<String> rawFileHandleClickedCallback;
	//this could be Reject or Suspend.  We store this state while the reason is being collected from the ACT user
	private VerificationStateEnum actRejectState;
	private boolean isACTMember;
	private boolean isNewSubmission;
	
	@Inject
	public VerificationSubmissionModal(
			VerificationSubmissionModalView view,
			UserProfileClientAsync userProfileClient,
			MarkdownWidget helpWikiPage,
			SynapseClientAsync synapseClient,
			SynapseAlert synAlert,
			FileHandleList fileHandleList,
			SynapseJSNIUtils jsniUtils,
			PromptModalView promptModalView,
			CookieProvider cookies,
			GlobalApplicationState globalAppState
			) {
		this.view = view;
		this.userProfileClient = userProfileClient;
		this.helpWikiPage = helpWikiPage;
		this.synapseClient = synapseClient;
		this.synAlert = synAlert;
		this.fileHandleList = fileHandleList;
		this.jsniUtils = jsniUtils;
		this.promptModal = promptModalView;
		this.cookies = cookies;
		this.globalAppState = globalAppState;
		
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
		view.setSynAlert(synAlert.asWidget());
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
		view.setPresenter(this);
	}
	
	public VerificationSubmissionModal configure(VerificationSubmission verificationSubmission, boolean isACTMember) {
		isNewSubmission = false;
		this.submission = verificationSubmission;
		this.isACTMember = isACTMember;
		this.orcId = null;
		this.profile = null;
		return this;
	}
	
	public VerificationSubmissionModal configure(UserProfile userProfile, String orcId, boolean isACTMember) {
		isNewSubmission = true;
		this.profile = userProfile;
		this.isACTMember = isACTMember;
		this.orcId = orcId;
		this.submission = null;
		return this;
	}
	
	public void getVerificationSubmissionHandleUrlAndOpen(String fileHandleId) {
		FileHandleAssociation fha = new FileHandleAssociation();
		fha.setAssociateObjectId(submission.getId());
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
		synAlert.clear();
		if (isNewSubmission) {
			showNewVerificationSubmission();
		} else {
			showExistingVerificationSubmission();
		}
	}
	
	public void showNewVerificationSubmission() {
		if (isPreconditionsForNewSubmissionMet()) {
			//show wiki on validation process
			view.setWikiPageVisible(true);
			loadWikiHelpContent();
			view.setCancelButtonVisible(true);
			view.setSubmitButtonVisible(true);
			fileHandleList.configure(rawFileHandleClickedCallback)
				.setUploadButtonText("Upload evidence...")
				.setCanDelete(true)
				.setCanUpload(true);
			view.setFirstName(profile.getFirstName());
			view.setLastName(profile.getLastName());
			view.setLocation(profile.getLocation());
			view.setOrganization(profile.getCompany());
			view.setOrcID(orcId);
			view.setEmails(profile.getEmails());
			view.setTitle("Profile Validation");
			
			fileHandleList.refreshLinkUI();
			view.show();
		}
	}
	
	public void showExistingVerificationSubmission() {
		//view an existing verification submission
		view.setWikiPageVisible(false);
		view.setFirstName(submission.getFirstName());
		view.setLastName(submission.getLastName());
		view.setLocation(submission.getLocation());
		view.setOrganization(submission.getCompany());
		view.setOrcID(submission.getOrcid());
		view.setEmails(submission.getEmails());
		
		//show delete button if not act (is the owner), and in alpha website mode
		view.setDeleteButtonVisible(!isACTMember && DisplayUtils.isInTestWebsite(cookies));
		
		view.setOKButtonVisible(true);
		VerificationState currentState = submission.getStateHistory().get(submission.getStateHistory().size()-1);
		if (VerificationStateEnum.SUBMITTED.equals(currentState.getState())) {
			//pending
			view.setApproveButtonVisible(isACTMember);
			view.setRejectButtonVisible(isACTMember);
			view.setTitle("Profile Validation Pending");
		} else if (VerificationStateEnum.APPROVED.equals(currentState.getState())) {
			//approved
			view.setSuspendButtonVisible(isACTMember);
			view.setTitle("Profile Validated");
		} else if (VerificationStateEnum.SUSPENDED.equals(currentState.getState()) || VerificationStateEnum.REJECTED.equals(currentState.getState())) {
			view.setTitle("Profile Validation Suspended");
			view.setSuspendedReason(currentState.getReason());
			view.setSuspendedAlertVisible(true);
		}
		fileHandleList.configure(fileHandleClickedCallback)
			.setCanDelete(false)
			.setCanUpload(false);
		for (AttachmentMetadata metadata : submission.getAttachments()) {
			fileHandleList.addFileLink(metadata.getId(), metadata.getFileName());
		}
		fileHandleList.refreshLinkUI();
		view.show();
	}
	
	public boolean isPreconditionsForNewSubmissionMet() {
		//new submission.  make sure orc id is set and profile is populated.
		if (!DisplayUtils.isDefined(orcId)) {
			view.showErrorMessage("Please link your ORC ID before requesting profile validation.");
			return false;
		}
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
		long verificationId = Long.parseLong(submission.getId());
		VerificationState newState = new VerificationState();
		newState.setState(state);
		newState.setReason(reason);
		userProfileClient.updateVerificationState(verificationId, newState, new AsyncCallback<Void>() {
			@Override
			public void onSuccess(Void result) {
				handleSuccess("Submission state has been updated.");
			}
			@Override
			public void onFailure(Throwable caught) {
				synAlert.handleException(caught);
			}
		});
	}
	
	public void handleSuccess(String message) {
		view.showInfo(message, "");
		view.hide();

		globalAppState.refreshPage();
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
		synAlert.clear();
		VerificationSubmission sub = new VerificationSubmission();
		List<AttachmentMetadata> attachments = new ArrayList<AttachmentMetadata>();
		for (String fileHandleId : fileHandleList.getFileHandleIds()) {
			AttachmentMetadata meta = new AttachmentMetadata();
			meta.setId(fileHandleId);
			attachments.add(meta);
		}
		if (attachments.size() == 0) {
			synAlert.showError("Please upload evidence and re-submit.");
			return;
		}
		sub.setAttachments(attachments);
		sub.setCompany(profile.getCompany());
		sub.setEmails(profile.getEmails());
		sub.setFirstName(profile.getFirstName());
		sub.setLastName(profile.getLastName());
		sub.setLocation(profile.getLocation());
		sub.setOrcid(orcId);
		userProfileClient.createVerificationSubmission(sub, new AsyncCallback<VerificationSubmission>() {
			@Override
			public void onSuccess(VerificationSubmission result) {
				handleSuccess("Successfully submitted profile for validation.");
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
	
	@Override
	public void deleteVerification() {
		long verificationId = Long.parseLong(submission.getId());
		userProfileClient.deleteVerificationSubmission(verificationId, new AsyncCallback<Void>() {
			@Override
			public void onSuccess(Void result) {
				handleSuccess("Submission deleted.");
			}
			@Override
			public void onFailure(Throwable caught) {
				synAlert.handleException(caught);
			}
		});
	}
}
