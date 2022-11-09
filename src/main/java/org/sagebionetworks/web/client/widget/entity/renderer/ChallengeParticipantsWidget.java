package org.sagebionetworks.web.client.widget.entity.renderer;

import static org.sagebionetworks.web.client.ServiceEntryPointUtils.fixServiceEntryPoint;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import java.util.Map;
import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.web.client.ChallengeClientAsync;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.WidgetRendererPresenter;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.client.widget.pagination.BasicPaginationWidget;
import org.sagebionetworks.web.client.widget.pagination.PageChangeListener;
import org.sagebionetworks.web.shared.UserProfilePagedResults;
import org.sagebionetworks.web.shared.WidgetConstants;
import org.sagebionetworks.web.shared.WikiPageKey;

public class ChallengeParticipantsWidget
  implements WidgetRendererPresenter, PageChangeListener {

  public static final String NO_CHALLENGE_PARTICIPANTS_FOUND_MESSAGE =
    "No challenge participants found.";
  private UserListView view;
  private Map<String, String> descriptor;
  private ChallengeClientAsync challengeClient;
  private String challengeId;
  private boolean isInTeam;
  private BasicPaginationWidget paginationWidget;
  private PortalGinInjector ginInjector;
  private SynapseAlert synAlert;
  public static final Long DEFAULT_PARTICIPANT_LIMIT = 50L;
  public static final Long DEFAULT_OFFSET = 0L;

  @Inject
  public ChallengeParticipantsWidget(
    UserListView view,
    BasicPaginationWidget paginationWidget,
    ChallengeClientAsync synapseClient,
    SynapseAlert synAlert,
    PortalGinInjector ginInjector
  ) {
    this.view = view;
    this.paginationWidget = paginationWidget;
    this.challengeClient = synapseClient;
    this.synAlert = synAlert;
    this.ginInjector = ginInjector;
    fixServiceEntryPoint(challengeClient);
    view.setPaginationWidget(paginationWidget.asWidget());
    view.setPaginationWidget(paginationWidget);
    view.setSynapseAlert(synAlert);
  }

  @Override
  public void configure(
    final WikiPageKey wikiKey,
    final Map<String, String> widgetDescriptor,
    Callback widgetRefreshRequired,
    Long wikiVersionInView
  ) {
    this.descriptor = widgetDescriptor;
    challengeId = descriptor.get(WidgetConstants.CHALLENGE_ID_KEY);
    isInTeam = false;
    String isInTeamString = descriptor.get(
      WidgetConstants.IS_IN_CHALLENGE_TEAM_KEY
    );
    if (isInTeamString != null) {
      isInTeam = Boolean.parseBoolean(isInTeamString);
    }
    descriptor = widgetDescriptor;
    if (challengeId == null) {
      synAlert.showError(WidgetConstants.CHALLENGE_ID_KEY + " is required.");
    } else {
      // get the challenge team summaries
      onPageChange(DEFAULT_OFFSET);
    }
  }

  @Override
  public void onPageChange(final Long newOffset) {
    synAlert.clear();
    view.clearRows();
    view.setLoadingVisible(true);
    challengeClient.getChallengeParticipants(
      isInTeam,
      challengeId,
      DEFAULT_PARTICIPANT_LIMIT.intValue(),
      newOffset.intValue(),
      new AsyncCallback<UserProfilePagedResults>() {
        @Override
        public void onSuccess(UserProfilePagedResults results) {
          view.setLoadingVisible(false);
          if (results.getTotalNumberOfResults() > 0) {
            // configure the pager, and the participant list
            paginationWidget.configure(
              DEFAULT_PARTICIPANT_LIMIT,
              newOffset,
              results.getTotalNumberOfResults(),
              ChallengeParticipantsWidget.this
            );
            for (UserProfile userProfile : results.getResults()) {
              UserListRowWidget row = ginInjector.getUserListRowWidget();
              row.configure(userProfile);
              view.addRow(row);
            }
          } else {
            synAlert.showError(NO_CHALLENGE_PARTICIPANTS_FOUND_MESSAGE);
          }
        }

        @Override
        public void onFailure(Throwable caught) {
          view.setLoadingVisible(false);
          synAlert.handleException(caught);
        }
      }
    );
  }

  @Override
  public Widget asWidget() {
    return view.asWidget();
  }
}
