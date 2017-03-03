package org.sagebionetworks.web.client.widget.accessrequirements;

import org.sagebionetworks.repo.model.ACTAccessRequirement;
import org.sagebionetworks.repo.model.ObjectType;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.widget.entity.WikiPageWidget;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
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
	@Inject
	public ACTAccessRequirementWidget(ACTAccessRequirementWidgetView view, 
			SynapseClientAsync synapseClient,
			WikiPageWidget wikiPageWidget,
			SynapseAlert synAlert) {
		this.view = view;
		this.synapseClient = synapseClient;
		this.synAlert = synAlert;
		this.wikiPageWidget = wikiPageWidget;
		wikiPageWidget.setModifiedCreatedByVisible(false);
		wikiPageWidget.showWikiHistory(false);
		view.setPresenter(this);
		view.setWikiTermsWidget(wikiPageWidget.asWidget());
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
	}
	
	public void setState() {
		//TODO:  set up view based on DataAccessSubmission state
		view.resetState();
		
	}
	
	@Override
	public void onCancelRequest() {
		//TODO: cancel DataAccessSubmission
	}
	
	@Override
	public void onRequestAccess() {
		//TODO: pop up DataAccessSubmission dialog
	}
	
	@Override
	public void onUpdateRequest() {
		//TODO: pop up DataAccessSubmission dialog (with existing submission)
	}
	
	public void addStyleNames(String styleNames) {
		view.addStyleNames(styleNames);
	}
	
	@Override
	public Widget asWidget() {
		return view.asWidget();
	}
}
