package org.sagebionetworks.web.client.widget.entity.file.downloadlist;

import static org.sagebionetworks.web.client.ServiceEntryPointUtils.fixServiceEntryPoint;

import java.util.List;

import org.sagebionetworks.repo.model.EntityBundle;
import org.sagebionetworks.repo.model.FileEntity;
import org.sagebionetworks.repo.model.VersionInfo;
import org.sagebionetworks.repo.model.file.DownloadList;
import org.sagebionetworks.repo.model.file.ExternalFileHandle;
import org.sagebionetworks.repo.model.file.ExternalObjectStoreFileHandle;
import org.sagebionetworks.repo.model.file.FileHandle;
import org.sagebionetworks.repo.model.file.FileHandleAssociation;
import org.sagebionetworks.repo.model.file.S3FileHandleInterface;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.SynapseJavascriptClient;
import org.sagebionetworks.web.client.SynapseProperties;
import org.sagebionetworks.web.client.events.DownloadListUpdatedEvent;
import org.sagebionetworks.web.client.events.EntityUpdatedHandler;
import org.sagebionetworks.web.client.widget.SynapseWidgetPresenter;
import org.sagebionetworks.web.client.widget.clienthelp.FileClientsHelp;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.shared.PaginatedResults;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class DownloadListWidget implements SynapseWidgetPresenter, DownloadListWidgetView.Presenter {
	
	private DownloadListWidgetView view;
	SynapseAlert synAlert;
	private SynapseJavascriptClient jsClient;
	EventBus eventBus;
	@Inject
	public DownloadListWidget(
			DownloadListWidgetView view, 
			SynapseAlert synAlert,
			SynapseJavascriptClient jsClient,
			EventBus eventBus) {
		this.view = view;
		this.jsClient = jsClient;
		this.synAlert = synAlert;
		this.eventBus = eventBus;
		view.setSynAlert(synAlert);
		view.setPresenter(this);
	}
	
	public void refresh() {
		view.clear();
		synAlert.clear();
		jsClient.getDownloadList(new AsyncCallback<DownloadList>() {
			@Override
			public void onFailure(Throwable caught) {
				synAlert.handleException(caught);
			}
			@Override
			public void onSuccess(DownloadList downloadList) {
				List<FileHandleAssociation> fhas = downloadList.getFilesToDownload();
				// get FileResults for all fhas?  and get access requirement status for all items?
			}
		});
	}
	
	@Override
	public void onClearDownloadList() {
		synAlert.clear();
		jsClient.clearDownloadList(new AsyncCallback<Void>() {
			@Override
			public void onFailure(Throwable caught) {
				synAlert.handleException(caught);
			}
			@Override
			public void onSuccess(Void result) {
				refresh();
				eventBus.fireEvent(new DownloadListUpdatedEvent());
			}
		});
	}
	@Override
	public Widget asWidget() {
		return view.asWidget();
	}
}
