package org.sagebionetworks.web.client.widget.clienthelp;

import java.util.List;
import org.sagebionetworks.repo.model.VersionInfo;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.SynapseJavascriptClient;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class FileClientsHelp implements IsWidget {
	private FileClientsHelpView view;
	private SynapseJavascriptClient jsClient;
	private SynapseJSNIUtils jsniUtils;

	@Inject
	public FileClientsHelp(FileClientsHelpView view, SynapseJavascriptClient jsClient, SynapseJSNIUtils jsniUtils) {
		this.view = view;
		this.jsClient = jsClient;
		this.jsniUtils = jsniUtils;
	}

	public void configureAndShow(String entityId, Long version) {
		jsClient.getEntityVersions(entityId, 0, 1, new AsyncCallback<List<VersionInfo>>() {
			@Override
			public void onSuccess(List<VersionInfo> results) {
				if (results.size() > 0) {
					VersionInfo versionInfo = results.get(0);
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
