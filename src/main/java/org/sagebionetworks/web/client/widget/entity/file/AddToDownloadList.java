package org.sagebionetworks.web.client.widget.entity.file;

import static org.sagebionetworks.web.client.widget.table.v2.results.TableQueryResultWidget.BUNDLE_MASK_QUERY_COUNT;
import static org.sagebionetworks.web.client.widget.table.v2.results.TableQueryResultWidget.BUNDLE_MASK_QUERY_SUM_FILE_SIZES;

import java.util.ArrayList;
import java.util.List;

import org.sagebionetworks.repo.model.EntityChildrenRequest;
import org.sagebionetworks.repo.model.EntityChildrenResponse;
import org.sagebionetworks.repo.model.EntityType;
import org.sagebionetworks.repo.model.asynch.AsynchronousResponseBody;
import org.sagebionetworks.repo.model.file.AddFileToDownloadListRequest;
import org.sagebionetworks.repo.model.table.Query;
import org.sagebionetworks.repo.model.table.QueryBundleRequest;
import org.sagebionetworks.repo.model.table.QueryResultBundle;
import org.sagebionetworks.web.client.PopupUtilsView;
import org.sagebionetworks.web.client.SynapseJavascriptClient;
import org.sagebionetworks.web.client.events.DownloadListUpdatedEvent;
import org.sagebionetworks.web.client.widget.asynch.AsynchronousProgressHandler;
import org.sagebionetworks.web.client.widget.asynch.AsynchronousProgressWidget;
import org.sagebionetworks.web.client.widget.asynch.InlineAsynchronousProgressViewImpl;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.client.widget.entity.file.downloadlist.PackageSizeSummary;
import org.sagebionetworks.web.shared.asynch.AsynchType;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class AddToDownloadList implements IsWidget, AddToDownloadListView.Presenter {
	
	public static final String NO_NEW_FILES_ADDED_MESSAGE = "No new files have been added to the Download List.";
	public static final String SUCCESS_ADDED_FILES_MESSAGE = "Successfully added files to the Download List.";
	public static final String ADD_QUERY_FILES_CONFIRMATION_MESSAGE = "Add all files from this query result to the Download List?";
	public static final Long FILE_SIZE_QUERY_PART_MASK = BUNDLE_MASK_QUERY_SUM_FILE_SIZES | BUNDLE_MASK_QUERY_COUNT; 
	AddToDownloadListView view;
	PopupUtilsView popupUtilsView;
	AddFileToDownloadListRequest request;
	EventBus eventBus;
	SynapseAlert synAlert;
	PackageSizeSummary packageSizeSummary;
	SynapseJavascriptClient jsClient;
	AsynchronousProgressWidget progress;
	String queryEntityID;
	int fileCountToAdd;
	
	@Inject
	public AddToDownloadList(AddToDownloadListView view, 
			AsynchronousProgressWidget progress,
			InlineAsynchronousProgressViewImpl inlineProgressView,
			PopupUtilsView popupUtilsView,
			EventBus eventBus,
			SynapseAlert synAlert,
			PackageSizeSummary packageSizeSummary,
			SynapseJavascriptClient jsClient) {
		this.jsClient = jsClient;
		this.view = view;
		this.popupUtilsView = popupUtilsView;
		this.progress = progress;
		progress.setView(inlineProgressView);
		view.setAsynchronousProgressWidget(progress);
		this.eventBus = eventBus;
		this.synAlert = synAlert;
		view.add(synAlert);
		this.packageSizeSummary = packageSizeSummary;
		view.setPackageSizeSummary(packageSizeSummary);
		view.setPresenter(this);
		view.hideAll();
	}
	public void clear() {
		fileCountToAdd = 0;
		view.hideAll();
		request = new AddFileToDownloadListRequest();
		packageSizeSummary.clear();
		synAlert.clear();
		
	}
	
	public void addToDownloadList(String entityID, Query query) {
		queryEntityID = entityID;
		clear();
		request.setQuery(query);
		confirmAddQueryResultsToDownloadList();
	}
	
	public void addToDownloadList(String folderId) {
		clear();
		request.setFolderId(folderId);
		confirmAddFolderChildrenToDownloadList();
	}
	
	public void confirmAddQueryResultsToDownloadList() {
		// run the query, ask for the stats
		QueryBundleRequest queryBundleRequest = new QueryBundleRequest();
		queryBundleRequest.setEntityId(queryEntityID);
		queryBundleRequest.setQuery(request.getQuery());
		queryBundleRequest.setPartMask(FILE_SIZE_QUERY_PART_MASK);
		view.showAsynchronousProgressWidget();
		progress.startAndTrackJob("Calculating the total query result file size", false, AsynchType.TableQuery, queryBundleRequest, new AsynchronousProgressHandler() {
			@Override
			public void onFailure(Throwable failure) {
				synAlert.handleException(failure);
				view.hideAll();
			}
			
			@Override
			public void onComplete(AsynchronousResponseBody response) {
				QueryResultBundle queryResultBundle = (QueryResultBundle) response;
				view.hideAll();
				//get sum file sizes from query result
				double sumFileSizesBytes = 0.0;
				if (queryResultBundle.getSumFileSizes() != null && queryResultBundle.getSumFileSizes().getSumFileSizesBytes() != null) {
					sumFileSizesBytes = queryResultBundle.getSumFileSizes().getSumFileSizesBytes().doubleValue();
				}
				fileCountToAdd = queryResultBundle.getQueryCount().intValue();
				packageSizeSummary.addFiles(fileCountToAdd, sumFileSizesBytes);
				view.showConfirmAdd();
			}
			
			@Override
			public void onCancel() {
				view.hideAll();
			}
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
				fileCountToAdd = entityChildrenResponse.getTotalChildCount().intValue();
				packageSizeSummary.addFiles(fileCountToAdd, entityChildrenResponse.getSumFileSizesBytes().doubleValue());
				view.showConfirmAdd();
			}
		});
	}
	
	@Override
	public void onConfirmAddToDownloadList() {
		startAddingFiles();
	}
	
	public void startAddingFiles() {
		view.hideAll();
		view.showAsynchronousProgressWidget();
		progress.startAndTrackJob("Adding files to Download List...", false, AsynchType.AddFileToDownloadList, request, new AsynchronousProgressHandler() {
			@Override
			public void onFailure(Throwable failure) {
				synAlert.handleException(failure);
				view.hideAll();
			}
			
			@Override
			public void onComplete(AsynchronousResponseBody response) {
				view.hideAll();
				view.showSuccess(fileCountToAdd);
				
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
