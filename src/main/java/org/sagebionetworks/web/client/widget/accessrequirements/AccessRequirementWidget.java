package org.sagebionetworks.web.client.widget.accessrequirements;

import org.sagebionetworks.repo.model.ACTAccessRequirement;
import org.sagebionetworks.repo.model.AccessRequirement;
import org.sagebionetworks.repo.model.LockAccessRequirement;
import org.sagebionetworks.repo.model.ManagedACTAccessRequirement;
import org.sagebionetworks.repo.model.SelfSignAccessRequirement;
import org.sagebionetworks.repo.model.TermsOfUseAccessRequirement;
import org.sagebionetworks.web.client.DataAccessClientAsync;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.exceptions.IllegalArgumentException;
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
				handleException(caught);
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
	
	private void handleException(Throwable t) {
		SynapseAlert synAlert = ginInjector.getSynapseAlertWidget();
		synAlert.handleException(t);
		div.clear();
		div.add(synAlert);
	}
	
	public void configure(AccessRequirement requirement) {
		div.clear();
		if (requirement instanceof ManagedACTAccessRequirement) {
			ManagedACTAccessRequirementWidget w = ginInjector.getManagedACTAccessRequirementWidget();
			w.setRequirement((ManagedACTAccessRequirement) requirement);
			if (isHideButtons) {
				w.hideButtons();
			}
			div.add(w);
		} else if (requirement instanceof ACTAccessRequirement) {
			ACTAccessRequirementWidget w = ginInjector.getACTAccessRequirementWidget();
			w.setRequirement((ACTAccessRequirement) requirement);
			if (isHideButtons) {
				w.hideButtons();
			} 
			div.add(w);
		} else if (requirement instanceof TermsOfUseAccessRequirement) {
			TermsOfUseAccessRequirementWidget w = ginInjector.getTermsOfUseAccessRequirementWidget();
			w.setRequirement((TermsOfUseAccessRequirement) requirement);
			div.add(w);
		} else if (requirement instanceof SelfSignAccessRequirement) {
			SelfSignAccessRequirementWidget w = ginInjector.getSelfSignAccessRequirementWidget();
			w.setRequirement((SelfSignAccessRequirement) requirement);
			div.add(w);
		} else if (requirement instanceof LockAccessRequirement) {
			LockAccessRequirementWidget w = ginInjector.getLockAccessRequirementWidget();
			w.setRequirement((LockAccessRequirement)requirement);
			div.add(w);
		} else {
			handleException(new IllegalArgumentException("unsupported access requirement type: " + requirement.getClass().getName()));
		}
	}
	
	@Override
	public Widget asWidget() {
		return div.asWidget();
	}
}
