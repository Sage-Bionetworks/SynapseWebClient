package org.sagebionetworks.web.client.widget.clienthelp;

import static org.sagebionetworks.web.client.ServiceEntryPointUtils.fixServiceEntryPoint;

import org.sagebionetworks.repo.model.VersionInfo;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.shared.PaginatedResults;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class FileClientsHelp implements IsWidget {
	private FileClientsHelpView view;
	private SynapseClientAsync synapseClient;
	private SynapseJSNIUtils jsniUtils;
	
	@Inject
	public FileClientsHelp(
			FileClientsHelpView view,
			SynapseClientAsync synapseClient,
			SynapseJSNIUtils jsniUtils) {
		this.view = view;
		this.synapseClient = synapseClient;
		this.jsniUtils = jsniUtils;
		fixServiceEntryPoint(synapseClient);
	}
	
	public void configureAndShow(String entityId, Long version) {
		synapseClient.getEntityVersions(entityId, 0, 1, new AsyncCallback<PaginatedResults<VersionInfo>>() {
			@Override
			public void onSuccess(PaginatedResults<VersionInfo> result) {
				if (result.getResults().size() > 0) {
					VersionInfo versionInfo = result.getResults().get(0);
					view.setVersionVisible(!version.equals(versionInfo.getVersionNumber()));
				} else {
					view.setVersionVisible(false);
				}
				view.configureAndShow(entityId, version);
			}
			
			@Override
			public void onFailure(Throwable caught) {
				jsniUtils.consoleError(caught.getMessage());
				view.setVersionVisible(false);
			}
		});
	}
	
	@Override
	public Widget asWidget() {
		return view.asWidget();
	}
}
