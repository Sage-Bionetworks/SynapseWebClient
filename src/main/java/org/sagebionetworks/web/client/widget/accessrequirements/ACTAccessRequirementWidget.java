package org.sagebionetworks.web.client.widget.accessrequirements;

import org.sagebionetworks.repo.model.ACTAccessRequirement;
import org.sagebionetworks.repo.model.ObjectType;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.widget.accessrequirements.requestaccess.CreateDataAccessRequestWizard;
import org.sagebionetworks.web.client.widget.entity.WikiPageWidget;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.client.widget.table.modal.wizard.ModalWizardWidget.WizardCallback;
import org.sagebionetworks.web.shared.WikiPageKey;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class ACTAccessRequirementWidget implements ACTAccessRequirementWidgetView.Presenter, IsWidget {
	
	private ACTAccessRequirementWidgetView view;
	SynapseClientAsync synapseClient;
	SynapseAlert synAlert;
	WikiPageWidget wikiPageWidget;
	ACTAccessRequirement ar;
	PortalGinInjector ginInjector;
	CreateAccessRequirementButton createAccessRequirementButton;
	DeleteAccessRequirementButton deleteAccessRequirementButton;
	SubjectsWidget subjectsWidget;
	
	@Inject
	public ACTAccessRequirementWidget(ACTAccessRequirementWidgetView view, 
			SynapseClientAsync synapseClient,
			WikiPageWidget wikiPageWidget,
			SynapseAlert synAlert,
			PortalGinInjector ginInjector,
			SubjectsWidget subjectsWidget,
			CreateAccessRequirementButton createAccessRequirementButton,
			DeleteAccessRequirementButton deleteAccessRequirementButton) {
		this.view = view;
		this.synapseClient = synapseClient;
		this.synAlert = synAlert;
		this.wikiPageWidget = wikiPageWidget;
		this.ginInjector = ginInjector;
		this.subjectsWidget = subjectsWidget;
		this.createAccessRequirementButton = createAccessRequirementButton;
		this.deleteAccessRequirementButton = deleteAccessRequirementButton;
		wikiPageWidget.setModifiedCreatedByHistoryVisible(false);
		view.setPresenter(this);
		view.setWikiTermsWidget(wikiPageWidget.asWidget());
		view.setEditAccessRequirementWidget(createAccessRequirementButton);
		view.setDeleteAccessRequirementWidget(deleteAccessRequirementButton);
		view.setSubjectsWidget(subjectsWidget);
	}
	
	public void setRequirement(ACTAccessRequirement ar) {
		this.ar = ar;
		if (!DisplayUtils.isDefined(ar.getActContactInfo())) {
 			//get wiki terms
 			WikiPageKey wikiKey = new WikiPageKey(ar.getId().toString(), ObjectType.ACCESS_REQUIREMENT.toString(), null);
 			wikiPageWidget.configure(wikiKey, false, null, false);
 			view.showWikiTermsUI();
 		} else {
 			view.setTerms(ar.getActContactInfo());
 			view.showTermsUI();
 		}
		createAccessRequirementButton.configure(ar);
		deleteAccessRequirementButton.configure(ar);
		subjectsWidget.configure(ar.getSubjectIds());
		refreshApprovalState();
	}
	
	public void refreshApprovalState() {
		//TODO:  set up view based on DataAccessSubmission state
		view.resetState();
		// (for testing)
		view.showRequestAccessButton();		
	}
	
	@Override
	public void onCancelRequest() {
		//TODO: cancel DataAccessSubmission
	}
	
	@Override
	public void onRequestAccess() {
		//pop up DataAccessRequest dialog
		CreateDataAccessRequestWizard wizard = ginInjector.getCreateDataAccessRequestWizard();
		view.setDataAccessRequestWizard(wizard);
		wizard.configure(ar);
		wizard.showModal(new WizardCallback() {
			//In any case, the state may have changed, so refresh this AR
			
			@Override
			public void onFinished() {
				refreshApprovalState();
			}
			
			@Override
			public void onCanceled() {
				refreshApprovalState();
			}
		});
	}
	
	public void addStyleNames(String styleNames) {
		view.addStyleNames(styleNames);
	}
	
	@Override
	public Widget asWidget() {
		return view.asWidget();
	}
}
