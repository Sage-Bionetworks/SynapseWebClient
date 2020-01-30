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
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.SynapseJavascriptClient;
import org.sagebionetworks.web.client.events.DownloadListUpdatedEvent;
import org.sagebionetworks.web.client.security.AuthenticationController;
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

	public static final String ZERO_FILES_IN_FOLDER_MESSAGE = "If a folder contains no files, no files will be added to the download list. Additionally, sub-folders cannot be added directly; please navigate into the directory that contains the files you’d like to download to add them.";
	public static final String SUCCESS_ADDED_FILES_MESSAGE = "Successfully added files to the Download List.";
	public static final String ADD_QUERY_FILES_CONFIRMATION_MESSAGE = "Add all files from this query result to the Download List?";
	public static final Long FILE_SIZE_QUERY_PART_MASK = BUNDLE_MASK_QUERY_SUM_FILE_SIZES | BUNDLE_MASK_QUERY_COUNT;
	public static final String PLEASE_LOGIN_TO_ADD_TO_DOWNLOAD_LIST = "Sign in to add files to your download list.";
	AddToDownloadListView view;
	PopupUtilsView popupUtilsView;
	AddFileToDownloadListRequest request;
	EventBus eventBus;
	SynapseAlert synAlert;
	SynapseJSNIUtils jsniUtils;
	PackageSizeSummary packageSizeSummary;
	SynapseJavascriptClient jsClient;
	AsynchronousProgressWidget progress;
	AuthenticationController authController;
	String queryEntityID;
	int fileCountToAdd;
	public static final String FILES_ADDED_TO_DOWNLOAD_LIST_EVENT_NAME = "FilesAddedToDownloadList";
	public static final String DOWNLOAD_ACTION_EVENT_NAME = "Download";

	@Inject
	public AddToDownloadList(AddToDownloadListView view, AsynchronousProgressWidget progress, InlineAsynchronousProgressViewImpl inlineProgressView, PopupUtilsView popupUtilsView, EventBus eventBus, SynapseAlert synAlert, PackageSizeSummary packageSizeSummary, SynapseJavascriptClient jsClient, SynapseJSNIUtils jsniUtils, AuthenticationController authController) {
		this.jsClient = jsClient;
		this.view = view;
		this.popupUtilsView = popupUtilsView;
		this.progress = progress;
		inlineProgressView.showWhiteSpinner();
		inlineProgressView.setProgressMessageVisible(false);
		progress.setView(inlineProgressView);
		view.setAsynchronousProgressWidget(progress);
		this.eventBus = eventBus;
		this.synAlert = synAlert;
		this.jsniUtils = jsniUtils;
		view.add(synAlert);
		this.packageSizeSummary = packageSizeSummary;
		packageSizeSummary.showWhiteSpinner();
		this.authController = authController;
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
		if (authController.isLoggedIn()) {
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
					// get sum file sizes from query result
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
		} else {
			synAlert.showError(PLEASE_LOGIN_TO_ADD_TO_DOWNLOAD_LIST);
		}

	}

	public void confirmAddFolderChildrenToDownloadList() {
		if (authController.isLoggedIn()) {
			// get children stats
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
					if (fileCountToAdd == 0) {
						synAlert.showError(ZERO_FILES_IN_FOLDER_MESSAGE);
					} else {
						packageSizeSummary.addFiles(fileCountToAdd, entityChildrenResponse.getSumFileSizesBytes().doubleValue());
						view.showConfirmAdd();
					}
				}
			});
		} else {
			synAlert.showError(PLEASE_LOGIN_TO_ADD_TO_DOWNLOAD_LIST);
		}
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
				jsniUtils.sendAnalyticsEvent(AddToDownloadList.DOWNLOAD_ACTION_EVENT_NAME, AddToDownloadList.FILES_ADDED_TO_DOWNLOAD_LIST_EVENT_NAME, Integer.toString(fileCountToAdd));
				view.hideAll();
				view.showSuccess(fileCountToAdd);

				// fire event to trigger UI element in header!
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
