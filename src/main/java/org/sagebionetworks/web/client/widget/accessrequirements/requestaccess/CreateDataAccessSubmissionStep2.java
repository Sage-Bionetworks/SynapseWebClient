package org.sagebionetworks.web.client.widget.accessrequirements.requestaccess;

import java.util.ArrayList;
import java.util.List;

import org.sagebionetworks.repo.model.ManagedACTAccessRequirement;
import org.sagebionetworks.repo.model.dataaccess.AccessType;
import org.sagebionetworks.repo.model.dataaccess.AccessorChange;
import org.sagebionetworks.repo.model.dataaccess.Renewal;
import org.sagebionetworks.repo.model.dataaccess.RequestInterface;
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
	public static final String SAVED_PROGRESS_MESSAGE = "Saved your progress.";
	public static final String SAVE_CHANGES_MESSAGE = "Would you want to save your recent changes?";
	public static final String SUCCESSFULLY_SUBMITTED_MESSAGE = "Your data access request has been successfully submitted for review.";
	CreateDataAccessSubmissionWizardStep2View view;
	PortalGinInjector ginInjector;
	DataAccessClientAsync client;
	ManagedACTAccessRequirement ar;
	ModalPresenter modalPresenter;
	ResearchProject researchProject;
	FileHandleWidget templateFileRenderer;
	FileHandleUploadWidget ducUploader, irbUploader, otherUploader;
	SynapseJSNIUtils jsniUtils;
	AuthenticationController authController;
	RequestInterface dataAccessRequest;
	UserBadgeList accessorChangesList;
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
		this.accessorChangesList = accessorsList;
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
				AccessorChange change = new AccessorChange();
				change.setUserId(suggestion.getId());
				change.setType(AccessType.GAIN_ACCESS);
				CreateDataAccessSubmissionStep2.this.accessorChangesList.addAccessorChange(change);
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
	public void configure(ResearchProject researchProject, ManagedACTAccessRequirement ar) {
		this.ar = ar;
		this.researchProject = researchProject;
		view.setIRBVisible(ValidationUtils.isTrue(ar.getIsIRBApprovalRequired()));
		view.setDUCVisible(ValidationUtils.isTrue(ar.getIsDUCRequired()));
		otherDocuments.clear();
		accessorChangesList.clear();
		view.setPublicationsVisible(false);
		view.setSummaryOfUseVisible(false);
		peopleSuggestWidget.clear();
		view.setValidatedUserProfileNoteVisible(ar.getIsValidatedProfileRequired());
		view.setOtherDocumentUploadVisible(ValidationUtils.isTrue(ar.getAreOtherAttachmentsRequired()));
		boolean isDucTemplate = ar.getDucTemplateFileHandleId() != null;
		view.setDUCTemplateVisible(isDucTemplate);
		if (isDucTemplate) {
			FileHandleAssociation fha = new FileHandleAssociation();
			fha.setAssociateObjectType(FileHandleAssociateType.AccessRequirementAttachment);
			fha.setAssociateObjectId(ar.getId().toString());
			fha.setFileHandleId(ar.getDucTemplateFileHandleId());
			templateFileRenderer.configure(fha);
		}

		// retrieve a suitable request object to start with
		client.getDataAccessRequest(ar.getId(), new AsyncCallback<RequestInterface>() {
			@Override
			public void onFailure(Throwable caught) {
				modalPresenter.setErrorMessage(caught.getMessage());
			}
			
			@Override
			public void onSuccess(RequestInterface dataAccessRequest) {
				CreateDataAccessSubmissionStep2.this.dataAccessRequest = dataAccessRequest;
				boolean isRenewal = dataAccessRequest instanceof Renewal;
				view.setPublicationsVisible(isRenewal);
				view.setSummaryOfUseVisible(isRenewal);
				if (isRenewal) {
					view.setPublications(((Renewal)dataAccessRequest).getPublication());
					view.setSummaryOfUse(((Renewal)dataAccessRequest).getSummaryOfUse());
				}
				if (dataAccessRequest.getDucFileHandleId() != null) {
					FileHandleWidget fileHandleWidget = getFileHandleWidget(dataAccessRequest.getId(), FileHandleAssociateType.DataAccessRequestAttachment, dataAccessRequest.getDucFileHandleId());
					view.setDUCUploadedFileWidget(fileHandleWidget);
				}
				if (dataAccessRequest.getIrbFileHandleId() != null) {
					FileHandleWidget fileHandleWidget = getFileHandleWidget(dataAccessRequest.getId(), FileHandleAssociateType.DataAccessRequestAttachment, dataAccessRequest.getIrbFileHandleId());
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
				fha.setAssociateObjectType(FileHandleAssociateType.DataAccessRequestAttachment);
				fha.setAssociateObjectId(dataAccessRequest.getId());
				fha.setFileHandleId(fileHandleId);
				otherDocuments.addFileLink(fha);
			}
		}
	}
	
	public void initAccessors() {
		boolean submitterFound = false;
		if (dataAccessRequest.getAccessorChanges() != null) {
			for (AccessorChange change : dataAccessRequest.getAccessorChanges()) {
				if (change.getUserId().equals(authController.getCurrentUserPrincipalId())) {
					submitterFound = true;
					accessorChangesList.addSubmitterAccessorChange(change);	
				} else {
					accessorChangesList.addAccessorChange(change);	
				}
			}
		}
		if (!submitterFound) {
			AccessorChange submitterChange = new AccessorChange();
			submitterChange.setUserId(authController.getCurrentUserPrincipalId());
			submitterChange.setType(AccessType.GAIN_ACCESS);
			accessorChangesList.addSubmitterAccessorChange(submitterChange);
		}
	}
	
	public void updateDataAccessRequest(final boolean isSubmit) {
		modalPresenter.setLoading(true);
		dataAccessRequest.setAccessorChanges(accessorChangesList.getAccessorChanges());
		dataAccessRequest.setAttachments(otherDocuments.getFileHandleIds());
		dataAccessRequest.setResearchProjectId(researchProject.getId());

		boolean isRenewal = dataAccessRequest instanceof Renewal;
		if (isRenewal) {
			((Renewal)dataAccessRequest).setPublication(view.getPublications());
			((Renewal)dataAccessRequest).setSummaryOfUse(view.getSummaryOfUse());
		}

		client.updateDataAccessRequest(dataAccessRequest, new AsyncCallback<RequestInterface>() {
			@Override
			public void onFailure(Throwable caught) {
				modalPresenter.setErrorMessage(caught.getMessage());
			}
			
			@Override
			public void onSuccess(RequestInterface result) {
				dataAccessRequest = result;
				if (isSubmit) {
					submitDataAccessRequest();
				} else {
					view.showInfo(SAVED_PROGRESS_MESSAGE);
					modalPresenter.setLoading(false);
					modalPresenter.onFinished();
				}
			}
		});
	}

	public void submitDataAccessRequest() {
		client.submitDataAccessRequest(dataAccessRequest, new AsyncCallback<Void>() {
			@Override
			public void onFailure(Throwable caught) {
				modalPresenter.setErrorMessage(caught.getMessage());
			}
			
			@Override
			public void onSuccess(Void result) {
				view.showInfo(SUCCESSFULLY_SUBMITTED_MESSAGE);
				modalPresenter.setLoading(false);
				modalPresenter.onFinished();
			}
		});
	}
	@Override
	public void onPrimary() {
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
