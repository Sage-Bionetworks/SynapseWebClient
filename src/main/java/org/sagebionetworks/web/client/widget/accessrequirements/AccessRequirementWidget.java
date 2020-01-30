package org.sagebionetworks.web.client.widget.accessrequirements;

import static org.sagebionetworks.web.client.ServiceEntryPointUtils.fixServiceEntryPoint;
import org.sagebionetworks.repo.model.ACTAccessRequirement;
import org.sagebionetworks.repo.model.AccessRequirement;
import org.sagebionetworks.repo.model.LockAccessRequirement;
import org.sagebionetworks.repo.model.ManagedACTAccessRequirement;
import org.sagebionetworks.repo.model.RestrictableObjectDescriptor;
import org.sagebionetworks.repo.model.SelfSignAccessRequirement;
import org.sagebionetworks.repo.model.TermsOfUseAccessRequirement;
import org.sagebionetworks.web.client.DataAccessClientAsync;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.exceptions.IllegalArgumentException;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.view.DivView;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class AccessRequirementWidget implements IsWidget {
	PortalGinInjector ginInjector;
	DataAccessClientAsync dataAccessClient;
	DivView div;
	boolean isHideButtons = false;

	@Inject
	public AccessRequirementWidget(PortalGinInjector ginInjector, DataAccessClientAsync dataAccessClient, DivView div) {
		this.ginInjector = ginInjector;
		this.dataAccessClient = dataAccessClient;
		fixServiceEntryPoint(dataAccessClient);
		this.div = div;
		div.addStyleName("border-bottom-1 margin-bottom-15");
	}

	public void configure(final String accessRequirementId, final RestrictableObjectDescriptor targetSubject) {
		dataAccessClient.getAccessRequirement(Long.parseLong(accessRequirementId), new AsyncCallback<AccessRequirement>() {
			@Override
			public void onFailure(Throwable caught) {
				handleException(caught);
			}

			@Override
			public void onSuccess(AccessRequirement requirement) {
				Callback refreshCallback = new Callback() {
					@Override
					public void invoke() {
						configure(accessRequirementId, targetSubject);
					}
				};
				configure(requirement, targetSubject, refreshCallback);
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

	public void configure(AccessRequirement requirement, RestrictableObjectDescriptor targetSubject, Callback refreshCallback) {
		div.clear();
		if (requirement instanceof ManagedACTAccessRequirement) {
			ManagedACTAccessRequirementWidget w = ginInjector.getManagedACTAccessRequirementWidget();
			w.setRequirement((ManagedACTAccessRequirement) requirement, refreshCallback);
			w.setTargetSubject(targetSubject);
			if (isHideButtons) {
				w.hideButtons();
			}
			div.add(w);
		} else if (requirement instanceof ACTAccessRequirement) {
			ACTAccessRequirementWidget w = ginInjector.getACTAccessRequirementWidget();
			w.setRequirement((ACTAccessRequirement) requirement, refreshCallback);
			if (isHideButtons) {
				w.hideButtons();
			}
			div.add(w);
		} else if (requirement instanceof TermsOfUseAccessRequirement) {
			TermsOfUseAccessRequirementWidget w = ginInjector.getTermsOfUseAccessRequirementWidget();
			w.setRequirement((TermsOfUseAccessRequirement) requirement, refreshCallback);
			div.add(w);
		} else if (requirement instanceof SelfSignAccessRequirement) {
			SelfSignAccessRequirementWidget w = ginInjector.getSelfSignAccessRequirementWidget();
			w.setRequirement((SelfSignAccessRequirement) requirement, refreshCallback);
			div.add(w);
		} else if (requirement instanceof LockAccessRequirement) {
			LockAccessRequirementWidget w = ginInjector.getLockAccessRequirementWidget();
			w.setRequirement((LockAccessRequirement) requirement, refreshCallback);
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
