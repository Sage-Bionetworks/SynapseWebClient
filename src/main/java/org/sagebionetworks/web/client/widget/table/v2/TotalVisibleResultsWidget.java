package org.sagebionetworks.web.client.widget.table.v2;

import static org.sagebionetworks.repo.model.table.QueryOptions.BUNDLE_MASK_QUERY_COUNT;
import static org.sagebionetworks.web.client.widget.asynch.AsynchronousProgressWidget.WAIT_MS;
import static org.sagebionetworks.web.client.widget.table.v2.results.QueryBundleUtils.getDefaultQuery;

import org.sagebionetworks.repo.model.asynch.AsynchronousJobStatus;
import org.sagebionetworks.repo.model.asynch.AsynchronousResponseBody;
import org.sagebionetworks.repo.model.table.Dataset;
import org.sagebionetworks.repo.model.table.QueryBundleRequest;
import org.sagebionetworks.repo.model.table.QueryResultBundle;
import org.sagebionetworks.repo.model.table.View;
import org.sagebionetworks.web.client.PopupUtilsView;
import org.sagebionetworks.web.client.widget.asynch.AsynchronousJobTracker;
import org.sagebionetworks.web.client.widget.asynch.UpdatingAsynchProgressHandler;
import org.sagebionetworks.web.shared.asynch.AsynchType;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class TotalVisibleResultsWidget implements IsWidget {

	private TotalVisibleResultsWidgetView view;
	private Integer totalNumberOfResults;
	private Integer totalNumberOfVisibleResults;
	private PopupUtilsView popupUtils;
	private AsynchronousJobTracker asyncJobTracker;

	private static final String DATASETS_HELP = "Files may be unavailable because you do not have permission to see them, they have been deleted, or the Dataset has been misconfigured.";

	@Inject
	public TotalVisibleResultsWidget(TotalVisibleResultsWidgetView view, PopupUtilsView popupUtils, AsynchronousJobTracker asyncJobTracker) {
		this.view = view;
		this.popupUtils = popupUtils;
		this.asyncJobTracker = asyncJobTracker;
	}

	@Override
	public Widget asWidget() {
		return view.asWidget();
	}

	public void configure(View viewEntity) {
		// Currently, the only case where we can reliably get this info is for the current version of a Dataset.
		// Other cases will need a new service, tracked by PLFM-7046
		if (viewEntity instanceof Dataset && viewEntity.getIsLatestVersion()) {
			this.totalNumberOfResults = ((Dataset) viewEntity).getItems().size();
			view.setTotalNumberOfResults(totalNumberOfResults);
			view.setHelpMarkdown(DATASETS_HELP);
			view.setVisible(true);
			// Query to get the total number of visible items
			QueryBundleRequest qbRequest = new QueryBundleRequest();
			qbRequest.setQuery(getDefaultQuery(viewEntity));
			qbRequest.setPartMask(BUNDLE_MASK_QUERY_COUNT);
			qbRequest.setEntityId(viewEntity.getId());
			// add widget to view
			asyncJobTracker.startAndTrack(AsynchType.TableQuery, qbRequest, WAIT_MS, new UpdatingAsynchProgressHandler() {

				@Override
				public void onUpdate(AsynchronousJobStatus status) {}

				@Override
				public boolean isAttached() {
					return true;
				}

				@Override
				public void onFailure(Throwable failure) {
					popupUtils.showErrorMessage(failure.getMessage());
				}

				@Override
				public void onComplete(AsynchronousResponseBody response) {
					totalNumberOfVisibleResults = ((QueryResultBundle) response).getQueryCount().intValue();
					view.setNumberOfHiddenResults(totalNumberOfResults - totalNumberOfVisibleResults);
					view.setNumberOfHiddenResultsVisible(!totalNumberOfResults.equals(totalNumberOfVisibleResults));
				}

				@Override
				public void onCancel() {
					popupUtils.showInfo("Query Cancelled");
				}
			});
		} else {
			view.setVisible(false);
		}
	}


}
