package org.sagebionetworks.web.client.widget.accessrequirements.createaccessrequirement;

import org.sagebionetworks.repo.model.ACTAccessRequirement;
import org.sagebionetworks.repo.model.AccessRequirement;
import org.sagebionetworks.repo.model.ObjectType;
import org.sagebionetworks.repo.model.file.FileHandleAssociateType;
import org.sagebionetworks.repo.model.file.FileHandleAssociation;
import org.sagebionetworks.repo.model.wiki.WikiPage;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.FileHandleWidget;
import org.sagebionetworks.web.client.widget.entity.WikiMarkdownEditor;
import org.sagebionetworks.web.client.widget.entity.WikiPageWidget;
import org.sagebionetworks.web.client.widget.table.modal.wizard.ModalPage;
import org.sagebionetworks.web.client.widget.upload.FileHandleUploadWidget;
import org.sagebionetworks.web.client.widget.upload.FileUpload;
import org.sagebionetworks.web.shared.WikiPageKey;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

/**
 * Second page of creating an access requirement (ACT)  
 * @author Jay
 *
 */
public class CreateACTAccessRequirementStep2 implements ModalPage, CreateACTAccessRequirementStep2View.Presenter {
	CreateACTAccessRequirementStep2View view;
	ModalPresenter modalPresenter;
	ACTAccessRequirement accessRequirement;
	SynapseClientAsync synapseClient;
	WikiMarkdownEditor wikiMarkdownEditor;
	WikiPageWidget wikiPageRenderer;
	WikiPageKey wikiKey;
	FileHandleUploadWidget ducTemplateUploader;
	FileHandleWidget ducTemplateFileHandleWidget;
	
	@Inject
	public CreateACTAccessRequirementStep2(
			CreateACTAccessRequirementStep2View view,
			SynapseClientAsync synapseClient,
			WikiMarkdownEditor wikiMarkdownEditor,
			WikiPageWidget wikiPageRenderer,
			FileHandleUploadWidget ducTemplateUploader,
			FileHandleWidget ducTemplateFileHandleWidget) {
		super();
		this.view = view;
		this.synapseClient = synapseClient;
		this.wikiMarkdownEditor = wikiMarkdownEditor;
		wikiMarkdownEditor.setDeleteButtonVisible(false);
		this.wikiPageRenderer = wikiPageRenderer;
		this.ducTemplateUploader = ducTemplateUploader;
		this.ducTemplateFileHandleWidget = ducTemplateFileHandleWidget;
		ducTemplateUploader.configure("Upload Template DUC...", new CallbackP<FileUpload>() {
			@Override
			public void invoke(FileUpload fileUpload) {
				setDUCFileHandle(fileUpload.getFileMeta().getFileName(), fileUpload.getFileHandleId());
			}
		});
		view.setWikiPageRenderer(wikiPageRenderer.asWidget());
		view.setDUCTemplateUploadWidget(ducTemplateUploader);
		view.setDUCTemplateWidget(ducTemplateFileHandleWidget);
		view.setPresenter(this);
		wikiPageRenderer.setModifiedCreatedByHistoryVisible(false);
	}
	
	public void setDUCFileHandle(String fileName, String fileHandleId) {
		accessRequirement.setDucTemplateFileHandleId(fileHandleId);
		ducTemplateFileHandleWidget.configure(fileName, fileHandleId);
		ducTemplateFileHandleWidget.setVisible(true);
	}
	
	/**
	 * Configure this widget before use.
	 * 
	 */
	public void configure(ACTAccessRequirement accessRequirement) {
		ducTemplateFileHandleWidget.setVisible(false);
		this.accessRequirement = accessRequirement;
		wikiKey = new WikiPageKey(accessRequirement.getId().toString(), ObjectType.ACCESS_REQUIREMENT.toString(), null);
		boolean isExistOldTermsOfUse = accessRequirement.getActContactInfo() != null;
		view.setOldTermsVisible(isExistOldTermsOfUse);
		view.setOldTerms(isExistOldTermsOfUse ? accessRequirement.getActContactInfo() : "");
		if (accessRequirement.getDucTemplateFileHandleId() != null) {
			FileHandleAssociation fha = new FileHandleAssociation();
			fha.setAssociateObjectType(FileHandleAssociateType.AccessRequirementAttachment);
			fha.setAssociateObjectId(accessRequirement.getId().toString());
			fha.setFileHandleId(accessRequirement.getDucTemplateFileHandleId());
			ducTemplateFileHandleWidget.configure(fha);
			ducTemplateFileHandleWidget.setVisible(true);
		}
		
		configureWiki();
		
		view.setAreOtherAttachmentsRequired(accessRequirement.getAreOtherAttachmentsRequired());
		view.setIsAnnualReviewRequired(accessRequirement.getIsAnnualReviewRequired());
		view.setIsCertifiedUserRequired(accessRequirement.getIsCertifiedUserRequired());
		view.setIsDUCRequired(accessRequirement.getIsDUCRequired());
		view.setIsIDUPublic(accessRequirement.getIsIDUPublic());
		view.setIsIRBApprovalRequired(accessRequirement.getIsIRBApprovalRequired());
		view.setIsValidatedProfileRequired(accessRequirement.getIsValidatedProfileRequired());
	}
	
	@Override
	public void onEditWiki() {
		wikiMarkdownEditor.configure(wikiKey, new CallbackP<WikiPage>() {
			@Override
			public void invoke(WikiPage wikiPage) {
				configureWiki();
			}
		});
	}
	
	private void configureWiki() {
		wikiPageRenderer.configure(wikiKey, false, null, false);
	}
	
	@Override
	public void onPrimary() {
		// update access requirement from view
		accessRequirement.setAreOtherAttachmentsRequired(view.areOtherAttachmentsRequired());
		accessRequirement.setIsAnnualReviewRequired(view.isAnnualReviewRequired());
		accessRequirement.setIsCertifiedUserRequired(view.isCertifiedUserRequired());
		accessRequirement.setIsDUCRequired(view.isDUCRequired());
		accessRequirement.setIsIDUPublic(view.isIDUPublic());
		accessRequirement.setIsIRBApprovalRequired(view.isIRBApprovalRequired());
		accessRequirement.setIsValidatedProfileRequired(view.isValidatedProfileRequired());
		
		// create/update access requirement
		modalPresenter.setLoading(true);
		synapseClient.createOrUpdateAccessRequirement(accessRequirement, new AsyncCallback<AccessRequirement>() {
			@Override
			public void onFailure(Throwable caught) {
				modalPresenter.setLoading(false);
				modalPresenter.setErrorMessage(caught.getMessage());
			}
			@Override
			public void onSuccess(AccessRequirement result) {
				modalPresenter.setLoading(false);
				modalPresenter.onFinished();
			}
		});
		
	}

	@Override
	public Widget asWidget() {
		return view.asWidget();
	}

	@Override
	public void setModalPresenter(ModalPresenter modalPresenter) {
		this.modalPresenter = modalPresenter;
		modalPresenter.setPrimaryButtonText(DisplayConstants.FINISH);
	}


}
