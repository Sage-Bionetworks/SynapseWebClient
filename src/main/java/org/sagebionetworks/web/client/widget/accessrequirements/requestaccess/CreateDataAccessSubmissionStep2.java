package org.sagebionetworks.web.client.widget.accessrequirements.requestaccess;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.sagebionetworks.repo.model.ACTAccessRequirement;
import org.sagebionetworks.repo.model.dataaccess.DataAccessRenewal;
import org.sagebionetworks.repo.model.dataaccess.DataAccessRequestInterface;
import org.sagebionetworks.repo.model.dataaccess.ResearchProject;
import org.sagebionetworks.repo.model.file.FileHandleAssociateType;
import org.sagebionetworks.repo.model.file.FileHandleAssociation;
import org.sagebionetworks.web.client.DataAccessClientAsync;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.ValidationUtils;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.FileHandleWidget;
import org.sagebionetworks.web.client.widget.entity.act.UserBadgeList;
import org.sagebionetworks.web.client.widget.search.SynapseSuggestBox;
import org.sagebionetworks.web.client.widget.search.SynapseSuggestion;
import org.sagebionetworks.web.client.widget.search.UserGroupSuggestionProvider;
import org.sagebionetworks.web.client.widget.table.modal.wizard.ModalPage;
import org.sagebionetworks.web.client.widget.table.modal.wizard.ModalWizardWidget;
import org.sagebionetworks.web.client.widget.upload.FileHandleList;
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
	public static final String SAVED_PROGRESS_MESSAGE = "Saved your progress...";
	public static final String SAVE_CHANGES_MESSAGE = "Would you want to save your recent changes?";
	public static final String SUCCESSFULLY_SUBMITTED_MESSAGE = "Your data access request has been successfully submitted for review.";
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
	private SynapseSuggestBox peopleSuggestWidget;
	FileHandleList otherDocuments;
	
	@Inject
	public CreateDataAccessSubmissionStep2(
			CreateDataAccessSubmissionWizardStep2View view,
			DataAccessClientAsync client,
			FileHandleWidget templateFileRenderer,
			FileHandleUploadWidget ducUploader,
			FileHandleUploadWidget irbUploader,
			SynapseJSNIUtils jsniUtils,
			AuthenticationController authController,
			PortalGinInjector ginInjector,
			UserBadgeList accessorsList,
			SynapseSuggestBox peopleSuggestBox,
			UserGroupSuggestionProvider provider,
			FileHandleList otherDocuments) {
		super();
		this.view = view;
		this.client = client;
		this.templateFileRenderer = templateFileRenderer;
		this.ginInjector = ginInjector;
		this.jsniUtils = jsniUtils;
		this.authController = authController;
		this.accessorsList = accessorsList;
		this.otherDocuments = otherDocuments;
		otherDocuments.configure()
			.setUploadButtonText("Browse...")
			.setCanDelete(true)
			.setCanUpload(true);
		view.setAccessorListWidget(accessorsList);
		view.setDUCTemplateFileWidget(templateFileRenderer.asWidget());
		view.setDUCUploadWidget(ducUploader.asWidget());
		view.setIRBUploadWidget(irbUploader.asWidget());
		view.setOtherDocumentUploaded(otherDocuments.asWidget());
		view.setPeopleSuggestWidget(peopleSuggestBox.asWidget());
		this.peopleSuggestWidget = peopleSuggestBox;
		peopleSuggestWidget.setSuggestionProvider(provider);
		peopleSuggestWidget.setPlaceholderText("Enter the user name of other accessors...");
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
		
		peopleSuggestWidget.addItemSelectedHandler(new CallbackP<SynapseSuggestion>() {
			public void invoke(SynapseSuggestion suggestion) {
				peopleSuggestWidget.clear();
				CreateDataAccessSubmissionStep2.this.accessorsList.addUserBadge(suggestion.getId());
			};
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
		view.setIRBUploadedFileWidget(fileHandleWidget);
	}
	
	public void addOtherDocumentFileHandle(String fileName, String fileHandleId) {
		if (dataAccessRequest.getAttachments() == null) {
			dataAccessRequest.setAttachments(new ArrayList<String>());
		}
		List<String> attachments = dataAccessRequest.getAttachments();
		attachments.add(fileHandleId);
		otherDocuments.addFileLink(fileName, fileHandleId);
	}
	
	/**
	 * Configure this widget before use.
	 */
	public void configure(ResearchProject researchProject, ACTAccessRequirement ar) {
		this.ar = ar;
		this.researchProject = researchProject;
		view.setIRBVisible(ValidationUtils.isTrue(ar.getIsIRBApprovalRequired()));
		view.setDUCVisible(ValidationUtils.isTrue(ar.getIsDUCRequired()));
		otherDocuments.clear();
		accessorsList.clear();
		view.setPublicationsVisible(false);
		view.setSummaryOfUseVisible(false);
		peopleSuggestWidget.clear();
		view.setOtherDocumentUploadVisible(ValidationUtils.isTrue(ar.getAreOtherAttachmentsRequired()));
		boolean isDucTemplate = ar.getDucTemplateFileHandleId() != null;
		view.setDUCTemplateVisible(isDucTemplate);
		if (isDucTemplate) {
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
					//TODO: set to new FileHandleAssociateType (data access request)
					FileHandleWidget fileHandleWidget = getFileHandleWidget(dataAccessRequest.getId(), FileHandleAssociateType.FileEntity, dataAccessRequest.getDucFileHandleId());
					view.setDUCUploadedFileWidget(fileHandleWidget);
				}
				if (dataAccessRequest.getIrbFileHandleId() != null) {
					//TODO: set to new FileHandleAssociateType (data access request)
					FileHandleWidget fileHandleWidget = getFileHandleWidget(dataAccessRequest.getId(), FileHandleAssociateType.FileEntity, dataAccessRequest.getIrbFileHandleId());
					view.setIRBUploadedFileWidget(fileHandleWidget);
				}
				
				initAttachments();
				initAccessors();
			}
		});
	}
	
	public FileHandleWidget getFileHandleWidget(String id, FileHandleAssociateType type, String fileHandleId) {
		FileHandleWidget fileHandleWidget = ginInjector.getFileHandleWidget();
		FileHandleAssociation fha = new FileHandleAssociation();
		fha.setAssociateObjectType(type);
		fha.setAssociateObjectId(id);
		fha.setFileHandleId(fileHandleId);
		fileHandleWidget.configure(fha);
		return fileHandleWidget;
	}
	
	public void initAttachments() {
		if (dataAccessRequest.getAttachments() != null) {
			for (String fileHandleId : dataAccessRequest.getAttachments()) {
				FileHandleAssociation fha = new FileHandleAssociation();
				//TODO: set to new FileHandleAssociateType (data access request)
				fha.setAssociateObjectType(FileHandleAssociateType.MessageAttachment);
				fha.setAssociateObjectId(dataAccessRequest.getId());
				fha.setFileHandleId(fileHandleId);
				otherDocuments.addFileLink(fha);
			}
		}
	}
	
	public void initAccessors() {
		Set<String> uniqueAccessors = new HashSet<String>();
		uniqueAccessors.add(authController.getCurrentUserPrincipalId());
		if (dataAccessRequest.getAccessors() != null) {
			uniqueAccessors.addAll(dataAccessRequest.getAccessors());
		}
		for (String userId : uniqueAccessors) {
			accessorsList.addUserBadge(userId);
		}
	}
	
	private void updateDataAccessRequest(final boolean isSubmit) {
		modalPresenter.setLoading(true);
		dataAccessRequest.setAccessors(accessorsList.getUserIds());
		dataAccessRequest.setAttachments(otherDocuments.getFileHandleIds());
		client.updateDataAccessRequest(dataAccessRequest, isSubmit, new AsyncCallback<Void>() {
			@Override
			public void onFailure(Throwable caught) {
				if (isSubmit) {
					modalPresenter.setErrorMessage(caught.getMessage());
				} else {
					view.showInfo(caught.getMessage());
				}
			}
			
			@Override
			public void onSuccess(Void result) {
				if (isSubmit) {
					view.showInfo(SUCCESSFULLY_SUBMITTED_MESSAGE);
				} else {
					view.showInfo(SAVED_PROGRESS_MESSAGE);
				}
				modalPresenter.setLoading(false);
				modalPresenter.onFinished();
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
		((ModalWizardWidget)modalPresenter).addCallback(new ModalWizardWidget.WizardCallback() {
			
			@Override
			public void onFinished() {
			}
			
			@Override
			public void onCanceled() {
				// check to see if the user would like to discard changes.
				// if saving, then update the DataAccessRequest/DataAccessRenewal (but do not submit)
				view.showConfirmDialog("Save?",SAVE_CHANGES_MESSAGE, new Callback() {
					@Override
					public void invoke() {
						updateDataAccessRequest(false);
					}
				});
			}
		});
	}
}
