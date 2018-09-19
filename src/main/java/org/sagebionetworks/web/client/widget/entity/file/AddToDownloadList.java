package org.sagebionetworks.web.client.widget.entity.file;

import org.sagebionetworks.repo.model.asynch.AsynchronousResponseBody;
import org.sagebionetworks.repo.model.file.AddFileToDownloadListRequest;
import org.sagebionetworks.repo.model.table.Query;
import org.sagebionetworks.web.client.PopupUtilsView;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.view.DivView;
import org.sagebionetworks.web.client.widget.asynch.AsynchronousProgressHandler;
import org.sagebionetworks.web.client.widget.asynch.AsynchronousProgressWidget;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.shared.asynch.AsynchType;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class AddToDownloadList implements IsWidget {
	
	DivView view;
	PopupUtilsView popupUtilsView;
	PortalGinInjector ginInjector;
	AddFileToDownloadListRequest request;
	
	@Inject
	public AddToDownloadList(DivView view, 
			PortalGinInjector ginInjector,
			PopupUtilsView popupUtilsView) {
		this.view = view;
		this.popupUtilsView = popupUtilsView;
		this.ginInjector = ginInjector;
	}
	
	public void addToDownloadList(Query query) {
		request = new AddFileToDownloadListRequest();
		request.setQuery(query);
		confirmAddToDownloadList("Add all files from this query result to the Download List?");
	}
	
	public void addToDownloadList(String folderId) {
		request = new AddFileToDownloadListRequest();
		request.setFolderId(folderId);
		confirmAddToDownloadList("Add all files in this folder to the Download List?");
	}
	
	public void confirmAddToDownloadList(String message) {
		popupUtilsView.showConfirmDialog("", message, () -> {
			addToDownloadList();
		});
	}
	
	private void addToDownloadList() {
		view.clear();
		AsynchronousProgressWidget progress = ginInjector.creatNewAsynchronousProgressWidget();
		view.add(progress);
		progress.startAndTrackJob("Adding files to Download List", false, AsynchType.AddFileToDownloadList, request, new AsynchronousProgressHandler() {
			
			@Override
			public void onFailure(Throwable failure) {
				view.clear();
				SynapseAlert synAlert = ginInjector.getSynapseAlertWidget();
				view.add(synAlert);
				synAlert.handleException(failure);
			}
			
			@Override
			public void onComplete(AsynchronousResponseBody response) {
				view.clear();
				popupUtilsView.showInfo("Successfully added files to the Download List.");
				//TODO: fire event to trigger UI element in header
			}
			
			@Override
			public void onCancel() {
				view.clear();
			}
		});
	}
	
	@Override
	public Widget asWidget() {
		return view.asWidget();
	}
    
}
