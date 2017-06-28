package org.sagebionetworks.web.client.widget.accessrequirements;

import org.sagebionetworks.repo.model.ACTAccessRequirement;
import org.sagebionetworks.repo.model.AccessRequirement;
import org.sagebionetworks.repo.model.ManagedACTAccessRequirement;
import org.sagebionetworks.repo.model.TermsOfUseAccessRequirement;
import org.sagebionetworks.web.client.DataAccessClientAsync;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.view.DivView;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class AccessRequirementWidget implements IsWidget{
	PortalGinInjector ginInjector;
	DataAccessClientAsync dataAccessClient;
	DivView div;
	boolean isHideButtons = false;
	@Inject
	public AccessRequirementWidget(PortalGinInjector ginInjector, 
			DataAccessClientAsync dataAccessClient,
			DivView div) {
		this.ginInjector = ginInjector;
		this.dataAccessClient = dataAccessClient;
		this.div = div;
	}
	
	public void configure(String accessRequirementId) {
		dataAccessClient.getAccessRequirement(Long.parseLong(accessRequirementId), new AsyncCallback<AccessRequirement>() {
			@Override
			public void onFailure(Throwable caught) {
				SynapseAlert synAlert = ginInjector.getSynapseAlertWidget();
				synAlert.handleException(caught);
				div.clear();
				div.add(synAlert);
			}
			@Override
			public void onSuccess(AccessRequirement requirement) {
				configure(requirement);
			}
		});
	}
	public void hideButtons() {
		isHideButtons = true;
	}
	
	public void configure(AccessRequirement requirement) {
		div.clear();
		if (requirement instanceof ManagedACTAccessRequirement) {
			ManagedACTAccessRequirementWidget arWidget = ginInjector.getManagedACTAccessRequirementWidget();
			arWidget.setRequirement((ManagedACTAccessRequirement) requirement);
			if (isHideButtons) {
				arWidget.hideButtons();
			}
			div.add(arWidget);
		} else if (requirement instanceof ACTAccessRequirement) {
			ACTAccessRequirementWidget arWidget = ginInjector.getACTAccessRequirementWidget();
			arWidget.setRequirement((ACTAccessRequirement) requirement);
			if (isHideButtons) {
				arWidget.hideButtons();
			} 
			div.add(arWidget);
		} else if (requirement instanceof TermsOfUseAccessRequirement) {
			TermsOfUseAccessRequirementWidget arWidget = ginInjector.getTermsOfUseAccessRequirementWidget();
			arWidget.setRequirement((TermsOfUseAccessRequirement) requirement);
			div.add(arWidget);
		}
	}
	
	@Override
	public Widget asWidget() {
		return div.asWidget();
	}
}
