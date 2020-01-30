package org.sagebionetworks.web.client.widget.entity.renderer;

import static org.sagebionetworks.web.client.ServiceEntryPointUtils.fixServiceEntryPoint;
import java.util.Map;
import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.web.client.ChallengeClientAsync;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.WidgetRendererPresenter;
import org.sagebionetworks.web.client.widget.pagination.PageChangeListener;
import org.sagebionetworks.web.client.widget.pagination.countbased.BasicPaginationWidget;
import org.sagebionetworks.web.shared.UserProfilePagedResults;
import org.sagebionetworks.web.shared.WidgetConstants;
import org.sagebionetworks.web.shared.WikiPageKey;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class ChallengeParticipantsWidget implements UserListView.Presenter, WidgetRendererPresenter, PageChangeListener {

	private UserListView view;
	private Map<String, String> descriptor;
	private ChallengeClientAsync challengeClient;
	private String challengeId;
	private boolean isInTeam;
	private Callback widgetRefreshRequired;
	private BasicPaginationWidget paginationWidget;
	public static final Long DEFAULT_PARTICIPANT_LIMIT = 50L;
	public static final Long DEFAULT_OFFSET = 0L;

	@Inject
	public ChallengeParticipantsWidget(UserListView view, BasicPaginationWidget paginationWidget, ChallengeClientAsync synapseClient) {
		this.view = view;
		this.paginationWidget = paginationWidget;
		this.challengeClient = synapseClient;
		fixServiceEntryPoint(challengeClient);
		view.setPaginationWidget(paginationWidget.asWidget());
		view.setPresenter(this);
	}

	@Override
	public void configure(final WikiPageKey wikiKey, final Map<String, String> widgetDescriptor, Callback widgetRefreshRequired, Long wikiVersionInView) {
		this.descriptor = widgetDescriptor;
		this.widgetRefreshRequired = widgetRefreshRequired;
		challengeId = descriptor.get(WidgetConstants.CHALLENGE_ID_KEY);
		isInTeam = false;
		String isInTeamString = descriptor.get(WidgetConstants.IS_IN_CHALLENGE_TEAM_KEY);
		if (isInTeamString != null) {
			isInTeam = Boolean.parseBoolean(isInTeamString);
		}
		descriptor = widgetDescriptor;
		if (challengeId == null) {
			view.showErrorMessage(WidgetConstants.CHALLENGE_ID_KEY + " is required.");
		} else {
			// get the challenge team summaries
			onPageChange(DEFAULT_OFFSET);
		}
	}

	@Override
	public void onPageChange(final Long newOffset) {
		view.hideErrors();
		view.showLoading();
		view.clearUsers();
		challengeClient.getChallengeParticipants(isInTeam, challengeId, DEFAULT_PARTICIPANT_LIMIT.intValue(), newOffset.intValue(), new AsyncCallback<UserProfilePagedResults>() {
			@Override
			public void onSuccess(UserProfilePagedResults results) {
				view.hideLoading();
				if (results.getTotalNumberOfResults() > 0) {
					// configure the pager, and the participant list
					paginationWidget.configure(DEFAULT_PARTICIPANT_LIMIT, newOffset, results.getTotalNumberOfResults(), ChallengeParticipantsWidget.this);
					for (UserProfile userProfile : results.getResults()) {
						view.addUser(userProfile);
					}
				} else {
					view.showNoUsers();
				}
			}

			@Override
			public void onFailure(Throwable caught) {
				view.hideLoading();
				view.showErrorMessage(caught.getMessage());
			}
		});
	}

	@Override
	public Widget asWidget() {
		return view.asWidget();
	}
}
