package org.sagebionetworks.web.client.widget.entity.file;

import org.sagebionetworks.repo.model.asynch.AsynchronousResponseBody;
import org.sagebionetworks.repo.model.file.AddFileToDownloadListRequest;
import org.sagebionetworks.repo.model.table.Query;
import org.sagebionetworks.web.client.PopupUtilsView;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.events.DownloadListUpdatedEvent;
import org.sagebionetworks.web.client.view.DivView;
import org.sagebionetworks.web.client.widget.asynch.AsynchronousProgressHandler;
import org.sagebionetworks.web.client.widget.asynch.AsynchronousProgressWidget;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.shared.asynch.AsynchType;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class AddToDownloadList implements IsWidget {
	
	public static final String SUCCESS_ADDED_FILES_MESSAGE = "Successfully added files to the Download List.";
	public static final String ADD_FOLDER_FILES_CONFIRMATION_MESSAGE = "Add all files in this folder to the Download List?";
	public static final String ADD_QUERY_FILES_CONFIRMATION_MESSAGE = "Add all files from this query result to the Download List?";
	DivView view;
	PopupUtilsView popupUtilsView;
	PortalGinInjector ginInjector;
	AddFileToDownloadListRequest request;
	EventBus eventBus;
	@Inject
	public AddToDownloadList(DivView view, 
			PortalGinInjector ginInjector,
			PopupUtilsView popupUtilsView,
			EventBus eventBus) {
		this.view = view;
		this.popupUtilsView = popupUtilsView;
		this.ginInjector = ginInjector;
		this.eventBus = eventBus;
	}
	
	public void addToDownloadList(Query query) {
		request = new AddFileToDownloadListRequest();
		request.setQuery(query);
		confirmAddToDownloadList(ADD_QUERY_FILES_CONFIRMATION_MESSAGE);
	}
	
	public void addToDownloadList(String folderId) {
		request = new AddFileToDownloadListRequest();
		request.setFolderId(folderId);
		confirmAddToDownloadList(ADD_FOLDER_FILES_CONFIRMATION_MESSAGE);
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
				popupUtilsView.showInfo(SUCCESS_ADDED_FILES_MESSAGE);
				//fire event to trigger UI element in header!
				eventBus.fireEvent(new DownloadListUpdatedEvent());
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
