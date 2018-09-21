package org.sagebionetworks.web.client.widget.entity.file.downloadlist;

import java.util.List;

import org.sagebionetworks.repo.model.asynch.AsynchronousResponseBody;
import org.sagebionetworks.repo.model.file.BulkFileDownloadRequest;
import org.sagebionetworks.repo.model.file.BulkFileDownloadResponse;
import org.sagebionetworks.repo.model.file.DownloadList;
import org.sagebionetworks.repo.model.file.DownloadOrder;
import org.sagebionetworks.repo.model.file.FileHandleAssociation;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.SynapseJavascriptClient;
import org.sagebionetworks.web.client.events.DownloadListUpdatedEvent;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.SynapseWidgetPresenter;
import org.sagebionetworks.web.client.widget.asynch.AsynchronousProgressHandler;
import org.sagebionetworks.web.client.widget.asynch.AsynchronousProgressWidget;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.shared.asynch.AsynchType;

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
	AsynchronousProgressWidget progressWidget;
	SynapseJSNIUtils jsniUtils;
	@Inject
	public DownloadListWidget(
			DownloadListWidgetView view, 
			SynapseAlert synAlert,
			SynapseJavascriptClient jsClient,
			EventBus eventBus,
			FileHandleAssociationTable fhaTable,
			PackageSizeSummary packageSizeSummary,
			AsynchronousProgressWidget progressWidget,
			SynapseJSNIUtils jsniUtils) {
		this.view = view;
		this.jsClient = jsClient;
		this.synAlert = synAlert;
		this.eventBus = eventBus;
		this.fhaTable = fhaTable;
		this.packageSizeSummary = packageSizeSummary;
		this.progressWidget = progressWidget;
		this.jsniUtils = jsniUtils;
		view.setSynAlert(synAlert);
		view.setFileHandleAssociationTable(fhaTable);
		view.setPackageSizeSummary(packageSizeSummary);
		view.setProgressTrackingWidgetVisible(false);
		view.setProgressTrackingWidget(progressWidget);
		view.setPresenter(this);
		
		addToPackageSizeCallback = fileSize -> {
			packageSizeSummary.addFile(fileSize);
		};
		onRemoveFileHandleAssociation = fha -> {
			onRemoveFileHandleAssociation(fha);
		};
	}
	
	public void refresh() {
		view.setCreatePackageUIVisible(true);
		view.setDownloadPackageUIVisible(false);
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
		packageSizeSummary.clear();
		fhaTable.configure(fhas, addToPackageSizeCallback, onRemoveFileHandleAssociation);
	}
	
	@Override
	public void onDownloadPackage(String zipFileName) {
		synAlert.clear();
		if (zipFileName.isEmpty()) {
			synAlert.showError("Please provide a package file name and try again.");
			return;
		}
		
		jsClient.createDownloadOrderFromUsersDownloadList(zipFileName + ".zip", new AsyncCallback<DownloadOrder>() {
			@Override
			public void onFailure(Throwable caught) {
				synAlert.handleException(caught);
			}
			public void onSuccess(DownloadOrder downloadOrder) {
				// and attempt to download!
				startDownload(downloadOrder);
			}; 
		});
	}
	
	public void startDownload(DownloadOrder order) {
		view.setProgressTrackingWidgetVisible(true);
		BulkFileDownloadRequest request = new BulkFileDownloadRequest();
		request.setRequestedFiles(order.getFiles());
		request.setZipFileName(order.getZipFileName());
		view.setCreatePackageUIVisible(false);
		progressWidget.startAndTrackJob("", true, AsynchType.BulkFileDownload, request, new AsynchronousProgressHandler() {
			@Override
			public void onFailure(Throwable failure) {
				view.setProgressTrackingWidgetVisible(false);
				synAlert.handleException(failure);
				view.setCreatePackageUIVisible(true);
			}
			
			@Override
			public void onComplete(AsynchronousResponseBody response) {
				view.setProgressTrackingWidgetVisible(false);
				BulkFileDownloadResponse bulkFileDownloadResponse = (BulkFileDownloadResponse) response;
				view.setPackageDownloadURL(jsniUtils.getRawFileHandleUrl(bulkFileDownloadResponse.getResultZipFileHandleId()));
				view.setDownloadPackageUIVisible(true);
			}
			
			@Override
			public void onCancel() {
				view.setCreatePackageUIVisible(true);
				view.setProgressTrackingWidgetVisible(false);
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
