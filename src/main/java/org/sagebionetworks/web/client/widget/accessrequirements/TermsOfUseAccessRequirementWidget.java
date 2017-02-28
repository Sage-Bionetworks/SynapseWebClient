package org.sagebionetworks.web.client.widget.accessrequirements;

import org.sagebionetworks.repo.model.AccessApproval;
import org.sagebionetworks.repo.model.ObjectType;
import org.sagebionetworks.repo.model.TermsOfUseAccessApproval;
import org.sagebionetworks.repo.model.TermsOfUseAccessRequirement;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.widget.entity.WikiPageWidget;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.shared.WikiPageKey;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class TermsOfUseAccessRequirementWidget implements TermsOfUseAccessRequirementWidgetView.Presenter, IsWidget {
	private TermsOfUseAccessRequirementWidgetView view;
	SynapseClientAsync synapseClient;
	SynapseAlert synAlert;
	WikiPageWidget wikiPageWidget;
	TermsOfUseAccessRequirement ar;
	AuthenticationController authController;
	@Inject
	public TermsOfUseAccessRequirementWidget(TermsOfUseAccessRequirementWidgetView view,
			AuthenticationController authController,
			SynapseClientAsync synapseClient,
			WikiPageWidget wikiPageWidget,
			SynapseAlert synAlert) {
		this.view = view;
		this.synapseClient = synapseClient;
		this.synAlert = synAlert;
		this.wikiPageWidget = wikiPageWidget;
		this.authController = authController;
		wikiPageWidget.setModifiedCreatedByVisible(false);
		wikiPageWidget.showWikiHistory(false);
		view.setPresenter(this);
		view.setWikiTermsWidget(wikiPageWidget.asWidget());
	}
	
	
	public void configure(TermsOfUseAccessRequirement ar) {
		//TODO:  set up view based on DataAccessSubmission state
		this.ar = ar;
		//if not approved
		view.showUnapprovedHeading();
		view.showSignTermsButton();
		if (!DisplayUtils.isDefined(ar.getTermsOfUse())) {
 			//get wiki terms
 			WikiPageKey wikiKey = new WikiPageKey(ar.getId().toString(), ObjectType.ACCESS_REQUIREMENT.toString(), null);
 			wikiPageWidget.configure(wikiKey, false, null, false);
 			view.showWikiTermsUI();
 		} else {
 			view.setTerms(ar.getTermsOfUse());
 			view.showTermsUI();
 		}
	}
	
	@Override
	public void onSignTerms() {
		// create the self-signed access approval, then update this object
		synAlert.clear();
		AsyncCallback<AccessApproval> callback = new AsyncCallback<AccessApproval>() {
			@Override
			public void onFailure(Throwable t) {
				synAlert.handleException(t);
			}
			@Override
			public void onSuccess(AccessApproval result) {
				
			}
		};
		TermsOfUseAccessApproval approval = new TermsOfUseAccessApproval();
		approval.setAccessorId(authController.getCurrentUserPrincipalId());
		approval.setRequirementId(ar.getId());
		synapseClient.createAccessApproval(approval, callback);
	}
	
	public void addStyleNames(String styleNames) {
		view.addStyleNames(styleNames);
	}
	
	@Override
	public Widget asWidget() {
		return view.asWidget();
	}
}
