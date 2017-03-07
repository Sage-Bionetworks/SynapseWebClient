package org.sagebionetworks.web.client.widget.accessrequirements.requestaccess;

import java.util.ArrayList;
import java.util.List;

import org.sagebionetworks.repo.model.ACTAccessRequirement;
import org.sagebionetworks.repo.model.dataaccess.DataAccessRenewal;
import org.sagebionetworks.repo.model.dataaccess.DataAccessRequest;
import org.sagebionetworks.repo.model.dataaccess.DataAccessRequestInterface;
import org.sagebionetworks.repo.model.dataaccess.ResearchProject;
import org.sagebionetworks.repo.model.file.FileHandleAssociateType;
import org.sagebionetworks.repo.model.file.FileHandleAssociation;
import org.sagebionetworks.web.client.DataAccessClientAsync;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.FileHandleWidget;
import org.sagebionetworks.web.client.widget.entity.act.UserBadgeItem;
import org.sagebionetworks.web.client.widget.entity.act.UserBadgeList;
import org.sagebionetworks.web.client.widget.table.modal.wizard.ModalPage;
import org.sagebionetworks.web.client.widget.table.modal.wizard.ModalWizardWidget;
import org.sagebionetworks.web.client.widget.upload.FileHandleUploadWidget;
import org.sagebionetworks.web.client.widget.upload.FileUpload;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

/**
 * Second page of data access wizard.  
 * @author Jay
 *
 */
public class CreateDataAccessSubmissionStep2 implements ModalPage {
	CreateDataAccessSubmissionWizardStep2View view;
	PortalGinInjector ginInjector;
	DataAccessClientAsync client;
	ACTAccessRequirement ar;
	ModalPresenter modalPresenter;
	ResearchProject researchProject;
	FileHandleWidget templateFileRenderer;
	FileHandleUploadWidget ducUploader, irbUploader, otherUploader;
	SynapseJSNIUtils jsniUtils;
	AuthenticationController authController;
	DataAccessRequestInterface dataAccessRequest;
	UserBadgeList accessorsList;
	
	@Inject
	public CreateDataAccessSubmissionStep2(
			final CreateDataAccessSubmissionWizardStep2View view,
			DataAccessClientAsync client,
			FileHandleWidget templateFileRenderer,
			FileHandleUploadWidget ducUploader,
			FileHandleUploadWidget irbUploader,
			FileHandleUploadWidget otherDocumentUploader,
			SynapseJSNIUtils jsniUtils,
			AuthenticationController authController,
			PortalGinInjector ginInjector,
			UserBadgeList accessorsList) {
		super();
		this.view = view;
		this.client = client;
		this.templateFileRenderer = templateFileRenderer;
		this.ginInjector = ginInjector;
		this.jsniUtils = jsniUtils;
		this.authController = authController;
		this.accessorsList = accessorsList;
		view.setAccessorListWidget(accessorsList);
		view.setDUCTemplateFileWidget(templateFileRenderer.asWidget());
		view.setDUCUploadWidget(ducUploader.asWidget());
		view.setIRBUploadWidget(irbUploader.asWidget());
		accessorsList.setCanDelete(true);
		ducUploader.configure("Browse...", new CallbackP<FileUpload>() {
			@Override
			public void invoke(FileUpload fileUpload) {
				setDUCFileHandle(fileUpload.getFileMeta().getFileName(), fileUpload.getFileHandleId());
			}
		});
		
		irbUploader.configure("Browse...", new CallbackP<FileUpload>() {
			@Override
			public void invoke(FileUpload fileUpload) {
				setIRBFileHandle(fileUpload.getFileMeta().getFileName(), fileUpload.getFileHandleId());
			}
		});
		
		otherDocumentUploader.configure("Browse...", new CallbackP<FileUpload>() {
			@Override
			public void invoke(FileUpload fileUpload) {
				addOtherDocumentFileHandle(fileUpload.getFileMeta().getFileName(), fileUpload.getFileHandleId());
			}
		});
	}
	
	public void setDUCFileHandle(String fileName, String ducFileHandleId) {
		dataAccessRequest.setDucFileHandleId(ducFileHandleId);
		FileHandleWidget fileHandleWidget = ginInjector.getFileHandleWidget();
		fileHandleWidget.configure(fileName, ducFileHandleId);
		view.setDUCUploadedFileWidget(fileHandleWidget);
	}

	public void setIRBFileHandle(String fileName, String irbFileHandleId) {
		dataAccessRequest.setIrbFileHandleId(irbFileHandleId);
		FileHandleWidget fileHandleWidget = ginInjector.getFileHandleWidget();
		fileHandleWidget.configure(fileName, irbFileHandleId);
		view.setDUCUploadedFileWidget(fileHandleWidget);
	}
	
	public void addOtherDocumentFileHandle(String fileName, String fileHandleId) {
		if (dataAccessRequest.getAttachments() == null) {
			dataAccessRequest.setAttachments(new ArrayList<String>());
		}
		List<String> attachments = dataAccessRequest.getAttachments();
		attachments.add(fileHandleId);
		FileHandleWidget fileHandleWidget = ginInjector.getFileHandleWidget();
		fileHandleWidget.configure(fileName, fileHandleId);
		view.addOtherDocumentUploaded(fileHandleWidget);
	}
	
