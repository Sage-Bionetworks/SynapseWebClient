package org.sagebionetworks.web.client.widget.verification;

import java.util.HashMap;

import org.sagebionetworks.repo.model.ObjectType;
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
	CallbackP<String> fileHandleClickedCallback;
	CallbackP<String> rawFileHandleClickedCallback;
	
	@Inject
	public VerificationSubmissionModal(
			VerificationSubmissionModalView view,
			UserProfileClientAsync userProfileClient,
			MarkdownWidget helpWikiPage,
			SynapseClientAsync synapseClient,
			SynapseAlert synAlert,
			FileHandleList fileHandleList,
			SynapseJSNIUtils jsniUtils
			) {
		this.view = view;
		this.userProfileClient = userProfileClient;
		this.helpWikiPage = helpWikiPage;
		this.synapseClient = synapseClient;
		this.synAlert = synAlert;
		this.fileHandleList = fileHandleList;
		this.jsniUtils = jsniUtils;
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
				} else if (VerificationStateEnum.APPROVED.equals(currentState.getState())) {
					//approved
					view.setSuspendButtonVisible(isACTMember);
				} else if (VerificationStateEnum.REJECTED.equals(currentState.getState())) {
					//rejected
					//show reason
					view.setSuspendedReason(currentState.getReason());
				}
				fileHandleList.configure(fileHandleClickedCallback)
					.setCanDelete(false)
					.setCanUpload(false);
				for (AttachmentMetadata metadata : submission.getAttachments()) {
					fileHandleList.addFileLink(metadata.getId(), metadata.getFileName());
				}
			}
		}
	}
	
	public boolean isPreconditionsMet() {
		if (userBundle.getVerificationSubmission() == null) {
			//new submission.  make sure orc id is set and profile is populated.
			if (!DisplayUtils.isDefined(userBundle.getORCID())) {
				view.showErrorMessage("ORC ID must be linked before requesting profile validation.");
				return false;
			}
			UserProfile profile = userBundle.getUserProfile();
			if (!DisplayUtils.isDefined(profile.getFirstName()) || !DisplayUtils.isDefined(profile.getLastName())) {
				view.showErrorMessage("First and last name must be filled in before requesting profile validation.");
				return false;
			}
			if (!DisplayUtils.isDefined(profile.getCompany())) {
				view.showErrorMessage("Affiliation must be filled in before requesting profile validation.");
				return false;
			}
			if (!DisplayUtils.isDefined(profile.getLocation())) {
				view.showErrorMessage("City,Country must be filled in before requesting profile validation.");
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
	}
	
	@Override
	public void approveVerification() {
		// TODO Auto-generated method stub
		view.showInfo("TODO","");
	}
	@Override
	public void rejectVerification() {
		// TODO Auto-generated method stub
		view.showInfo("TODO","");
	}
	@Override
	public void submitVerification() {
		// TODO Auto-generated method stub
		view.showInfo("TODO","");
	}
	@Override
	public void suspendVerification() {
		// TODO Auto-generated method stub
		view.showInfo("TODO","");
	}
}
