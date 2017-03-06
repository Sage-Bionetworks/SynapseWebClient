package org.sagebionetworks.web.client.widget.accessrequirements.requestaccess;

import org.sagebionetworks.repo.model.ACTAccessRequirement;
import org.sagebionetworks.repo.model.dataaccess.ResearchProject;
import org.sagebionetworks.repo.model.file.FileHandleAssociateType;
import org.sagebionetworks.web.client.DataAccessClientAsync;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.table.modal.wizard.ModalPage;
import org.sagebionetworks.web.client.widget.table.modal.wizard.ModalWizardWidget;
import org.sagebionetworks.web.client.widget.table.v2.results.cell.FileCellRendererImpl;
import org.sagebionetworks.web.client.widget.upload.FileHandleList;
import org.sagebionetworks.web.client.widget.upload.FileHandleUploadWidget;
import org.sagebionetworks.web.client.widget.upload.FileUpload;

import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

/**
 * Second page of data access wizard.  
 * @author Jay
 *
 */
public class CreateDataAccessSubmissionStep2 implements ModalPage {
	CreateDataAccessSubmissionWizardStep2View view;
	DataAccessClientAsync client;
	ACTAccessRequirement ar;
	ModalPresenter modalPresenter;
	ResearchProject researchProject;
	FileCellRendererImpl templateFileRenderer;
	FileHandleUploadWidget ducUploader, irbUploader, otherUploader;
	FileHandleList otherFileHandleList;
	String ducFileHandleId, irbFileHandleId;
	SynapseJSNIUtils jsniUtils;
	AuthenticationController authController;
	CallbackP<String> fileHandleClickedCallback;
	CallbackP<String> rawFileHandleClickedCallback;
	
	@Inject
	public CreateDataAccessSubmissionStep2(
			CreateDataAccessSubmissionWizardStep2View view,
			DataAccessClientAsync client,
			FileCellRendererImpl templateFileRenderer,
			final FileCellRendererImpl ducFileRenderer,
			FileHandleUploadWidget ducUploader,
			final FileCellRendererImpl irbFileRenderer,
			FileHandleUploadWidget irbUploader,
			FileHandleList otherFileHandleList,
			SynapseJSNIUtils jsniUtils,
			AuthenticationController authController) {
		super();
		this.view = view;
		this.client = client;
		this.templateFileRenderer = templateFileRenderer;
		this.otherFileHandleList = otherFileHandleList;
		this.jsniUtils = jsniUtils;
		this.authController = authController;
		view.setDUCTemplateFileWidget(templateFileRenderer.asWidget());
		view.setDUCUploadedFileWidget(ducFileRenderer.asWidget());
		view.setDUCUploadWidget(ducUploader.asWidget());
		view.setIRBUploadedFileWidget(irbFileRenderer.asWidget());
		view.setIRBUploadWidget(irbUploader.asWidget());
		view.setOtherDocumentUploadWidget(otherFileHandleList.asWidget());
		
		ducUploader.configure("Browse...", new CallbackP<FileUpload>() {
			@Override
			public void invoke(FileUpload fileUpload) {
				ducFileHandleId = fileUpload.getFileHandleId();
				ducFileRenderer.setValue(ducFileHandleId);
			}
		});
		
		irbUploader.configure("Browse...", new CallbackP<FileUpload>() {
			@Override
			public void invoke(FileUpload fileUpload) {
				irbFileHandleId = fileUpload.getFileHandleId();
				irbFileRenderer.setValue(irbFileHandleId);
			}
		});
		
		fileHandleClickedCallback = new CallbackP<String>(){
			@Override
			public void invoke(String fileHandleId) {
				getDataAccessRequestFileHandleUrlAndOpen(fileHandleId);
			}
		};
		
		rawFileHandleClickedCallback = new CallbackP<String>(){
			@Override
			public void invoke(String fileHandleId) {
				getRawFileHandleUrlAndOpen(fileHandleId);
			}
		};
	}
	
	public void getDataAccessRequestFileHandleUrlAndOpen(String fileHandleId) {
		String xsrfToken = authController.getCurrentXsrfToken();
		String url = jsniUtils.getFileHandleAssociationUrl(submission.getId(), FileHandleAssociateType.VerificationSubmission, fileHandleId, xsrfToken);
		view.openWindow(url);
	}
	
	public void getRawFileHandleUrlAndOpen(String fileHandleId) {
		String url = jsniUtils.getBaseFileHandleUrl() + "?rawFileHandleId=" + fileHandleId;
		view.openWindow(url);
	}

	
	
	/**
	 * Configure this widget before use.
	 */
	public void configure(ResearchProject researchProject, ACTAccessRequirement ar) {
		this.ar = ar;
		this.researchProject = researchProject;
		// TODO: retrieve a suitable request object to start with, /accessRequirement/{id}/dataAccessRequestForUpdate
	}
	
	private void createDataAccessSubmission() {
		modalPresenter.setLoading(true);
		//TODO: create data access submission
	}

	@Override
	public void onPrimary() {
		// TODO: validate values from the view
		createDataAccessSubmission();
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
				// need to check to see if the user would like to discard changes.
				// if Discard recent changes, then do nothing.
				// if Save, then update the DataAccessRequest/DataAccessRenewal
			}
		});
	}


}