	/**
	 * Configure this widget before use.
	 */
	public void configure(ResearchProject researchProject, ACTAccessRequirement ar) {
		this.ar = ar;
		this.researchProject = researchProject;
		view.setIRBVisible(ar.getIsIRBApprovalRequired());
		view.setDUCVisible(ar.getIsDUCRequired());
		view.clearOtherDocumentsUploaded();
		view.setPublicationsVisible(false);
		view.setSummaryOfUseVisible(false);
		view.setOtherDocumentUploadVisible(ar.getAreOtherAttachmentsRequired());
		if (ar.getDucTemplateFileHandleId() != null) {
			FileHandleAssociation fha = new FileHandleAssociation();
			//TODO: set to new FileHandleAssociateType (Access Requirement)
			fha.setAssociateObjectType(FileHandleAssociateType.VerificationSubmission);
			fha.setAssociateObjectId(ar.getId().toString());
			fha.setFileHandleId(ar.getDucTemplateFileHandleId());
			templateFileRenderer.configure(fha);
		}

		// retrieve a suitable request object to start with
		client.getDataAccessRequest(ar.getId(), new AsyncCallback<DataAccessRequestInterface>() {
			@Override
			public void onFailure(Throwable caught) {
				modalPresenter.setErrorMessage(caught.getMessage());
			}
			
			@Override
			public void onSuccess(DataAccessRequestInterface dataAccessRequest) {
				CreateDataAccessSubmissionStep2.this.dataAccessRequest = dataAccessRequest;
				boolean isRenewal = dataAccessRequest instanceof DataAccessRenewal;
				view.setPublicationsVisible(isRenewal);
				view.setSummaryOfUseVisible(isRenewal);
				if (isRenewal) {
					view.setPublications(((DataAccessRenewal)dataAccessRequest).getPublication());
					view.setSummaryOfUse(((DataAccessRenewal)dataAccessRequest).getSummaryOfUse());
				}
				if (dataAccessRequest.getDucFileHandleId() != null) {
					FileHandleWidget fileHandleWidget = getFileHandleWidget(dataAccessRequest.getDucFileHandleId());
					view.setDUCUploadedFileWidget(fileHandleWidget);
				}
				if (dataAccessRequest.getIrbFileHandleId() != null) {
					FileHandleWidget fileHandleWidget = getFileHandleWidget(dataAccessRequest.getIrbFileHandleId());
					view.setIRBUploadedFileWidget(fileHandleWidget);
				}
				
				initAttachments();
				initAccessors();
			}
		});
	}
	
	public FileHandleWidget getFileHandleWidget(String fileHandleId) {
		FileHandleWidget fileHandleWidget = ginInjector.getFileHandleWidget();
		FileHandleAssociation fha = new FileHandleAssociation();
		//TODO: set to new FileHandleAssociateType (data access request)
		fha.setAssociateObjectType(FileHandleAssociateType.VerificationSubmission);
		fha.setAssociateObjectId(ar.getId().toString());
		fha.setFileHandleId(fileHandleId);
		fileHandleWidget.configure(fha);
		return fileHandleWidget;
	}
	
	
	public void initAttachments() {
		view.clearOtherDocumentsUploaded();
		for (String fileHandleId : dataAccessRequest.getAttachments()) {
			FileHandleWidget fileHandleWidget = getFileHandleWidget(fileHandleId);
			view.addOtherDocumentUploaded(fileHandleWidget);
		}
	}
	
	public void initAccessors() {
		accessorsList.clear();
		for (String userId : dataAccessRequest.getAccessors()) {
			accessorsList.addUserBadge(userId);
		}
	}
	
	private void updateDataAccessRequest(final boolean isSubmit) {
		modalPresenter.setLoading(true);
		dataAccessRequest.setAccessors(accessorsList.getUserIds());
		
		client.updateDataAccessRequest(dataAccessRequest, isSubmit, new AsyncCallback<Void>() {
			@Override
			public void onFailure(Throwable caught) {
				modalPresenter.setErrorMessage(caught.getMessage());
			}
			
			@Override
			public void onSuccess(Void result) {
				if (isSubmit) {
					view.showInfo("Your data access request has been successfully submitted for review.");
				} else {
					view.showInfo("Saved your progress...");
				}
			}
		});
	}

	@Override
	public void onPrimary() {
		// TODO: validate values from the view
		updateDataAccessRequest(true);
	}

	@Override
	public Widget asWidget() {
		return view.asWidget();
	}

	@Override
	public void setModalPresenter(ModalPresenter modalPresenter) {
		this.modalPresenter = modalPresenter;
		modalPresenter.setPrimaryButtonText(DisplayConstants.SUBMIT);
		((ModalWizardWidget)modalPresenter).configure(this);
		((ModalWizardWidget)modalPresenter).showModal(new ModalWizardWidget.WizardCallback() {
			
			@Override
			public void onFinished() {
			}
			
			@Override
			public void onCanceled() {
				//TODO: need to check to see if the user would like to discard changes.
				// if Discard recent changes, then do nothing.
				// if Save, then update the DataAccessRequest/DataAccessRenewal
				updateDataAccessRequest(false);
			}
		});
	}


}
