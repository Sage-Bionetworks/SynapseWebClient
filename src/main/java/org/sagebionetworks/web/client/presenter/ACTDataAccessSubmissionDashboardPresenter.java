package org.sagebionetworks.web.client.presenter;

import static org.sagebionetworks.web.client.ServiceEntryPointUtils.fixServiceEntryPoint;
import org.sagebionetworks.repo.model.dataaccess.OpenSubmission;
import org.sagebionetworks.repo.model.dataaccess.OpenSubmissionPage;
import org.sagebionetworks.web.client.DataAccessClientAsync;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.place.ACTDataAccessSubmissionDashboardPlace;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.view.DivView;
import org.sagebionetworks.web.client.view.PlaceView;
import org.sagebionetworks.web.client.widget.LoadMoreWidgetContainer;
import org.sagebionetworks.web.client.widget.accessrequirements.submission.OpenSubmissionWidget;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.inject.Inject;

public class ACTDataAccessSubmissionDashboardPresenter extends AbstractActivity implements Presenter<ACTDataAccessSubmissionDashboardPlace> {
	public static final String TITLE = "Data Access Submission Dashboard";
	public static final String NO_RESULTS = "There is no new Data Access Submissions.";
	private ACTDataAccessSubmissionDashboardPlace place;
	private PlaceView view;
	private PortalGinInjector ginInjector;
	private SynapseAlert synAlert;
	private DataAccessClientAsync dataAccessClient;
	private LoadMoreWidgetContainer loadMoreContainer;
	private DivView noResultsDiv;
	String nextPageToken;

	@Inject
	public ACTDataAccessSubmissionDashboardPresenter(PlaceView view, DataAccessClientAsync dataAccessClient, SynapseAlert synAlert, PortalGinInjector ginInjector, LoadMoreWidgetContainer loadMoreContainer, DivView noResultsDiv) {
		this.view = view;
		this.synAlert = synAlert;
		this.ginInjector = ginInjector;
		this.dataAccessClient = dataAccessClient;
		fixServiceEntryPoint(dataAccessClient);
		this.loadMoreContainer = loadMoreContainer;
		this.noResultsDiv = noResultsDiv;

		view.add(loadMoreContainer.asWidget());
		view.add(synAlert.asWidget());
		view.addTitle(TITLE);
		noResultsDiv.setText(NO_RESULTS);
		noResultsDiv.addStyleName("min-height-400");
		noResultsDiv.setVisible(false);
		view.add(noResultsDiv.asWidget());

		loadMoreContainer.configure(new Callback() {
			@Override
			public void invoke() {
				loadMore();
			}
		});
	}

	@Override
	public void start(AcceptsOneWidget panel, EventBus eventBus) {
		// Install the view
		panel.setWidget(view);
	}

	@Override
	public void setPlace(ACTDataAccessSubmissionDashboardPlace place) {
		this.place = place;
		view.initHeaderAndFooter();
		loadData();
	}

	public void loadData() {
		loadMoreContainer.clear();
		nextPageToken = null;
		loadMore();
	}

	public void loadMore() {
		synAlert.clear();
		dataAccessClient.getOpenSubmissions(nextPageToken, new AsyncCallback<OpenSubmissionPage>() {

			@Override
			public void onFailure(Throwable caught) {
				synAlert.handleException(caught);
				loadMoreContainer.setIsMore(false);
			}

			@Override
			public void onSuccess(OpenSubmissionPage openSubmissionPage) {
				noResultsDiv.setVisible(nextPageToken == null && openSubmissionPage.getOpenSubmissionList().isEmpty());
				nextPageToken = openSubmissionPage.getNextPageToken();
				for (OpenSubmission openSubmission : openSubmissionPage.getOpenSubmissionList()) {
					OpenSubmissionWidget w = ginInjector.getOpenSubmissionWidget();
					w.configure(openSubmission);
					loadMoreContainer.add(w.asWidget());
				}
				loadMoreContainer.setIsMore(nextPageToken != null);
			}
		});
	}

	public ACTDataAccessSubmissionDashboardPlace getPlace() {
		return place;
	}

	@Override
	public String mayStop() {
		return null;
	}
}
