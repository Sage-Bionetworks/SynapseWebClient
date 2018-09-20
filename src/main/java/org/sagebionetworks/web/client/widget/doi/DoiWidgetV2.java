package org.sagebionetworks.web.client.widget.doi;

import static org.sagebionetworks.web.client.ServiceEntryPointUtils.fixServiceEntryPoint;

import org.sagebionetworks.repo.model.doi.v2.DoiAssociation;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class DoiWidgetV2 implements IsWidget {
	private DoiWidgetV2View view;
	GlobalApplicationState globalApplicationState;
	SynapseClientAsync synapseClient;

	@Inject
	public DoiWidgetV2(DoiWidgetV2View view,
					   GlobalApplicationState globalApplicationState,
					   SynapseClientAsync synapseClient,
					   SynapseAlert synAlert) {
		this.view = view;
		this.synapseClient = synapseClient;
		fixServiceEntryPoint(synapseClient);
		this.globalApplicationState = globalApplicationState;
		view.setSynAlert(synAlert);
	}
	
	public Widget asWidget() {
		return view.asWidget();
	}
	
	public void configure(DoiAssociation newDoi) {
		clear();
		if (newDoi != null && newDoi.getDoiUri() != null) {
			view.showDoiCreated(newDoi.getDoiUri());
		}
	}

	public void clear() {
		view.setVisible(false);
		view.clear();
	}
}
