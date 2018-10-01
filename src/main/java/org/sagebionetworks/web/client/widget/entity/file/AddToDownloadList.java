package org.sagebionetworks.web.client.widget.entity.file;

import java.util.ArrayList;
import java.util.List;

import org.sagebionetworks.repo.model.EntityChildrenRequest;
import org.sagebionetworks.repo.model.EntityChildrenResponse;
import org.sagebionetworks.repo.model.EntityType;
import org.sagebionetworks.repo.model.asynch.AsynchronousResponseBody;
import org.sagebionetworks.repo.model.file.AddFileToDownloadListRequest;
import org.sagebionetworks.repo.model.table.Query;
import org.sagebionetworks.web.client.PopupUtilsView;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.SynapseJavascriptClient;
import org.sagebionetworks.web.client.events.DownloadListUpdatedEvent;
import org.sagebionetworks.web.client.widget.asynch.AsynchronousProgressHandler;
import org.sagebionetworks.web.client.widget.asynch.AsynchronousProgressWidget;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.client.widget.entity.file.downloadlist.PackageSizeSummary;
import org.sagebionetworks.web.shared.asynch.AsynchType;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class AddToDownloadList implements IsWidget, AddToDownloadListView.Presenter {
	
	public static final String SUCCESS_ADDED_FILES_MESSAGE = "Successfully added files to the Download List.";
	public static final String ADD_QUERY_FILES_CONFIRMATION_MESSAGE = "Add all files from this query result to the Download List?";
	AddToDownloadListView view;
	PopupUtilsView popupUtilsView;
	PortalGinInjector ginInjector;
	AddFileToDownloadListRequest request;
	EventBus eventBus;
	SynapseAlert synAlert;
	PackageSizeSummary packageSizeSummary;
	SynapseJavascriptClient jsClient;
	Integer fileCount = null;
	Double fileSize = null;
	@Inject
	public AddToDownloadList(AddToDownloadListView view, 
			PortalGinInjector ginInjector,
			PopupUtilsView popupUtilsView,
			EventBus eventBus,
			SynapseAlert synAlert,
			PackageSizeSummary packageSizeSummary,
			SynapseJavascriptClient jsClient) {
		this.jsClient = jsClient;
		this.view = view;
		this.popupUtilsView = popupUtilsView;
		this.ginInjector = ginInjector;
		this.eventBus = eventBus;
		this.synAlert = synAlert;
		view.setSynAlert(synAlert);
		this.packageSizeSummary = packageSizeSummary;
		view.setPackageSizeSummary(packageSizeSummary);
		view.setPresenter(this);
		view.hideAll();
	}
	
	private void init() {
		fileCount = null;
		fileSize = null;
		request = new AddFileToDownloadListRequest();
		packageSizeSummary.clear();
		synAlert.clear();
	}
	
	public void addToDownloadList(Query query) {
		init();
		// TODO: waiting for service to return the file count and size.
		request.setQuery(query);
		confirmAddQueryResultsToDownloadList();
	}
	
	public void addToDownloadList(String folderId) {
		init();
		request.setFolderId(folderId);
		confirmAddFolderChildrenToDownloadList();
	}
	
	public void confirmAddQueryResultsToDownloadList() {
		popupUtilsView.showConfirmDialog("", ADD_QUERY_FILES_CONFIRMATION_MESSAGE, () -> {
			onConfirmAddToDownloadList();
		});
	}
	
	public void confirmAddFolderChildrenToDownloadList() {
		//get children stats
		EntityChildrenRequest entityChildrenRequest = new EntityChildrenRequest();
		entityChildrenRequest.setIncludeSumFileSizes(true);
		entityChildrenRequest.setIncludeTotalChildCount(true);
		entityChildrenRequest.setParentId(request.getFolderId());
		List<EntityType> includeTypes = new ArrayList<EntityType>();
		includeTypes.add(EntityType.file);
		entityChildrenRequest.setIncludeTypes(includeTypes);
		jsClient.getEntityChildren(entityChildrenRequest, new AsyncCallback<EntityChildrenResponse>() {
			@Override
			public void onFailure(Throwable caught) {
				synAlert.handleException(caught);
			}
			@Override
			public void onSuccess(EntityChildrenResponse entityChildrenResponse) {
				updateFileCountFileSize(entityChildrenResponse.getTotalChildCount().intValue(), entityChildrenResponse.getSumFileSizesBytes().doubleValue());
			}
		});
	}
	
	public void updateFileCountFileSize(Integer fileCount, Double fileSize) {
		this.fileCount = fileCount;
		this.fileSize = fileSize;
		packageSizeSummary.addFiles(fileCount, fileSize);
		view.showConfirmAdd();		
	}
	
	@Override
	public void onConfirmAddToDownloadList() {
		view.hideAll();
		AsynchronousProgressWidget progress = ginInjector.creatNewAsynchronousProgressWidget();
		view.setAsynchronousProgressWidget(progress);
		view.showAsynchronousProgressWidget();
		progress.startAndTrackJob("Adding files to Download List", false, AsynchType.AddFileToDownloadList, request, new AsynchronousProgressHandler() {
			@Override
			public void onFailure(Throwable failure) {
				synAlert.handleException(failure);
				view.hideAll();
			}
			
			@Override
			public void onComplete(AsynchronousResponseBody response) {
//				view.hideAll();
				if (fileSize != null) {
					view.showSuccess(fileCount);	
				} else {
					popupUtilsView.showInfo(SUCCESS_ADDED_FILES_MESSAGE);	
				}
				//fire event to trigger UI element in header!
				eventBus.fireEvent(new DownloadListUpdatedEvent());
			}
			
			@Override
			public void onCancel() {
				view.hideAll();
			}
		});
	}
	
	@Override
	public Widget asWidget() {
		return view.asWidget();
	}
    
}
