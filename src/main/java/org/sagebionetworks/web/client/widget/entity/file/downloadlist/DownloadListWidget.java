package org.sagebionetworks.web.client.widget.entity.file.downloadlist;

import java.util.List;

import org.sagebionetworks.repo.model.file.DownloadList;
import org.sagebionetworks.repo.model.file.FileHandleAssociation;
import org.sagebionetworks.web.client.SynapseJavascriptClient;
import org.sagebionetworks.web.client.events.DownloadListUpdatedEvent;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.SynapseWidgetPresenter;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class DownloadListWidget implements IsWidget, SynapseWidgetPresenter, DownloadListWidgetView.Presenter {
	
	private DownloadListWidgetView view;
	SynapseAlert synAlert;
	private SynapseJavascriptClient jsClient;
	private FileHandleAssociationTable fhaTable;
	EventBus eventBus;
	private PackageSizeSummary packageSizeSummary;
	CallbackP<Double> addToPackageSizeCallback;
	CallbackP<FileHandleAssociation> onRemoveFileHandleAssociation;
	@Inject
	public DownloadListWidget(
			DownloadListWidgetView view, 
			SynapseAlert synAlert,
			SynapseJavascriptClient jsClient,
			EventBus eventBus,
			FileHandleAssociationTable fhaTable,
			PackageSizeSummary packageSizeSummary) {
		this.view = view;
		this.jsClient = jsClient;
		this.synAlert = synAlert;
		this.eventBus = eventBus;
		this.fhaTable = fhaTable;
		this.packageSizeSummary = packageSizeSummary;
		view.setSynAlert(synAlert);
		view.setFileHandleAssociationTable(fhaTable);
		view.setPackageSizeSummary(packageSizeSummary);
		view.setPresenter(this);
		
		addToPackageSizeCallback = fileSize -> {
			packageSizeSummary.addFile(fileSize);
		};
		onRemoveFileHandleAssociation = fha -> {
			onRemoveFileHandleAssociation(fha);
		};
	}
	
	public void refresh() {
		view.clear();
		synAlert.clear();
		packageSizeSummary.clear();
		jsClient.getDownloadList(new AsyncCallback<DownloadList>() {
			@Override
			public void onFailure(Throwable caught) {
				synAlert.handleException(caught);
			}
			@Override
			public void onSuccess(DownloadList downloadList) {
				setDownloadList(downloadList);
			}
		});
	}
	
	private void setDownloadList(DownloadList downloadList) {
		List<FileHandleAssociation> fhas = downloadList.getFilesToDownload();
		fhaTable.configure(fhas, addToPackageSizeCallback, onRemoveFileHandleAssociation);
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
	
	public void onRemoveFileHandleAssociation(FileHandleAssociation fha) {
		jsClient.removeFileFromDownloadList(fha, new AsyncCallback<DownloadList>() {
			@Override
			public void onFailure(Throwable caught) {
				synAlert.handleException(caught);
			}
			@Override
			public void onSuccess(DownloadList downloadList) {
				setDownloadList(downloadList);
				eventBus.fireEvent(new DownloadListUpdatedEvent());
			}
		});
	}
	@Override
	public Widget asWidget() {
		return view.asWidget();
	}
}
