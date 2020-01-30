package org.sagebionetworks.web.client.widget.accessrequirements.createaccessrequirement;

import static org.sagebionetworks.web.client.ServiceEntryPointUtils.fixServiceEntryPoint;
import org.sagebionetworks.repo.model.AccessRequirement;
import org.sagebionetworks.repo.model.ManagedACTAccessRequirement;
import org.sagebionetworks.repo.model.ObjectType;
import org.sagebionetworks.repo.model.file.FileHandleAssociateType;
import org.sagebionetworks.repo.model.file.FileHandleAssociation;
import org.sagebionetworks.repo.model.wiki.WikiPage;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
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
 * 
 * @author Jay
 *
 */
public class CreateManagedACTAccessRequirementStep2 implements ModalPage, CreateManagedACTAccessRequirementStep2View.Presenter {
	CreateManagedACTAccessRequirementStep2View view;
	ModalPresenter modalPresenter;
	ManagedACTAccessRequirement accessRequirement;
	SynapseClientAsync synapseClient;
	WikiMarkdownEditor wikiMarkdownEditor;
	WikiPageWidget wikiPageRenderer;
	WikiPageKey wikiKey;
	FileHandleUploadWidget ducTemplateUploader;
	FileHandleWidget ducTemplateFileHandleWidget;
	public static final int DAY_IN_MS = 1000 * 60 * 60 * 24;

	@Inject
	public CreateManagedACTAccessRequirementStep2(CreateManagedACTAccessRequirementStep2View view, SynapseClientAsync synapseClient, WikiMarkdownEditor wikiMarkdownEditor, WikiPageWidget wikiPageRenderer, FileHandleUploadWidget ducTemplateUploader, FileHandleWidget ducTemplateFileHandleWidget) {
		super();
		this.view = view;
		this.synapseClient = synapseClient;
		fixServiceEntryPoint(synapseClient);
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
	public void configure(ManagedACTAccessRequirement accessRequirement) {
		ducTemplateFileHandleWidget.setVisible(false);
		this.accessRequirement = accessRequirement;
		wikiKey = new WikiPageKey(accessRequirement.getId().toString(), ObjectType.ACCESS_REQUIREMENT.toString(), null);
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
		if (accessRequirement.getExpirationPeriod() != null) {
			view.setExpirationPeriod(Long.toString(accessRequirement.getExpirationPeriod() / DAY_IN_MS));
		} else {
			view.setExpirationPeriod("");
		}
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
		wikiPageRenderer.configure(wikiKey, false, null);
	}

	@Override
	public void onPrimary() {
		// update access requirement from view
		modalPresenter.setLoading(true);
		accessRequirement.setAreOtherAttachmentsRequired(view.areOtherAttachmentsRequired());
		String expirationPeriod = view.getExpirationPeriod();
		if (DisplayUtils.isDefined(expirationPeriod)) {
			try {
				long expirationPeriodInDays = Long.parseLong(expirationPeriod);
				if (expirationPeriodInDays < 0) {
					throw new NumberFormatException("Must be a positive integer.");
				}
				accessRequirement.setExpirationPeriod(expirationPeriodInDays * DAY_IN_MS);
			} catch (NumberFormatException e) {
				modalPresenter.setErrorMessage("Please enter a valid expiration period (in days): " + e.getMessage());
				return;
			}
		} else {
			accessRequirement.setExpirationPeriod(null);
		}

		accessRequirement.setIsCertifiedUserRequired(view.isCertifiedUserRequired());
		accessRequirement.setIsDUCRequired(view.isDUCRequired());
		accessRequirement.setIsIDUPublic(view.isIDUPublic());
		accessRequirement.setIsIRBApprovalRequired(view.isIRBApprovalRequired());
		accessRequirement.setIsValidatedProfileRequired(view.isValidatedProfileRequired());
		// create/update access requirement
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
